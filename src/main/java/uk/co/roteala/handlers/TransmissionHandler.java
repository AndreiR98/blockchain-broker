package uk.co.roteala.handlers;


import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import reactor.core.publisher.Sinks;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import uk.co.roteala.common.messenger.Message;
import uk.co.roteala.common.messenger.MessengerUtils;
import uk.co.roteala.messanging.AssemblerMessenger;
import uk.co.roteala.messanging.ExecutorMessenger;
import uk.co.roteala.net.ConnectionsStorage;
import uk.co.roteala.storage.Storages;


import java.util.function.BiFunction;


/**
 * Handles the connection between the clients and server
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransmissionHandler implements Handler<Buffer> {

    @Autowired
    private ConnectionsStorage connectionsStorage;

    @Autowired
    private Sinks.Many<Message> sink;

    private Buffer buffer = Buffer.buffer();
    @Override
    public void handle(Buffer event) {
        log.info("V:{}", event.toString());
    }
}
