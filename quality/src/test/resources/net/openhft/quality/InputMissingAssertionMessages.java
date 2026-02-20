/*
 * Test input for missing JUnit assertion and assumption messages.
 */
package net.openhft.quality;

public class InputMissingAssertionMessages {

    public void junit4Missing() {
        org.junit.Assert.assertTrue(true);
        org.junit.Assert.assertFalse(false);
        org.junit.Assume.assumeTrue(true);
        org.junit.Assume.assumeFalse(false);
        org.junit.Assert.assertEquals(1, 1);
        org.junit.Assert.assertEquals(1.0, 1.0, 0.1);
        org.junit.Assert.assertNull(null);
        org.junit.Assert.assertNotNull(new Object());
        org.junit.Assert.fail();
        org.junit.Assert.fail((String) null);
        org.junit.Assert.assertThrows(IllegalStateException.class, () -> {
        });
        org.junit.Assert.assertEquals("cache entry should be ready for lookup because callers rely on it", 1, 1);
    }

    public void junit5Missing() {
        org.junit.jupiter.api.Assertions.assertTrue(true);
        org.junit.jupiter.api.Assertions.assertFalse(false);
        org.junit.jupiter.api.Assumptions.assumeTrue(true);
        org.junit.jupiter.api.Assumptions.assumeFalse(false);
        org.junit.jupiter.api.Assertions.assertEquals(1, 1);
        org.junit.jupiter.api.Assertions.assertEquals(1.0, 1.0, 0.1);
        org.junit.jupiter.api.Assertions.assertNull(null);
        org.junit.jupiter.api.Assertions.assertNotNull(new Object());
        org.junit.jupiter.api.Assertions.fail();
        org.junit.jupiter.api.Assertions.fail((String) null);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
        });
        org.junit.jupiter.api.Assertions.assertThrowsExactly(IllegalStateException.class, () -> {
        });
        org.junit.jupiter.api.Assertions.assertTimeout(java.time.Duration.ofSeconds(1), () -> {
        });
        org.junit.jupiter.api.Assertions.assertTimeout(java.time.Duration.ofSeconds(1), () -> {
        }, "cache entry should be ready for timeout");
        org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(1), () -> {
        });
        org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(1), () -> {
        }, () -> cacheEntryTimeoutMessage());
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
        });
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
        }, "cache entry should not throw for valid input");
        org.junit.jupiter.api.Assertions.assertEquals(1, 1, () -> cacheEntryMessage());
        org.junit.jupiter.api.Assertions.assertAll("cache entry should be initialised before tests", () -> {
        });
        org.junit.jupiter.api.Assertions.assertAll(() -> {
        });
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
        }, () -> cacheEntryThrowsMessage());
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () -> {
        }, (String) null);
    }

    public void missingMessageWithComment() {
        org.junit.Assert.fail(/* legacy failure */);
        org.junit.jupiter.api.Assertions.fail(/* legacy failure */);
    }

    private String cacheEntryMessage() {
        return "cache entry should be ready for lookup";
    }

    private String cacheEntryTimeoutMessage() {
        return "cache entry should be ready for timeout";
    }

    private String cacheEntryThrowsMessage() {
        return "cache entry should throw on invalid state";
    }
}
