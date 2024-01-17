package uk.co.roteala.configs;



import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.websocket.WebsocketOutbound;
import uk.co.roteala.common.MempoolTransaction;
import uk.co.roteala.common.messenger.Message;
import uk.co.roteala.common.messenger.MessageContainer;
import uk.co.roteala.common.messenger.MessageTemplate;
import uk.co.roteala.common.messenger.Messenger;
import uk.co.roteala.common.monetary.Funding;
import uk.co.roteala.common.monetary.FundingServices;
import uk.co.roteala.common.storage.ColumnFamilyTypes;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.core.Blockchain;
import uk.co.roteala.exceptions.StorageException;
import uk.co.roteala.exceptions.errorcodes.StorageErrorCode;

import uk.co.roteala.handlers.WebSocketRouterHandler;
import uk.co.roteala.messanging.AssemblerMessenger;
import uk.co.roteala.net.ConnectionsStorage;
import uk.co.roteala.server.MessageProcessor;
import uk.co.roteala.processor.TransactionProcessor;
import uk.co.roteala.storage.Storages;
import uk.co.roteala.utils.Constants;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ServerConfig {
    private final Storages storage;

    private List<WebsocketOutbound> webSocketConnections = new ArrayList<>();

    @Bean
    public ConnectionsStorage connectionsStorage() {
        return new ConnectionsStorage();
    }

    @Bean
    @DependsOn({
            "initializeStateTrieStorage",
            "initializeMempoolStorage",
            "initializeBlockchainStorage",
            "initializePeersStorage"
    })
    public void genesisConfig() {
        try {
            if(!storage.getStorage(StorageTypes.STATE)
                    .has(ColumnFamilyTypes.STATE, Constants.DEFAULT_STATE_NAME.getBytes(StandardCharsets.UTF_8))) {
                log.info("Creating new genesis state");
                Blockchain.initializeGenesisState(storage.getStorage(StorageTypes.STATE));
                Blockchain.initializeGenesisBlock(storage.getStorage(StorageTypes.BLOCKCHAIN));
            }
        } catch (Exception e) {
            log.error("Filed to initialize genesis state!", e);
            throw new StorageException(StorageErrorCode.STORAGE_FAILED);
        }
    }

    @Bean
    public Cache<String, MessageContainer> cache() {
        return Caffeine.newBuilder()
                .build();
    }

    @Bean
    public Cache<String, Integer> responseCache() {
        return Caffeine.newBuilder()
                .build();
    }
    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public Sinks.Many<Message> incomingRawMessagesSink() {
        return Sinks.many().multicast()
                .onBackpressureBuffer();
    }

    @Bean
    public Flux<Message> incomingRawMessagesAssembledFlux(Sinks.Many<Message> incomingRawMessagesSink) {
        return incomingRawMessagesSink.asFlux();
    }

    @Bean
    public Sinks.Many<MessageTemplate> outgoingMessageTemplateSink() {
        return Sinks.many().multicast()
                .onBackpressureBuffer();
    }

    @Bean
    public Flux<MessageTemplate> outgoingMessageTemplateFlux(Sinks.Many<MessageTemplate> outgoingMessageTemplateSink) {
        return outgoingMessageTemplateSink.asFlux();
    }

    @Bean
    public Sinks.Many<MempoolTransaction> mempoolSink() {
        return Sinks.many().multicast()
                .onBackpressureBuffer();
    }

    @Bean
    public Flux<MempoolTransaction> mempoolFlux(Sinks.Many<MempoolTransaction> sink) {
        return sink.asFlux();
    }

    @Bean
    public Messenger messenger(Flux<MessageTemplate> outgoingMessageTemplateFlux, ConnectionsStorage connectionsStorage) {
        Messenger messenger = new Messenger(connectionsStorage);
        messenger.accept(outgoingMessageTemplateFlux);

        return messenger;
    }

    @Bean
    public Sinks.Many<Funding> fundingSink() {
        return Sinks.many().multicast()
                .onBackpressureBuffer();
    }

    @Bean
    public Flux<Funding> fundingFlux(Sinks.Many<Funding> sink) {
        return sink.asFlux();
    }

    @Bean
    public FundingServices fundingServices(Flux<Funding> fundingFlux) {
        FundingServices fundingServices = new FundingServices(storage.getStorage(StorageTypes.STATE));
        fundingServices.accept(fundingFlux);

        return fundingServices;
    }

    @Bean
    public TransactionProcessor transactionProcessor(Flux<MempoolTransaction> mempoolFlux,
                                                     Sinks.Many<MessageTemplate> outgoingMessageTemplateSink) {
        TransactionProcessor processor = new TransactionProcessor(storage, outgoingMessageTemplateSink);
        processor.accept(mempoolFlux);

        return processor;
    }

    @Bean
    public AssemblerMessenger messageAssembler() {
        return new AssemblerMessenger();
    }

    @Bean
    public MessageProcessor messageProcessor(Flux<Message> incomingRawMessagesAssembledFlux,
                                             AssemblerMessenger assemblerMessenger) {
        MessageProcessor processor = new MessageProcessor(assemblerMessenger);
        processor.accept(incomingRawMessagesAssembledFlux);

        return processor;
    }

    @Bean
    public DisposableServer startWebSocketServer(WebSocketRouterHandler webSocketRouterHandler) {
        return HttpServer.create()
                .port(1337)
                .handle()
                .route(webSocketRouterHandler)
                .bindNow();
    }

    @Bean
    public WebSocketRouterHandler webSocketRouterHandler() {
        return new WebSocketRouterHandler();
    }
}
