/*
 * Test input for MMTooShort suppression.
 */
package net.openhft.quality;

import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputSuppressWarningsTooShort {

    @SuppressWarnings("MMTooShort")
    @DisplayName("alpha beta gamma delta echo")
    public void methodSuppressedDisplayName() {
        Logger log = LoggerFactory.getLogger(InputSuppressWarningsTooShort.class);
        log.info("bravo charlie delta");
    }

    @DisplayName("foxtrot golf hotel india juliet")
    public void methodUnsuppressedDisplayName() {
        Logger log = LoggerFactory.getLogger(InputSuppressWarningsTooShort.class);
        log.info("kilo lima mike");
    }

    @SuppressWarnings("MMTooShort")
    public static class SuppressedClass {
        @DisplayName("november oscar papa quebec romeo")
        public void classSuppressedDisplayName() {
            Logger log = LoggerFactory.getLogger(SuppressedClass.class);
            log.info("sierra tango uniform");
        }
    }
}
