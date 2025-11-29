/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.SimpleEmail;
import org.joda.time.DateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.xerial.snappy.Snappy;

import javax.lang.model.element.Modifier;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Minimal usage smoke tests for utility dependencies from third-party-bom.
 * <p>
 * These tests validate collections, utilities, code generation, compression,
 * and native access.
 * See {@code SMOKE-TEST-007} in
 * {@code src/main/docs/project-requirements.adoc}.
 */
class MinimalUtilitiesSmokeTest {

    /**
     * Test value.
     */
    private static final int TEST_VAL = 100;

    @Test
    @DisplayName("Trove4j map can be used")
    void troveMapUsage() {
        TIntIntHashMap map = new TIntIntHashMap();
        map.put(1, TEST_VAL);
        assertEquals(TEST_VAL, map.get(1));
    }

    @Test
    @DisplayName("Commons Lang StringUtils can be used")
    void commonsLangUsage() {
        assertEquals("test", StringUtils.trim(" test "));
    }

    @Test
    @DisplayName("Commons CLI Options can be created")
    void commonsCliUsage() {
        Options options = new Options();
        options.addOption("t", "test", false, "test option");
        assertEquals(1, options.getOptions().size());
    }

    @Test
    @DisplayName("JavaPoet can build a Java file")
    void javaPoetUsage() {
        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .build();
        JavaFile javaFile = JavaFile.builder("com.example.helloworld",
                        helloWorld)
                .build();
        assertNotNull(javaFile.toString());
    }

    @Test
    @DisplayName("Joda-Time DateTime can be instantiated")
    void jodaTimeUsage() {
        DateTime dt = new DateTime();
        assertNotNull(dt.toString());
    }

    @Test
    @DisplayName("Commons Email can be instantiated")
    void commonsEmailUsage() throws Exception {
        SimpleEmail email = new SimpleEmail();
        assertNotNull(email);
    }

    @Test
    @DisplayName("Snappy Java can compress and uncompress")
    void snappyUsage() throws Exception {
        String input = "Hello Snappy-Java";
        byte[] compressed = Snappy.compress(
                input.getBytes(StandardCharsets.UTF_8));
        byte[] uncompressed = Snappy.uncompress(compressed);
        assertEquals(input, new String(uncompressed, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("ClassGraph can scan")
    void classGraphScans() {
        try (io.github.classgraph.ScanResult scanResult =
                     new io.github.classgraph.ClassGraph()
                .enableClassInfo()
                .scan()) {
            assertFalse(scanResult.getAllClasses().isEmpty());
        }
    }

    @Test
    @DisplayName("JNA Native class is accessible")
    void jnaAccess() {
        assertNotNull(com.sun.jna.Native.POINTER_SIZE);
    }

    @Test
    @DisplayName("JNR Runtime is accessible")
    void jnrUsage() {
        assertNotNull(jnr.ffi.Runtime.getSystemRuntime());
    }
}
