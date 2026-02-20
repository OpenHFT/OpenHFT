/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.beust.jcommander.Parameter;
import com.sun.jna.Native;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Shared test fixtures for smoke tests providing reusable POJO classes used across multiple test files.
 * <p>
 * This utility class reduces code duplication and improves maintainability by centralising
 * common test data structures such as SimplePojo, CliArgs, NamedArgs, and Person.
 */
public final class SmokeTestFixtures {

    /**
     * Shared empty value used by fixture defaults.
     */
    private static final String EMPTY_VALUE = "";
    private static volatile boolean byteBuddyChecked;
    private static volatile String byteBuddyFailure;

    private SmokeTestFixtures() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a Person fixture with the supplied name.
     *
     * @param name person name
     * @return person fixture
     */
    public static Person person(final String name) {
        return new Person(name);
    }

    /**
     * Skips tests when socket operations are unavailable in the runtime environment.
     */
    public static void skipIfNoSockets() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.getLocalPort();
        } catch (IOException | SecurityException ex) {
            Assumptions.assumeTrue(false,
                    "Socket operations not permitted: " + ex.getClass().getSimpleName() + ": "
                            + ex.getMessage());
        }
    }

    /**
     * Skips tests when JNA native loading is blocked by the environment.
     */
    public static void skipIfNoJna() {
        try {
            int size = Native.getNativeSize(Integer.TYPE);
            Assumptions.assumeTrue(size > 0, "JNA native access not available");
        } catch (Throwable ex) {
            Assumptions.assumeTrue(false,
                    "JNA native access not available: " + ex.getClass().getSimpleName() + ": "
                            + ex.getMessage());
        }
    }

    /**
     * Skips tests when Byte Buddy agent attachment is blocked by the environment.
     */
    public static void skipIfNoByteBuddyAgent() {
        if (!byteBuddyChecked) {
            synchronized (SmokeTestFixtures.class) {
                if (!byteBuddyChecked) {
                    if (!isSelfAttachAllowed()) {
                        byteBuddyFailure = "self-attach disabled (set -Djdk.attach.allowAttachSelf=true)";
                        byteBuddyChecked = true;
                    } else {
                        try {
                            ByteBuddyAgent.install();
                        } catch (Throwable ex) {
                            byteBuddyFailure = ex.getClass().getSimpleName() + ": "
                                    + ex.getMessage();
                        } finally {
                            byteBuddyChecked = true;
                        }
                    }
                }
            }
        }
        if (byteBuddyFailure != null) {
            Assumptions.assumeTrue(false, "Byte Buddy agent not available: " + byteBuddyFailure);
        }
    }

    private static boolean isSelfAttachAllowed() {
        int version = javaMajorVersion();
        if (version >= 9) {
            // Read attach self flag.
            return "true".equalsIgnoreCase(System.getProperty("jdk.attach.allowAttachSelf"));
        }
        return true;
    }

    private static int javaMajorVersion() {
        // Read Java specification version.
        String version = System.getProperty("java.specification.version", "8");
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        int dot = version.indexOf('.');
        if (dot > 0) {
            version = version.substring(0, dot);
        }
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException ex) {
            return 8;
        }
    }

    /**
     * Simple POJO for serialisation round-trip tests.
     * <p>
     * Used by Gson, Jackson, and other serialisation libraries.
     */
    public static final class SimplePojo {

        /**
         * Stored value used for round-tripping.
         */
        private final String value;

        /**
         * Creates a new instance using the supplied text value.
         *
         * @param newValue textual value
         */
        public SimplePojo(final String newValue) {
            this.value = newValue;
        }

        /**
         * Default constructor required for deserialisation by Jackson and Gson.
         */
        @SuppressWarnings("unused")
        public SimplePojo() {
            this.value = EMPTY_VALUE;
        }

        /**
         * Returns the stored string value used for serialisation round-trip verification.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Simple container for JCommander argument parsing with a single flag.
     * <p>
     * Used for smoke testing JCommander CLI parsing functionality.
     */
    public static final class CliArgs {

        /**
         * Sample flag parsed by JCommander for command-line coverage.
         */
        @Parameter(names = "-f")
        private String flag;

        /**
         * Returns the parsed flag value from JCommander command-line argument parsing.
         *
         * @return the flag value
         */
        public String getFlag() {
            return flag;
        }
    }

    /**
     * Command-line arguments container with a named parameter.
     * <p>
     * Used for JCommander smoke-test parsing with --name argument.
     */
    public static final class NamedArgs {

        /**
         * Name parameter parsed by JCommander for the sample request.
         */
        @Parameter(names = "--name")
        @SuppressFBWarnings(
                value = "SS_SHOULD_BE_STATIC",
                justification = "JCommander binds instance fields via reflection for smoke tests."
        )
        private final String name = "default";

        /**
         * Returns the parsed name value from JCommander command-line argument parsing.
         *
         * @return the name value
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Simple value object for equality and schema generation tests.
     * <p>
     * Used by Guava EqualsTester, Jackson JSON schema generator, and reflection tests.
     */
    public static final class Person {

        /**
         * Name string used to identify the sample entry in tests.
         */
        private String name;

        /**
         * Default constructor required for schema generator instantiation.
         */
        public Person() {
            this.name = EMPTY_VALUE;
        }

        /**
         * Creates a new instance using the supplied name.
         *
         * @param newName person name
         */
        public Person(final String newName) {
            this.name = newName;
        }

        /**
         * Returns the stored name used for equality testing and schema generation.
         *
         * @return person name
         */
        @NotNull
        public String getName() {
            return name;
        }

        /**
         * Updates the name (needed for JAXB and other frameworks).
         *
         * @param newName updated name
         */
        public void setName(final String newName) {
            this.name = newName;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof Person)) {
                return false;
            }
            return name.equals(((Person) other).name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
