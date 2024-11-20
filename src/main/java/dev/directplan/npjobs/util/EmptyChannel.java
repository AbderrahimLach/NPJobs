package dev.directplan.npjobs.util;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.EventLoop;

import java.net.SocketAddress;

/**
 * @author DirectPlan
 */
public final class EmptyChannel extends AbstractChannel {

    private static final ChannelMetadata SINGLETON_METADATA = new ChannelMetadata(true);

    private final DefaultChannelConfig defaultChannelConfig = new DefaultChannelConfig(this);

    public EmptyChannel() {
        super(null);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return false;
    }

    @Override
    protected SocketAddress localAddress0() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

    @Override
    protected void doBind(SocketAddress localAddress) {}

    @Override
    protected void doDisconnect() {}

    @Override
    protected void doClose() {}

    @Override
    protected void doBeginRead() {}

    @Override
    protected void doWrite(ChannelOutboundBuffer in) {}

    @Override
    public ChannelConfig config() {
        return defaultChannelConfig;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public ChannelMetadata metadata() {
        return SINGLETON_METADATA;
    }
}
