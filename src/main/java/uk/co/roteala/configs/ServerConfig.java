package uk.co.roteala.configs;


import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.ReaderInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.http.websocket.WebsocketOutbound;
import reactor.netty.tcp.TcpServer;
import uk.co.roteala.common.messenger.*;
import uk.co.roteala.common.storage.ColumnFamilyTypes;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.core.Blockchain;
import uk.co.roteala.exceptions.StorageException;
import uk.co.roteala.exceptions.errorcodes.StorageErrorCode;

import uk.co.roteala.messanging.AssemblerMessenger;
import uk.co.roteala.messanging.ClientReaderSupplier;
import uk.co.roteala.messanging.ExecutorMessenger;
import uk.co.roteala.security.ECKey;
import uk.co.roteala.storage.Storages;
import uk.co.roteala.utils.Constants;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class ServerConfig {
    private final BrokerConfigs configs;

    private final Storages storage;

    private List<WebsocketOutbound> webSocketConnections = new ArrayList<>();

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

                storage.getStorage(StorageTypes.BLOCKCHAIN)
                        .put(true, ColumnFamilyTypes.BLOCKS, "2".getBytes(), Constants.GENESIS_BLOCK);
            }
        } catch (Exception e) {
            log.error("Filed to initialize genesis state!", e);
            throw new StorageException(StorageErrorCode.STORAGE_FAILED);
        }
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
