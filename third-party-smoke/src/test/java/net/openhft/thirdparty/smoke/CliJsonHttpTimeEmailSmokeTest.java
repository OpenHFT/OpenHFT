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
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke tests for CLI parsing, JSON, HTTP request building, time, and email.
 */
class CliJsonHttpTimeEmailSmokeTest {

    @Test
    void jcommanderParsesArgument() {
        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse("--name", "Peter");
        assertEquals("Peter", args.name);
    }

    @Test
    void gsonCanSerializeAndDeserializeMap() {
        Gson gson = new Gson();
        Map<String, String> source =
                Collections.singletonMap("hello", "world");
        String json = gson.toJson(source);
        @SuppressWarnings("unchecked")
        Map<String, String> restored = gson.fromJson(json, Map.class);
        assertEquals("world", restored.get("hello"));
    }

    @Test
    void unirestBuildsRequestWithoutExecuting() {
        HttpRequest request = Unirest.get("http://example.com")
                .queryString("q", "x");
        assertNotNull(request);
    }

    @Test
    void jodaTimeFormatsDate() {
        final int year = 2020;
        final int month = 1;
        final int day = 2;
        final int hour = 3;
        final int minute = 4;
        final int second = 5;
        DateTime dt = new DateTime(year, month, day, hour, minute, second);
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        assertEquals("2020-01-02", fmt.print(dt));
    }

    @Test
    void commonsEmailCanBeConfiguredWithoutSending() throws Exception {
        SimpleEmail email = new SimpleEmail();
        email.setHostName("localhost");
        email.setFrom("from@example.com");
        email.addTo("to@example.com");
        email.setSubject("Test");
        email.setMsg("Hello");
        assertEquals("Test", email.getSubject());
    }

    /**
     * Command-line arguments container.
     */
    static class Args {
        /**
         * Name parameter parsed by JCommander.
         */
        @Parameter(names = "--name")
        private String name = "default";
    }
}
