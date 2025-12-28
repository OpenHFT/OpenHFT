/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Advanced smoke tests verifying Netty networking framework features beyond basic bootstrap configuration.
 * <p>
 * Tests ByteBuf memory operations, ChannelPipeline handler composition, EmbeddedChannel for unit testing
 * handlers, and EventLoop scheduling to ensure deeper Netty functionality is available and working.
 */
class NettyAdvancedSmokeTest {

    private static final int TEST_INT_VALUE = 42;
    private static final int INITIAL_CAPACITY = 256;
    private static final long SCHEDULE_DELAY_MS = 100;

    @Test
    @DisplayName("Netty ByteBuf should support allocate, write, read, and release memory operations")
    void byteBufSupportsBasicOperations() {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(INITIAL_CAPACITY);
        try {
            assertEquals(1, buffer.refCnt(),
                    "Newly allocated ByteBuf should have reference count of 1");

            buffer.writeInt(TEST_INT_VALUE);
            assertEquals(4, buffer.readableBytes(),
                    "ByteBuf should have 4 readable bytes after writing one int");

            int readValue = buffer.readInt();
            assertEquals(TEST_INT_VALUE, readValue,
                    "ByteBuf readInt should return the value " + TEST_INT_VALUE + " that was written");
        } finally {
            buffer.release();
            assertEquals(0, buffer.refCnt(),
                    "ByteBuf reference count should be 0 after release()");
        }
    }

    @Test
    @DisplayName("Netty ChannelPipeline should accept and maintain order of multiple handlers")
    void channelPipelineAcceptsMultipleHandlers() {
        CountingHandler firstHandler = new CountingHandler();
        CountingHandler secondHandler = new CountingHandler();

        EmbeddedChannel channel = new EmbeddedChannel(firstHandler, secondHandler);
        try {
            List<String> handlerNames = channel.pipeline().names();

            assertTrue(handlerNames.size() >= 2,
                    "Pipeline should contain at least 2 handlers, found " + handlerNames.size());
            assertNotNull(channel.pipeline().first(),
                    "Pipeline.first() should return the first handler in the chain");
        } finally {
            channel.close();
        }
    }

    @Test
    @DisplayName("Netty EmbeddedChannel should process inbound messages through handler chain")
    void embeddedChannelProcessesInboundMessages() {
        CountingHandler handler = new CountingHandler();
        EmbeddedChannel channel = new EmbeddedChannel(handler);

        try {
            ByteBuf message = Unpooled.wrappedBuffer("test".getBytes(StandardCharsets.UTF_8));
            channel.writeInbound(message);

            assertEquals(1, handler.messageCount,
                    "Handler should have received exactly 1 inbound message");
            assertTrue(channel.finish(),
                    "EmbeddedChannel.finish() should return true when messages were processed");
        } finally {
            channel.close();
        }
    }

    @Test
    @DisplayName("Netty EmbeddedChannel should process outbound messages and make them readable")
    void embeddedChannelProcessesOutboundMessages() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            ByteBuf outboundData = Unpooled.wrappedBuffer("outbound".getBytes(StandardCharsets.UTF_8));
            channel.writeOutbound(outboundData);

            ByteBuf readData = channel.readOutbound();
            assertNotNull(readData,
                    "readOutbound() should return the ByteBuf that was written");
            assertEquals(8, readData.readableBytes(),
                    "Outbound ByteBuf should have 8 bytes for 'outbound' string");
            readData.release();
        } finally {
            channel.close();
        }
    }

    @Test
    @DisplayName("Netty EventLoop should schedule tasks and return cancellable ScheduledFuture")
    void eventLoopSchedulesDelayedTasks() {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            AtomicBoolean taskRan = new AtomicBoolean(false);

            ScheduledFuture<?> future = group.schedule(
                    () -> taskRan.set(true),
                    SCHEDULE_DELAY_MS,
                    TimeUnit.MILLISECONDS
            );

            assertNotNull(future,
                    "EventLoop.schedule() should return a non-null ScheduledFuture");
            assertFalse(future.isDone(),
                    "Scheduled task should not be done immediately after scheduling");

            // Cancel the task to avoid waiting
            future.cancel(false);
            assertTrue(future.isCancelled(),
                    "ScheduledFuture should be cancellable via cancel()");
        } finally {
            group.shutdownGracefully().syncUninterruptibly();
        }
    }

    /**
     * Simple channel handler that counts received messages for testing pipeline processing.
     * Passes messages through to the next handler in the chain after incrementing the count.
     */
    static class CountingHandler extends ChannelInboundHandlerAdapter {
        int messageCount = 0;

        @Override
        public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
            messageCount++;
            ctx.fireChannelRead(msg);
        }
    }
}
