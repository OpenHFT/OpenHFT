/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke tests for {@link AdviceConsoleWriter}.
 */
@DisplayName("Advice console writer tests")
class AdviceConsoleWriterTest {

    @Test
    @DisplayName("Console writer prints file and advice headers")
    void consoleWriter_printsHeaders() {
        AdviceTextLoader loader = AdviceTextLoader.loadFromResource(AdviceReportManager.ADVICE_TEXT_RESOURCE);
        AdviceText text = loader.textFor(AdviceId.MMAssertionMessageMissing);
        AdviceOccurrence occurrence = new AdviceOccurrence(12, AdviceSource.ASSERTION,
                "missing message", null, "assertTrue(condition)");
        AdviceGroup lineGroup = new AdviceGroup(AdviceId.MMAssertionMessageMissing, text, 1,
                Collections.singletonList(occurrence));
        FileAdviceDetails fileDetails = new FileAdviceDetails(AdviceId.MMOverusedWord, 3,
                Collections.singletonList("value"), 5, null, null, null, null, null);
        FileAdviceGroup fileGroup = new FileAdviceGroup(AdviceId.MMOverusedWord,
                loader.textFor(AdviceId.MMOverusedWord), 1, fileDetails);
        FileReport report = new FileReport("Test.java",
                Collections.singletonList(fileGroup),
                Collections.singletonList(lineGroup),
                Collections.emptyMap(),
                Collections.emptyList());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(out));
        try {
            new AdviceConsoleWriter(false).write(report);
        } finally {
            System.setOut(original);
        }

        String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(output.contains("FILE: Test.java"),
                "output should include file header");
        assertTrue(output.contains("FILE-LEVEL MMOverusedWord"),
                "output should include file-level advice header");
        assertTrue(output.contains("MMAssertionMessageMissing"),
                "output should include line-level advice header");
    }
}
