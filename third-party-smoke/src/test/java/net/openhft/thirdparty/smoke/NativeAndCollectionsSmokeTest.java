/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.sun.jna.Native;
import com.sun.jna.platform.FileUtils;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for Trove and JNA artefacts.
 */
class NativeAndCollectionsSmokeTest {

    /**
     * Answer value used in Trove assertions.
     */
    private static final int ANSWER = 42;

    /**
     * Acceptable native sizes for integer types.
     */
    private static final int SIZE_TWO = 2;

    /**
     * Acceptable native sizes for integer types.
     */
    private static final int SIZE_FOUR = 4;

    /**
     * Acceptable native sizes for integer types.
     */
    private static final int SIZE_EIGHT = 8;

    @Test
    void troveMapStoresAndRetrievesValues() {
        TObjectIntHashMap<String> map = new TObjectIntHashMap<>();
        map.put("answer", ANSWER);
        assertEquals(ANSWER, map.get("answer"));
    }

    @Test
    void jnaNativeCanReportNativeSize() {
        int size = Native.getNativeSize(Integer.TYPE);
        assertTrue(size == SIZE_TWO || size == SIZE_FOUR || size == SIZE_EIGHT);
    }

    @Test
    void jnaPlatformFileUtilsCanBeInstantiated() {
        FileUtils utils = FileUtils.getInstance();
        assertNotNull(utils);
    }
}
