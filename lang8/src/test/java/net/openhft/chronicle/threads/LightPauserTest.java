package net.openhft.chronicle.threads;

import org.junit.Test;

/**
 * Created by peter on 11/12/14.
 */
public class LightPauserTest {
    @Test
    public void testLightPauser() throws InterruptedException {
        final LightPauser pauser = new LightPauser(100 * 1000, 100 * 1000);
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!Thread.interrupted())
                    pauser.pause();
            }
        };
        thread.start();

        for (int t = 0; t < 3; t++) {
            long start = System.nanoTime();
            int runs = 10000000;
            for (int i = 0; i < runs; i++)
                pauser.unpause();
            long time = System.nanoTime() - start;
            System.out.printf("Average time to unpark was %,d ns%n", time / runs);
            Thread.sleep(20);
        }
        thread.interrupt();
    }
}
