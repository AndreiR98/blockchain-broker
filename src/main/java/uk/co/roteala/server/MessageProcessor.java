package uk.co.roteala.server;

import com.github.benmanes.caffeine.cache.Cache;
import io.reactivex.rxjava3.functions.Consumer;
import io.vertx.core.net.NetSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import uk.co.roteala.common.*;
import uk.co.roteala.common.messenger.*;
import uk.co.roteala.common.monetary.Funding;
import uk.co.roteala.common.monetary.FundingServices;
import uk.co.roteala.common.monetary.MoveFund;
import uk.co.roteala.common.storage.ColumnFamilyTypes;
import uk.co.roteala.common.storage.Operator;
import uk.co.roteala.common.storage.SearchField;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.messanging.AssemblerMessenger;
import uk.co.roteala.net.ConnectionsStorage;
import uk.co.roteala.net.Peer;
import uk.co.roteala.storage.Storages;
import uk.co.roteala.utils.BlockchainUtils;
import uk.co.roteala.utils.Constants;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MessageProcessor implements Consumer<Flux<Message>> {
    @Autowired
    private Storages storages;

    @Autowired
    private ConnectionsStorage connectionsStorage;

    @Autowired
    private Cache<String, Integer> responseCache;

    @Autowired
    private FundingServices fundingServices;

    @Autowired
    private Sinks.Many<Funding> fundingSink;

    @Autowired
    private Sinks.Many<MessageTemplate> sink;

    private final AssemblerMessenger assemblerMessenger;
    @Override
    public void accept(Flux<Message> messageFlux) {
        messageFlux.parallel()
                .runOn(Schedulers.parallel())
                .map(this.assemblerMessenger)
                .flatMap(optionalTemplate -> optionalTemplate.map(Mono::just).orElseGet(Mono::empty))
                .doOnNext(this::internalMessageTemplateProcessor)
                .then().subscribe();
    }

    private void internalMessageTemplateProcessor(MessageTemplate messageTemplate) {
        switch (messageTemplate.getEventType()) {
            case MEMPOOL_TRANSACTION:
                processMempoolEvent(messageTemplate);
                break;
            case PEERS:
                processPeersEvent(messageTemplate);
                break;
            case STATECHAIN:
                processStateChain(messageTemplate);
            case BLOCK:
                processBlockEvent(messageTemplate);
                break;
        }
    }

    private void processBlockEvent(MessageTemplate messageTemplate) {
        switch (messageTemplate.getEventAction()) {
            case MINED_BLOCK:
                try {
                    Block newminedBlock = (Block) messageTemplate.getMessage();

                    final ChainState chainState = (ChainState) this.storages
                            .getStorage(StorageTypes.STATE).get(ColumnFamilyTypes.STATE, Constants.DEFAULT_STATE_NAME
                                    .getBytes(StandardCharsets.UTF_8));

                    if(newminedBlock.isValidBlock(this.storages.getStorage(StorageTypes.MEMPOOL), chainState.getTarget())) {

                        if(!this.storages.getStorage(StorageTypes.BLOCKCHAIN)
                                .has(ColumnFamilyTypes.BLOCKS, newminedBlock.getKey())) {

                            this.storages.getStorage(StorageTypes.MEMPOOL)
                                    .putIfAbsent(true, ColumnFamilyTypes.BLOCKS, newminedBlock.getHash()
                                            .getBytes(StandardCharsets.UTF_8), newminedBlock);

                            log.info("Received a new block with hash: {} at index: {}", newminedBlock.getHash(),
                                    newminedBlock.getIndex());

                            sink.tryEmitNext(MessageTemplate.builder()
                                    .eventType(EventTypes.BLOCK)
                                    .message(newminedBlock)
                                    .withOut(List.of(messageTemplate.getOwner()))
                                    .group(ReceivingGroup.ALL)
                                    .eventAction(EventActions.VERIFY)
                                    .build());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error: {}", e);
                }
                break;
            case RESPONSE:
                try {
                    final Response response = (Response) messageTemplate.getMessage();

                    Integer responseCount = responseCache.get(response.getLocation(), i -> 0);

                    if(response.isStatus()) {
                        log.info("Block: {} received a verification!", response.getLocation());
                        responseCount += 1;
                        responseCache.put(response.getLocation(), responseCount);
                    }

                    if(responseCount >= getThresholdValue()) {
                        responseCache.invalidate(response.getLocation());

                        try {
                            Block block = (Block) this.storages
                                    .getStorage(StorageTypes.MEMPOOL)
                                    .get(ColumnFamilyTypes.BLOCKS, response.getLocation()
                                            .getBytes(StandardCharsets.UTF_8));

                            processFinalNewMinedBlock(block);
                        } catch (Exception e) {
                            log.error("Failed to find proposed block: {}", response.getLocation());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error: {}", e);
                }
                break;
        }
    }

    private void processStateChain(MessageTemplate messageTemplate) {
        switch (messageTemplate.getEventAction()) {
            case REQUEST_SYNC:
                try {
                    ChainState state = (ChainState) this.storages
                            .getStorage(StorageTypes.STATE)
                            .get(ColumnFamilyTypes.STATE, Constants.DEFAULT_STATE_NAME
                                    .getBytes(StandardCharsets.UTF_8));

                    sink.tryEmitNext(MessageTemplate.builder()
                                    .eventType(EventTypes.STATECHAIN)
                                    .group(ReceivingGroup.CLIENT)
                                    .eventAction(EventActions.RESPONSE)
                                    .owner(messageTemplate.getOwner())
                                    .message(Response.builder()
                                            .payload(state)
                                            .location(null)
                                            .type(EventTypes.STATECHAIN)
                                            .status(true)
                                            .build())
                            .build());
                } catch (Exception e) {
                    log.error("Error: {}", e);
                }
                break;
        }
    }

    private void processPeersEvent(MessageTemplate messageTemplate) {
        switch (messageTemplate.getEventAction()) {
            case REQUEST:
                try {
                    //Filter the list so that we dont send himself as peer
                    String ownerHostAddress = messageTemplate.getOwner().remoteAddress().hostAddress();

                    Object fetched = this.storages.getStorage(StorageTypes.PEERS)
                            .withCriteria(Operator.EQ, true, SearchField.STATUS)
                            .asBasicModel();

                    List<Peer> peers = (List<Peer>) fetched;
                    peers.removeIf(peer -> Objects.equals(peer.getAddress(), ownerHostAddress));

                    PeersContainer peersContainer = new PeersContainer(peers);

                    sink.tryEmitNext(MessageTemplate.builder()
                                    .eventType(EventTypes.PEERS)
                                    .group(ReceivingGroup.CLIENT)
                                    .eventAction(EventActions.RESPONSE)
                                    .owner(messageTemplate.getOwner())
                                    .message(Response.builder()
                                            .status(true)
                                            .type(EventTypes.PEERS)
                                            .location(null)
                                            .payload(peersContainer)
                                            .build())
                            .build());
                } catch (Exception e){
                    log.info("Error:{}", e);
                }
                break;
        }
    }

    private void processMempoolEvent(MessageTemplate messageTemplate) {
        switch (messageTemplate.getEventAction()) {
            case RESPONSE:
                final Response response = (Response) messageTemplate
                        .getMessage();
                try {
                    Integer responseCount = responseCache.get(response.getLocation(), i -> 0);

                    if(response.isStatus()) {
                        responseCount += 1;
                        responseCache.put(response.getLocation(), responseCount);
                    }

                    if(responseCount >= getThresholdValue()) {
                        responseCache.invalidate(response.getLocation());

                        MempoolTransaction mempoolTransaction = (MempoolTransaction) this.storages.getStorage(StorageTypes.MEMPOOL)
                                .get(ColumnFamilyTypes.TRANSACTIONS, response.getLocation()
                                        .getBytes(StandardCharsets.UTF_8));

                        fundingSink.tryEmitNext(Funding.builder()
                                        .targetAccountAddress(mempoolTransaction.getTo())
                                        .sourceAccountAddress(mempoolTransaction.getFrom())
                                        .amount(mempoolTransaction.getAmount())
                                        .networkFees(mempoolTransaction.getNetworkFees())
                                        .processingFees(mempoolTransaction.getFees())
                                        .hash(mempoolTransaction.getHash())
                                        .type(Funding.TransactionType.MEMPOOL)
                                .build());

                        sink.tryEmitNext(MessageTemplate.builder()
                                        .eventType(EventTypes.MEMPOOL_TRANSACTION)
                                        .eventAction(EventActions.APPEND)
                                        .message(mempoolTransaction)
                                        .group(ReceivingGroup.ALL)
                                .build());
                    }
                } catch (Exception e) {
                    log.error("Error:{}", e);
                }
                break;
        }
    }

    private int getThresholdValue() {
        int connectionsSize = this.connectionsStorage.getAsServerConnections().size();
        int thresHold = 0;

        if (connectionsSize == 1) {
            thresHold = 1;
        } else if (connectionsSize == 2) {
            thresHold = 2;
        } else {
            thresHold = ((int) Math.ceil((double) connectionsSize / 2));
        }

        return thresHold;
    }

    private void processFinalNewMinedBlock(Block newMinedBlock) {
        sink.tryEmitNext(MessageTemplate.builder()
                        .eventAction(EventActions.APPEND)
                        .message(newMinedBlock)
                        .group(ReceivingGroup.ALL)
                        .eventType(EventTypes.BLOCK)
                .build());

        this.storages.getStorage(StorageTypes.MEMPOOL)
                .delete(ColumnFamilyTypes.BLOCKS, newMinedBlock.getHash()
                        .getBytes(StandardCharsets.UTF_8));

        int index = 0;
        for(String transactionHash : newMinedBlock.getTransactions()) {

            MempoolTransaction mempoolTransaction = (MempoolTransaction) this.storages
                    .getStorage(StorageTypes.MEMPOOL)
                    .get(ColumnFamilyTypes.TRANSACTIONS, transactionHash.getBytes(StandardCharsets.UTF_8));

            Transaction transaction = mempoolTransaction.toTransaction(newMinedBlock, index);

            this.storages.getStorage(StorageTypes.BLOCKCHAIN)
                    .put(true, transaction.getKey(), transaction);

            fundingSink.tryEmitNext(Funding.builder()
                    .targetAccountAddress(mempoolTransaction.getTo())
                    .sourceAccountAddress(mempoolTransaction.getFrom())
                    .amount(mempoolTransaction.getAmount())
                    .networkFees(mempoolTransaction.getNetworkFees())
                    .processingFees(mempoolTransaction.getFees())
                    .hash(mempoolTransaction.getHash())
                    .type(Funding.TransactionType.TRANSACTION)
                    .build());

            this.storages.getStorage(StorageTypes.MEMPOOL)
                    .delete(ColumnFamilyTypes.TRANSACTIONS, transaction.getKey());

            index++;
        }

        this.storages.getStorage(StorageTypes.BLOCKCHAIN)
                .put(true, ColumnFamilyTypes.BLOCKS, newMinedBlock.getKey(), newMinedBlock);
        log.info("New block with hash: {} appended at index: {}", newMinedBlock.getHash(), newMinedBlock.getIndex());

        ChainState state = (ChainState) this.storages.getStorage(StorageTypes.STATE)
                .get(ColumnFamilyTypes.STATE, Constants.DEFAULT_STATE_NAME.getBytes(StandardCharsets.UTF_8));

        state.setLastBlockIndex(state.getLastBlockIndex() + 1);

        this.storages.getStorage(StorageTypes.STATE)
                .modify(ColumnFamilyTypes.STATE, Constants.DEFAULT_STATE_NAME.getBytes(StandardCharsets.UTF_8), state);

        log.info("State updated: {}", state);
    }
}
