package net.openhft.lang.data;

/**
 * Created by peter on 1/10/15.
 */
public interface WireKey<T> {
    String name();

    int code();

    Class<T> type();

    boolean skipIfDefault();

    boolean hasDefault();

    EncodeMode encodeMode();

    T defaultValue();
}
