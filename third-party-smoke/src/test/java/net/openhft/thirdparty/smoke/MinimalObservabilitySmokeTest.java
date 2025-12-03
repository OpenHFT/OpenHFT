/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Minimal usage smoke tests for observability dependencies from
 * third-party-bom.
 * <p>
 * These tests validate Prometheus metrics and OpenTelemetry tracing can
 * initialise.
 * See {@code SMOKE-TEST-003} in
 * {@code src/main/docs/project-requirements.adoc}.
 */
class MinimalObservabilitySmokeTest {

    /**
     * Tolerance used for floating point comparisons.
     */
    private static final double DELTA = 0.0001d;

    @Test
    @DisplayName("Prometheus counters can be registered")
    void prometheusCountersCollect() {
        CollectorRegistry registry = new CollectorRegistry();
        Counter counter = Counter.build()
                .name("third_party_smoke_counter")
                .help("smoke")
                .register(registry);
        counter.inc();
        Double value = registry.getSampleValue(
                "third_party_smoke_counter_total"
        );
        assertNotNull(value);
        assertEquals(1.0, value, DELTA);
    }

    @Test
    @DisplayName("OpenTelemetry tracer provider initialises")
    void openTelemetryInitialises() {
        SdkTracerProvider provider = SdkTracerProvider.builder()
                .setResource(Resource.getDefault())
                .build();
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(provider)
                .build();
        assertNotNull(sdk.getTracer("smoke")
                .spanBuilder("hello-span")
                .startSpan());
        provider.close();
    }

    @Test
    @DisplayName("OTLP exporter builds without network calls")
    void otlpExporterBuilds() {
        OtlpGrpcSpanExporter exporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://localhost:4317")
                .build();
        SdkTracerProvider provider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                .build();
        // No spans exported; this simply exercises wiring and shutdown paths
        provider.close();
    }
}
