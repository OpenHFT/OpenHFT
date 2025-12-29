/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class QualityCheckstyleSelfTest {

    private static final String CONFIG_RESOURCE =
            "net/openhft/quality/checkstyle26/chronicle-baseline-checkstyle.xml";
    private static final String SUPPRESSIONS_RESOURCE =
            "net/openhft/quality/checkstyle26/checkstyle-suppressions.xml";
    private static final String SUPPRESSIONS_TESTS_RESOURCE =
            "net/openhft/quality/checkstyle26/checkstyle-suppressions-tests.xml";
    private static final String FIXTURE_PATH_SUFFIX_UNIX =
            "net/openhft/quality/selfcheck/SelfCheckFixture.java";
    private static final String FIXTURE_PATH_SUFFIX_WIN =
            "net\\openhft\\quality\\selfcheck\\SelfCheckFixture.java";
    private static final Pattern RULE_CODE_PATTERN = Pattern.compile("\\[(MM[A-Za-z]+)\\]");
    private static final Map<String, Integer> EXPECTED_RULE_COUNTS = new LinkedHashMap<>();
    private static final int EXPECTED_VIOLATION_COUNT;

    static {
        EXPECTED_RULE_COUNTS.put("MMMissingMessage", 1);
        EXPECTED_RULE_COUNTS.put("MMRestatesDerivedAssertion", 1);
        EXPECTED_RULE_COUNTS.put("MMContextless", 1);
        EXPECTED_RULE_COUNTS.put("MMIndexOnly", 1);
        EXPECTED_RULE_COUNTS.put("MMLongWord", 1);
        EXPECTED_RULE_COUNTS.put("MMTrivialSupplier", 1);
        EXPECTED_RULE_COUNTS.put("MMGenericMessage", 1);
        EXPECTED_RULE_COUNTS.put("MMRedundantClassName", 1);
        EXPECTED_RULE_COUNTS.put("MMRedundantMethodName", 1);
        EXPECTED_RULE_COUNTS.put("MMRedundantLineNumber", 1);
        EXPECTED_RULE_COUNTS.put("MMRestatesAssertion", 1);
        EXPECTED_RULE_COUNTS.put("MMWhitespaceRun", 1);
        EXPECTED_RULE_COUNTS.put("MMTooShort", 1);
        EXPECTED_RULE_COUNTS.put("MMTooLong", 1);
        EXPECTED_RULE_COUNTS.put("MMTooFewMeaningfulWords", 1);
        EXPECTED_RULE_COUNTS.put("MMMissingSubject", 1);
        EXPECTED_RULE_COUNTS.put("MMDuplicate", 1);

        int total = 0;
        for (int count : EXPECTED_RULE_COUNTS.values()) {
            total += count;
        }
        EXPECTED_VIOLATION_COUNT = total;
    }

    private static List<File> collectJavaFiles(final Path root) throws Exception {
        final List<File> files = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .forEach(path -> files.add(path.toFile()));
        }
        files.sort(Comparator.comparing(File::getPath));
        return files;
    }

    private static Properties buildProperties() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("chronicle.checkstyle.suppressions",
                resourcePath(SUPPRESSIONS_RESOURCE).toString());
        properties.setProperty("chronicle.checkstyle.suppressions.tests",
                resourcePath(SUPPRESSIONS_TESTS_RESOURCE).toString());
        return properties;
    }

    private static Path resourcePath(final String resource) throws Exception {
        final URL url = QualityCheckstyleSelfTest.class.getClassLoader().getResource(resource);
        Assert.assertNotNull("Missing required classpath resource: " + resource, url);
        return Paths.get(url.toURI());
    }

    private static Path locateBaseDir() throws Exception {
        final URL root = QualityCheckstyleSelfTest.class.getResource("/");
        Assert.assertNotNull("Missing test classpath root.", root);
        final Path testClasses = Paths.get(root.toURI());
        final Path targetDir = testClasses.getParent();
        if (targetDir != null) {
            final Path baseDir = targetDir.getParent();
            if (baseDir != null) {
                return baseDir;
            }
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    private static String validateDetailFormat(final String details) {
        final String[] lines = details.split("\\r?\\n");
        for (final String line : lines) {
            final String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!trimmed.matches("\\[[A-Z]+\\].*:\\d+:.*\\[[^\\]]+\\]$")) {
                return "Checkstyle output missing location or rule information: " + trimmed;
            }
        }
        return null;
    }

    private static List<String> extractViolationLines(final String details) {
        if (details == null || details.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        final String[] lines = details.split("\\r?\\n");
        final List<String> violations = new ArrayList<>();
        for (final String line : lines) {
            final String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!(trimmed.startsWith("[WARN]") || trimmed.startsWith("[ERROR]"))) {
                continue;
            }
            violations.add(trimmed);
        }
        return violations;
    }

    private static int sumCounts(Map<String, Integer> counts) {
        int total = 0;
        for (int count : counts.values()) {
            total += count;
        }
        return total;
    }

    private static String formatCounts(Map<String, Integer> counts) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }
        return builder.toString();
    }

    @Test
    public void testMainSourcesRespectBaseline() throws Exception {
        final Path baseDir = locateBaseDir();
        final Path srcMainJava = baseDir.resolve("src/main/java");
        final Path srcTestJava = baseDir.resolve("src/test/java");
        Assert.assertTrue("Missing src/main/java at " + srcMainJava, Files.isDirectory(srcMainJava));
        Assert.assertTrue("Missing src/test/java at " + srcTestJava, Files.isDirectory(srcTestJava));

        final List<File> files = new ArrayList<>();
        files.addAll(collectJavaFiles(srcMainJava));
        files.addAll(collectJavaFiles(srcTestJava));
        files.sort(Comparator.comparing(File::getPath));
        Assert.assertFalse("No Java sources under " + srcMainJava + " or " + srcTestJava, files.isEmpty());

        final Configuration configuration = ConfigurationLoader.loadConfiguration(
                resourcePath(CONFIG_RESOURCE).toString(),
                new PropertiesExpander(buildProperties()),
                ConfigurationLoader.IgnoredModulesOptions.EXECUTE);

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final DefaultLogger logger = new DefaultLogger(output, AutomaticBean.OutputStreamOptions.NONE);

        final Checker checker = new Checker();
        checker.setModuleClassLoader(QualityCheckstyleSelfTest.class.getClassLoader());
        checker.addListener(logger);

        final int errorCount;
        try {
            checker.configure(configuration);
            errorCount = checker.process(files);
        } finally {
            checker.destroy();
        }

        final String details = output.toString(StandardCharsets.UTF_8.name());
        final List<String> violations = extractViolationLines(details);
        if (violations.isEmpty()) {
            Assert.fail("Expected " + EXPECTED_VIOLATION_COUNT
                    + " baseline violations from SelfCheckFixture.");
        }
        final String violationDetails = String.join(System.lineSeparator(), violations);
        final String formatError = validateDetailFormat(violationDetails);
        if (formatError != null) {
            Assert.fail("Checkstyle output format issue: " + formatError
                    + System.lineSeparator()
                    + violationDetails);
        }

        final Map<String, Integer> actualCounts = new LinkedHashMap<>();
        final List<String> unexpected = new ArrayList<>();
        for (String line : violations) {
            if (!line.contains(FIXTURE_PATH_SUFFIX_UNIX) && !line.contains(FIXTURE_PATH_SUFFIX_WIN)) {
                unexpected.add(line);
            }
            Matcher matcher = RULE_CODE_PATTERN.matcher(line);
            if (!matcher.find()) {
                Assert.fail("Checkstyle output missing rule code: " + line);
            }
            String ruleCode = matcher.group(1);
            actualCounts.put(ruleCode, actualCounts.getOrDefault(ruleCode, 0) + 1);
        }

        if (!unexpected.isEmpty()) {
            Assert.fail("Unexpected Checkstyle violations outside SelfCheckFixture:"
                    + System.lineSeparator()
                    + String.join(System.lineSeparator(), unexpected));
        }

        final int actualTotal = sumCounts(actualCounts);
        if (actualTotal != EXPECTED_VIOLATION_COUNT) {
            Assert.fail("Unexpected number of Checkstyle violations. Expected "
                    + EXPECTED_VIOLATION_COUNT + " but saw " + actualTotal + "."
                    + System.lineSeparator()
                    + "Expected: " + formatCounts(EXPECTED_RULE_COUNTS)
                    + System.lineSeparator()
                    + "Actual: " + formatCounts(actualCounts)
                    + System.lineSeparator()
                    + violationDetails);
        }

        for (String rule : EXPECTED_RULE_COUNTS.keySet()) {
            int expectedCount = EXPECTED_RULE_COUNTS.get(rule);
            int actualCount = actualCounts.getOrDefault(rule, 0);
            if (actualCount != expectedCount) {
                Assert.fail("Unexpected Checkstyle violations for " + rule + ". Expected "
                        + expectedCount + " but saw " + actualCount + "."
                        + System.lineSeparator()
                        + "Expected: " + formatCounts(EXPECTED_RULE_COUNTS)
                        + System.lineSeparator()
                        + "Actual: " + formatCounts(actualCounts)
                        + System.lineSeparator()
                        + violationDetails);
            }
        }
        for (String rule : actualCounts.keySet()) {
            if (!EXPECTED_RULE_COUNTS.containsKey(rule)) {
                Assert.fail("Unexpected Checkstyle rule reported: " + rule
                        + System.lineSeparator()
                        + violationDetails);
            }
        }

        if (errorCount > 0) {
            Assert.fail("Checkstyle reported " + errorCount + " error(s) but no details were captured.");
        }
    }
}
