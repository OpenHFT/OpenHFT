package net.openhft.chronicle.threads;

/**
 * Created by peter on 11/12/14.
 */
public enum BusyPauser implements Pauser {
    INSTANCE;

    @Override
    public void reset() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void pause(long maxPauseNS) {

    }

    @Override
    public void unpause() {
    }

}
