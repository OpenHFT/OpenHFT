/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.selfcheck;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Self check fixture exercises map declaration guidance for dynamic payloads and schema clarity.
 */
public class SelfCheckMapStringObjectFixture {

    public void triggerMapWithoutReasonComment() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", 1);
    }

    public void triggerWildcardMapWithoutReasonComment() {
        Map<String, ?> attributes = new LinkedHashMap<>();
        attributes.hashCode();
    }

    public void triggerStringMapWithoutReasonComment() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("id", "1");
    }

    public void usesDynamicKeysWithComment() {
        // dynamic payload keys are defined at runtime because tool schema varies
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", 1);
    }
}
