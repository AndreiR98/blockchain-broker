package uk.co.roteala.processor;

import io.reactivex.rxjava3.functions.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;
import reactor.core.publisher.Sinks;
import uk.co.roteala.common.BasicModel;
import uk.co.roteala.common.MempoolTransaction;
import uk.co.roteala.common.messenger.*;
import uk.co.roteala.common.storage.ColumnFamilyTypes;
import uk.co.roteala.common.storage.Storage;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.net.ConnectionsStorage;
import uk.co.roteala.storage.Storages;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionProcessor implements Consumer<Flux<MempoolTransaction>> {
    private final Storages storages;
    private final Sinks.Many<MessageTemplate> messageTemplateSink;
    @Override
    public void accept(Flux<MempoolTransaction> flux) {
        flux.doOnEach(this::processIncomingTransaction)
                .then()
                .subscribe();
    }

    private void processIncomingTransaction(Signal<MempoolTransaction> mempoolTransactionSignal) {
        final MempoolTransaction mempoolTransaction = mempoolTransactionSignal.get();

        Storage storage = storages.getStorage(StorageTypes.MEMPOOL);

        if(!storage.has(ColumnFamilyTypes.TRANSACTIONS, mempoolTransaction.getKey())) {
            log.info("Received new transaction: {}", mempoolTransaction.getHash());
            storage.put(true, ColumnFamilyTypes.TRANSACTIONS,
                    mempoolTransaction.getKey(), mempoolTransaction);

            messageTemplateSink.tryEmitNext(MessageTemplate.builder()
                    .eventAction(EventActions.VERIFY_THEN_APPEND)
                    .eventType(EventTypes.MEMPOOL_TRANSACTION)
                    .group(ReceivingGroup.CLIENTS)
                    .message(mempoolTransaction)
                    .build());
        }

    }
}
