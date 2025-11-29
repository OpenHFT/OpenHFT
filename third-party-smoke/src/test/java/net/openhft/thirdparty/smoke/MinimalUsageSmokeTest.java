/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.mongodb.ConnectionString;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import gnu.trove.map.hash.TIntIntHashMap;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.undertow.Undertow;
import io.netty.bootstrap.ServerBootstrap;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;
import org.yaml.snakeyaml.Yaml;

import javax.lang.model.element.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MinimalUsageSmokeTest {

    /**
     * Tolerance used for floating point comparisons.
     */
    private static final double DELTA = 0.0001d;

    /**
     * Test value.
     */
    private static final int TEST_VAL = 100;

    @Test
    @DisplayName("JSONAssert can compare payloads")
    void jsonAssertWorks() throws Exception {
        JSONAssert.assertEquals("{\"key\":1}", "{\"key\":1}", false);
    }

    @Test
    @DisplayName("SnakeYAML can parse a simple document")
    void snakeYamlParses() {
        Yaml yaml = new Yaml();
        Map<?, ?> parsed = yaml.load("name: demo");
        assertEquals("demo", parsed.get("name"));
    }

    @Test
    @DisplayName("XStream can round-trip a simple object")
    void xstreamRoundTrips() {
        XStream xStream = new XStream(new StaxDriver());
        xStream.allowTypes(new Class<?>[]{SimplePojo.class});
        SimplePojo pojo = new SimplePojo("value");
        String xml = xStream.toXML(pojo);
        SimplePojo read = (SimplePojo) xStream.fromXML(xml);
        assertEquals(pojo.value, read.value);
    }

    @Test
    @DisplayName("Jackson jsonSchema can generate a schema")
    void jacksonSchemaGenerates() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        JsonSchema schema = generator.generateSchema(SimplePojo.class);
        assertNotNull(schema);
    }

    @Test
    @DisplayName("Mongo ConnectionString parses without network")
    void mongoConnectionStringParses() {
        ConnectionString connectionString = new ConnectionString(
                "mongodb://localhost:27017/testdb"
        );
        assertEquals("testdb", connectionString.getDatabase());
    }

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
        SdkTracerProvider provider = SdkTracerProvider.builder().build();
        OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(provider)
                .build();
        assertNotNull(sdk.getTracer("smoke"));
        provider.close();
    }

    @Test
    @DisplayName("Undertow can build without starting")
    void undertowBuilds() {
        Undertow undertow = Undertow.builder()
                .addHttpListener(0, "localhost")
                .setHandler(exchange ->
                        exchange.getResponseSender().send("ok"))
                .build();
        undertow.stop();
    }

    @Test
    @DisplayName("Jetty WebSocket client constructs")
    void jettyClientConstructs() {
        assertDoesNotThrow(() -> new WebSocketClient());
    }

    @Test
    @DisplayName("Netty bootstrap can be configured")
    void nettyBootstrapConfigurable() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        assertNotNull(bootstrap);
    }

    @Test
    @DisplayName("Grizzly HTTP server base class is available")
    void grizzlyServerAccessible() {
        FilterChainBuilder builder = FilterChainBuilder.stateless();
        assertNotNull(builder);
    }

    @Test
    @DisplayName("MockWebServer can be constructed and closed")
    void mockWebServerLifecycle() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            assertNotNull(server);
        }
    }

    @Test
    @DisplayName("Jettison can build a JSON object")
    void jettisonBuildsJson() throws Exception {
        JSONObject object = new JSONObject();
        object.put("hello", "world");
        assertEquals("world", object.getString("hello"));
    }

    @Test
    @DisplayName("Commons Lang StringUtils can be used")
    void commonsLangUsage() {
        assertEquals("test", StringUtils.trim(" test "));
    }

    @Test
    @DisplayName("Commons CLI Options can be created")
    void commonsCliUsage() {
        Options options = new Options();
        options.addOption("t", "test", false, "test option");
        assertEquals(1, options.getOptions().size());
    }

    @Test
    @DisplayName("JavaPoet can build a Java file")
    void javaPoetUsage() {
        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .build();
        JavaFile javaFile = JavaFile.builder("com.example.helloworld",
                        helloWorld)
                .build();
        assertNotNull(javaFile.toString());
    }

    @Test
    @DisplayName("Trove4j map can be used")
    void troveMapUsage() {
        TIntIntHashMap map = new TIntIntHashMap();
        map.put(1, TEST_VAL);
        assertEquals(TEST_VAL, map.get(1));
    }

    @Test
    @DisplayName("Joda-Time DateTime can be instantiated")
    void jodaTimeUsage() {
        DateTime dt = new DateTime();
        assertNotNull(dt.toString());
    }

    @Test
    @DisplayName("Commons Email can be instantiated")
    void commonsEmailUsage() throws Exception {
        SimpleEmail email = new SimpleEmail();
        assertNotNull(email);
    }

    @Test
    @DisplayName("Snappy Java can compress and uncompress")
    void snappyUsage() throws Exception {
        String input = "Hello Snappy-Java";
        byte[] compressed = Snappy.compress(
                input.getBytes(StandardCharsets.UTF_8));
        byte[] uncompressed = Snappy.uncompress(compressed);
        assertEquals(input, new String(uncompressed, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("ClassGraph can scan")
    void classGraphScans() {
        try (io.github.classgraph.ScanResult scanResult =
                     new io.github.classgraph.ClassGraph()
                .enableClassInfo()
                .scan()) {
            assertFalse(scanResult.getAllClasses().isEmpty());
        }
    }

    @Test
    @DisplayName("JNA Native class is accessible")
    void jnaAccess() {
        assertNotNull(com.sun.jna.Native.POINTER_SIZE);
    }

    @Test
    @DisplayName("JNR Runtime is accessible")
    void jnrUsage() {
        assertNotNull(jnr.ffi.Runtime.getSystemRuntime());
    }

    @Test
    @DisplayName("Log4j2 can log")
    void log4j2Usage() {
        org.apache.logging.log4j.Logger logger =
                LogManager.getLogger(MinimalUsageSmokeTest.class);
        logger.info("Log4j2 smoke test");
        assertNotNull(logger);
    }

    @Test
    @DisplayName("SLF4J can log")
    void slf4jUsage() {
        org.slf4j.Logger logger =
                LoggerFactory.getLogger(MinimalUsageSmokeTest.class);
        logger.info("SLF4J smoke test");
        assertNotNull(logger);
    }

    private static final class SimplePojo {
        /**
         * Stored value used for round-tripping.
         */
        private final String value;

        /**
         * Creates a SimplePojo with the given value.
         *
         * @param newValue textual value
         */
        private SimplePojo(final String newValue) {
            this.value = newValue;
        }

        @SuppressWarnings("unused")
        SimplePojo() {
            this.value = "";
        }
    }
}
