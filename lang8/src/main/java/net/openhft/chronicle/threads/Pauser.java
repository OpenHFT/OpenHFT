package net.openhft.chronicle.threads;

/**
 * Created by peter on 11/12/14.
 */
public interface Pauser {
    public void reset();

    public void pause();

    public void pause(long maxPauseNS);

    public void unpause();
}
