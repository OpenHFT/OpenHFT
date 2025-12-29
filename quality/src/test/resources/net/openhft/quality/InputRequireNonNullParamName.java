/*
 * Precondition messages that repeat the parameter name should not trigger MM rules.
 */
package net.openhft.quality;

import java.util.Objects;

public class InputRequireNonNullParamName {
    public void testRequireNonNull(Object value) {
        Objects.requireNonNull(value, "value");
    }

    public void testRequireNonNullSupplier(Object value) {
        Objects.requireNonNull(value, () -> "value");
    }

    public void testRequireNotNull(Object value) {
        requireNotNull(value, "value");
    }
}
