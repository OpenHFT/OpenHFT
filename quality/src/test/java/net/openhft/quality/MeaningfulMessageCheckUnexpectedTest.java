/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.Violation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.SortedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Meaningful message check reports unexpected exceptions safely")
class MeaningfulMessageCheckUnexpectedTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Unexpected exception logs a warning with the source line")
    void unexpectedExceptionLogsWarningWithSourceLine() throws Exception {
        MeaningfulMessageCheck check = new MeaningfulMessageCheck();
        check.configure(new DefaultConfiguration("MeaningfulMessageCheck"));
        FileContents contents = createFileContents("InputUnexpected.java",
                "class InputUnexpected { void test() {} }");
        check.setFileContents(contents);

        DetailAstImpl ast = new DetailAstImpl();
        ast.initialize(TokenTypes.IMPORT, "import");
        ast.setLineNo(4);

        check.visitToken(ast);

        SortedSet<Violation> violations = check.getViolations();
        assertEquals(1, violations.size(),
                "Unexpected exception should emit a single warning");
        Violation violation = violations.first();
        assertEquals("assert.message.unexpected.exception", violation.getKey(),
                "Unexpected exception should use the unexpected rule key");
        assertEquals(4, violation.getLineNo(),
                "Unexpected exception should use the AST line number");
    }

    @Test
    @DisplayName("Unexpected exceptions in finish and leave paths are logged")
    void unexpectedExceptionsInFinishAndLeavePathsAreLogged() throws Exception {
        MeaningfulMessageCheck check = new MeaningfulMessageCheck();
        check.configure(new DefaultConfiguration("MeaningfulMessageCheck"));
        FileContents contents = createFileContents("InputUnexpectedFinish.java",
                "class InputUnexpectedFinish { void test() {} }");
        check.setFileContents(contents);

        assertEquals(check.getRequiredTokens().length, check.getAcceptableTokens().length,
                "Acceptable tokens should mirror required tokens");

        check.leaveToken(null);
        setProcessor(check, null);
        check.finishTree(null);

        SortedSet<Violation> violations = check.getViolations();
        assertEquals(2, violations.size(),
                "Unexpected exceptions should be logged for finish and leave paths");
    }

    private FileContents createFileContents(String fileName, String... lines) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.write(file, Arrays.asList(lines), StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), Arrays.asList(lines));
        return new FileContents(text);
    }

    private void setProcessor(MeaningfulMessageCheck check, Object processor) throws Exception {
        Field field = MeaningfulMessageCheck.class.getDeclaredField("processor");
        field.setAccessible(true);
        field.set(check, processor);
        if (field.get(check) != processor) {
            throw new IllegalStateException("Unable to replace processor for test coverage");
        }
    }
}
