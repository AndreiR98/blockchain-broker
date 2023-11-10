package uk.co.roteala.messanging;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.buffer.ByteBuf;
import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.co.roteala.common.BasicModel;
import uk.co.roteala.common.messenger.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;


public class AssemblerMessenger implements Function<Message, BasicModel> {
    private Cache<String, MessageContainer> cache = Caffeine.newBuilder()
            .maximumSize(500)
            .build();

    @Override
    public synchronized BasicModel apply(Message message) {
        try {
            if(this.cache.getIfPresent(message.getMessageId()) != null) {
                MessageContainer container = this.cache.getIfPresent(message.getMessageId());

                if(container != null) {
                    //check if you can aggregate into a message
                    if(canAggregate(message, container)) {}
                } else {
                    //if container does not exists then create one and add the empty key and chunk
                }
            }
        } catch (Exception e) {
            //
        }

        return null;
    }

    //check if it has key and all chunks
    private boolean canAggregate() {}
}
