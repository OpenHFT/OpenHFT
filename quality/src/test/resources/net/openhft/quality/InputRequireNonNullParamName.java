/*
 * Precondition messages may repeat the first argument for requireNonNull/requireNotNull.
 * requireNonNull/requireNotNull without a message should not trigger missing message warnings.
 */
package net.openhft.quality;

import static java.util.Objects.requireNonNull;

public class InputRequireNonNullParamName {
    public void testRequireNonNull(Object value) {
        requireNonNull(value, "value");
    }

    public void testRequireNotNull(Object value) {
        requireNotNull(value, "value");
    }

    public void testRequireNonNullMethodCall(Holder holder) {
        requireNonNull(holder.getIn(), "holder.getIn()");
    }

    public void testRequireNotNullWithoutMessage(Object value) {
        requireNotNull(value);
    }

    public void testRequireNonNullWithoutMessage(Object value) {
        requireNonNull(value);
    }

    static final class Holder {
        Object getIn() {
            return null;
        }
    }
}
