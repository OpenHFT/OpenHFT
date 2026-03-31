/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Comment message extractor tests scenario case")
class CommentMessageExtractorTest {

    @TempDir
    Path tempDir;

    private MessageExtractionContext context;
    private CommentMessageExtractor extractor;
    private TestMessageSink sink;

    @BeforeEach
    void setUp() {
        context = new MessageExtractionContext(new MessageAstSupport());
    }

    @Test
    @DisplayName("Return null without comment emits missing message")
    void returnNullWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Arrays.asList("", "return null;"));

        extractor.handleReturnStatement(createReturnNull(2));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(2, sink.missingMessages.get(0).lineNo, "Should use return line");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("Return null with single comment emits candidate")
    void returnNullWithSingleCommentEmitsCandidate() throws Exception {
        List<String> lines = Arrays.asList("// reason with four words", "return null;");
        FileContents contents = createFileContents("InputReturn.java", lines);
        contents.reportSingleLineComment(1, lines.get(0).indexOf("//"));
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(2));

        assertEquals(1, sink.candidates.size(), "Should emit one candidate");
        assertEquals("reason with four words", sink.candidates.get(0).message(),
                "Should strip comment markers");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Return null with block comment emits candidate")
    void returnNullWithBlockCommentEmitsCandidate() throws Exception {
        List<String> lines = Arrays.asList("/* legacy case requires null */", "return null;");
        FileContents contents = createFileContents("InputReturnBlock.java", lines);
        contents.reportBlockComment(1, lines.get(0).indexOf("/*"), 1, lines.get(0).length() - 1);
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(2));

        assertEquals(1, sink.candidates.size(), "Should emit one candidate");
        assertEquals("legacy case requires null", sink.candidates.get(0).message(),
                "Should strip block comment markers");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Return null with multiline block comment emits candidate")
    void returnNullWithMultilineBlockCommentEmitsCandidate() throws Exception {
        List<String> lines = Arrays.asList("/* legacy case", " * requires null */", "return null;");
        FileContents contents = createFileContents("InputReturnBlockMulti.java", lines);
        contents.reportBlockComment(1, lines.get(0).indexOf("/*"),
                2, lines.get(1).length() - 1);
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(3));

        assertEquals(1, sink.candidates.size(), "Should emit one candidate");
        assertEquals("legacy case requires null", sink.candidates.get(0).message(),
                "Should strip block comment markers");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Return null with prior and inline comments emits nothing")
    void returnNullWithMultipleCommentsEmitsNothing() throws Exception {
        List<String> lines = Arrays.asList("// first comment", "return null; // second comment");
        FileContents contents = createFileContents("InputReturnMulti.java", lines);
        contents.reportSingleLineComment(1, lines.get(0).indexOf("//"));
        contents.reportSingleLineComment(2, lines.get(1).indexOf("//"));
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(2));

        assertTrue(sink.candidates.isEmpty(), "Should skip when multiple comments are present");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Return null with inline block and line comments emits nothing")
    void returnNullWithInlineBlockAndLineCommentsEmitsNothing() throws Exception {
        String line = "return null; /* legacy */ // second";
        List<String> lines = Collections.singletonList(line);
        FileContents contents = createFileContents("InputReturnInlineBlock.java", lines);
        contents.reportBlockComment(1, line.indexOf("/*"), 1, line.indexOf("*/") + 1);
        contents.reportSingleLineComment(1, line.indexOf("//"));
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(1));

        assertTrue(sink.candidates.isEmpty(), "Should skip when multiple comments are present");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Return null with same line comment emits candidate")
    void returnNullWithSameLineCommentEmitsCandidate() throws Exception {
        List<String> lines = Collections.singletonList("return null; // legacy case requires null");
        FileContents contents = createFileContents("InputReturnInline.java", lines);
        contents.reportSingleLineComment(1, lines.get(0).indexOf("//"));
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(1));

        assertEquals(1, sink.candidates.size(), "Should emit one candidate");
        assertEquals("legacy case requires null", sink.candidates.get(0).message(),
                "Should extract inline comment");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Return null with multiple comments before emits nothing")
    void returnNullWithMultipleCommentsBeforeEmitsNothing() throws Exception {
        List<String> lines = Arrays.asList("// first reason", "// second reason", "return null;");
        FileContents contents = createFileContents("InputReturnMultiBefore.java", lines);
        contents.reportSingleLineComment(1, lines.get(0).indexOf("//"));
        contents.reportSingleLineComment(2, lines.get(1).indexOf("//"));
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(3));

        assertTrue(sink.candidates.isEmpty(), "Should skip when multiple comments are present");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Return null with separated comments before emits nothing")
    void returnNullWithSeparatedCommentsBeforeEmitsNothing() throws Exception {
        List<String> lines = Arrays.asList("int x = 1;", "", "// first reason", "",
                "// second reason", "return null;");
        FileContents contents = createFileContents("InputReturnSeparated.java", lines);
        contents.reportSingleLineComment(3, lines.get(2).indexOf("//"));
        contents.reportSingleLineComment(5, lines.get(4).indexOf("//"));
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(6));

        assertTrue(sink.candidates.isEmpty(), "Should skip when multiple comments are present");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Return null ignores comments before previous code line")
    void returnNullIgnoresCommentsBeforePreviousCodeLine() throws Exception {
        List<String> lines = Arrays.asList("int x = 1; // old comment", "", "return null;");
        FileContents contents = createFileContents("InputReturnIgnore.java", lines);
        contents.reportSingleLineComment(1, lines.get(0).indexOf("//"));
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(3));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(3, sink.missingMessages.get(0).lineNo, "Should use return line");
    }

    @Test
    @DisplayName("Return null ignores inline block comment on previous code line")
    void returnNullIgnoresInlineBlockCommentOnPreviousCodeLine() throws Exception {
        List<String> lines = Arrays.asList("int x = 1; /* old comment */", "return null;");
        FileContents contents = createFileContents("InputReturnInlineBlockIgnore.java", lines);
        contents.reportBlockComment(1, lines.get(0).indexOf("/*"),
                1, lines.get(0).indexOf("*/") + 1);
        prepareContext(contents);

        extractor.handleReturnStatement(createReturnNull(2));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(2, sink.missingMessages.get(0).lineNo, "Should use return line");
    }

    @Test
    @DisplayName("System usage without comment emits missing message")
    void systemUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("System.getProperty(\"user.dir\");"));

        extractor.handleMethodCall(createMethodCall(new String[]{"System", "getProperty"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("System out usage without comment emits nothing")
    void systemOutUsageWithoutCommentEmitsNothing() throws Exception {
        prepareContext(Collections.singletonList("System.out.println(\"ok\");"));

        extractor.handleMethodCall(createMethodCall(new String[]{"System", "out", "println"}, 1));

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for System.out");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("System lineSeparator usage without comment emits nothing")
    void systemLineSeparatorUsageWithoutCommentEmitsNothing() throws Exception {
        prepareContext(Collections.singletonList("System.lineSeparator();"));

        extractor.handleMethodCall(createMethodCall(new String[]{"System", "lineSeparator"}, 1));

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for System.lineSeparator");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("System getLogger usage without comment emits nothing")
    void systemGetLoggerUsageWithoutCommentEmitsNothing() throws Exception {
        prepareContext(Collections.singletonList("System.getLogger(\"test\");"));

        extractor.handleMethodCall(createMethodCall(new String[]{"System", "getLogger"}, 1));

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for System.getLogger");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("System Logger usage without comment emits nothing")
    void systemLoggerUsageWithoutCommentEmitsNothing() throws Exception {
        prepareContext(Collections.singletonList("System.Logger.Level.INFO.getName();"));

        extractor.handleMethodCall(createMethodCall(
                new String[]{"System", "Logger", "Level", "INFO", "getName"}, 1));

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for System.Logger");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Thread sleep usage without comment emits missing message")
    void threadSleepUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("Thread.sleep(1L);"));

        extractor.handleMethodCall(createMethodCall(new String[]{"Thread", "sleep"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("Thread instance priority usage without comment emits missing message")
    void threadInstancePriorityUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("thread.setPriority(1);"));
        context.recordVariableType(createVariableDef("thread", "Thread"));

        extractor.handleMethodCall(createMethodCall(new String[]{"thread", "setPriority"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("ThreadLocal set usage without comment emits missing message")
    void threadLocalSetUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("local.set(value);"));
        context.recordVariableType(createVariableDef("local", "ThreadLocal"));

        extractor.handleMethodCall(createMethodCall(new String[]{"local", "set"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("InheritableThreadLocal set usage without comment emits missing message")
    void inheritableThreadLocalSetUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("local.set(value);"));
        context.recordVariableType(createVariableDef("local", "InheritableThreadLocal"));

        extractor.handleMethodCall(createMethodCall(new String[]{"local", "set"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("Object wait usage without comment emits missing message")
    void objectWaitUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("wait();"));

        extractor.handleMethodCall(createMethodCall(new String[]{"wait"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("Object notifyAll usage without comment emits missing message")
    void objectNotifyAllUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("notifyAll();"));

        extractor.handleMethodCall(createMethodCall(new String[]{"notifyAll"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("Class forName usage without comment emits missing message")
    void classForNameUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("Class.forName(\"test\");"));

        extractor.handleMethodCall(createMethodCall(new String[]{"Class", "forName"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("ProcessBuilder start usage with new without comment emits missing message")
    void processBuilderStartUsageWithNewWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("new ProcessBuilder().start();"));

        extractor.handleMethodCall(createMethodCallWithQualifier(
                createLiteralNew("ProcessBuilder"), "start", 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("ProcessBuilder start usage without comment emits missing message")
    void processBuilderStartUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("builder.start();"));
        context.recordVariableType(createVariableDef("builder", "ProcessBuilder"));

        extractor.handleMethodCall(createMethodCall(new String[]{"builder", "start"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("String literal intern usage without comment emits missing message")
    void stringLiteralInternUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("\"value\".intern();"));

        extractor.handleMethodCall(createMethodCallWithQualifier(
                createStringLiteral("\"value\""), "intern", 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("String new intern usage without comment emits missing message")
    void stringNewInternUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("new String(\"value\").intern();"));

        extractor.handleMethodCall(createMethodCallWithQualifier(
                createLiteralNew("String"), "intern", 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("String intern usage without comment emits missing message")
    void stringInternUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("value.intern();"));
        context.recordVariableType(createVariableDef("value", "String"));

        extractor.handleMethodCall(createMethodCall(new String[]{"value", "intern"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("Runtime instance usage without comment emits missing message")
    void runtimeInstanceUsageWithoutCommentEmitsMissingMessage() throws Exception {
        prepareContext(Collections.singletonList("runtime.exec(\"ls\");"));
        context.recordVariableType(createVariableDef("runtime", "Runtime"));

        extractor.handleMethodCall(createMethodCall(new String[]{"runtime", "exec"}, 1));

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertEquals(MessageSource.COMMENT, sink.missingMessages.get(0).source,
                "Should use comment source");
    }

    @Test
    @DisplayName("Thread method recognition handles known and unknown names")
    void threadMethodRecognitionHandlesKnownAndUnknownNames() throws Exception {
        prepareContext(Collections.singletonList(""));
        assertTrue(invokeIsThreadMethod("sleep"));
        assertFalse(invokeIsThreadMethod("run"));
    }

    @Test
    @DisplayName("Instance method call returns false for unknown qualifier type")
    void instanceMethodCallReturnsFalseForUnknownQualifierType() throws Exception {
        prepareContext(Collections.singletonList(""));
        DetailAstImpl methodCall = createMethodCall(new String[]{"value", "setPriority"}, 1);

        assertFalse(invokeIsInstanceMethodCall(methodCall, "setPriority", "Thread"));
    }

    @Test
    @DisplayName("Instance method call returns false for mismatched method name")
    void instanceMethodCallReturnsFalseForMismatchedMethodName() throws Exception {
        prepareContext(Collections.singletonList(""));
        DetailAstImpl methodCall = createMethodCall(new String[]{"thread", "setPriority"}, 1);
        context.recordVariableType(createVariableDef("thread", "Thread"));

        assertFalse(invokeIsInstanceMethodCall(methodCall, "sleep", "Thread"));
    }

    @Test
    @DisplayName("Instance method call returns false for unexpected literal new type")
    void instanceMethodCallReturnsFalseForUnexpectedLiteralNewType() throws Exception {
        prepareContext(Collections.singletonList(""));
        DetailAstImpl qualifier = createLiteralNew("String");
        DetailAstImpl methodCall = createMethodCallWithQualifier(qualifier, "intern", 1);

        assertFalse(invokeIsInstanceMethodCall(methodCall, "intern", "Thread"));
    }

    @Test
    @DisplayName("Comment line detection recognises block comment only line")
    void commentLineDetectionRecognisesBlockCommentOnlyLine() throws Exception {
        String line = "/* comment */";
        FileContents contents = createFileContents("InputBlockOnly.java", Collections.singletonList(line));
        prepareContext(contents);
        TextBlock block = mock(TextBlock.class);
        when(block.getStartLineNo()).thenReturn(1);
        when(block.getEndLineNo()).thenReturn(1);
        when(block.getStartColNo()).thenReturn(0);
        when(block.getEndColNo()).thenReturn(line.indexOf("*/") + 1);

        assertTrue(invokeIsCommentLine(contents, 0, Collections.singletonList(block)),
                "Block-only line should be treated as comment line");
    }

    @Test
    @DisplayName("Block comment only line returns false when trailing text exists")
    void blockCommentOnlyLineReturnsFalseWhenTrailingTextExists() throws Exception {
        String line = "/* comment */ int x;";
        prepareContext(Collections.singletonList(line));
        TextBlock block = mock(TextBlock.class);
        when(block.getStartLineNo()).thenReturn(1);
        when(block.getEndLineNo()).thenReturn(1);
        when(block.getStartColNo()).thenReturn(0);
        when(block.getEndColNo()).thenReturn(line.indexOf("*/") + 1);

        assertFalse(invokeIsBlockCommentOnlyLine(line, 1, block),
                "Trailing text should not be treated as comment-only line");
    }

    @Test
    @DisplayName("Clamp column and whitespace checks handle boundaries")
    void clampColumnAndWhitespaceChecksHandleBoundaries() throws Exception {
        prepareContext(Collections.singletonList(""));
        assertEquals(0, invokeClampColumn(-4, 3));
        assertEquals(3, invokeClampColumn(10, 3));
        assertTrue(invokeIsWhitespace(" \t"));
        assertFalse(invokeIsWhitespace("x "));
    }

    private void prepareContext(List<String> lines) throws Exception {
        prepareContext(createFileContents("Input.java", lines));
    }

    private void prepareContext(FileContents contents) {
        context.reset(contents);
        sink = new TestMessageSink();
        extractor = new CommentMessageExtractor(context, sink);
    }

    private FileContents createFileContents(String fileName, List<String> lines) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.write(file, lines, StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), lines);
        return new FileContents(text);
    }

    private DetailAstImpl createReturnNull(int lineNo) {
        DetailAstImpl returnAst = new DetailAstImpl();
        returnAst.setType(TokenTypes.LITERAL_RETURN);
        returnAst.setLineNo(lineNo);

        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        returnAst.addChild(expr);

        DetailAstImpl literalNull = new DetailAstImpl();
        literalNull.setType(TokenTypes.LITERAL_NULL);
        literalNull.setLineNo(lineNo);
        expr.addChild(literalNull);
        return returnAst;
    }

    private DetailAstImpl createMethodCall(String[] chain, int lineNo) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setLineNo(lineNo);

        methodCall.addChild(createDotChain(chain));

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        return methodCall;
    }

    private DetailAstImpl createMethodCallWithQualifier(DetailAstImpl qualifier, String methodName,
                                                        int lineNo) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        methodCall.setLineNo(lineNo);

        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(qualifier);
        dot.addChild(createIdent(methodName));
        methodCall.addChild(dot);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        return methodCall;
    }

    private DetailAstImpl createStringLiteral(String text) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText(text);
        return literal;
    }

    private DetailAstImpl createLiteralNew(String className) {
        DetailAstImpl literalNew = new DetailAstImpl();
        literalNew.setType(TokenTypes.LITERAL_NEW);
        literalNew.addChild(createIdent(className));
        return literalNew;
    }

    private DetailAstImpl createDotChain(String[] chain) {
        DetailAstImpl current = createIdent(chain[0]);
        for (int i = 1; i < chain.length; i++) {
            DetailAstImpl dot = new DetailAstImpl();
            dot.setType(TokenTypes.DOT);
            dot.addChild(current);
            dot.addChild(createIdent(chain[i]));
            current = dot;
        }
        return current;
    }

    private DetailAstImpl createIdent(String text) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(text);
        return ident;
    }

    private DetailAstImpl createVariableDef(String name, String typeName) {
        DetailAstImpl varDef = new DetailAstImpl();
        varDef.setType(TokenTypes.VARIABLE_DEF);

        DetailAstImpl type = new DetailAstImpl();
        type.setType(TokenTypes.TYPE);
        type.addChild(createIdent(typeName));
        varDef.addChild(type);

        varDef.addChild(createIdent(name));
        return varDef;
    }

    private boolean invokeIsThreadMethod(String methodName) throws Exception {
        return extractor.isThreadMethod(methodName);
    }

    private boolean invokeIsInstanceMethodCall(DetailAST methodCall, String methodName,
                                               String... classNames) throws Exception {
        return extractor.isInstanceMethodCall(methodCall, methodName, classNames);
    }

    private boolean invokeIsCommentLine(FileContents contents, int lineIndex,
                                        List<TextBlock> blockComments) throws Exception {
        return extractor.isCommentLine(contents, lineIndex, blockComments);
    }

    private boolean invokeIsBlockCommentOnlyLine(String line, int lineNo, TextBlock block) throws Exception {
        return extractor.isBlockCommentOnlyLine(line, lineNo, block);
    }

    private int invokeClampColumn(int column, int length) throws Exception {
        return extractor.clampColumn(column, length);
    }

    private boolean invokeIsWhitespace(String text) throws Exception {
        return extractor.isWhitespace(text);
    }

    // --- Tests for newly package-local methods ---

    @Test
    @DisplayName("requiresObjectMonitorComment returns true for wait method")
    void requiresObjectMonitorComment_returnsTrueForWait() throws Exception {
        prepareContext(Arrays.asList("", "obj.wait();"));
        DetailAstImpl methodCall = createMethodCallWithName("wait");
        assertTrue(extractor.requiresObjectMonitorComment(methodCall),
                "wait() should require object monitor comment");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment returns true for notify method")
    void requiresObjectMonitorComment_returnsTrueForNotify() throws Exception {
        prepareContext(Arrays.asList("", "obj.notify();"));
        DetailAstImpl methodCall = createMethodCallWithName("notify");
        assertTrue(extractor.requiresObjectMonitorComment(methodCall),
                "notify() should require object monitor comment");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment returns true for notifyAll method")
    void requiresObjectMonitorComment_returnsTrueForNotifyAll() throws Exception {
        prepareContext(Arrays.asList("", "obj.notifyAll();"));
        DetailAstImpl methodCall = createMethodCallWithName("notifyAll");
        assertTrue(extractor.requiresObjectMonitorComment(methodCall),
                "notifyAll() should require object monitor comment");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment returns false for other method")
    void requiresObjectMonitorComment_returnsFalseForOtherMethod() throws Exception {
        prepareContext(Arrays.asList("", "obj.toString();"));
        DetailAstImpl methodCall = createMethodCallWithName("toString");
        assertFalse(extractor.requiresObjectMonitorComment(methodCall),
                "toString() should not require object monitor comment");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment skips helper overload with wait arguments")
    void requiresObjectMonitorComment_skipsHelperOverloadWithWaitArguments() throws Exception {
        prepareContext(Arrays.asList("class HelperWait {", "  void wait(int millis) {}", "}"));
        context.setDeclaredMethodArities(Collections.singletonMap("wait", Collections.singleton(1)));

        DetailAstImpl methodCall = createMethodCallWithNameAndArgs("wait", 1);
        assertFalse(extractor.requiresObjectMonitorComment(methodCall),
                "helper wait(int) overload should not require object monitor comment");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment skips notifyAll helper overload with arguments")
    void requiresObjectMonitorComment_skipsNotifyAllHelperOverloadWithArguments() throws Exception {
        prepareContext(Arrays.asList("class HelperWait {", "  void notifyAll(int signal) {}", "}"));

        DetailAstImpl methodCall = createMethodCallWithNameAndArgs("notifyAll", 1);
        assertFalse(extractor.requiresObjectMonitorComment(methodCall),
                "helper notifyAll(int) overload should not require object monitor comment");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment keeps wait overload warnings when no helper signature matches")
    void requiresObjectMonitorComment_keepsWaitWarningsWhenNoHelperSignatureMatches() throws Exception {
        prepareContext(Arrays.asList("", "wait(100L);"));

        DetailAstImpl methodCall = createMethodCallWithNameAndArgs("wait", 1);
        assertTrue(extractor.requiresObjectMonitorComment(methodCall),
                "wait(arg) should still require an object monitor comment when no helper overload exists");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment ignores impossible wait overloads with more than two arguments")
    void requiresObjectMonitorComment_ignoresWaitOverloadsWithMoreThanTwoArguments() throws Exception {
        prepareContext(Arrays.asList("", "wait(1, 2, 3);"));

        DetailAstImpl methodCall = createMethodCallWithNameAndArgs("wait", 3);
        assertFalse(extractor.requiresObjectMonitorComment(methodCall),
                "wait(arg1, arg2, arg3) should not be treated as an object monitor call");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment skips this qualified helper overload because it resolves to the current class")
    void requiresObjectMonitorComment_skipsThisQualifiedHelperOverload() throws Exception {
        prepareContext(Arrays.asList("class HelperWait {", "  void wait(int millis) {}", "}"));
        context.setDeclaredMethodArities(Collections.singletonMap("wait", Collections.singleton(1)));

        DetailAstImpl literalThis = new DetailAstImpl();
        literalThis.setType(TokenTypes.LITERAL_THIS);

        DetailAstImpl methodCall = createQualifiedMethodCallWithArgs(literalThis, "wait", 1, 2);
        assertFalse(extractor.requiresObjectMonitorComment(methodCall),
                "this.wait(arg) should be treated as the helper overload on the current class");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment skips field qualified helper overload from the current class")
    void requiresObjectMonitorComment_skipsFieldQualifiedHelperOverloadFromCurrentClass() throws Exception {
        prepareContext(Arrays.asList("class HelperWait {", "  void wait(int millis) {}", "}"));
        context.enterType(createTypeDef("HelperWait", 1));
        context.setDeclaredMethodArities(Collections.singletonMap("wait", Collections.singleton(1)));
        context.recordVariableType(createVariableDef("helper", "HelperWait"));

        DetailAstImpl methodCall = createQualifiedMethodCallWithArgs(createIdent("helper"), "wait", 1, 2);
        assertFalse(extractor.requiresObjectMonitorComment(methodCall),
                "helper.wait(arg) should be treated as the current-class helper overload");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment keeps field qualified helper warning when current class context is missing")
    void requiresObjectMonitorComment_keepsFieldQualifiedWarningWhenCurrentClassContextIsMissing() throws Exception {
        prepareContext(Arrays.asList("class HelperWait {", "  void wait(int millis) {}", "}"));
        context.setDeclaredMethodArities(Collections.singletonMap("wait", Collections.singleton(1)));
        context.recordVariableType(createVariableDef("helper", "HelperWait"));

        DetailAstImpl methodCall = createQualifiedMethodCallWithArgs(createIdent("helper"), "wait", 1, 2);
        assertTrue(extractor.requiresObjectMonitorComment(methodCall),
                "helper.wait(arg) should still require a comment when the current class is unknown");
    }

    @Test
    @DisplayName("requiresObjectMonitorComment keeps field qualified helper warning for a different owner type")
    void requiresObjectMonitorComment_keepsFieldQualifiedWarningForDifferentOwnerType() throws Exception {
        prepareContext(Arrays.asList("class HelperWait {", "  void wait(int millis) {}", "}"));
        context.enterType(createTypeDef("HelperWait", 1));
        context.setDeclaredMethodArities(Collections.singletonMap("wait", Collections.singleton(1)));
        context.recordVariableType(createVariableDef("helper", "OtherHelper"));

        DetailAstImpl methodCall = createQualifiedMethodCallWithArgs(createIdent("helper"), "wait", 1, 2);
        assertTrue(extractor.requiresObjectMonitorComment(methodCall),
                "helper.wait(arg) should still require a comment when the field is not the current class type");
    }

    @Test
    @DisplayName("requiresClassForNameComment returns true for Class.forName call")
    void requiresClassForNameComment_returnsTrueForForName() throws Exception {
        prepareContext(Arrays.asList("", "Class.forName(\"test\");"));
        DetailAstImpl methodCall = createDotMethodCall("Class", "forName");
        assertTrue(extractor.requiresClassForNameComment(methodCall),
                "Class.forName() should require reason comment");
    }

    @Test
    @DisplayName("requiresClassForNameComment returns false for other Class method")
    void requiresClassForNameComment_returnsFalseForOtherMethod() throws Exception {
        prepareContext(Arrays.asList("", "Class.cast(obj);"));
        DetailAstImpl methodCall = createDotMethodCall("Class", "cast");
        assertFalse(extractor.requiresClassForNameComment(methodCall),
                "Class.cast() should not require reason comment");
    }

    @Test
    @DisplayName("requiresSystemComment returns false for allowed system member currentTimeMillis")
    void requiresSystemComment_returnsFalseForAllowedMember() throws Exception {
        prepareContext(Arrays.asList("", "System.currentTimeMillis();"));
        DetailAstImpl methodCall = createDotMethodCall("System", "currentTimeMillis");
        assertFalse(extractor.requiresSystemComment(methodCall),
                "System.currentTimeMillis() is an allowed member");
    }

    @Test
    @DisplayName("requiresSystemComment returns true for exit call")
    void requiresSystemComment_returnsTrueForExit() throws Exception {
        prepareContext(Arrays.asList("", "System.exit(0);"));
        DetailAstImpl methodCall = createDotMethodCall("System", "exit");
        assertTrue(extractor.requiresSystemComment(methodCall),
                "System.exit() should require reason comment");
    }

    @Test
    @DisplayName("requiresSystemComment returns false for nanoTime member")
    void requiresSystemComment_returnsFalseForNanoTime() throws Exception {
        prepareContext(Arrays.asList("", "System.nanoTime();"));
        DetailAstImpl methodCall = createDotMethodCall("System", "nanoTime");
        assertFalse(extractor.requiresSystemComment(methodCall),
                "System.nanoTime() is an allowed system member");
    }

    @Test
    @DisplayName("requiresSystemComment returns false for lineSeparator member")
    void requiresSystemComment_returnsFalseForLineSeparator() throws Exception {
        prepareContext(Arrays.asList("", "System.lineSeparator();"));
        DetailAstImpl methodCall = createDotMethodCall("System", "lineSeparator");
        assertFalse(extractor.requiresSystemComment(methodCall),
                "System.lineSeparator() is an allowed system member");
    }

    @Test
    @DisplayName("requiresSystemComment returns false for out member")
    void requiresSystemComment_returnsFalseForOut() throws Exception {
        prepareContext(Arrays.asList("", "System.out;"));
        DetailAstImpl methodCall = createDotMethodCall("System", "out");
        assertFalse(extractor.requiresSystemComment(methodCall),
                "System.out is an allowed system member");
    }

    @Test
    @DisplayName("requiresSystemComment returns false when member is not System qualified")
    void requiresSystemComment_returnsFalseForNonSystemQualifier() throws Exception {
        prepareContext(Arrays.asList("", "MyUtil.exit(0);"));
        DetailAstImpl methodCall = createDotMethodCall("MyUtil", "exit");
        assertFalse(extractor.requiresSystemComment(methodCall),
                "MyUtil.exit() should not require system comment");
    }

    @Test
    @DisplayName("requiresThreadLocalComment returns true for ThreadLocal.set call")
    void requiresThreadLocalComment_returnsTrueForSet() throws Exception {
        prepareContext(Arrays.asList("", "local.set(val);"));
        context.recordVariableType(createVariableDef("local", "ThreadLocal"));
        DetailAstImpl methodCall = createDotMethodCall("local", "set");
        assertTrue(extractor.requiresThreadLocalComment(methodCall),
                "ThreadLocal.set() should require reason comment");
    }

    @Test
    @DisplayName("requiresThreadLocalComment returns false for get method")
    void requiresThreadLocalComment_returnsFalseForGet() throws Exception {
        prepareContext(Arrays.asList("", "local.get();"));
        DetailAstImpl methodCall = createDotMethodCall("local", "get");
        assertFalse(extractor.requiresThreadLocalComment(methodCall),
                "ThreadLocal.get() should not require thread local comment");
    }

    @Test
    @DisplayName("requiresProcessBuilderComment returns false for non-start method")
    void requiresProcessBuilderComment_returnsFalseForNonStart() throws Exception {
        prepareContext(Arrays.asList("", "pb.command();"));
        DetailAstImpl methodCall = createDotMethodCall("pb", "command");
        assertFalse(extractor.requiresProcessBuilderComment(methodCall),
                "ProcessBuilder.command() should not require process builder comment");
    }

    @Test
    @DisplayName("requiresStringInternComment returns false for non-intern method")
    void requiresStringInternComment_returnsFalseForNonIntern() throws Exception {
        prepareContext(Arrays.asList("", "str.length();"));
        DetailAstImpl methodCall = createDotMethodCall("str", "length");
        assertFalse(extractor.requiresStringInternComment(methodCall),
                "String.length() should not require string intern comment");
    }

    private DetailAstImpl createMethodCallWithName(String name) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        methodCall.addChild(ident);
        return methodCall;
    }

    private DetailAstImpl createMethodCallWithNameAndArgs(String name, int argumentCount) {
        DetailAstImpl methodCall = createMethodCallWithName(name);
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        for (int i = 0; i < argumentCount; i++) {
            DetailAstImpl expr = new DetailAstImpl();
            expr.setType(TokenTypes.EXPR);
            DetailAstImpl ident = new DetailAstImpl();
            ident.setType(TokenTypes.IDENT);
            ident.setText("arg" + i);
            expr.addChild(ident);
            elist.addChild(expr);
        }
        methodCall.addChild(elist);
        return methodCall;
    }

    private DetailAstImpl createQualifiedMethodCallWithArgs(DetailAstImpl qualifier, String name,
                                                            int argumentCount, int lineNo) {
        DetailAstImpl methodCall = createMethodCallWithQualifier(qualifier, name, lineNo);
        DetailAstImpl elist = (DetailAstImpl) methodCall.findFirstToken(TokenTypes.ELIST);
        for (int i = 0; i < argumentCount; i++) {
            DetailAstImpl expr = new DetailAstImpl();
            expr.setType(TokenTypes.EXPR);
            DetailAstImpl ident = new DetailAstImpl();
            ident.setType(TokenTypes.IDENT);
            ident.setText("arg" + i);
            expr.addChild(ident);
            elist.addChild(expr);
        }
        return methodCall;
    }

    private DetailAstImpl createDotMethodCall(String qualifier, String name) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        DetailAstImpl qualIdent = new DetailAstImpl();
        qualIdent.setType(TokenTypes.IDENT);
        qualIdent.setText(qualifier);
        dot.addChild(qualIdent);
        DetailAstImpl nameIdent = new DetailAstImpl();
        nameIdent.setType(TokenTypes.IDENT);
        nameIdent.setText(name);
        dot.addChild(nameIdent);
        methodCall.addChild(dot);
        return methodCall;
    }

    private DetailAstImpl createTypeDef(String name, int lineNo) {
        DetailAstImpl typeDef = new DetailAstImpl();
        typeDef.setType(TokenTypes.CLASS_DEF);
        typeDef.setLineNo(lineNo);
        typeDef.addChild(createIdent(name));
        return typeDef;
    }

    private static final class TestMessageSink implements MessageCandidateSink {
        final List<MessageCandidate> candidates = new ArrayList<>();
        final List<MissingMessage> missingMessages = new ArrayList<>();

        @Override
        public void emitCandidate(MessageCandidate candidate) {
            candidates.add(candidate);
        }

        @Override
        public void emitMissingMessage(int lineNo, MessageSource source) {
            missingMessages.add(new MissingMessage(lineNo, source));
        }

        @Override
        public void emitMissingMessage(int lineNo, MessageSource source,
                                       AdviceSource adviceSource,
                                       MissingMessageKind missingMessageKind) {
            emitMissingMessage(lineNo, source);
        }
    }

    private static final class MissingMessage {
        final int lineNo;
        final MessageSource source;

        MissingMessage(int lineNo, MessageSource source) {
            this.lineNo = lineNo;
            this.source = source;
        }
    }
}
