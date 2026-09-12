/*
 * Test input for AssertJ format messages with arguments.
 */
package net.openhft.quality;

import static org.assertj.core.api.Assertions.assertThat;

public class InputAssertJMessageArgs {

    public void testAssertJMessageArgs() {
        Object actual = "value";
        assertThat(actual).withFailMessage("expected %s", "value").isEqualTo("other");
    }
}
