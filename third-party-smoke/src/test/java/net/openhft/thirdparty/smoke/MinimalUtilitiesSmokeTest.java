/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.koloboke.collect.map.hash.HashIntIntMaps;
import com.samskivert.util.QuickSort;
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

import static org.junit.jupiter.api.Assertions.*;

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

    /**
     * Values used for quick sort smoke test.
     */
    private static final Integer QUICK_SORT_FIRST = 3;
    /**
     * Middle value for quick sort validation.
     */
    private static final Integer QUICK_SORT_SECOND = 1;
    /**
     * Highest value for quick sort validation.
     */
    private static final Integer QUICK_SORT_THIRD = 2;

    /**
     * Values used for Koloboke map assertions.
     */
    private static final int KOLOBOKE_KEY = 7;

    /**
     * Stored value for Koloboke map assertions.
     */
    private static final int KOLOBOKE_VALUE = 11;

    /**
     * CLI flag value used for JCommander parsing.
     */
    private static final String FLAG_VALUE = "value";

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

    @Test
    @DisplayName("Samskivert QuickSort sorts arrays")
    void samskivertQuickSort() {
        Integer[] nums = {
                QUICK_SORT_FIRST,
                QUICK_SORT_SECOND,
                QUICK_SORT_THIRD
        };
        QuickSort.sort(nums);
        assertEquals(QUICK_SORT_SECOND, nums[0]);
        assertEquals(QUICK_SORT_FIRST, nums[2]);
    }

    @Test
    @DisplayName("Koloboke map factory class is accessible")
    void kolobokeMapFactoryAccessible() {
        assertNotNull(HashIntIntMaps.class.getName());
    }

    @Test
    @DisplayName("JCommander parses simple args")
    void jCommanderParses() {
        CliArgs args = new CliArgs();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse("-f", FLAG_VALUE);
        assertEquals(FLAG_VALUE, args.flag);
    }

    /**
     * Simple container for JCommander argument parsing.
     */
    private static final class CliArgs {
        /**
         * Sample flag parsed by JCommander.
         */
        @Parameter(names = "-f")
        private String flag;
    }
}
