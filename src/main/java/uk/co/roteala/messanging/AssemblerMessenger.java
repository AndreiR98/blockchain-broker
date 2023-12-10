package uk.co.roteala.messanging;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.co.roteala.common.BasicModel;
import uk.co.roteala.common.events.MessageTemplate;
import uk.co.roteala.common.messenger.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;


public class AssemblerMessenger implements Function<Message, MessageTemplate> {
    private final Cache<String, MessageContainer> cache = Caffeine.newBuilder()
            .maximumSize(500)
            .build();

    @Override
    public MessageTemplate apply(Message message) {
        try {
            MessageContainer container = cache.get(message.getMessageId(), k -> new MessageContainer());

            if (message.getMessageType() == MessageType.EMPTY) {
                return null;
            }

            if (container.canAggregate()) {
                return container.aggregate();
            }

            if (message.getMessageType() == MessageType.KEY) {
                container.setKey((MessageKey) message);
            }

            if (message.getMessageType() == MessageType.CHUNK) {
                container.addChunk((MessageChunk) message);
            }
        } catch (Exception e) {
            // Handle the exception appropriately, logging or rethrowing if necessary
            e.printStackTrace();
        }

        return null;
    }
}
