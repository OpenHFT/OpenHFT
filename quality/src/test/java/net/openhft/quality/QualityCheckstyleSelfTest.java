/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("MMDisplayName")
@DisplayName("Quality checkstyle self tests because diagnostics matter so that reports stay clear")
public class QualityCheckstyleSelfTest {

    private static final String CONFIG_RESOURCE =
            "net/openhft/quality/checkstyle26/chronicle-baseline-checkstyle.xml";
    private static final String SUPPRESSIONS_RESOURCE =
            "net/openhft/quality/checkstyle26/checkstyle-suppressions.xml";
    private static final String SUPPRESSIONS_TESTS_RESOURCE =
            "net/openhft/quality/checkstyle26/checkstyle-suppressions-tests.xml";
    private static final String SELF_CHECK_PATH_UNIX =
            "net/openhft/quality/selfcheck/";
    private static final String SELF_CHECK_PATH_WIN =
            "net\\openhft\\quality\\selfcheck\\";
    private static final Set<String> SELF_CHECK_FIXTURES = new LinkedHashSet<>(Arrays.asList(
            "SelfCheckFixture.java",
            "SelfCheckAssertionFixture.java",
            "SelfCheckPreconditionFixture.java",
            "SelfCheckThrowFixture.java",
            "SelfCheckLogFixture.java",
            "SelfCheckCommentFixture.java",
            "SelfCheckMapStringObjectFixture.java",
            "SelfCheckJavadocMemberFixture.java",
            "SelfCheckJavadocClassRedundantFixture.java",
            "SelfCheckJavadocClassTooShortFixture.java",
            "SelfCheckJavadocClassTooFewMeaningfulFixture.java",
            "SelfCheckJavadocClassLongWordFixture.java",
            "SelfCheckJavadocClassDuplicateFixture.java",
            "SelfCheckAnnotationMessagesFixture.java",
            "SelfCheckMissingDisplayNameFixture.java",
            "SelfCheckAnnotationOrderFixture.java",
            "SelfCheckJUnit4Fixture.java",
            "SelfCheckFileLevelFixture.java",
            "SelfCheckLowEntropyFixture.java",
            "SelfCheckUnhandledFixture.java"
    ));
    private static final Pattern RULE_CODE_PATTERN = Pattern.compile("\\[(MM[A-Za-z0-9]+)\\]");
    private static final Map<String, Integer> EXPECTED_RULE_COUNTS = new LinkedHashMap<>();
    private static final int EXPECTED_VIOLATION_COUNT;

    static {
        EXPECTED_RULE_COUNTS.put("MMTooShort", 16);
        EXPECTED_RULE_COUNTS.put("MMRestatesDerivedAssertion", 6);
        EXPECTED_RULE_COUNTS.put("MMContextless", 7);
        EXPECTED_RULE_COUNTS.put("MMIndexOnly", 7);
        EXPECTED_RULE_COUNTS.put("MMLongWord", 9);
        EXPECTED_RULE_COUNTS.put("MMTooLong", 6);
        EXPECTED_RULE_COUNTS.put("MMGenericMessage", 7);
        EXPECTED_RULE_COUNTS.put("MMRestatesAssertion", 6);
        EXPECTED_RULE_COUNTS.put("MMWhitespaceRun", 7);
        EXPECTED_RULE_COUNTS.put("MMRedundantLineNumber", 7);
        EXPECTED_RULE_COUNTS.put("MMTooFewMeaningfulWords", 10);
        EXPECTED_RULE_COUNTS.put("MMDuplicate", 11);
        EXPECTED_RULE_COUNTS.put("MMMissingMessage", 8);
        EXPECTED_RULE_COUNTS.put("MMMapStringObject", 3);
        EXPECTED_RULE_COUNTS.put("MMTestAnnotationOrder", 1);
        EXPECTED_RULE_COUNTS.put("MMLowSignalAssertAllHeading", 1);
        EXPECTED_RULE_COUNTS.put("MMAssertJGenericOverride", 1);
        EXPECTED_RULE_COUNTS.put("MMTrivialSupplier", 4);
        EXPECTED_RULE_COUNTS.put("MMMissingLoopIndex", 1);
        EXPECTED_RULE_COUNTS.put("MMMissingComparisonValues", 1);
        EXPECTED_RULE_COUNTS.put("MMMissingStringSearchValue", 1);
        EXPECTED_RULE_COUNTS.put("MMRedundantClassName", 3);
        EXPECTED_RULE_COUNTS.put("MMRedundantMethodName", 3);
        EXPECTED_RULE_COUNTS.put("MMMissingSubject", 4);
        EXPECTED_RULE_COUNTS.put("MMOverusedWord", 1);
        EXPECTED_RULE_COUNTS.put("MMJUnit4Annotation", 1);
        EXPECTED_RULE_COUNTS.put("MMJUnit4Assertion", 1);
        EXPECTED_RULE_COUNTS.put("MMLowEntropy", 1);
        EXPECTED_RULE_COUNTS.put("MMLacksPurpose", 9);
        EXPECTED_RULE_COUNTS.put("MMDisplayName", 2);
        EXPECTED_RULE_COUNTS.put("MMThrowNull", 1);
        EXPECTED_RULE_COUNTS.put("MMUnhandled", 1);

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
        assertNotNull(url, "Missing required classpath resource: " + resource);
        return Paths.get(url.toURI());
    }

