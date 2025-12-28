/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

/**
 * Marker class used to keep the smoke-test module non-empty for tooling, packaging, and jar metadata.
 *
 * <p>This module is primarily test-only, but Maven still produces a main artifact; keeping a non-empty jar avoids
 * warning noise in build logs.
 */
public final class ThirdPartySmokeModule {

    public static final String MODULE_NAME = "third-party-smoke";

    private ThirdPartySmokeModule() {
    }
}
