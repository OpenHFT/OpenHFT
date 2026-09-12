/*
 * Test input for qualified non-JUnit assert methods with JUnit static imports.
 */
package net.openhft.quality;

import static org.junit.jupiter.api.Assertions.*;

public class InputQualifiedNonJUnitAssertions {

    private final LocalAsserter asserter = new LocalAsserter();

    public void customAsserterCalls() {
        asserter.assertThreadConfined();
        LocalAsserter.assertIfEnabled(true);
        asserter.assertIfEnabled(false);
    }

    private static final class LocalAsserter {
        void assertThreadConfined() {
        }

        static void assertIfEnabled(boolean enabled) {
        }

        boolean assertIfEnabled(boolean enabled) {
            return enabled;
        }
    }
}