    private static Path locateBaseDir() throws Exception {
        final URL root = QualityCheckstyleSelfTest.class.getResource("/");
        assertNotNull(root, "Missing test classpath root.");
        final Path testClasses = Paths.get(root.toURI());
        final Path targetDir = testClasses.getParent();
        if (targetDir != null) {
            final Path baseDir = targetDir.getParent();
            if (baseDir != null) {
                return baseDir;
            }
        }
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize(); // use working directory property
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
        return null; // no format issues detected
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

    private static boolean isSelfCheckFixtureViolation(String line) {
        if (line == null) {
            return false;
        }
        for (String fixture : SELF_CHECK_FIXTURES) {
            if (line.contains(SELF_CHECK_PATH_UNIX + fixture)
                    || line.contains(SELF_CHECK_PATH_WIN + fixture)) {
                return true;
            }
        }
        return false;
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
    @DisplayName("Test main sources respect baseline scenario")
    public void testMainSourcesRespectBaseline() throws Exception {
        final Path baseDir = locateBaseDir();
        final Path srcMainJava = baseDir.resolve("src/main/java");
        final Path srcTestJava = baseDir.resolve("src/test/java");
        assertTrue(Files.isDirectory(srcMainJava), "Missing src/main/java at " + srcMainJava);
        assertTrue(Files.isDirectory(srcTestJava), "Missing src/test/java at " + srcTestJava);

        final List<File> files = new ArrayList<>();
        files.addAll(collectJavaFiles(srcMainJava));
        files.addAll(collectJavaFiles(srcTestJava));
        files.sort(Comparator.comparing(File::getPath));
        assertFalse(files.isEmpty(), "No Java sources under " + srcMainJava + " or " + srcTestJava);

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
        final String newline = System.lineSeparator(); // platform newline for diagnostics
        if (violations.isEmpty()) {
            fail("SelfCheckFixture should produce " + EXPECTED_VIOLATION_COUNT
                    + " baseline violations.");
        }
        final String violationDetails = String.join(newline, violations);
        final String formatError = validateDetailFormat(violationDetails);
        if (formatError != null) {
            fail("Checkstyle output format issue: " + formatError
                    + newline
                    + violationDetails);
        }

        final Map<String, Integer> actualCounts = new LinkedHashMap<>();
        final List<String> unexpected = new ArrayList<>();
        for (String line : violations) {
            if (!isSelfCheckFixtureViolation(line)) {
                unexpected.add(line);
            }
            Matcher matcher = RULE_CODE_PATTERN.matcher(line);
            if (!matcher.find()) {
                fail("Checkstyle output missing rule code: " + line);
            }
            String ruleCode = matcher.group(1);
            actualCounts.put(ruleCode, actualCounts.getOrDefault(ruleCode, 0) + 1);
        }

        if (!unexpected.isEmpty()) {
            fail("Unexpected Checkstyle violations outside self-check fixtures:"
                    + newline
                    + String.join(newline, unexpected));
        }

        final int actualTotal = sumCounts(actualCounts);
        if (actualTotal != EXPECTED_VIOLATION_COUNT) {
            fail("Unexpected number of Checkstyle violations. Expected "
                    + EXPECTED_VIOLATION_COUNT + " but saw " + actualTotal + "."
                    + newline
                    + "Expected: " + formatCounts(EXPECTED_RULE_COUNTS)
                    + newline
                    + "Actual: " + formatCounts(actualCounts)
                    + newline
                    + violationDetails);
        }

        for (String rule : EXPECTED_RULE_COUNTS.keySet()) {
            int expectedCount = EXPECTED_RULE_COUNTS.get(rule);
            int actualCount = actualCounts.getOrDefault(rule, 0);
            if (actualCount != expectedCount) {
                fail("Unexpected Checkstyle violations for " + rule + ". Expected "
                        + expectedCount + " but saw " + actualCount + "."
                        + newline
                        + "Expected: " + formatCounts(EXPECTED_RULE_COUNTS)
                        + newline
                        + "Actual: " + formatCounts(actualCounts)
                        + newline
                        + violationDetails);
            }
        }
        for (String rule : actualCounts.keySet()) {
            if (!EXPECTED_RULE_COUNTS.containsKey(rule)) {
                fail("Unexpected Checkstyle rule reported: " + rule
                        + newline
                        + violationDetails);
            }
        }

        if (errorCount > 0) {
            fail("Checkstyle reported " + errorCount + " error(s) but no details were captured.");
        }
    }
}
