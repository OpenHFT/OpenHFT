package net.openhft.quality.checkstyle.inputs;

// Interface with default and static methods - no test annotations
interface InputInterfaceMethods {
    default void defaultMethod() {
    }

    static void staticMethod() {
    }

    void abstractMethod();
}
