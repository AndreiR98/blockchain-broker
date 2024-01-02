package uk.co.roteala.processor;

import io.reactivex.rxjava3.functions.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import uk.co.roteala.common.messenger.MessageTemplate;
import uk.co.roteala.common.messenger.Messenger;
import uk.co.roteala.common.monetary.MoveFund;
import uk.co.roteala.messanging.AssemblerMessenger;
import uk.co.roteala.net.ConnectionsStorage;
import uk.co.roteala.storage.Storages;

@Slf4j
@Component
public class MessageProcessor {
    @Autowired
    private AssemblerMessenger assemblerMessenger;

    @Autowired
    private ConnectionsStorage connectionsStorage;

    @Autowired
    private Storages storages;

    @Autowired
    private Messenger messenger;

    @Autowired
    private Flux<MessageTemplate> incomingRawMessagesAssembledFlux;

    @Bean
    public void messageFluxConsumer() {
        new MessageFluxConsumer().accept(this.incomingRawMessagesAssembledFlux);
    }

    private class MessageFluxConsumer implements Consumer<Flux<MessageTemplate>> {

        @Override
        public void accept(Flux<MessageTemplate> messageTemplateFlux) {
            //processMessageTemplate(messageTemplateFlux);
        }

        private void processMessageTemplate(MessageTemplate template) {
            log.info("Incoming from client:{}", template);
        }
    }
}
