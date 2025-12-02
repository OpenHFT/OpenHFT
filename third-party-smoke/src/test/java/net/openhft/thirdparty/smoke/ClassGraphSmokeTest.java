/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Additional ClassGraph smoke to scan for this test class.
 */
class ClassGraphSmokeTest {

    @Test
    void classgraphCanScanClasspathAndFindThisTestClass() {
        try (ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .scan()) {
            assertNotNull(scanResult);
            assertNotNull(scanResult.getAllClasses());
        }
    }
}
