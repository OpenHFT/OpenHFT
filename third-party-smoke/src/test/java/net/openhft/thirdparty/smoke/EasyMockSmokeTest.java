/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Smoke test for EasyMock.
 */
class EasyMockSmokeTest {

    interface GreetingService {
        String greet(String name);
    }

    @Test
    void easyMockCanRecordReplayAndVerify() {
        GreetingService service = EasyMock.createMock(GreetingService.class);
        EasyMock.expect(service.greet("World")).andReturn("Hello World");
        EasyMock.replay(service);

        String result = service.greet("World");
        assertEquals("Hello World", result);

        EasyMock.verify(service);
    }
}
