/*
 * Test input for log-site message checks (SLF4J, Log4j2, JUL, System.Logger, Jvm).
 */
package net.openhft.quality;

public class InputLogMessages {

    public void testLogMessages() {
        org.slf4j.Logger slf4j = org.slf4j.LoggerFactory.getLogger(InputLogMessages.class);
        org.apache.logging.log4j.Logger log4j = org.apache.logging.log4j.LogManager.getLogger(InputLogMessages.class);
        java.util.logging.Logger jul = java.util.logging.Logger.getLogger(InputLogMessages.class.getName());
        System.Logger systemLogger = System.getLogger("test");
        Throwable throwable = new RuntimeException("boom");

        // SLF4J
        slf4j.warn((String) null);
        slf4j.warn((String) null, throwable);
        slf4j.warn((String) null, 1, 2);
        slf4j.info("should retry");
        slf4j.trace("comparison");
        slf4j.debug("0");
        slf4j.error("value");
        slf4j.warn("order  should persist");

        // Log4j2
        log4j.fatal((String) null);
        log4j.error("", throwable);
        log4j.warn("indices should be valid");
        log4j.info("status should be ok for {}", "node");

        // JUL
        jul.warning("value");
        jul.log(java.util.logging.Level.INFO, (String) null);
        jul.log(java.util.logging.Level.INFO, (String) null, throwable);
        jul.log(java.util.logging.Level.INFO, "should reconnect");

        // System.Logger
        systemLogger.log(System.Logger.Level.INFO, (String) null);
        systemLogger.log(System.Logger.Level.INFO, (String) null, throwable);
        systemLogger.log(System.Logger.Level.INFO, () -> "expected value should match");
        systemLogger.log(System.Logger.Level.INFO, () -> "value " + 7);

        // Jvm
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), (String) null);
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), (String) null, throwable);
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), "");
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), "", throwable);
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), "should retry");
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), "comparison");
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), "0");
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), "value");
        net.openhft.chronicle.core.Jvm.warn().on(slf4j, "order  should persist");
        systemLogger.log(System.Logger.Level.INFO, "comparison");
        net.openhft.chronicle.core.Jvm.debug().on(getClass(), "comparison");
        net.openhft.chronicle.core.Jvm.error().on(getClass(), "value");
        net.openhft.chronicle.core.Jvm.startup().on(getClass(), "should retry");
        net.openhft.chronicle.core.Jvm.perf().on(getClass(), "order  should persist");
        slf4j.error("value", throwable);
        log4j.trace("comparison");
        log4j.debug("0");
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), throwable);
        net.openhft.chronicle.core.Jvm.warn().on(slf4j, (String) null);
        slf4j.warn((String) null /* low latency */);
        log4j.error((String) null /* low latency */);
        jul.log(java.util.logging.Level.INFO, (String) null /* low latency */);
        systemLogger.log(System.Logger.Level.INFO, (String) null /* low latency */);
        net.openhft.chronicle.core.Jvm.warn().on(getClass(), (String) null /* low latency */);
        slf4j.warn(
                (String) null,
                /* low latency */
                1);
        slf4j.warn((String) null, "literal /* not a comment */");
        net.openhft.chronicle.core.Jvm.debug().on(getClass(),
                "Breaking out of send loop after " + System.currentTimeMillis() + " ms.");
        // Exception-only logging should be allowed.
        slf4j.error("", throwable);

        slf4j.info("Expected more retries");
        slf4j.info("expected: more retries");
    }
}
