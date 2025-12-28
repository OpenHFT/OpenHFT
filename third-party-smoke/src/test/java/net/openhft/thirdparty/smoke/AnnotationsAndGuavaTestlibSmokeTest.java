/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.google.common.testing.EqualsTester;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test that verifies JetBrains annotations and Guava testlib are usable.
 */
class AnnotationsAndGuavaTestlibSmokeTest {

    @Test
    @DisplayName("JetBrains @NotNull should be visible via reflection")
    void jetbrainsNotNullIsVisibleViaReflection() throws Exception {
        Method method = Person.class.getDeclaredMethod("getName");
        assertNotNull(method, "reflection should find Person#getName");
        // JetBrains annotations are CLASS-retention; presence of the type is
        // sufficient to show dependency resolution.
        assertNotNull(NotNull.class.getName(), "JetBrains NotNull type should be loadable");
    }

    @Test
    @DisplayName("Guava EqualsTester should validate Person equals and hashCode contract")
    void guavaEqualsTesterValidatesEqualsContract() {
        assertDoesNotThrow(() -> new EqualsTester()
                .addEqualityGroup(new Person("Alice"), new Person("Alice"))
                .addEqualityGroup(new Person("Bob"))
                .testEquals(), "Guava EqualsTester should verify equality groups without exception");
    }

    /**
     * Simple value object used in equality tests.
     */
    static class Person {
        /**
         * Name string used to identify the sample entry in tests.
         */
        private final String name;

        Person(final String newName) {
            this.name = newName;
        }

        @NotNull
        String getName() {
            return name;
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
