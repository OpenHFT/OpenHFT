package net.openhft.lang.thread;

/**
 * Created by peter.lawrey on 11/12/14.
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
