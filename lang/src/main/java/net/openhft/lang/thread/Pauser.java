package net.openhft.lang.thread;

/**
 * Created by peter on 11/12/14.
 */
public interface Pauser {
    public void reset();

    public void pause();

    public void unpause();
}
