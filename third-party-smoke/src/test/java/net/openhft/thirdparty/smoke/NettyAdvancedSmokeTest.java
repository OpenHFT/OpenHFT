/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NettyAdvancedSmokeTest {
    @Test
    void pipelineDecodesAndReleasesItsInput() {
        EmbeddedChannel channel = new EmbeddedChannel(new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            protected void channelRead0(ChannelHandlerContext context, ByteBuf input) {
                context.fireChannelRead(input.readInt() + 1);
            }
        }, new SimpleChannelInboundHandler<Integer>() {
            @Override
            protected void channelRead0(ChannelHandlerContext context, Integer input) {
                context.fireChannelRead(input * 2);
            }
        });
        ByteBuf input = Unpooled.buffer().writeInt(20);
        try {
            assertTrue(channel.writeInbound(input));
            assertEquals(Integer.valueOf(42), channel.readInbound());
            assertEquals(0, input.refCnt(), "the decoder must release its input buffer");
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void eventLoopExecutesAScheduledTask() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            assertEquals(Integer.valueOf(42), group.next().schedule(() -> 42, 1, TimeUnit.MILLISECONDS)
                    .get(5, TimeUnit.SECONDS));
        } finally {
            assertTrue(group.shutdownGracefully(0, 5, TimeUnit.SECONDS).await(6, TimeUnit.SECONDS),
                    "the owned event loop must terminate");
        }
    }
}
