/*
 * Test input for logger calls with unresolved message parameter types.
 */
package net.openhft.quality;

import java.util.function.Consumer;

public class InputLogUnknownThrowableType {

    public void testJvmLambdaMessage() {
        Consumer<String> consumer =
                message -> net.openhft.chronicle.core.Jvm.startup()
                        .on(InputLogUnknownThrowableType.class, message);
        consumer.accept("example");
    }
}
