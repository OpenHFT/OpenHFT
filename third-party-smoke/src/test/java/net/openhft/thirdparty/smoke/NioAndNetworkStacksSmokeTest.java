/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.junit.jupiter.api.Test;
import proguard.Configuration;
import proguard.ProGuard;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke tests for NIO/network stacks and ProGuard construction.
 */
class NioAndNetworkStacksSmokeTest {

    /**
     * Single thread count used in worker configuration.
     */
    private static final int SINGLE_THREAD = 1;

    @Test
    void grizzlyCanBuildStartAndShutdownTransport() throws Exception {
        TCPNIOTransport transport = TCPNIOTransportBuilder.newInstance()
                .build();
        transport.start();
        transport.shutdownNow();
    }

    @Test
    void minaCanCreateAndDisposeAcceptor() {
        IoAcceptor acceptor = new NioSocketAcceptor();
        assertNotNull(acceptor);
        acceptor.dispose();
    }

    @Test
    void nettyCanConfigureServerBootstrap() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(SINGLE_THREAD);
        EventLoopGroup workerGroup = new NioEventLoopGroup(SINGLE_THREAD);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class);
            assertNotNull(bootstrap);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Test
    void proguardConfigurationAndProGuardCanBeConstructed() {
        Configuration configuration = new Configuration();
        ProGuard proGuard = new ProGuard(configuration);
        assertNotNull(proGuard);
    }
}
