/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MockitoSmokeTest {
    @Test
    @SuppressWarnings("unchecked")
    void stubsAndVerifiesCalls() {
        List<String> values = mock(List.class);
        when(values.get(3)).thenReturn("three");
        assertEquals("three", values.get(3));
        values.add("next");
        verify(values).get(3);
        verify(values).add("next");
        verifyNoMoreInteractions(values);
    }
}
