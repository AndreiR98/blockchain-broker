package uk.co.roteala.server;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpServer;
import uk.co.roteala.common.storage.StorageTypes;
import uk.co.roteala.net.ConnectionsStorage;
import uk.co.roteala.net.Peer;
import uk.co.roteala.storage.Storages;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private final Storages storage;

    private final ConnectionsStorage connectionStorage;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        new Thread(() -> TcpServer.create()
                .doOnConnection(connectionStorageHandler())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .port(7331)
                .doOnBound(server -> log.info("Server started on address:{} and port:{}", server.address(), server.port()))
                .doOnUnbound(server -> log.info("Server stopped!"))
                .bindNow()
                .onDispose()
                .doOnError(e -> log.error("Error occurred with the TCP server", e)))
                .start();
    }
    private Consumer<Connection> connectionStorageHandler() {
        return connection -> {
            Peer peer = new Peer();
            peer.setActive(true);
            peer.setPort(7331);
            peer.setAddress(parseAddress(connection.address()));

            this.storage.getStorage(StorageTypes.PEERS)
                    .put(true, peer.getKey(), peer);

            log.info("New connection from:{}", peer.getAddress());
            log.info("Connection:{}", connection);
            this.connectionStorage.getServerConnections()
                                    .add(connection);

            connection.onDispose(() -> {
                peer.setActive(false);
                peer.setLastTimeSeen(System.currentTimeMillis());

                this.storage.getStorage(StorageTypes.PEERS)
                        .put(true, peer.getKey(), peer);

                log.info("Node disconnected!");
                this.connectionStorage.getServerConnections()
                        .remove(connection);
            });
        };
    }

    private String parseAddress(SocketAddress address) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;

        return inetSocketAddress.getAddress().getHostAddress();
    }
}
