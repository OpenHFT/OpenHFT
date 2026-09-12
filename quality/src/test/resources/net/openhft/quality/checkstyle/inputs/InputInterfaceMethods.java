package net.openhft.quality.checkstyle.inputs;

// Interface with default and static methods - no test annotations
interface InputInterfaceMethods {
    static void staticMethod() {
    }

    default void defaultMethod() {
    }

    void abstractMethod();
}
