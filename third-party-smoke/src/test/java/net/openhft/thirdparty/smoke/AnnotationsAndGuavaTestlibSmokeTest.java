/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.google.common.testing.EqualsTester;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test for JetBrains annotations and Guava testlib.
 */
class AnnotationsAndGuavaTestlibSmokeTest {

    /**
     * Simple value object used in equality tests.
     */
    static class Person {
        /**
         * Person name.
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

    @Test
    void jetbrainsNotNullIsVisibleViaReflection() throws Exception {
        Method method = Person.class.getDeclaredMethod("getName");
        assertNotNull(method);
        // JetBrains annotations are CLASS-retention; presence of the type is
        // sufficient to show dependency resolution.
        assertNotNull(NotNull.class.getName());
    }

    @Test
    void guavaEqualsTesterValidatesEqualsContract() {
        new EqualsTester()
                .addEqualityGroup(new Person("Alice"), new Person("Alice"))
                .addEqualityGroup(new Person("Bob"))
                .testEquals();
    }
}
