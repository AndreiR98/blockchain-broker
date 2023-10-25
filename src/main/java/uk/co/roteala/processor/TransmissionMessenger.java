package uk.co.roteala.processor;

import reactor.netty.ByteBufFlux;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import uk.co.roteala.common.messenger.AbstractStream;
import uk.co.roteala.common.messenger.InboundSupplier;
import uk.co.roteala.common.messenger.OutboundSupplier;

//public class TransmissionMessenger implements AbstractStream<ByteBufFlux, NettyOutbound> {
//    @Override
//    public ByteBufFlux inboundSupplier(InboundSupplier<? super ByteBufFlux> inboundSupplier) {
//        return null;
//    }
//
//    @Override
//    public NettyOutbound outboundSupplier(OutboundSupplier<? super NettyOutbound> outboundSupplier) {
//        return null;
//    }
//}
