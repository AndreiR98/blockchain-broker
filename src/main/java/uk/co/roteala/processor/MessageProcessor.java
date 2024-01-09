package uk.co.roteala.processor;

import com.github.benmanes.caffeine.cache.Cache;
import io.reactivex.rxjava3.functions.Consumer;
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
import uk.co.roteala.common.BasicModel;
import uk.co.roteala.common.MempoolTransaction;
import uk.co.roteala.common.messenger.*;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
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
            case PEERS:
                processPeersEvent(messageTemplate);
        }
    }

    private void processPeersEvent(MessageTemplate messageTemplate) {
        switch (messageTemplate.getEventAction()) {
            case REQUEST:
                try {
                    Object result = this.storages
                            .getStorage(StorageTypes.PEERS)
                            .withCriteria(Operator.EQ, true, SearchField.STATUS)
                            .asBasicModel();

                    List<Peer> activePeers = (List<Peer>) result;

                    //Filter the list so that we dont send himself as peer
                    String ownerHostAddress = messageTemplate.getOwner().remoteAddress()
                            .hostAddress();

                    activePeers.stream()
                            .filter(peer -> !(Objects.equals(peer.getAddress(), ownerHostAddress)))
                            .collect(Collectors.toList())
                            .forEach(peer -> {
                                sink.tryEmitNext(MessageTemplate.builder()
                                                .eventType(EventTypes.PEERS)
                                                .group(ReceivingGroup.CLIENT)
                                                .eventAction(EventActions.RESPONSE)
                                                .owner(messageTemplate.getOwner())
                                                .message(Response.builder()
                                                        .status(true)
                                                        .type(EventTypes.PEERS)
                                                        .location(null)
                                                        .payload(peer)
                                                        .build())
                                        .build());
                            });
                } catch (Exception e){
                    log.info("Error:{}", e);
                }
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

                        //Update real balances and virtual
                        MempoolTransaction mempoolTransaction = (MempoolTransaction) this.storages.getStorage(StorageTypes.MEMPOOL)
                                .get(ColumnFamilyTypes.TRANSACTIONS, response.getLocation()
                                        .getBytes(StandardCharsets.UTF_8));

                        fundingServices.processVirtualBalance(mempoolTransaction);
                    }
                } catch (Exception e) {
                    log.error("Error:{}", e);
                }
        }
    }

    private int getThresholdValue() {
        int connectionsSize = this.connectionsStorage.getClientConnections().size();

        if (connectionsSize == 1 || connectionsSize == 2) {
            return 1;
        }

        return (int) Math.ceil((double) connectionsSize / 2);
    }
}
