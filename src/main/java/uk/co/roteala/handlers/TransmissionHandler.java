package uk.co.roteala.handlers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import uk.co.roteala.common.messenger.MessengerUtils;
import uk.co.roteala.messanging.AssemblerMessenger;
import uk.co.roteala.messanging.ExecutorMessenger;


import java.util.function.BiFunction;


/**
 * Handles the connection between the clients and server
 * */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransmissionHandler implements BiFunction<NettyInbound, NettyOutbound, Publisher<Void>> {

    @Autowired
    private AssemblerMessenger assembler;

    @Autowired
    private ExecutorMessenger executor;

    @Override
    public Mono<Void> apply(NettyInbound inbound, NettyOutbound outbound) {
        inbound.receive().retain()
                .parallel(4)
                .map(MessengerUtils::deserialize)//Map into message chunk
                .map(this.assembler)//assemble the chunks into
                .flatMap(this.executor)
                .then()
                .subscribe();

        return outbound.neverComplete();
    }
}
