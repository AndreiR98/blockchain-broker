package uk.co.roteala.messanging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import uk.co.roteala.common.messenger.MessageTemplate;

import java.util.function.Function;

@Slf4j
public class ExecutorMessenger implements Function<MessageTemplate, Publisher<Void>> {
    @Override
    public Publisher<Void> apply(MessageTemplate basicModel) {
        return null;
    }
}
