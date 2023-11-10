package uk.co.roteala.handlers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import uk.co.roteala.common.messenger.AssemblerSupplier;
import uk.co.roteala.common.messenger.Message;
import uk.co.roteala.messanging.AssemblerMessenger;
import uk.co.roteala.messanging.MessageTransformer;



import java.util.function.BiFunction;


/**
 * Handles the connection between the clients and server
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransmissionHandler implements BiFunction<NettyInbound, NettyOutbound, Publisher<Void>>, AssemblerSupplier<Message> {

    @Autowired
    private AssemblerMessenger assembler;

    @Override
    public Mono<Void> apply(NettyInbound inbound, NettyOutbound outbound) {
        inbound.receive().retain()
                .parallel(4)
                .map(this::mapper)//Map into message chunk
                .map(this.assembler)//assemble the chunks into
                .flatMap(executor)
                .then()
                .subscribe();

        return outbound.neverComplete();
    }
}
