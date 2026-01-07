/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InOrder;

import static net.openhft.thirdparty.smoke.SmokeTestFixtures.skipIfNoByteBuddyAgent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Advanced smoke tests verifying Mockito mocking framework features beyond basic mock creation.
 * <p>
 * Tests ArgumentCaptor, spy objects, InOrder verification, stubbing chains, verification modes,
 * and BDD-style mocking to ensure deeper Mockito functionality is available and working.
 */
@DisplayName("Smoke test verifies advanced Mockito features work")
class MockitoAdvancedSmokeTest {

    private static final String TEST_KEY = "testKey";
    private static final String TEST_VALUE = "testValue";
    private static final String FIRST_VALUE = "first";
    private static final String SECOND_VALUE = "second";

    @BeforeAll
    static void ensureByteBuddyAgentAvailable() {
        skipIfNoByteBuddyAgent();
    }

    @Test
    @DisplayName("Mockito ArgumentCaptor should capture method arguments for later verification")
    void argumentCaptorCapturesMethodArguments() {
        DataService mockService = mock(DataService.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        mockService.fetchData(TEST_KEY);

        verify(mockService).fetchData(keyCaptor.capture());
        assertEquals(TEST_KEY, keyCaptor.getValue(),
                "ArgumentCaptor should capture the exact argument '" + TEST_KEY + "' passed to fetchData");
    }

    @Test
    @DisplayName("Mockito doReturn-when syntax should stub methods without calling real implementation")
    void doReturnWhenSyntaxStubsMethods() {
        DataService mockService = mock(DataService.class);

        // Use doReturn-when syntax (alternative to when-thenReturn)
        doReturn(TEST_VALUE).when(mockService).fetchData(TEST_KEY);

        String result = mockService.fetchData(TEST_KEY);

        assertEquals(TEST_VALUE, result,
                "doReturn-when syntax should stub method to return '" + TEST_VALUE + "'");
    }

    @Test
    @DisplayName("Mockito InOrder should verify that methods were called in expected sequence")
    void inOrderVerifiesCallSequence() {
        DataService mockService = mock(DataService.class);

        mockService.saveData(TEST_KEY, TEST_VALUE);
        mockService.fetchData(TEST_KEY);

        InOrder inOrder = inOrder(mockService);
        inOrder.verify(mockService).saveData(TEST_KEY, TEST_VALUE);
        inOrder.verify(mockService).fetchData(TEST_KEY);
        // If sequence was wrong, InOrder would throw - reaching here means sequence was correct
    }

    @Test
    @DisplayName("Mockito stubbing chain should return different values on consecutive calls")
    void stubbingChainReturnsDifferentValues() {
        DataService mockService = mock(DataService.class);
        when(mockService.fetchData(TEST_KEY))
                .thenReturn(FIRST_VALUE)
                .thenReturn(SECOND_VALUE);

        String firstCall = mockService.fetchData(TEST_KEY);
        String secondCall = mockService.fetchData(TEST_KEY);

        assertEquals(FIRST_VALUE, firstCall,
                "First call to stubbed method should return '" + FIRST_VALUE + "'");
        assertEquals(SECOND_VALUE, secondCall,
                "Second call to stubbed method should return '" + SECOND_VALUE + "'");
    }

    @Test
    @DisplayName("Mockito verification modes should validate exact call counts on mock methods")
    void verificationModesValidateCallCounts() {
        DataService mockService = mock(DataService.class);

        mockService.fetchData(TEST_KEY);
        mockService.fetchData(TEST_KEY);
        mockService.getCount();

        verify(mockService, times(2)).fetchData(TEST_KEY);
        verify(mockService, never()).saveData(anyString(), anyString());
        verify(mockService, atLeastOnce()).getCount();
        // All verifications passed - verification modes work correctly
    }

    @Test
    @DisplayName("BDDMockito given-willReturn syntax should configure mocks in declarative BDD style")
    void bddStyleMockingConfiguresMocks() {
        DataService mockService = mock(DataService.class);
        BDDMockito.given(mockService.fetchData(TEST_KEY)).willReturn(TEST_VALUE);

        String result = mockService.fetchData(TEST_KEY);

        assertEquals(TEST_VALUE, result,
                "BDD-style stubbing should return configured value '" + TEST_VALUE + "'");
        BDDMockito.then(mockService).should().fetchData(TEST_KEY);
    }

    /**
     * Simple data service interface for testing Mockito mocking capabilities.
     * Provides methods covering common mock scenarios: return values, void methods, and primitives.
     */
    interface DataService {
        /**
         * Fetches data from the service by looking up the specified key in the data store.
         *
         * @param key the lookup key for data retrieval
         * @return the data value associated with the key
         */
        String fetchData(String key);

        /**
         * Saves data with the given key-value pair.
         *
         * @param key   the storage key for the data
         * @param value the data value to store
         */
        void saveData(String key, String value);

        /**
         * Returns a count value for testing primitive return stubbing.
         *
         * @return the current count value
         */
        // TODO add a tests for this method
        int getCount();
    }
}
