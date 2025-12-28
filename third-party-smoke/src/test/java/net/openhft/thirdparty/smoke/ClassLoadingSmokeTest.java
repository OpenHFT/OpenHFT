/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ClassLoadingSmokeTest {

    /**
     * Loader used to resolve BOM artefacts.
     */
    private static final ClassLoader LOADER =
            Thread.currentThread().getContextClassLoader();

    private static Collection<DynamicTest> observabilityTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic(
                "OpenTelemetry API",
                () -> assertClassesLoad(
                        "io.opentelemetry.api.OpenTelemetry",
                        "io.opentelemetry.api.trace.Tracer"
                )
        ));
        tests.add(dynamic(
                "OpenTelemetry SDK",
                () -> assertClassesLoad(
                        "io.opentelemetry.sdk.OpenTelemetrySdk",
                        "io.opentelemetry.sdk.trace.SdkTracerProvider"
                )
        ));
        tests.add(dynamic(
                "OpenTelemetry OTLP exporter",
                () -> assertClassesLoad(
                        "io.opentelemetry.exporter.otlp.trace."
                                + "OtlpGrpcSpanExporter"
                )
        ));
        tests.add(dynamic(
                "Prometheus client",
                () -> assertClassesLoad(
                        "io.prometheus.client.Counter",
                        "io.prometheus.client.CollectorRegistry"
                )
        ));
        tests.add(dynamic(
                "Prometheus hotspot collectors",
                () -> assertClassesLoad(
                        "io.prometheus.client.hotspot.MemoryPoolsExports"
                )
        ));
        tests.add(dynamic(
                "Prometheus simple HTTP server",
                () -> assertClassesLoad(
                        "io.prometheus.client.exporter.HTTPServer"
                )
        ));
        return tests;
    }

    private static Collection<DynamicTest> additionalPeerTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic(
                "JCommander",
                () -> assertClassesLoad("com.beust.jcommander.JCommander")
        ));
        tests.add(dynamic(
                "Gson",
                () -> assertClassesLoad("com.google.gson.Gson")
        ));
        tests.add(dynamic(
                "Unirest",
                () -> assertClassesLoad("com.mashape.unirest.http.Unirest")
        ));
        tests.add(dynamic(
                "Joda-Time",
                () -> assertClassesLoad("org.joda.time.DateTime")
        ));
        tests.add(dynamic(
                "Commons Email",
                () -> assertClassesLoad("org.apache.commons.mail.Email")
        ));
        tests.add(dynamic(
                "Maven Model",
                () -> assertClassesLoad("org.apache.maven.model.Model")
        ));
        return tests;
    }

    private static Collection<DynamicTest> serialisationTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic(
                "JSONAssert",
                () -> assertClassesLoad("org.skyscreamer.jsonassert.JSONAssert")
        ));
        tests.add(dynamic(
                "Samskivert utilities",
                () -> assertPackagePresent("com.samskivert")
        ));
        tests.add(dynamic(
                "SnakeYAML",
                () -> assertClassesLoad("org.yaml.snakeyaml.Yaml")
        ));
        tests.add(dynamic(
                "Jettison",
                () -> assertClassesLoad("org.codehaus.jettison.json.JSONObject")
        ));
        tests.add(dynamic(
                "Mongo BSON",
                () -> assertClassesLoad("org.bson.Document")
        ));
        tests.add(dynamic(
                "Jackson jsonSchema module",
                () -> assertClassesLoad(
                        "com.fasterxml.jackson.module.jsonSchema.JsonSchema",
                        "com.fasterxml.jackson.module.jsonSchema"
                                + ".JsonSchemaGenerator"
                )
        ));
        return tests;
    }

    private static Collection<DynamicTest> mongoTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic("Mongo legacy driver", () -> assertClassesLoad(
                "com.mongodb.MongoClient",
                "com.mongodb.MongoClientURI")));
        tests.add(dynamic("Mongo GridFS driver", () -> assertClassesLoad(
                "com.mongodb.gridfs.GridFS")));
        tests.add(dynamic("Mongo driver core", () -> assertClassesLoad(
                "com.mongodb.connection.ClusterSettings")));
        tests.add(dynamic("Mongo synchronous driver", () -> assertClassesLoad(
                "com.mongodb.client.MongoClients",
                "com.mongodb.client.MongoClient")));
        return tests;
    }

    private static Collection<DynamicTest> networkingTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic("Undertow core", () -> assertClassesLoad(
                "io.undertow.Undertow")));
        tests.add(dynamic("Undertow servlet", () -> assertClassesLoad(
                "io.undertow.servlet.api.DeploymentInfo")));
        tests.add(dynamic("Undertow websockets", () -> assertClassesLoad(
                "io.undertow.websockets.jsr.ServerWebSocketContainer")));
        tests.add(dynamic("Jetty websocket API", () -> assertClassesLoad(
                "org.eclipse.jetty.websocket.api.Session")));
        tests.add(dynamic("Jetty websocket server", () -> assertClassesLoad(
                "org.eclipse.jetty.websocket.server.WebSocketServerFactory")));
        tests.add(dynamic("Jetty websocket client", () -> assertClassesLoad(
                "org.eclipse.jetty.websocket.client.WebSocketClient")));
        tests.add(dynamic("Netty", () -> assertClassesLoad(
                "io.netty.bootstrap.ServerBootstrap",
                "io.netty.channel.Channel")));
        tests.add(dynamic("Grizzly", () -> assertClassesLoad(
                "org.glassfish.grizzly.filterchain.FilterChainBuilder")));
        tests.add(dynamic("Apache MINA", () -> assertClassesLoad(
                "org.apache.mina.core.session.IoSession")));
        tests.add(dynamic("XNIO NIO provider", () -> assertClassesLoad(
                "org.xnio.Xnio")));
        tests.add(dynamic("OkHttp MockWebServer", () -> assertClassesLoad(
                "okhttp3.mockwebserver.MockWebServer")));
        return tests;
    }

    private static Collection<DynamicTest> loggingTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic(
                "Log4j 2 core and API",
                () -> assertClassesLoad(
                        "org.apache.logging.log4j.LogManager",
                        "org.apache.logging.log4j.core.LoggerContext"
                )
        ));
        tests.add(dynamic(
                "Log4j 2 SLF4J and JUL bridges",
                () -> assertClassesLoad(
                        "org.apache.logging.log4j.jul.LogManager",
                        "org.apache.logging.slf4j.Log4jLoggerFactory"
                )
        ));
        tests.add(dynamic(
                "SLF4J API",
                () -> assertClassesLoad("org.slf4j.LoggerFactory")
        ));
        boolean slf4jSimplePresent = isClassPresent(
                "org.slf4j.simple.SimpleServiceProvider");
        boolean slf4jNopPresent = isClassPresent(
                "org.slf4j.nop.NOPServiceProvider");

        tests.add(dynamic(
                "SLF4J binding (exactly one) is present",
                () -> assertTrue(
                        slf4jSimplePresent ^ slf4jNopPresent,
                        "Expected exactly one SLF4J binding on the classpath"
                )
        ));
        if (slf4jSimplePresent) {
            tests.add(dynamic(
                    "SLF4J simple binding",
                    () -> assertClassesLoad(
                            "org.slf4j.simple.SimpleServiceProvider",
                            "org.slf4j.simple.SimpleLogger"
                    )
            ));
        }
        if (slf4jNopPresent) {
            tests.add(dynamic(
                    "SLF4J NOP binding",
                    () -> assertClassesLoad(
                            "org.slf4j.nop.NOPServiceProvider"
                    )
            ));
        }
        tests.add(dynamic(
                "Commons Logging",
                () -> assertClassesLoad("org.apache.commons.logging.LogFactory")
        ));
        return tests;
    }

    private static Collection<DynamicTest> collectionTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic("Koloboke API",
                () -> assertPackagePresent("com.koloboke.collect")));
        tests.add(dynamic("Koloboke implementation",
                () -> assertPackagePresent("com.koloboke.collect.impl")));
        tests.add(dynamic("Koloboke compile-time helpers",
                () -> assertPackagePresent("com.koloboke.compile")));
        tests.add(dynamic("Trove4j", () -> assertClassesLoad(
                "gnu.trove.map.hash.TIntIntHashMap")));
        tests.add(dynamic("Commons Lang", () -> assertClassesLoad(
                "org.apache.commons.lang3.StringUtils")));
        tests.add(dynamic("Commons CLI", () -> assertClassesLoad(
                "org.apache.commons.cli.Options")));
        tests.add(dynamic("JavaPoet", () -> assertClassesLoad(
                "com.squareup.javapoet.JavaFile")));
        tests.add(dynamic("JSR-330 inject (jakarta or javax)",
                () -> assertAnyClassLoads(
                        "jakarta.inject.Inject",
                        "javax.inject.Inject"
                )));
        tests.add(dynamic("JetBrains annotations", () -> assertClassesLoad(
                "org.jetbrains.annotations.NotNull")));
        tests.add(dynamic("QuickFIX/J bundle",
                () -> assertPackagePresent("quickfix")));
        tests.add(dynamic("ProGuard base", () -> assertClassesLoad(
                "proguard.ProGuard")));
        tests.add(dynamic("JNR constants", () -> assertClassesLoad(
                "jnr.constants.platform.OpenFlags")));
        tests.add(dynamic("JNR FFI", () -> assertClassesLoad(
                "jnr.ffi.LibraryLoader")));
        tests.add(dynamic("JNA core", () -> assertClassesLoad(
                "com.sun.jna.Native")));
        tests.add(dynamic("JNA platform", () -> assertClassesLoad(
                "com.sun.jna.platform.win32.Kernel32")));
        tests.add(dynamic("Snappy Java", () -> assertClassesLoad(
                "org.xerial.snappy.Snappy")));
        tests.add(dynamic("ClassGraph", () -> assertClassesLoad(
                "io.github.classgraph.ClassGraph")));
        return tests;
    }

    private static Collection<DynamicTest> databaseTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic(
                "HSQLDB driver",
                ClassLoadingSmokeTest::assertHsqldbDriver
        ));
        tests.add(dynamic(
                "Jakarta XML Bind",
                () -> assertClassesLoad("javax.xml.bind.JAXBContext")
        ));
        return tests;
    }

    private static Collection<DynamicTest> testingFrameworkTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic("JUnit 4", () -> assertClassesLoad(
                "org.junit.Test")));
        tests.add(dynamic("JUnit Jupiter", () -> assertClassesLoad(
                "org.junit.jupiter.api.Test",
                "org.junit.jupiter.params.ParameterizedTest")));
        tests.add(dynamic("JUnit Platform engine", () -> assertClassesLoad(
                "org.junit.platform.engine.TestEngine")));
        tests.add(dynamic("JUnit Platform launcher", () -> assertClassesLoad(
                "org.junit.platform.launcher.Launcher")));
        tests.add(dynamic("JUnit Vintage engine", () -> assertClassesLoad(
                "org.junit.vintage.engine.VintageTestEngine")));
        tests.add(dynamic(
                "JUnit migration support",
                () -> assertClassesLoad(
                        "org.junit.jupiter.migrationsupport.rules."
                                + "EnableRuleMigrationSupport"
                )
        ));
        tests.add(dynamic("Hamcrest", () -> assertClassesLoad(
                "org.hamcrest.MatcherAssert")));
        tests.add(dynamic("Mockito", () -> assertClassesLoad(
                "org.mockito.Mockito",
                "org.mockito.junit.jupiter.MockitoExtension")));
        tests.add(dynamic("PowerMock", () -> assertClassesLoad(
                "org.powermock.api.mockito.PowerMockito",
                "org.powermock.modules.junit4.PowerMockRunner")));
        tests.add(dynamic("EasyMock", () -> assertClassesLoad(
                "org.easymock.EasyMock")));
        tests.add(dynamic("Guava testlib", () -> assertClassesLoad(
                "com.google.common.testing.EqualsTester")));
        tests.add(dynamic("JMH core", () -> assertClassesLoad(
                "org.openjdk.jmh.annotations.Benchmark")));
        tests.add(dynamic("JMH core benchmarks",
                () -> assertPackagePresent("org.openjdk.jmh.benchmarks")));
        tests.add(dynamic("JMH generator annotation processor",
                () -> assertPackagePresent("org.openjdk.jmh.generators")));
        return tests;
    }

    private static Collection<DynamicTest> osgiTests() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.add(dynamic("OSGi core", () -> assertClassesLoad(
                "org.osgi.framework.Bundle",
                "org.osgi.framework.launch.Framework")));
        tests.add(dynamic("OSGi compendium", () -> assertClassesLoad(
                "org.osgi.service.event.EventAdmin")));
        tests.add(dynamic(
                "Pax Exam",
                () -> assertPackagePresent("org.ops4j.pax.exam")
        ));
        tests.add(dynamic(
                "Pax Exam native container",
                () -> assertClassesLoad(
                        "org.ops4j.pax.exam.nat.internal.NativeTestContainer"
                )
        ));
        tests.add(dynamic(
                "Pax Exam JUnit runner",
                () -> assertClassesLoad(
                        "org.ops4j.pax.exam.junit.impl.ProbeRunner"
                )
        ));
        tests.add(dynamic(
                "Pax Exam link-mvn",
                () -> assertResourcePresent(
                        "META-INF/maven/org.ops4j.pax.exam/"
                                + "pax-exam-link-mvn/pom.properties"
                )
        ));
        tests.add(dynamic(
                "Pax URL Aether",
                () -> assertPackagePresent("org.ops4j.pax.url.mvn")
        ));
        tests.add(dynamic(
                "Pax URL reference",
                () -> assertPackagePresent("org.ops4j.pax.url.reference")
        ));
        tests.add(dynamic(
                "Apache Felix Framework",
                () -> assertClassesLoad(
                        "org.apache.felix.framework.FrameworkFactory"
                )
        ));
        return tests;
    }

    private static DynamicTest dynamic(
            final String name,
            final Runnable executable
    ) {
        return DynamicTest.dynamicTest(
                name,
                () -> assertDoesNotThrow(executable::run, name)
        );
    }

    private static void assertAnyClassLoads(final String... classNames) {
        List<String> names = Arrays.asList(classNames);
        List<String> failures = new ArrayList<>();
        for (String className : names) {
            try {
                Class.forName(className, false, LOADER);
                return;
            } catch (ClassNotFoundException | LinkageError t) {
                failures.add(className + ": " + t.toString());
            }
        }
        fail("Could not load any of " + names + " -> "
                + failures.stream().collect(Collectors.joining("; ")));
    }

    private static void assertClassesLoad(final String... classNames) {
        List<String> names = Arrays.asList(classNames);
        for (String className : names) {
            assertDoesNotThrow(
                    () -> Class.forName(className, false, LOADER),
                    className
            );
        }
    }

    private static boolean isClassPresent(final String className) {
        try {
            Class.forName(className, false, LOADER);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    private static void assertPackagePresent(final String packagePrefix) {
        try (ScanResult scan = new ClassGraph()
                .acceptPackages(packagePrefix)
                .ignoreClassVisibility()
                .enableClassInfo()
                .scan()) {
            assertFalse(
                    scan.getAllClasses().isEmpty(),
                    "No classes discovered for " + packagePrefix
            );
            String representative = scan.getAllClasses().get(0).getName();
            assertDoesNotThrow(
                    () -> Class.forName(representative, false, LOADER),
                    representative
            );
        }
    }

    private static void assertResourcePresent(final String resourcePath) {
        assertNotNull(
                LOADER.getResource(resourcePath),
                "Expected to find resource " + resourcePath
        );
    }

    private static void assertHsqldbDriver() {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver", false, LOADER);
        } catch (UnsupportedClassVersionError e) {
            if (isJava8()) {
                return;
            }
            throw e;
        } catch (ClassNotFoundException e) {
            AssertionError error =
                    new AssertionError("HSQLDB JDBC driver class should be loadable");
            error.initCause(e);
            throw error;
        }
    }

    private static boolean isJava8() {
        String spec = System.getProperty("java.specification.version", "");
        return spec.startsWith("1.8") || "8".equals(spec);
    }

    @TestFactory
    @DisplayName("Loads representative classes from each third-party artefact")
    Collection<DynamicTest> classLoads() {
        List<DynamicTest> tests = new ArrayList<>();
        tests.addAll(observabilityTests());
        tests.addAll(serialisationTests());
        tests.addAll(mongoTests());
        tests.addAll(networkingTests());
        tests.addAll(loggingTests());
        tests.addAll(collectionTests());
        tests.addAll(databaseTests());
        tests.addAll(testingFrameworkTests());
        tests.addAll(osgiTests());
        tests.addAll(additionalPeerTests());
        assertFalse(tests.isEmpty(), "Dynamic class-loading test list should not be empty");
        return tests;
    }
}
