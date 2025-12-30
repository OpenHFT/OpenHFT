/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.sun.jna.Native;
import com.sun.jna.platform.FileUtils;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests verifying Trove collections and JNA native helpers load correctly.
 */
@DisplayName("Smoke test verifies native and collections libraries load")
class NativeAndCollectionsSmokeTest {

    /**
     * Answer value used in Trove assertions.
     */
    private static final int ANSWER = 42;

    /**
     * Native size candidate representing two-byte integer types.
     */
    private static final int SIZE_TWO = 2;

    /**
     * Native size candidate representing four-byte integer types.
     */
    private static final int SIZE_FOUR = 4;

    /**
     * Native size candidate representing eight-byte integer types.
     */
    private static final int SIZE_EIGHT = 8;

    @Test
    @DisplayName("Trove map should store and retrieve values")
    void troveMapStoresAndRetrievesValues() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<>();
        map.put("answer", ANSWER);
        assertEquals(ANSWER, map.get("answer"), "Trove map should return stored answer");
    }

    @Test
    @DisplayName("JNA Native should report native size")
    void jnaNativeCanReportNativeSize() {
        int size = Native.getNativeSize(Integer.TYPE);
        assertTrue(size == SIZE_TWO || size == SIZE_FOUR || size == SIZE_EIGHT,
                "JNA should report an expected native int size");
    }

    @Test
    @DisplayName("JNA Platform FileUtils should be instantiable")
    void jnaPlatformFileUtilsCanBeInstantiated() {
        FileUtils utils = FileUtils.getInstance();
        assertNotNull(utils, "JNA FileUtils should be instantiable");
    }
}
