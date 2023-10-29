package uk.co.roteala.handlers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import uk.co.roteala.common.messenger.ClientMessage;
import uk.co.roteala.common.messenger.MStream;
import uk.co.roteala.messanging.ClientReaderSupplier;
import uk.co.roteala.messanging.NettyInboundSupplier;



import java.util.function.BiFunction;


/**
 * Handles the connection between the clients and server
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransmissionHandler implements BiFunction<NettyInbound, NettyOutbound, Publisher<Void>> {

    @Override
    public Mono<Void> apply(NettyInbound inbound, NettyOutbound outbound) {

        return outbound.neverComplete();
    }
}
