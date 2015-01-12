package net.openhft.lang.data;

/**
 * Created by peter on 1/10/15.
 */
public enum TestField implements WireKey {
    BOOL1(true), BOOL2(false);

    final Object defaultValue;

    TestField(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public int code() {
        return ordinal();
    }

    @Override
    public Class type() {
        return defaultValue.getClass();
    }

    @Override
    public boolean skipIfDefault() {
        return false;
    }

    @Override
    public boolean hasDefault() {
        return defaultValue() != null;
    }

    @Override
    public EncodeMode encodeMode() {
        return EncodeMode.LITERAL;
    }

    @Override
    public Object defaultValue() {
        return defaultValue;
    }
}
