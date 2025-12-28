/*
 * Test input for Hamcrest assertThat messages.
 */
package net.openhft.quality;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class InputHamcrestMessages {

    public void testHamcrestMessages() {
        assertThat("actual", is("expected"));
        assertThat("bad input", "actual", is("expected"));
        assertThat("expected result should match", "actual", is("expected"));
    }
}
