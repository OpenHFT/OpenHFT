/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry for mapping rule codes to rule identifiers.
 */
public final class RuleRegistry {

    private static final Map<String, RuleId> BY_CODE;

    static {
        Map<String, RuleId> byCode = new HashMap<>();
        for (RuleId ruleId : RuleId.values()) {
            byCode.put(ruleId.code(), ruleId);
        }
        BY_CODE = Collections.unmodifiableMap(byCode);
    }

    private RuleRegistry() {
    }

    /**
     * Look up a rule identifier by its short code.
     *
     * @param code short rule code.
     * @return matching rule identifier, or {@code null} if unknown.
     */
    public static RuleId forCode(String code) {
        Objects.requireNonNull(code);
        return BY_CODE.get(code);
    }
}
