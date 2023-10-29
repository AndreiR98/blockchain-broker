package uk.co.roteala.messanging;

import lombok.Builder;
import lombok.Data;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import uk.co.roteala.common.messenger.MessageChunk;
import uk.co.roteala.common.messenger.ReaderSupplier;
import uk.co.roteala.common.messenger.SourceSupplier;

@Data
@Builder
public class NettyInboundSupplier implements SourceSupplier<NettyInbound> {
    private NettyInbound inboundSource;
    private NettyOutbound outbound;
    private ReaderSupplier<MessageChunk> readerSupplier;

    @Override
    public NettyInbound source() {
        return this.inboundSource;
    }

    @Override
    public ReaderSupplier<MessageChunk> reader() {
        return this.readerSupplier;
    }
}
