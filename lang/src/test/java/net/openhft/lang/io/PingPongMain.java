package net.openhft.lang.io;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

/**
 * Created by peter on 21/11/14.
 */
public class PingPongMain {
    public static final int PORT = Integer.getInteger("port", 8181);
    public static final int SIZE = Integer.getInteger("size", 128);

    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            startServer();
        else
            startClient(args[0]);
    }

    private static void startClient(String hostname) throws IOException {
        int runs = 100000;
        int[] times = new int[runs];
        SocketChannel sc = SocketChannel.open(new InetSocketAddress(hostname, PORT));
        System.out.println("Connected to " + hostname + ":" + PORT);
        sc.configureBlocking(false);
        ByteBuffer bb = ByteBuffer.allocateDirect(SIZE);
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < runs; i++) {
                long start = System.nanoTime();
                bb.clear();
                while (bb.remaining() > 0) {
                    int len = sc.write(bb);
                    if (len < 0)
                        throw new EOFException("write");
                }
                bb.clear();
                while (bb.remaining() > 0) {
                    int len = sc.read(bb);
                    if (len < 0)
                        throw new EOFException("read");
                }
                times[i] = (int) (System.nanoTime() - start);
            }
            Arrays.sort(times);
            System.out.printf("50/90/99/99.9 %%tile %,d / %,d / %,d / %,d%n",
                    times[runs / 2] / 1000,
                    times[runs - runs / 10] / 1000,
                    times[runs - runs / 100] / 1000,
                    times[runs - runs / 1000] / 1000
            );
        }
        sc.close();
    }

    private static void startServer() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().setReuseAddress(true);
        ssc.socket().bind(new InetSocketAddress(PORT));

        System.out.println("Listening for one connection on port " + PORT);
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);
        ByteBuffer bb = ByteBuffer.allocateDirect(16 * 1024);
        bb.clear();          // Prepare buffer for use
        while (sc.read(bb) >= 0 || bb.position() != 0) {
            bb.flip();
            sc.write(bb);
            bb.compact();    // In case of partial write
        }
        sc.close();
        ssc.close();
        System.out.println("... finished");
    }
}
