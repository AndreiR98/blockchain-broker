package uk.co.roteala.messanging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import reactor.netty.NettyInbound;
import uk.co.roteala.common.messenger.MessageChunk;
import uk.co.roteala.common.messenger.ReaderSupplier;

@Slf4j
@RequiredArgsConstructor
public class ClientReaderSupplier implements ReaderSupplier<MessageChunk> {
}
