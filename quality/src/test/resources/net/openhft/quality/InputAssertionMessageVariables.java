/*
 * Test input for assertion message variable templates.
 */
package net.openhft.quality;

public class InputAssertionMessageVariables {

    public void junit4MessageVariable(String actual) {
        String message = "dump: " + actual;
        org.junit.Assert.assertEquals(message, "text", actual);
    }

    public void junit5MessageVariable(String actual) {
        String message = "snapshot: " + actual;
        org.junit.jupiter.api.Assertions.assertEquals("text", actual, message);
    }
}
