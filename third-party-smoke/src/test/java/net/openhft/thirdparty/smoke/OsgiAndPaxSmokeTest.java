/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Test;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.service.log.LogService;

import java.util.Collections;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

/**
 * Smoke tests covering OSGi and Pax Exam/URL artefacts.
 */
class OsgiAndPaxSmokeTest {

    @Test
    void osgiCoreAndCompendiumTypesAreUsable() {
        Bundle bundle = FrameworkUtil.getBundle(
                OsgiAndPaxSmokeTest.class);
        // Bundle may be null outside a container, but class availability is
        // validated.
        assertNotNull(Version.parseVersion("1.2.3"), "OSGi Version.parseVersion should work");
        assertEquals("org.osgi.service.log.LogService",
                LogService.class.getName(), "OSGi LogService should be resolvable");
        assertNotNull(bundle == null ? FrameworkUtil.class : bundle,
                "OSGi FrameworkUtil should be available even without a bundle");
    }

    @Test
    void felixFrameworkFactoryCanBeLoaded() {
        ServiceLoader<org.osgi.framework.launch.FrameworkFactory> loader =
                ServiceLoader.load(
                        org.osgi.framework.launch.FrameworkFactory.class);
        assertTrue(loader.iterator().hasNext(),
                "FrameworkFactory should be discoverable via ServiceLoader");
        org.osgi.framework.launch.FrameworkFactory factory =
                loader.iterator().next();
        Framework framework = factory.newFramework(
                Collections.<String, String>emptyMap());
        assertNotNull(framework, "FrameworkFactory should create a Framework instance");
    }

    @Test
    void paxExamCoreOptionsAndOptionsInterfaceArePresent() {
        Option[] options = {
                junitBundles(),
                systemProperty("pax.exam.smoke").value("true")
        };
        assertEquals(2, options.length, "Pax Exam options should include junitBundles and systemProperty");
        assertNotNull(options[0], "Pax Exam junitBundles option should be present");
    }

    @Test
    void paxUrlHandlersClassesCanBeLoaded() throws Exception {
        Class<?> mvnHandler = Class.forName(
                "org.ops4j.pax.url.mvn.Handler");
        Class<?> refHandler = Class.forName(
                "org.ops4j.pax.url.reference.Handler");
        assertNotNull(mvnHandler, "Pax URL mvn handler class should load");
        assertNotNull(refHandler, "Pax URL reference handler class should load");
    }
}
