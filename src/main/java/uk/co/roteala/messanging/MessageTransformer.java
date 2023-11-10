package uk.co.roteala.messanging;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import uk.co.roteala.common.messenger.*;

import java.util.function.Function;
@RequiredArgsConstructor
public class MessageTransformer implements Function<Flux<Message>, Publisher<Message>> {

    private final AssemblerMessenger assemblerMessenger;

    private final ExecutorMessenger executorMessenger;

    @Override
    public Publisher<Message> apply(Flux<Message> messageFlux) {
        return this.assemblerMessenger
                .assemble(messageFlux)
                .subscribe(executorMessenger);
    }
}
