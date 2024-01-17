package uk.co.roteala.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.configs.BrokerConfigs;
import uk.co.roteala.handlers.TransmissionHandler;
import uk.co.roteala.net.ConnectionsStorage;
import uk.co.roteala.net.Peer;
import uk.co.roteala.storage.Storages;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerInitializer extends AbstractVerticle {

    @Autowired
    private final Storages storage;

    @Autowired
    private final BrokerConfigs configs;

    @Autowired
    private TransmissionHandler transmissionHandler;

    @Autowired
    private ConnectionsStorage connectionStorage;

    @Override
    public void start(Promise<Void> startPromise) {
        NetServer server = this.tcpServerSetUp();
        HttpServer webSocket = this.webSocketServer();

        server.listen(result -> {
            if(result.succeeded()) {
                log.info("Broker server listening on port: {}", server.actualPort());
                //startPromise.complete();
            }
        });

        webSocket.listen(1337, result -> {
            if (result.succeeded()) {
                log.info("Web socket server started!");
            }
        });

        startPromise.complete();
    }

    private NetServer tcpServerSetUp() {
        NetServerOptions options = new NetServerOptions();
        options.setPort(7331);
        options.setHost(configs.getNodeServerIP());

        NetServer server = vertx.createNetServer(options);

        server.connectHandler(new SocketConnectionHandler());

        return server;
    }

    private HttpServer webSocketServer() {
        HttpServerOptions options = new HttpServerOptions();
        options.setPort(1337);
        options.setHost(configs.getNodeServerIP());

        HttpServer server = vertx.createHttpServer(options);

        server.connectionHandler(new HttpWebSocketHandler());

        server.webSocketHandler(new WebSocketHandler());

        return server;
    }

    private class WebSocketHandler implements Handler<ServerWebSocket> {
        @Override
        public void handle(ServerWebSocket event) {
            //event.write();
        }
    }

    private class HttpWebSocketHandler implements Handler<HttpConnection> {
        @Override
        public void handle(HttpConnection event) {
        }
    }

    private class SocketConnectionHandler implements Handler<NetSocket> {
        @Override
        public void handle(NetSocket event) {
            Peer peer = new Peer();
            peer.setActive(true);
            peer.setPort(7331);
            peer.setAddress(event.remoteAddress().hostAddress());

            storage.getStorage(StorageTypes.PEERS)
                            .put(true, peer.getKey(), peer);

            log.info("New peer connection from: {}", event.remoteAddress().hostAddress());
            connectionStorage.getAsServerConnections().add(event);

            event.handler(transmissionHandler
                    .processWithConnection(event)
            );

            event.closeHandler(close -> {
                peer.setActive(false);
                peer.setLastTimeSeen(System.currentTimeMillis());

                storage.getStorage(StorageTypes.PEERS)
                                .put(true, peer.getKey(), peer);

                log.info("Node: {} disconnected!", event.remoteAddress().hostAddress());

                connectionStorage.getAsServerConnections()
                        .remove(event);
            });
        }
    }

//    private Consumer<Connection> connectionStorageHandler() {
//        return connection -> {
//            Peer peer = new Peer();
//            peer.setActive(true);
//            peer.setPort(7331);
//            peer.setAddress(parseAddress(connection.address()));
//
//            this.storage.getStorage(StorageTypes.PEERS)
//                    .put(true, peer.getKey(), peer);
//
//            log.info("New connection from:{}", peer.getAddress());
//            log.info("Connection:{}", connection);
//            this.connectionStorage.getServerConnections()
//                                    .add(connection);
//
//            connection.onDispose(() -> {
//                peer.setActive(false);
//                peer.setLastTimeSeen(System.currentTimeMillis());
//
//                this.storage.getStorage(StorageTypes.PEERS)
//                        .put(true, peer.getKey(), peer);
//
//                log.info("Node disconnected!");
//                this.connectionStorage.getServerConnections()
//                        .remove(connection);
//            });
//        };
//    }

    private String parseAddress(SocketAddress address) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;

        return inetSocketAddress.getAddress().getHostAddress();
    }
}
