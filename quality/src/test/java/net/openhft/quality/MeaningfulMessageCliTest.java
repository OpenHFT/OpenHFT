/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MeaningfulMessageCli validates usage errors so that broken command lines fail loudly before analysis starts")
class MeaningfulMessageCliTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Help prints usage and exits successfully so that operators can discover the expected arguments")
    void helpPrintsUsageAndExitsSuccessfully() throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        int exitCode = MeaningfulMessageCli.run(new String[]{"--help"},
                new PrintStream(stdout), new PrintStream(stderr));

        assertEquals(0, exitCode, "help should exit successfully");
        assertTrue(stdout.toString(StandardCharsets.UTF_8).contains("Usage: MeaningfulMessageCli"),
                "help should print usage");
        assertEquals("", stderr.toString(StandardCharsets.UTF_8),
                "help should not print an error");
    }

    @Test
    @DisplayName("Missing --jsonl value prints usage guidance and returns exit code two")
    void missingJsonlValueReturnsUsageError() throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        int exitCode = MeaningfulMessageCli.run(new String[]{"src/test/resources/net/openhft/quality/InputThrowNull.java",
                        "--jsonl"},
                new PrintStream(stdout), new PrintStream(stderr));

        String error = stderr.toString(StandardCharsets.UTF_8);
        assertEquals(2, exitCode, "missing jsonl value should be a usage error");
        assertTrue(error.contains("--jsonl requires a path argument."),
                "stderr should include \"--jsonl requires a path argument.\" for the missing jsonl value");
        assertTrue(error.contains("Usage: MeaningfulMessageCli"),
                "stderr should include \"Usage: MeaningfulMessageCli\" so the missing jsonl value also shows the expected command shape");
    }

    @Test
    @DisplayName("Unknown option prints usage guidance and returns exit code two so that typos stay obvious")
    void unknownOptionReturnsUsageError() throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        int exitCode = MeaningfulMessageCli.run(new String[]{"--verbsoe"},
                new PrintStream(stdout), new PrintStream(stderr));

        String error = stderr.toString(StandardCharsets.UTF_8);
        assertEquals(2, exitCode, "unknown option should be a usage error");
        assertTrue(error.contains("Unknown option: --verbsoe"),
                "stderr should include \"Unknown option: --verbsoe\" for the misspelled option");
        assertTrue(error.contains("Usage: MeaningfulMessageCli"),
                "stderr should include \"Usage: MeaningfulMessageCli\" so the misspelled option also shows the supported command shape");
    }

    @Test
    @DisplayName("Missing input path fails before analysis so that broken CI wiring is visible immediately")
    void missingInputPathFailsBeforeAnalysis() throws Exception {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        int exitCode = MeaningfulMessageCli.run(new String[]{"src/test/resources/net/openhft/quality/DoesNotExist.java"},
                new PrintStream(stdout), new PrintStream(stderr));

        assertEquals(2, exitCode, "missing input path should fail");
        assertTrue(stderr.toString(StandardCharsets.UTF_8)
                        .contains("Input path not found: src/test/resources/net/openhft/quality/DoesNotExist.java"),
                "missing path should be reported");
        assertEquals("", stdout.toString(StandardCharsets.UTF_8),
                "missing path should not start analysis");
    }

    @Test
    @DisplayName("Collect files returns one Java source when directory and file inputs overlap")
    void collectFilesDeduplicatesOverlappingInputs() throws Exception {
        Path sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        Path javaFile = sourceDir.resolve("Sample.java");
        Files.writeString(javaFile, "class Sample {}\n", StandardCharsets.UTF_8);

        List<File> files = MeaningfulMessageCli.collectFiles(List.of(
                sourceDir.toString(),
                javaFile.toString(),
                javaFile.toString()));

        assertEquals(1, files.size(), "collectFiles should deduplicate overlapping inputs");
        assertEquals(javaFile.toAbsolutePath().normalize().toFile(), files.get(0),
                "the collected file should be the unique Java input");
    }
}
