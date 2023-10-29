package uk.co.roteala.messanging;

import uk.co.roteala.common.messenger.ClientMessage;
import uk.co.roteala.common.messenger.Executor;
import uk.co.roteala.common.messenger.ExecutorSupplier;

public class ExecutorMessenger implements ExecutorSupplier<ClientMessage> {
    @Override
    public Executor<ClientMessage> get() {
        return new CustomExecutor();
    }

    private class CustomExecutor implements Executor<ClientMessage> {
        @Override
        public void init() {

        }

        @Override
        public void process(ClientMessage message) {

        }

        @Override
        public void close() {

        }
    }
}
