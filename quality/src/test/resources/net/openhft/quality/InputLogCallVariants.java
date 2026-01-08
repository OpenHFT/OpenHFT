/*
 * Test input for log message extraction paths.
 * Covers checkJvmLogCall, resolveMessageIndex, extractConstantSupplierMessage branches.
 */
package net.openhft.quality;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputLogCallVariants {

    private static final Logger LOG = LoggerFactory.getLogger(InputLogCallVariants.class);
    private static final java.util.logging.Logger JUL_LOG =
            java.util.logging.Logger.getLogger(InputLogCallVariants.class.getName());

    // --- SLF4J log methods ---

    public void slf4jLogMethods() {
        LOG.trace("Trace message for detailed debugging");
        LOG.debug("Debug message for development purposes");
        LOG.info("Info message for operational monitoring");
        LOG.warn("Warning message for potential issues");
        LOG.error("Error message for failure conditions");
    }

    // --- SLF4J with placeholders ---

    public void slf4jWithPlaceholders() {
        String name = "test";
        int count = 42;
        LOG.info("Processing {} items for user {}", count, name);
        LOG.debug("Cache hit ratio: {}/{} = {}%", 8, 10, 80);
    }

    // --- SLF4J with throwable ---

    public void slf4jWithThrowable() {
        Exception ex = new RuntimeException("test");
        LOG.error("Operation failed with unexpected error", ex);
        LOG.warn("Recoverable error occurred during processing", ex);
    }

    // --- SLF4J with throwable and message ---

    public void slf4jWithThrowableAndMessage() {
        Exception ex = new RuntimeException("test");
        LOG.error("Failed to process request for user {}", "admin", ex);
    }

    // --- JUL (java.util.logging) methods ---

    public void julLogMethods() {
        JUL_LOG.severe("Severe error occurred during startup");
        JUL_LOG.warning("Warning condition detected in configuration");
        JUL_LOG.info("Information about system state");
        JUL_LOG.config("Configuration setting applied successfully");
        JUL_LOG.fine("Fine-grained debugging information");
        JUL_LOG.finer("Finer debugging information available");
        JUL_LOG.finest("Finest level debugging detail");
    }

    // --- JUL log with level ---

    public void julLogWithLevel() {
        JUL_LOG.log(java.util.logging.Level.INFO, "Log with explicit level");
        JUL_LOG.log(java.util.logging.Level.WARNING, "Warning with explicit level");
    }

    // --- System.Logger (Java 9+) ---

    public void systemLogger() {
        System.Logger logger = System.getLogger(InputLogCallVariants.class.getName());
        logger.log(System.Logger.Level.INFO, "System logger info message");
        logger.log(System.Logger.Level.WARNING, "System logger warning message");
        logger.log(System.Logger.Level.ERROR, "System logger error message");
    }

    // --- Log4j2 style (if available) ---

    public void log4j2Style() {
        // Simulating Log4j2 API patterns
        org.apache.logging.log4j.Logger log4j = org.apache.logging.log4j.LogManager.getLogger();
        log4j.trace("Log4j2 trace message for debugging");
        log4j.debug("Log4j2 debug message for development");
        log4j.info("Log4j2 info message for monitoring");
        log4j.warn("Log4j2 warning message for issues");
        log4j.error("Log4j2 error message for failures");
        log4j.fatal("Log4j2 fatal message for critical errors");
    }

    // --- Log4j2 with supplier message ---

    public void log4j2WithSupplier() {
        org.apache.logging.log4j.Logger log4j = org.apache.logging.log4j.LogManager.getLogger();
        int count = 100;
        log4j.debug(() -> "Lazy message: count = " + count);
        log4j.info(() -> String.format("Formatted lazy message: %d", count));
    }

    // --- Chronicle Jvm logging ---

    public void chronicleJvmLogging() {
        // Jvm.warn().on(getClass(), "message");
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), "Warning from Jvm logger");
        net.openhft.chronicle.core.Jvm.debug().on(getClass(), "Debug from Jvm logger");
    }

    // --- Chronicle Jvm with exception ---

    public void chronicleJvmWithException() {
        Exception ex = new RuntimeException("test");
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), "Warning with exception", ex);
    }

    // --- Chronicle Jvm with format ---

    public void chronicleJvmWithFormat() {
        int count = 42;
        net.openhft.chronicle.core.Jvm.debug().on(getClass(), "Processing count: " + count);
    }

    // --- Empty or blank log messages (edge cases) ---

    public void edgeCaseMessages() {
        LOG.debug("");
        LOG.info(" ");
        LOG.warn("   ");
    }

    // --- Log with only placeholders ---

    public void onlyPlaceholders() {
        String value = "test";
        LOG.debug("{}", value);
        LOG.info("{} {}", "a", "b");
    }
}
