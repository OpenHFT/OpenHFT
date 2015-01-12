package net.openhft.lang.data.attic;

/**
 * Created by peter on 1/10/15.
 */
public interface WireValue {
    boolean booleanValue();
    byte byteValue();
    short shortValue();
    char charValue();
    long longValue();
    float floatValue();
    double doubleValue();
    String stringValue();
    void copyTo(StringBuilder sb);
    <T> T asInstanceOf(Class<T> tClass);

}
