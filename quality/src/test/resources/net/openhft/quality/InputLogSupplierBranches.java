/*
 * Test input for MeaningfulMessageCheck System.Logger supplier branches.
 */
package net.openhft.quality;

import java.util.function.Supplier;

public class InputLogSupplierBranches {

    private final Supplier<String> fieldSupplier = this::buildMessage;

    public void testSupplierBranches() {
        System.Logger systemLogger = System.getLogger("test");
        Supplier<String> localSupplier =
                () -> "cache entry should be ready for replay";

        systemLogger.log(System.Logger.Level.INFO, localSupplier);
        systemLogger.log(System.Logger.Level.INFO, this.fieldSupplier);
        systemLogger.log(System.Logger.Level.INFO,
                (Supplier<String>) localSupplier);
        systemLogger.log(System.Logger.Level.INFO, this::buildMessage);
    }

    private String buildMessage() {
        return "cache entry should be ready for replay";
    }
}
