/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Additional smoke test that ensures ClassGraph can scan the test classpath.
 */
@DisplayName("Smoke test verifies ClassGraph scans the classpath")
class ClassGraphSmokeTest {

    @Test
    @DisplayName("ClassGraph should scan classpath and find classes")
    void classgraphCanScanClasspathAndFindThisTestClass() {
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .scan()) {
            assertNotNull(scanResult, "ClassGraph scan should produce a result");
            assertNotNull(scanResult.getAllClasses(), "ClassGraph scan should list classes");
        }
    }
}
