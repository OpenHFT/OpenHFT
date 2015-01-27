package net.openhft.lang.thread;

import org.junit.Ignore;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Created by peter on 11/12/14.
 */
public class LightPauserTest {
    @Test
    @Ignore
    public void testLightPauser() throws InterruptedException {
        final LightPauser pauser = new LightPauser(NANOSECONDS.convert(20, MICROSECONDS), NANOSECONDS.convert(200, MICROSECONDS));
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
