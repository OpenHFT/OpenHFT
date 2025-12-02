/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Smoke tests for validating third-party BOM compatibility with Java 8.
 * <p>
 * This package contains three types of smoke tests:
 * <ul>
 *   <li>{@link ClassLoadingSmokeTest} - 85+ tests loading classes from each BOM
 *     artefact</li>
 *   <li>Minimal usage tests - Category-based runtime behaviour validation:
 *     <ul>
 *       <li>{@link MinimalObservabilitySmokeTest} - Prometheus,
 *         OpenTelemetry</li>
 *       <li>{@link MinimalSerializationSmokeTest} - JSON, YAML, XML</li>
 *       <li>{@link MinimalNetworkingSmokeTest} - Undertow, Jetty, Netty,
 *         Grizzly</li>
 *       <li>{@link MinimalLoggingSmokeTest} - Log4j2, SLF4J</li>
 *       <li>{@link MinimalUtilitiesSmokeTest} - Commons, Trove4j, Snappy,
 *         JNA, etc.</li>
 *       <li>{@link MinimalDatabaseSmokeTest} - MongoDB, HSQLDB</li>
 *     </ul>
 *   </li>
 *   <li>{@link LegacyJUnit4SmokeTest} - JUnit 4 vintage engine compatibility
 *   </li>
 * </ul>
 * <p>
 * See {@code src/main/docs/project-requirements.adoc} for detailed
 * requirements.
 */
package net.openhft.thirdparty.smoke;
