/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.easymock.EasyMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Smoke test that verifies EasyMock can record, replay, and verify expectations.
 */
@DisplayName("Smoke test verifies EasyMock replay works")
class EasyMockSmokeTest {

    @Test
    @DisplayName("EasyMock should record, replay and verify expectations")
    void easyMockCanRecordReplayAndVerify() {
        GreetingService service = EasyMock.createMock(GreetingService.class);
        EasyMock.expect(service.greet("World")).andReturn("Hello World");
        EasyMock.replay(service);

        String result = service.greet("World");
        assertEquals("Hello World", result, "EasyMock should replay expected greeting");

        EasyMock.verify(service);
    }

    interface GreetingService {
        String greet(String name);
    }
}
