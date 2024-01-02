package uk.co.roteala.configs;



import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.netty.tcp.TcpServer;
import uk.co.roteala.common.MempoolTransaction;
import uk.co.roteala.common.messenger.Message;
import uk.co.roteala.common.messenger.MessageContainer;
import uk.co.roteala.common.messenger.MessageTemplate;
import uk.co.roteala.common.messenger.Messenger;
import uk.co.roteala.common.storage.ColumnFamilyTypes;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.core.Blockchain;
import uk.co.roteala.exceptions.StorageException;
import uk.co.roteala.exceptions.errorcodes.StorageErrorCode;

import uk.co.roteala.messanging.AssemblerMessenger;
import uk.co.roteala.messanging.ExecutorMessenger;
import uk.co.roteala.net.ConnectionsStorage;
import uk.co.roteala.processor.MessageProcessor;
import uk.co.roteala.processor.TransactionProcessor;
import uk.co.roteala.security.ECKey;
import uk.co.roteala.server.ServerInitializer;
import uk.co.roteala.storage.Storages;
import uk.co.roteala.utils.BlockchainUtils;
import uk.co.roteala.utils.Constants;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ServerConfig {
    private final BrokerConfigs configs;

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
    public TransactionProcessor transactionProcessor(Messenger messenger, Flux<MempoolTransaction> mempoolFlux,
                                                     Sinks.Many<MessageTemplate> outgoingMessageTemplateSink) {
        TransactionProcessor processor = new TransactionProcessor(storage, outgoingMessageTemplateSink);
        processor.accept(mempoolFlux);

        return processor;
    }

    //@Bean
//    public Mono<Void> startWebsocket() {
//        return HttpServer.create()
//                .port(1337)
//                .route(routerWebSocket())
//                .doOnBind(server -> log.info("Websocket server started!"))
//                //.doOnConnection(webSocketConnectionHandler())
//                .bindNow()
//                .onDispose();
//    }

   // @Bean
    public Consumer<Connection> webSocketConnectionHandler() {
        return connection -> {

            log.info("New explorer connected from:{}", connection);

            this.webSocketConnections.add((WebsocketOutbound) connection.outbound());

            connection.onDispose(() -> {
                log.info("Node disconnected!");
                this.webSocketConnections.remove((WebsocketOutbound) connection);
            });
        };
    }

    @Bean
    public AssemblerMessenger messageAssembler() {
        return new AssemblerMessenger();
    }

    @Bean
    public MessageProcessor messageProcessor() {
        return new MessageProcessor();
    }


//    @Bean
//    public Consumer<HttpServerRoutes> routerWebSocket() {
//        return httpServerRoutes -> httpServerRoutes.ws("/stateChain", webSocketRouterStorage());
//
    @Bean
    public List<WebsocketOutbound> webSocketConnections() {
        return this.webSocketConnections;
    }

//    @Bean
//    public WebSocketRouterHandler webSocketRouterStorage() {
//        return new WebSocketRouterHandler(storage);
//    }
}
