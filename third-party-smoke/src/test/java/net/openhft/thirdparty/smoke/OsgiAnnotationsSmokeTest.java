/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.annotation.bundle.Capability;
import org.osgi.annotation.bundle.Header;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.annotation.versioning.Version;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests verifying OSGi annotation classes are available from osgi.annotation dependency.
 * <p>
 * Tests that OSGi bundle and versioning annotations can be loaded and used for
 * compile-time metadata generation in OSGi bundle projects.
 */
class OsgiAnnotationsSmokeTest {

    @Test
    @DisplayName("OSGi @Version annotation class should be loadable from classpath")
    void osgiVersionAnnotationIsPresent() {
        assertNotNull(Version.class.getName(),
                "OSGi Version annotation class should be loadable for semantic versioning");
        assertTrue(Version.class.isAnnotation(),
                "Version should be an annotation type for package versioning");
    }

    @Test
    @DisplayName("OSGi @ProviderType annotation class should be loadable for API contracts")
    void osgiProviderTypeAnnotationIsPresent() {
        assertNotNull(ProviderType.class.getName(),
                "OSGi ProviderType annotation should be loadable for API type marking");
        assertTrue(ProviderType.class.isAnnotation(),
                "ProviderType should be an annotation type for provider interfaces");
    }

    @Test
    @DisplayName("OSGi @Capability annotation class should be loadable for bundle capabilities")
    void osgiCapabilityAnnotationIsPresent() {
        assertNotNull(Capability.class.getName(),
                "OSGi Capability annotation should be loadable for bundle manifest generation");
        assertTrue(Capability.class.isAnnotation(),
                "Capability should be an annotation type for declaring bundle capabilities");
    }

    @Test
    @DisplayName("OSGi @Requirement annotation class should be loadable for bundle requirements")
    void osgiRequirementAnnotationIsPresent() {
        assertNotNull(Requirement.class.getName(),
                "OSGi Requirement annotation should be loadable for dependency declaration");
        assertTrue(Requirement.class.isAnnotation(),
                "Requirement should be an annotation type for declaring bundle requirements");
    }

    @Test
    @DisplayName("OSGi @Header annotation class should be loadable for manifest headers")
    void osgiHeaderAnnotationIsPresent() {
        assertNotNull(Header.class.getName(),
                "OSGi Header annotation should be loadable for custom manifest headers");
        assertTrue(Header.class.isAnnotation(),
                "Header should be an annotation type for adding manifest entries");
    }

    @Test
    @DisplayName("OSGi @ProviderType has CLASS retention so verify class element type instead")
    void osgiProviderTypeHasTypeElementTarget() {
        // ProviderType has CLASS retention (not RUNTIME), so we verify its target element type
        // This confirms the annotation can be applied to types for compile-time processing
        java.lang.annotation.Target target = ProviderType.class.getAnnotation(java.lang.annotation.Target.class);

        assertNotNull(target, "ProviderType should have @Target annotation defining where it can be applied");
        assertTrue(target.value().length > 0,
                "ProviderType @Target element count " + target.value().length + " should be > 0");
    }
}
