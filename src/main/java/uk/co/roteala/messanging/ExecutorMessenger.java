package uk.co.roteala.messanging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import uk.co.roteala.common.BasicModel;
import uk.co.roteala.common.events.MessageTemplate;
import uk.co.roteala.common.messenger.Message;

import java.util.function.Function;

@Slf4j
public class ExecutorMessenger implements Function<MessageTemplate, Publisher<Void>> {
    @Override
    public Publisher<Void> apply(MessageTemplate basicModel) {
        return null;
    }
}
