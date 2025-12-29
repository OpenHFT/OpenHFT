/*
 * Test input for annotation description message checks.
 */
package net.openhft.quality;

import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.ParameterizedTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.runners.Parameterized.Parameters;

public class InputAnnotationMessages {

    @DisplayName("cache ready")
    public void displayNameTest() {
    }

    @ParameterizedTest(name = "expected")
    public void parameterisedNameTest(String value) {
    }

    @RepeatedTest(name = "L42 run")
    public void repeatedNameTest() {
    }

    @Disabled("should be true")
    public void disabledTest() {
    }

    @DisabledIfSystemProperty(named = "os.name", matches = "Windows", disabledReason = "line 10")
    public void disabledReasonTest() {
    }

    @Ignore("value")
    public void ignoredTest() {
    }

    @Disabled
    public void disabledMissingReason() {
    }

    @Ignore
    public void ignoredMissingReason() {
    }

    @Parameters(name = "{index}: {0}")
    public Object[] parameters() {
        return new Object[0];
    }

    @DisplayName("order  should persist")
    public void displayNameWhitespaceTest() {
    }

    @Disabled(/* awaiting lower-latency fix */)
    public void disabledMissingReasonWithComment() {
    }
}
