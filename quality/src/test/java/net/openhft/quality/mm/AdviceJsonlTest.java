/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import net.openhft.quality.MeaningfulMessageCheck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Golden test for JSONL output structure.
 */
@DisplayName("Advice JSONL output tests")
class AdviceJsonlTest {

    @Test
    @DisplayName("JSONL output includes run and file records")
    void jsonlOutputIncludesRunAndFileRecords() throws Exception {
        Path jsonl = Files.createTempFile("mm-advice", ".jsonl");
        Path input = Paths.get("src/test/resources/net/openhft/quality/InputAdviceJsonl.java");

        Configuration config = buildConfiguration(jsonl.toString());
        Checker checker = new Checker();
        checker.setModuleClassLoader(AdviceJsonlTest.class.getClassLoader());
        checker.configure(config);
        checker.process(Collections.singletonList(input.toFile()));
        checker.destroy();

        List<String> lines = Files.readAllLines(jsonl);
        String expectedPath = normalisePath(input);
        assertEquals(2, lines.size(), "JSONL should contain run and file records");
        assertEquals("{\"type\":\"run\",\"schema_version\":1,\"tool\":\"MeaningfulMessage\",\"dry_run\":false,"
                        + "\"verbose\":false,\"rank_resource\":\"" + AdviceReportManager.RANK_RESOURCE + "\"}",
                lines.get(0), "run record should match expected JSONL");
        assertEquals("{\"type\":\"file\",\"file\":\"" + expectedPath
                        + "\",\"file_level_advice\":[],\"line_level_advice\":[]}",
                lines.get(1), "file record should match expected JSONL");
    }

    private static Configuration buildConfiguration(String jsonlPath) {
        DefaultConfiguration checker = new DefaultConfiguration("Checker");
        DefaultConfiguration treeWalker = new DefaultConfiguration("TreeWalker");
        DefaultConfiguration mm = new DefaultConfiguration(MeaningfulMessageCheck.class.getName());
        mm.addProperty("jsonl", jsonlPath);
        treeWalker.addChild(mm);
        checker.addChild(treeWalker);
        return checker;
    }

    private static String normalisePath(Path file) {
        Path root = Paths.get(".").toAbsolutePath().normalize();
        Path path = file.toAbsolutePath().normalize();
        if (path.startsWith(root)) {
            return root.relativize(path).toString();
        }
        return file.toString();
    }
}
