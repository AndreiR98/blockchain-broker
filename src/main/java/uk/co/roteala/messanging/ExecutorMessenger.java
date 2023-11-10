package uk.co.roteala.messanging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import uk.co.roteala.common.messenger.ClientMessage;
import uk.co.roteala.common.messenger.Executor;
import uk.co.roteala.common.messenger.ExecutorSupplier;
import uk.co.roteala.common.messenger.Message;

@Slf4j
@RequiredArgsConstructor
public class ExecutorMessenger implements Subscriber<Message> {
    @Override
    public void onSubscribe(Subscription subscription) {

    }

    @Override
    public void onNext(Message message) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
