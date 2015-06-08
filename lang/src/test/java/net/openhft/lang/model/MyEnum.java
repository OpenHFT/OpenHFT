package net.openhft.lang.model;

/**
 * Created by pct25 on 6/4/2015.
 */
public enum MyEnum {
    A(1), B(2), C(3);

    private final int var;

    MyEnum(int var) {
        this.var = var;
    }

    public int getVar() {
        return this.var;
    }
}
