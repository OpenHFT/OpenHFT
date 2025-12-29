/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequest;
import org.apache.commons.mail.SimpleEmail;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke tests for CLI parsing, JSON, HTTP request building, time, and email.
 */
@DisplayName("CliJsonHttpTimeEmailSmokeTest")
class CliJsonHttpTimeEmailSmokeTest {

    @Test
    @DisplayName("JCommander should parse and bind --name command-line argument")
    void jcommanderParsesArgument() {
        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse("--name", "Peter");
        assertEquals("Peter", args.name, "JCommander should bind --name argument");
    }

    @Test
    @DisplayName("Gson should serialize and deserialize a map")
    void gsonCanSerializeAndDeserializeMap() {
        Gson gson = new Gson();
        Map<String, String> source =
                Collections.singletonMap("hello", "world");
        String json = gson.toJson(source);
        @SuppressWarnings("unchecked")
        Map<String, String> restored = gson.fromJson(json, Map.class);
        assertEquals("world", restored.get("hello"), "Gson should round-trip map content");
    }

    @Test
    @DisplayName("Unirest HTTP client should build GET request without network execution")
    void unirestBuildsRequestWithoutExecuting() {
        HttpRequest request = Unirest.get("http://example.com")
                .queryString("q", "x");
        assertNotNull(request, "Unirest should build request without executing");
    }

    @Test
    @DisplayName("Joda-Time should format date as yyyy-MM-dd")
    void jodaTimeFormatsDate() {
        final int year = 2020;
        final int month = 1;
        final int day = 2;
        final int hour = 3;
        final int minute = 4;
        final int second = 5;
        DateTime dateTime = new DateTime(year, month, day, hour, minute, second);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertEquals("2020-01-02", fmt.print(dateTime), "JodaTime should format date as yyyy-MM-dd");
    }

    @Test
    @DisplayName("Commons Email should be configurable without sending")
    void commonsEmailCanBeConfiguredWithoutSending() throws Exception {
        SimpleEmail email = new SimpleEmail();
        email.setHostName("localhost");
        email.setFrom("from@example.com");
        email.addTo("to@example.com");
        email.setSubject("Test");
        email.setMsg("Hello");
        assertEquals("Test", email.getSubject(), "Commons Email should retain configured subject");
    }

    /**
     * Command-line arguments container used by JCommander for smoke-test parsing defaults.
     */
    static class Args {
        /**
         * Name parameter parsed by JCommander for the sample request.
         */
        @Parameter(names = "--name")
        private String name = "default";
    }
}
