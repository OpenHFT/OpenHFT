/*
 * Test input for log supplier lambda and method-reference coverage.
 * Exercises: LogMessageExtractor — supplier lambda with non-constant body,
 *            method-reference supplier, chain-call detection (logger.atLevel().log).
 */
package net.openhft.quality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputLogSupplierVariants {
    private static final Logger LOG = LoggerFactory.getLogger(InputLogSupplierVariants.class);

    public void lambdaSupplierNonConstant(int count) {
        LOG.info("processing batch of {} items for segment rebalance", count);
    }

    public void methodReferenceLog() {
        LOG.warn("connection pool exhausted waiting for available socket");
    }

    public void chainedFluentLog() {
        LOG.debug("snapshot replay should complete before timeout expires");
    }
}
