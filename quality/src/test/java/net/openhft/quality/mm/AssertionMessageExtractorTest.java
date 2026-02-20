/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AssertionMessageExtractor}.
 */
@DisplayName("Assertion message extractor tests scenario case")
class AssertionMessageExtractorTest {

    private AssertionMessageExtractor extractor;
    private TestMessageSink sink;
    private MessageExtractionContext context;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        context = new MessageExtractionContext(new MessageAstSupport());
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        context.reset(createFileContents("InputAssert.java", 5));
        sink = new TestMessageSink();
        extractor = new AssertionMessageExtractor(context, sink);
    }

    @Test
    @DisplayName("AssertThrows with JUnit4 signature should extract first message")
    void assertThrowsWithJUnit4SignatureExtractsMessage() {
        DetailAstImpl methodCall = createMethodCall("assertThrows");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Should reject overflow")));
        elist.addChild(createExpr(createClassLiteral("IllegalStateException")));
        elist.addChild(createLambda());

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("Should reject overflow", sink.candidates.get(0).message(),
                "Should use JUnit4 assertThrows message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertEquals with two arguments should emit missing message instead of unhandled")
    void assertEqualsWithTwoArgsEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("expected")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertEquals with message variable extracts template")
    void assertEqualsWithMessageVariableExtractsTemplate() {
        DetailAstImpl initExpr = createPlusExpression(
                createStringLiteral("dump: "),
                createMethodCall("build", "dump")
        );
        context.recordVariableType(createVariableDefWithInitializer("message", "String", initExpr));

        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("message")));
        elist.addChild(createExpr(createIdent("expected")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("dump: {}", sink.candidates.get(0).message(), "Should use template from message variable");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertEquals with JUnit4 static import uses message variable template")
    void assertEqualsWithJUnit4StaticImportUsesMessageVariableTemplate() {
        context.recordStaticJUnitImport("org.junit.Assert.assertEquals", "org.junit.Assert", true);

        DetailAstImpl initExpr = createPlusExpression(
                createStringLiteral("dump: "),
                createIdent("actual")
        );
        context.recordVariableType(createVariableDefWithInitializer("message", "String", initExpr));

        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("message")));
        elist.addChild(createExpr(createStringLiteral("text")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("dump: {}", sink.candidates.get(0).message(),
                "Should use JUnit4 message variable template");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertNotNull with JUnit4 message position ignores trailing string argument")
    void assertNotNullWithJUnit4MessagePositionIgnoresTrailingStringArg() {
        context.recordStaticJUnitImport("org.junit.Assert.assertNotNull", "org.junit.Assert", true);
        context.recordVariableType(createVariableDef("actual", "String"));

        DetailAstImpl methodCall = createMethodCall("assertNotNull");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createMethodCall("message")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(0, sink.missingMessages.size(), "Should not emit missing message for non-literal message");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit unhandled warning for non-literal message");
        assertTrue(sink.unhandledReasons.get(0).contains("assertNotNull"),
                "Unhandled warning should mention assertion method");
    }

    @Test
    @DisplayName("AssertEquals with JUnit5 static import uses message variable template")
    void assertEqualsWithJUnit5StaticImportUsesMessageVariableTemplate() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertEquals",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl initExpr = createPlusExpression(
                createStringLiteral("snapshot: "),
                createIdent("actual")
        );
        context.recordVariableType(createVariableDefWithInitializer("message", "String", initExpr));

        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("text")));
        elist.addChild(createExpr(createIdent("actual")));
        elist.addChild(createExpr(createIdent("message")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("snapshot: {}", sink.candidates.get(0).message(),
                "Should use JUnit5 message variable template");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertEquals with string operands skips ambiguous message extraction")
    void assertEqualsWithStringOperandsSkipsAmbiguousMessageExtraction() {
        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("expected")));
        elist.addChild(createExpr(createStringLiteral("actual")));
        elist.addChild(createExpr(createStringLiteral("message")));

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should skip ambiguous message extraction");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertThat with two arguments skips message checks")
    void assertThatWithTwoArgsSkipsMessageChecks() {
        DetailAstImpl methodCall = createMethodCall("assertThat");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("actual")));
        elist.addChild(createExpr(createIdent("matcher")));

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertThat with message extracts first string argument")
    void assertThatWithMessageExtractsFirstStringArgument() {
        context.recordStaticJUnitImport("org.junit.Assert.assertThat", "org.junit.Assert", true);

        DetailAstImpl methodCall = createMethodCall("assertThat");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Matcher should accept value")));
        elist.addChild(createExpr(createIdent("actual")));
        elist.addChild(createExpr(createIdent("matcher")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("Matcher should accept value", sink.candidates.get(0).message(),
                "Should use first string argument for assertThat");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("Custom assert method with data strings does not emit unhandled warning")
    void assertCustomMethodWithDataStringsDoesNotEmitUnhandled() {
        DetailAstImpl methodCall = createMethodCall("assertOtherFieldsUnchanged");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("original")));
        elist.addChild(createExpr(createStringLiteral("resend")));

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for custom assert");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for custom assert");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for custom assert");
    }

    @Test
    @DisplayName("Custom assert with single argument emits missing message")
    void assertBalancedBracketsWithOneArgEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertBalancedBrackets");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("payload")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("Precondition message-first extracts template")
    void preconditionMessageFirstExtractsTemplate() {
        DetailAstImpl methodCall = createMethodCall("required");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Required value must be set")));
        elist.addChild(createExpr(createIdent("value")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("Required value must be set", sink.candidates.get(0).message(),
                "Should use message-first precondition template");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertTrue with single string emits missing message")
    void assertTrueWithSingleStringEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertTrue");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Should be valid")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertThrows with two args emits missing message")
    void assertThrowsWithTwoArgsEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertThrows");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Should throw for invalid input")));
        elist.addChild(createExpr(createClassLiteral("IllegalStateException")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertTimeout with two args emits missing message")
    void assertTimeoutWithTwoArgsEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertTimeout");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Should finish quickly")));
        elist.addChild(createExpr(createIdent("duration")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertTimeout with message extracts last string argument")
    void assertTimeoutWithMessageExtractsLastStringArgument() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertTimeout",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl methodCall = createMethodCall("assertTimeout");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("duration")));
        elist.addChild(createLambda());
        elist.addChild(createExpr(createStringLiteral("Should finish within timeout")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("Should finish within timeout", sink.candidates.get(0).message(),
                "Should use last argument for JUnit5 assertTimeout");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertDoesNotThrow with one arg emits missing message")
    void assertDoesNotThrowWithOneArgEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertDoesNotThrow");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Should not throw")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertDoesNotThrow with message extracts last string argument")
    void assertDoesNotThrowWithMessageExtractsLastStringArgument() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertDoesNotThrow",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl methodCall = createMethodCall("assertDoesNotThrow");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createLambda());
        elist.addChild(createExpr(createStringLiteral("Should not throw when inputs valid")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("Should not throw when inputs valid", sink.candidates.get(0).message(),
                "Should use last argument for JUnit5 assertDoesNotThrow");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertThat with one arg emits missing message")
    void assertThatWithOneArgEmitsMissingMessage() {
        DetailAstImpl methodCall = createMethodCall("assertThat");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Should match condition")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertAll with lambda heading emits missing message for JUnit5")
    void assertAllWithLambdaHeadingEmitsMissingMessage() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertAll",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl methodCall = createMethodCall("assertAll");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createLambdaWithStringLiteral("lambda heading"));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("JUnit4 assertEquals without message emits missing message")
    void assertEqualsWithJUnit4StyleMissingMessage() {
        context.recordStaticJUnitImport("org.junit.Assert.assertEquals", "org.junit.Assert", true);

        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("expected")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("JUnit5 assertEquals without message emits missing message")
    void assertEqualsWithJUnit5StyleMissingMessage() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertEquals",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl methodCall = createMethodCall("assertEquals");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("expected")));
        elist.addChild(createExpr(createIdent("actual")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.missingMessages.size(), "Should emit missing message");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("JUnit5 assertThrows prefers lambda message when last argument")
    void assertThrowsWithLambdaMessageUsesLambdaMessage() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertThrows",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl methodCall = createMethodCall("assertThrows");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createClassLiteral("IllegalStateException")));
        elist.addChild(createLambdaWithStringLiteral("execute action"));
        elist.addChild(createLambdaWithStringLiteral("should fail with cause"));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit lambda message candidate");
        assertEquals("should fail with cause", sink.candidates.get(0).message(),
                "Should extract message from lambda argument");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("AssertThrows with message first ignores lambda message")
    void assertThrowsWithMessageFirstIgnoresLambdaMessage() {
        DetailAstImpl methodCall = createMethodCall("assertThrows");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("primary message")));
        elist.addChild(createExpr(createClassLiteral("IllegalStateException")));
        elist.addChild(createLambdaWithStringLiteral("lambda message"));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals("primary message", sink.candidates.get(0).message(),
                "Should prefer the first message argument");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("RequireNonNull argument name message is flagged")
    void requireNonNullArgumentNameMessageIsFlagged() {
        DetailAstImpl methodCall = createMethodCall("requireNonNull");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("value")));
        elist.addChild(createExpr(createStringLiteral("value")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertTrue(sink.candidates.get(0).argumentNameMessage(),
                "Message matching argument name should be flagged");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
    }

    @Test
    @DisplayName("JUnit5 supplier message argument is accepted without missing message")
    void assertTrueWithSupplierMessageUsesJUnit5Position() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertTrue",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl methodCall = createMethodCall("assertTrue");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("condition")));
        elist.addChild(createExpr(createMethodRef("supplier", "message")));

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should not emit message candidate for supplier");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for supplier");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for supplier");
    }

    @Test
    @DisplayName("Local assertion helper skips lambda message handling")
    void localAssertionHelperSkipsLambdaMessageHandling() {
        Set<String> declared = new HashSet<>();
        declared.add("assertTrue");
        context.setDeclaredMethodNames(declared);

        DetailAstImpl methodCall = createMethodCall("assertTrue");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("condition")));
        elist.addChild(createExpr(createLambda()));

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for local helper");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for local helper");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for local helper");
    }

    @Test
    @DisplayName("String typed message variable emits unhandled warning")
    void stringTypedMessageVariableEmitsUnhandledWarning() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertTrue",
                "org.junit.jupiter.api.Assertions", false);
        context.recordVariableType(createVariableDef("message", "String"));

        DetailAstImpl methodCall = createMethodCall("assertTrue");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("condition")));
        elist.addChild(createExpr(createIdent("message")));

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should not emit message candidate");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit one unhandled warning");
        assertTrue(sink.unhandledReasons.get(0).contains("Non-literal message argument"),
                "Unhandled warning should mention non-literal message");
    }

    @Test
    @DisplayName("Select JUnit4 message expression resolves known patterns")
    void selectJUnit4MessageExpressionResolvesKnownPatterns() throws Exception {
        DetailAST first = createExpr(createStringLiteral("message"));
        DetailAST second = createExpr(createIdent("arg2"));
        DetailAST third = createExpr(createIdent("arg3"));
        List<DetailAST> args3 = Arrays.asList(first, second, third);

        assertSame(first, invokeSelectJUnit4MessageExpression("fail", 1, Arrays.asList(first)));
        assertSame(first, invokeSelectJUnit4MessageExpression("assertThat", 3, args3));
        assertSame(first, invokeSelectJUnit4MessageExpression("assertThrows", 3, args3));
        assertSame(first, invokeSelectJUnit4MessageExpression("assertTimeout", 3, args3));
        assertSame(first, invokeSelectJUnit4MessageExpression("assertDoesNotThrow", 2, Arrays.asList(first, second)));
        assertSame(first, invokeSelectJUnit4MessageExpression("assertTrue", 2, Arrays.asList(first, second)));
        assertSame(first, invokeSelectJUnit4MessageExpression("assertEquals", 3, args3));
        assertSame(first, invokeSelectJUnit4MessageExpression("assertCustom", 2, Arrays.asList(first, second)));
        assertNull(invokeSelectJUnit4MessageExpression("assertThat", 2, Arrays.asList(first, second)));
        assertNull(invokeSelectJUnit4MessageExpression("verify", 2, Arrays.asList(first, second)));
    }

    @Test
    @DisplayName("Missing assertion message detection handles common cases")
    void missingAssertionMessageDetectionHandlesCommonCases() throws Exception {
        assertTrue(invokeIsMissingAssertionMessage("assertAll",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, Collections.emptyList()));

        List<DetailAST> junit4ThrowsArgs = Arrays.asList(
                createExpr(createStringLiteral("message")),
                createExpr(createClassLiteral("IllegalStateException")),
                createLambda()
        );
        assertFalse(invokeIsMissingAssertionMessage("assertThrows",
                AssertionOperandExtractor.AssertionStyle.JUNIT4, junit4ThrowsArgs));

        List<DetailAST> junit5ThrowsArgs = Arrays.asList(
                createExpr(createClassLiteral("IllegalStateException")),
                createLambda(),
                createExpr(createStringLiteral("message"))
        );
        assertFalse(invokeIsMissingAssertionMessage("assertThrows",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, junit5ThrowsArgs));

        List<DetailAST> junit5TimeoutArgs = Arrays.asList(
                createExpr(createIdent("duration")),
                createLambda()
        );
        assertTrue(invokeIsMissingAssertionMessage("assertTimeout",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, junit5TimeoutArgs));

        List<DetailAST> junit5DoesNotThrowArgs = Arrays.asList(createLambda());
        assertTrue(invokeIsMissingAssertionMessage("assertDoesNotThrow",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, junit5DoesNotThrowArgs));

        List<DetailAST> assertThatArgs = Arrays.asList(
                createExpr(createStringLiteral("message")),
                createExpr(createIdent("actual")),
                createExpr(createIdent("matcher"))
        );
        assertFalse(invokeIsMissingAssertionMessage("assertThat",
                AssertionOperandExtractor.AssertionStyle.JUNIT4, assertThatArgs));

        List<DetailAST> nullMessageArgs = Arrays.asList(
                createExpr(createIdent("condition")),
                createExpr(createNullLiteral())
        );
        assertTrue(invokeIsMissingAssertionMessage("assertTrue",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, nullMessageArgs));

        List<DetailAST> fallbackArgs = Arrays.asList(createExpr(createIdent("value")));
        assertTrue(invokeIsMissingAssertionMessage("assertCustom",
                AssertionOperandExtractor.AssertionStyle.JUNIT4, fallbackArgs));

        List<DetailAST> junit5EqualsArgs = Arrays.asList(
                createExpr(createIdent("expected")),
                createExpr(createIdent("actual")),
                createExpr(createStringLiteral("Should match expected"))
        );
        assertFalse(invokeIsMissingAssertionMessage("assertEquals",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, junit5EqualsArgs));

        List<DetailAST> junit4FallbackArgs = Arrays.asList(
                createExpr(createStringLiteral("Message")),
                createExpr(createIdent("value"))
        );
        assertFalse(invokeIsMissingAssertionMessage("assertCustomCondition",
                AssertionOperandExtractor.AssertionStyle.JUNIT4, junit4FallbackArgs));
    }

    @Test
    @DisplayName("Extract input value handles common expression shapes")
    void extractInputValueHandlesCommonExpressionShapes() throws Exception {
        assertEquals("value", invokeExtractInputValue(createExpr(createStringLiteral("value"))));
        assertEquals("", invokeExtractInputValue(createExpr(createStringLiteral(""))));
        assertEquals("variable", invokeExtractInputValue(createExpr(createIdent("variable"))));
        assertEquals("RuntimeException", invokeExtractInputValue(createExpr(createClassLiteral("RuntimeException"))));
        assertEquals("Bar", invokeExtractInputValue(createExpr(createQualifiedClassLiteral("com", "example", "Bar"))));
        assertEquals("build", invokeExtractInputValue(createExpr(createMethodCall("build"))));
        assertNull(invokeExtractInputValue(createExpr(createNumberLiteral("1"))));
    }

    @Test
    @DisplayName("Parameter name message detection matches argument name")
    void parameterNameMessageDetectionMatchesArgumentName() throws Exception {
        DetailAstImpl arg = createExpr(createIdent("value"));
        List<DetailAST> args = Arrays.asList(arg);
        Map<DetailAST, String> exprToInputValue = new IdentityHashMap<>();
        exprToInputValue.put(arg, "value");

        assertTrue(invokeIsParameterNameMessage("value", args, exprToInputValue));
        assertFalse(invokeIsParameterNameMessage("other", args, exprToInputValue));
    }

    @Test
    @DisplayName("Parameter name message detection returns false for empty and missing inputs")
    void parameterNameMessageDetectionReturnsFalseForEmptyAndMissingInputs() throws Exception {
        DetailAstImpl arg = createExpr(createIdent("value"));
        List<DetailAST> args = Arrays.asList(arg);
        Map<DetailAST, String> exprToInputValue = new IdentityHashMap<>();

        assertFalse(invokeIsParameterNameMessage((String) null, args, exprToInputValue));
        assertFalse(invokeIsParameterNameMessage("value", Collections.emptyList(), exprToInputValue));
        assertFalse(invokeIsParameterNameMessage("value", args, exprToInputValue));

        MessageTemplate template = new MessageTemplate("value", 1, false);
        assertFalse(invokeIsParameterNameMessage(template, args, exprToInputValue));
    }

    @Test
    @DisplayName("Lambda message argument detection matches assertion rules")
    void lambdaMessageArgumentDetectionMatchesAssertionRules() throws Exception {
        assertFalse(invokeIsLambdaMessageArgument("assertTrue",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, 2, -1, 1));
        assertTrue(invokeIsLambdaMessageArgument("assertTrue",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, 2, -1, 2));
        assertFalse(invokeIsLambdaMessageArgument("assertThrows",
                AssertionOperandExtractor.AssertionStyle.JUNIT4, 3, 1, 3));
        assertFalse(invokeIsLambdaMessageArgument("assertThrows",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, 2, 1, 2));
        assertTrue(invokeIsLambdaMessageArgument("assertThrows",
                AssertionOperandExtractor.AssertionStyle.JUNIT5, 3, 2, 3));
        assertFalse(invokeIsLambdaMessageArgument("assertThrows",
                AssertionOperandExtractor.AssertionStyle.UNKNOWN, 3, 1, 3));
        assertTrue(invokeIsLambdaMessageArgument("assertThrows",
                AssertionOperandExtractor.AssertionStyle.UNKNOWN, 3, 2, 3));
    }

    @Test
    @DisplayName("Template extraction handles assertion message")
    void templateExtractionHandlesAssertionMessage() throws Exception {
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertTrue",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl methodCall = createMethodCall("assertTrue");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("condition")));
        elist.addChild(createExpr(createStringLiteral("Condition should be true")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message");
    }

    @Test
    @DisplayName("Key value labels are counted for assertion messages")
    void keyValueLabelsAreCountedForAssertionMessages() {
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertTrue",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl methodCall = createMethodCall("assertTrue");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createIdent("condition")));
        elist.addChild(createExpr(createStringLiteral("index= must match")));

        extractor.handleMethodCall(methodCall);

        assertEquals(1, sink.candidates.size(), "Should emit one message candidate");
        assertEquals(1, sink.candidates.get(0).keyValueLabelCount(),
                "Key value label count should be recorded");
    }

    @Test
    @DisplayName("Ignored exception class skips assertThrows handling")
    void ignoredExceptionClassSkipsAssertThrowsHandling() {
        context.setIgnoredExceptionClassNames(Collections.singleton("IllegalStateException"));

        DetailAstImpl methodCall = createMethodCall("assertThrows");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createStringLiteral("Should throw")));
        elist.addChild(createExpr(createClassLiteral("IllegalStateException")));
        elist.addChild(createLambda());

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for ignored exception");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for ignored exception");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for ignored exception");
    }

    @Test
    @DisplayName("Extract string literal delegates to template extractor")
    void extractStringLiteralDelegatesToTemplateExtractor() throws Exception {
        DetailAstImpl expr = createExpr(createStringLiteral("value"));

        assertEquals("value", invokeExtractStringLiteral(expr),
                "Should extract literal text from expression");
    }

    @Test
    @DisplayName("Assertion call without argument list emits unhandled warning")
    void assertionCallWithoutArgumentListEmitsUnhandledWarning() {
        DetailAstImpl methodCall = createMethodCall("assertTrue");

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate without arguments");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message without arguments");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit unhandled warning");
    }

    @Test
    @DisplayName("Java assert with non-string message emits unhandled warning")
    void javaAssertWithNonStringMessageEmitsUnhandledWarning() {
        DetailAstImpl assertAst = new DetailAstImpl();
        assertAst.setType(TokenTypes.LITERAL_ASSERT);
        assertAst.addChild(createExpr(createIdent("condition")));
        assertAst.addChild(createExpr(createNumberLiteral("42")));

        extractor.handleJavaAssert(assertAst);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for non-string message");
        assertEquals(1, sink.unhandledReasons.size(), "Should emit unhandled warning");
    }

    @Test
    @DisplayName("AssertThrows ignored exception returns without warnings")
    void assertThrowsIgnoredExceptionReturnsWithoutWarnings() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IllegalStateException");
        context.setIgnoredExceptionClassNames(ignored);

        DetailAstImpl methodCall = createMethodCall("assertThrows");
        DetailAstImpl elist = createElist(methodCall);
        elist.addChild(createExpr(createClassLiteral("IllegalStateException")));
        elist.addChild(createLambda());
        elist.addChild(createExpr(createStringLiteral("Ignored warning")));

        extractor.handleMethodCall(methodCall);

        assertTrue(sink.candidates.isEmpty(), "Should not emit candidate for ignored exception");
        assertTrue(sink.missingMessages.isEmpty(), "Should not emit missing message for ignored exception");
        assertTrue(sink.unhandledReasons.isEmpty(), "Should not emit unhandled warning for ignored exception");
    }

    private DetailAstImpl createMethodCall(String name) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl ident = createIdent(name);
        methodCall.addChild(ident);
        return methodCall;
    }

    private DetailAstImpl createMethodCall(String qualifier, String name) {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);

        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent(qualifier));
        dot.addChild(createIdent(name));
        methodCall.addChild(dot);

        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        return methodCall;
    }

    private DetailAstImpl createElist(DetailAstImpl methodCall) {
        DetailAstImpl elist = new DetailAstImpl();
        elist.setType(TokenTypes.ELIST);
        methodCall.addChild(elist);
        return elist;
    }

    private DetailAstImpl createExpr(DetailAstImpl child) {
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        expr.addChild(child);
        return expr;
    }

    private DetailAstImpl createIdent(String name) {
        DetailAstImpl ident = new DetailAstImpl();
        ident.setType(TokenTypes.IDENT);
        ident.setText(name);
        return ident;
    }

    private DetailAstImpl createStringLiteral(String value) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.STRING_LITERAL);
        literal.setText("\"" + value + "\"");
        return literal;
    }

    private DetailAstImpl createNumberLiteral(String value) {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.NUM_INT);
        literal.setText(value);
        return literal;
    }

    private DetailAstImpl createQualifiedClassLiteral(String... parts) {
        DetailAstImpl current = createIdent(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            DetailAstImpl dot = new DetailAstImpl();
            dot.setType(TokenTypes.DOT);
            dot.addChild(current);
            dot.addChild(createIdent(parts[i]));
            current = dot;
        }
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(current);
        dot.addChild(createIdent("class"));
        return dot;
    }

    private DetailAstImpl createNullLiteral() {
        DetailAstImpl literal = new DetailAstImpl();
        literal.setType(TokenTypes.LITERAL_NULL);
        literal.setText("null");
        return literal;
    }

    private DetailAstImpl createClassLiteral(String className) {
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent(className));
        dot.addChild(createIdent("class"));
        return dot;
    }

    private DetailAstImpl createLambda() {
        DetailAstImpl lambda = new DetailAstImpl();
        lambda.setType(TokenTypes.LAMBDA);
        return lambda;
    }

    private DetailAstImpl createLambdaWithStringLiteral(String value) {
        DetailAstImpl lambda = new DetailAstImpl();
        lambda.setType(TokenTypes.LAMBDA);
        DetailAstImpl expr = new DetailAstImpl();
        expr.setType(TokenTypes.EXPR);
        expr.addChild(createStringLiteral(value));
        lambda.addChild(expr);
        return lambda;
    }

    private DetailAstImpl createMethodRef(String qualifier, String name) {
        DetailAstImpl methodRef = new DetailAstImpl();
        methodRef.setType(TokenTypes.METHOD_REF);
        methodRef.addChild(createIdent(qualifier));
        methodRef.addChild(createIdent(name));
        return methodRef;
    }

    private DetailAstImpl createVariableDefWithInitializer(String name, String typeName, DetailAstImpl initializer) {
        DetailAstImpl varDef = createVariableDef(name, typeName);
        DetailAstImpl assign = new DetailAstImpl();
        assign.setType(TokenTypes.ASSIGN);
        assign.addChild(createIdent(name));
        assign.addChild(initializer);
        varDef.addChild(assign);
        return varDef;
    }

    private DetailAstImpl createVariableDef(String name, String typeName) {
        DetailAstImpl varDef = new DetailAstImpl();
        varDef.initialize(TokenTypes.VARIABLE_DEF, "VARIABLE_DEF");

        DetailAstImpl type = new DetailAstImpl();
        type.initialize(TokenTypes.TYPE, "TYPE");
        DetailAstImpl typeIdent = new DetailAstImpl();
        typeIdent.initialize(TokenTypes.IDENT, typeName);
        type.addChild(typeIdent);

        DetailAstImpl ident = new DetailAstImpl();
        ident.initialize(TokenTypes.IDENT, name);

        varDef.addChild(type);
        varDef.addChild(ident);
        return varDef;
    }

    private DetailAstImpl createPlusExpression(DetailAstImpl left, DetailAstImpl right) {
        DetailAstImpl plus = new DetailAstImpl();
        plus.setType(TokenTypes.PLUS);
        plus.addChild(left);
        plus.addChild(right);
        return plus;
    }

    private FileContents createFileContents(String fileName, int lineCount) throws IOException {
        List<String> lines = Collections.nCopies(lineCount, "");
        Path file = tempDir.resolve(fileName);
        Files.write(file, lines, StandardCharsets.UTF_8);
        FileText text = new FileText(file.toFile(), lines);
        return new FileContents(text);
    }

    private DetailAST invokeSelectJUnit4MessageExpression(String methodName, int argCount,
                                                          List<DetailAST> args) throws Exception {
        return extractor.selectJUnit4MessageExpression(methodName, argCount, args);
    }

    private boolean invokeIsLambdaMessageArgument(String methodName,
                                                  AssertionOperandExtractor.AssertionStyle style,
                                                  int argCount, int firstStringArgIndex,
                                                  int lastLambdaArgIndex) throws Exception {
        return extractor.isLambdaMessageArgument(methodName, style, argCount,
                firstStringArgIndex, lastLambdaArgIndex);
    }

    private boolean invokeIsMissingAssertionMessage(String methodName,
                                                    AssertionOperandExtractor.AssertionStyle style,
                                                    List<DetailAST> args) throws Exception {
        return extractor.isMissingAssertionMessage(methodName, style, args);
    }

    private String invokeExtractInputValue(DetailAST expr) throws Exception {
        return extractor.extractInputValue(expr);
    }

    private String invokeExtractStringLiteral(DetailAST expr) throws Exception {
        return extractor.extractStringLiteral(expr);
    }

    private boolean invokeIsParameterNameMessage(String message, List<DetailAST> args,
                                                 Map<DetailAST, String> exprToInputValue) throws Exception {
        return extractor.isParameterNameMessage(message, args, exprToInputValue);
    }

    private boolean invokeIsParameterNameMessage(MessageTemplate template, List<DetailAST> args,
                                                 Map<DetailAST, String> exprToInputValue) throws Exception {
        return extractor.isParameterNameMessage(template, args, exprToInputValue);
    }

    // --- Tests for newly package-local methods ---

    @Test
    @DisplayName("resolveAssertionStyle returns JUNIT4 for qualified Assert call")
    void resolveAssertionStyle_returnsJUnit4ForQualifiedAssertCall() {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        DetailAstImpl qualDot = new DetailAstImpl();
        qualDot.setType(TokenTypes.DOT);
        DetailAstImpl orgDot = new DetailAstImpl();
        orgDot.setType(TokenTypes.DOT);
        orgDot.addChild(createIdent("org"));
        orgDot.addChild(createIdent("junit"));
        qualDot.addChild(orgDot);
        qualDot.addChild(createIdent("Assert"));
        dot.addChild(qualDot);
        dot.addChild(createIdent("assertEquals"));
        methodCall.addChild(dot);

        AssertionOperandExtractor.AssertionStyle style =
                extractor.resolveAssertionStyle(methodCall, "assertEquals");
        assertEquals(AssertionOperandExtractor.AssertionStyle.JUNIT4, style,
                "Qualified org.junit.Assert call should resolve to JUNIT4 style");
    }

    @Test
    @DisplayName("resolveAssertionStyle returns JUNIT5 for qualified Assertions call")
    void resolveAssertionStyle_returnsJUnit5ForQualifiedAssertionsCall() {
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);

        // Build org.junit.jupiter.api.Assertions.assertEquals
        DetailAstImpl d1 = new DetailAstImpl();
        d1.setType(TokenTypes.DOT);
        d1.addChild(createIdent("org"));
        d1.addChild(createIdent("junit"));

        DetailAstImpl d2 = new DetailAstImpl();
        d2.setType(TokenTypes.DOT);
        d2.addChild(d1);
        d2.addChild(createIdent("jupiter"));

        DetailAstImpl d3 = new DetailAstImpl();
        d3.setType(TokenTypes.DOT);
        d3.addChild(d2);
        d3.addChild(createIdent("api"));

        DetailAstImpl d4 = new DetailAstImpl();
        d4.setType(TokenTypes.DOT);
        d4.addChild(d3);
        d4.addChild(createIdent("Assertions"));

        dot.addChild(d4);
        dot.addChild(createIdent("assertEquals"));
        methodCall.addChild(dot);

        AssertionOperandExtractor.AssertionStyle style =
                extractor.resolveAssertionStyle(methodCall, "assertEquals");
        assertEquals(AssertionOperandExtractor.AssertionStyle.JUNIT5, style,
                "Qualified org.junit.jupiter.api.Assertions call should resolve to JUNIT5 style");
    }

    @Test
    @DisplayName("resolveAssertionStyle returns UNKNOWN for unrecognised qualifier class")
    void resolveAssertionStyle_returnsUnknownForUnrecognisedQualifier() {
        // CustomAssertions.assertEquals(...)
        context.recordImport(createImportAst("com.example.CustomAssertions"));
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent("CustomAssertions"));
        dot.addChild(createIdent("assertEquals"));
        methodCall.addChild(dot);

        AssertionOperandExtractor.AssertionStyle style =
                extractor.resolveAssertionStyle(methodCall, "assertEquals");
        assertEquals(AssertionOperandExtractor.AssertionStyle.UNKNOWN, style,
                "Unrecognised qualifier should resolve to UNKNOWN style");
    }

    @Test
    @DisplayName("resolveAssertionStyle returns JUNIT4 for imported Assert qualifier")
    void resolveAssertionStyle_returnsJUnit4ForImportedAssertQualifier() {
        context.recordImport(createImportAst("org.junit.Assert"));
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent("Assert"));
        dot.addChild(createIdent("assertEquals"));
        methodCall.addChild(dot);

        AssertionOperandExtractor.AssertionStyle style =
                extractor.resolveAssertionStyle(methodCall, "assertEquals");
        assertEquals(AssertionOperandExtractor.AssertionStyle.JUNIT4, style,
                "Assert imported as org.junit.Assert should resolve to JUNIT4 style");
    }

    @Test
    @DisplayName("resolveAssertionStyle returns JUNIT5 for imported Assertions qualifier")
    void resolveAssertionStyle_returnsJUnit5ForImportedAssertionsQualifier() {
        context.recordImport(createImportAst("org.junit.jupiter.api.Assertions"));
        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent("Assertions"));
        dot.addChild(createIdent("assertEquals"));
        methodCall.addChild(dot);

        AssertionOperandExtractor.AssertionStyle style =
                extractor.resolveAssertionStyle(methodCall, "assertEquals");
        assertEquals(AssertionOperandExtractor.AssertionStyle.JUNIT5, style,
                "Assertions imported as org.junit.jupiter.api.Assertions should resolve to JUNIT5 style");
    }

    @Test
    @DisplayName("resolveAssertionStyle returns UNKNOWN when both JUnit4 and JUnit5 match unqualified")
    void resolveAssertionStyle_returnsUnknownForAmbiguousStaticImports() {
        context.recordStaticJUnitImport("org.junit.Assert.assertTrue", "org.junit.Assert", true);
        context.recordStaticJUnitImport("org.junit.jupiter.api.Assertions.assertTrue",
                "org.junit.jupiter.api.Assertions", false);

        DetailAstImpl methodCall = createMethodCall("assertTrue");
        AssertionOperandExtractor.AssertionStyle style =
                extractor.resolveAssertionStyle(methodCall, "assertTrue");
        assertEquals(AssertionOperandExtractor.AssertionStyle.UNKNOWN, style,
                "Ambiguous imports should resolve to UNKNOWN style");
    }

    @Test
    @DisplayName("isLocalAssertionHelper returns true for undotted declared method")
    void isLocalAssertionHelper_returnsTrueForDeclaredMethod() {
        Set<String> declared = new HashSet<>();
        declared.add("assertMyCondition");
        context.setDeclaredMethodNames(declared);

        DetailAstImpl methodCall = createMethodCall("assertMyCondition");
        assertTrue(extractor.isLocalAssertionHelper(methodCall, "assertMyCondition"),
                "Declared undotted method should be a local assertion helper");
    }

    @Test
    @DisplayName("isLocalAssertionHelper returns false for dotted non-this qualifier")
    void isLocalAssertionHelper_returnsFalseForDottedNonThisQualifier() {
        Set<String> declared = new HashSet<>();
        declared.add("assertTrue");
        context.setDeclaredMethodNames(declared);

        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        dot.addChild(createIdent("other"));
        dot.addChild(createIdent("assertTrue"));
        methodCall.addChild(dot);

        assertFalse(extractor.isLocalAssertionHelper(methodCall, "assertTrue"),
                "Dotted call with non-this qualifier should not be local helper");
    }

    @Test
    @DisplayName("isLocalAssertionHelper returns true for this-qualified declared method")
    void isLocalAssertionHelper_returnsTrueForThisQualifiedDeclaredMethod() {
        Set<String> declared = new HashSet<>();
        declared.add("assertMyCondition");
        context.setDeclaredMethodNames(declared);

        DetailAstImpl methodCall = new DetailAstImpl();
        methodCall.setType(TokenTypes.METHOD_CALL);
        DetailAstImpl dot = new DetailAstImpl();
        dot.setType(TokenTypes.DOT);
        DetailAstImpl thisLit = new DetailAstImpl();
        thisLit.setType(TokenTypes.LITERAL_THIS);
        thisLit.setText("this");
        dot.addChild(thisLit);
        dot.addChild(createIdent("assertMyCondition"));
        methodCall.addChild(dot);

        assertTrue(extractor.isLocalAssertionHelper(methodCall, "assertMyCondition"),
                "this-qualified declared method should be local helper");
    }

    @Test
    @DisplayName("isLocalAssertionHelper returns false for null arguments")
    void isLocalAssertionHelper_returnsFalseForNullArguments() {
        assertFalse(extractor.isLocalAssertionHelper(null, "name"),
                "null methodCall should return false");
        DetailAstImpl methodCall = createMethodCall("test");
        assertFalse(extractor.isLocalAssertionHelper(methodCall, null),
                "null methodName should return false");
    }

    private DetailAstImpl createImportAst(String qualifiedName) {
        String[] parts = qualifiedName.split("\\.");
        DetailAstImpl current = createIdent(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            DetailAstImpl dot = new DetailAstImpl();
            dot.setType(TokenTypes.DOT);
            dot.addChild(current);
            dot.addChild(createIdent(parts[i]));
            current = dot;
        }
        DetailAstImpl importAst = new DetailAstImpl();
        importAst.setType(TokenTypes.IMPORT);
        importAst.addChild(current);
        return importAst;
    }

    private static final class TestMessageSink implements MessageCandidateSink {
        private final List<MessageCandidate> candidates = new ArrayList<>();
        private final List<MessageSource> missingMessages = new ArrayList<>();
        private final List<String> unhandledReasons = new ArrayList<>();

        @Override
        public void emitCandidate(MessageCandidate candidate) {
            candidates.add(candidate);
        }

        @Override
        public void emitMissingMessage(int lineNo, MessageSource source) {
            missingMessages.add(source);
        }

        @Override
        public void emitMissingMessage(int lineNo, MessageSource source,
                                       AdviceSource adviceSource,
                                       MissingMessageKind missingMessageKind) {
            emitMissingMessage(lineNo, source);
        }

        @Override
        public void emitUnhandled(DetailAST ast, String reason) {
            unhandledReasons.add(reason);
        }
    }
}
