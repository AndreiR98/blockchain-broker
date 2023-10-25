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
import uk.co.roteala.net.Peer;
import uk.co.roteala.storage.Storages;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private final Storages storage;

    private List<Connection> connections = new ArrayList<>();

    @Bean
    public List<Connection> connectionStorage() {
        return this.connections;
    }
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        new Thread(() -> {
            TcpServer.create()
                    .doOnConnection(connection -> {
                        // TODO: Handle the connection here
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .port(7331)
                    .doOnBound(server -> log.info("Server started on address:{} and port:{}", server.address(), server.port()))
                    .doOnUnbound(server -> log.info("Server stopped!"))
                    .bindNow()
                    .onDispose()
                    .doOnError(e -> log.error("Error occurred with the TCP server", e));
        }).start();
    }

    @Bean
    public Consumer<Connection> handleClientDisconnect() {
        return connection -> {
            log.info("Client disconnected!!");
        };
    }

    @Bean
    public Consumer<Connection> connectionStorageHandler() {
        return connection -> {
            Peer peer = new Peer();
            peer.setActive(true);
            peer.setPort(7331);
            peer.setAddress(parseAddress(connection.address()));

//            storage.addPeer(peer);
//
//            log.info("New connection from:{}", peer);
//            this.connections.add(connection);
//
//            connection.onDispose(() -> {
//                peer.setActive(false);
//                peer.setLastTimeSeen(System.currentTimeMillis());
//
//                storage.addPeer(peer);
//
//                log.info("Node disconnected!");
//                connections.remove(connection);
//            });
        };
    }

    private String parseAddress(SocketAddress address) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
        String hostWithoutPort = inetSocketAddress.getAddress().getHostAddress();

        return hostWithoutPort;
    }

    /**
     * Create bean to handle the server-client communications sending and receiving responses
     * */
//    @Bean
//    public TransmissionHandler transmissionHandler() {
//        return new TransmissionHandler(messageProcessor());
//    }
}
