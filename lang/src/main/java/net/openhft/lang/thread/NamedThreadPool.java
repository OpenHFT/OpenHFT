package net.openhft.lang.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: peter
 * Date: 18/08/13
 * Time: 11:37
 */
public class NamedThreadPool implements ThreadFactory {
    private final AtomicInteger id = new AtomicInteger();
    private final String name;
    private final Boolean daemon;

    public NamedThreadPool(String name) {
        this(name, null);
    }

    public NamedThreadPool(String name, Boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        int id = this.id.getAndIncrement();
        String nameN = id == 0 ? name : (name + '-' + id);
        Thread t = new Thread(r, nameN);
        if (daemon != null)
            t.setDaemon(daemon);
        return t;
    }
}
