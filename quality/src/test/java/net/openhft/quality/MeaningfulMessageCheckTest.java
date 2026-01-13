/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import net.openhft.quality.mm.RuleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@SuppressWarnings("MMDisplayName")
@DisplayName("Meaningful message check tests because intent matters so that reports stay clear")
public class MeaningfulMessageCheckTest extends AbstractModuleTestSupport {
    private static final String FIX_TOO_SHORT_ASSERTION =
            "add subject + expected behaviour, include key values if relevant";
    private static final String FIX_TOO_SHORT_THROW =
            "state operation + input/state + failure reason";
    private static final String FIX_TOO_SHORT_ANNOTATION =
            "describe scenario + expected outcome";
    private static final String FIX_TOO_SHORT_LOG =
            "include action + subject + identifier or outcome";
    private static final String FIX_TOO_SHORT_JAVADOC_CLASS =
            "state responsibility + lifecycle, thread-safety, or performance intent";
    private static final String FIX_TOO_SHORT_JAVADOC_MEMBER =
            "state contract + units, edge cases, or side effects";
    private static final String FIX_TOO_FEW_MEANINGFUL_ASSERTION =
            "add unique words: subject + expected behaviour, include key values";
    private static final String FIX_TOO_FEW_MEANINGFUL_ANNOTATION =
            "add unique words: scenario + expected outcome";
    private static final String FIX_MISSING_MESSAGE_DEFAULT =
            "add a meaningful message, supply a Throwable, or add a /* reason */ comment "
                    + "inside the argument list when a message must be omitted";

    static Stream<Arguments> provideStandardTestCases() {
        return Stream.of(
                arguments("DuplicateMessages", "InputDuplicateAssertMessages.java", new Object[][]{
                        {12, RuleId.MISSING_SUBJECT, "expected checksum should match"},
                        {18, RuleId.DUPLICATE, "expected checksum should match", 12}
                }),
                arguments("AssertionBranchCoverage", "InputAssertionBranchCoverage.java", new Object[][]{
                        {15, RuleId.CONTEXTLESS, "comparison"},
                        {16, RuleId.MISSING_MESSAGE},
                        {17, RuleId.CONTEXTLESS, "validation"}
                }),
                arguments("PriorityTieBreak", "InputPriorityTieBreak.java", new Object[][]{
                        {12, RuleId.LONG_WORD, "12345678901234567890123456789012345678901234567890",
                                "12345678901234567890123456789012345678901234567890", 42}
                }),
                arguments("RedundantClassName", "InputRedundantClassName.java", new Object[][]{
                        {10, RuleId.REDUNDANT_CLASS, "InputRedundantClassName failed here", "InputRedundantClassName",
                                "only filler words remain: failed, here"}
                }),
                arguments("RedundantMethodName", "InputRedundantMethodName.java", new Object[][]{
                        {10, RuleId.REDUNDANT_METHOD, "testSomething assertion failed", "testSomething",
                                "only filler words remain: assertion, failed"}
                }),
                arguments("RedundantLineNumber", "InputRedundantLineNumber.java", new Object[][]{
                        {10, RuleId.REDUNDANT_LINE, "error at line 10", "line 10"},
                        {11, RuleId.REDUNDANT_LINE, "check L42 failed", "L42"}
                }),
                arguments("JavadocFirstParagraphs", "InputJavadocMessages.java", new Object[][]{
                        {6, RuleId.REDUNDANT_CLASS, "InputJavadocMessages class.", "InputJavadocMessages",
                                "only filler words remain: class"},
                        {11, RuleId.TOO_SHORT, "Does stuff.", 2, 6, 42, FIX_TOO_SHORT_JAVADOC_MEMBER},
                        {17, RuleId.DUPLICATE, "Does stuff.", 11},
                        {23, RuleId.TOO_SHORT, "Uses {@code} now.", 3, 6, 42, FIX_TOO_SHORT_JAVADOC_MEMBER},
                        {29, RuleId.REDUNDANT_METHOD, "methodNamed method class method check method.", "methodNamed",
                                "only filler words remain: method, class, method, check, method"}
                }),
                arguments("GoodMessages", "InputGoodAssertMessages.java", new Object[][]{}),
                arguments("RequireNonNullParameterName", "InputRequireNonNullParamName.java", new Object[][]{}),
                arguments("QualifiedNonJUnitAssertions", "InputQualifiedNonJUnitAssertions.java", new Object[][]{}),
                arguments("DuplicateRedundantAndWordQualityWarnings", "InputAllWarningTypes.java", new Object[][]{
                        {17, RuleId.MISSING_SUBJECT, "expected checksum digest value"},
                        {19, RuleId.DUPLICATE, "expected checksum digest value", 17},
                        {24, RuleId.DUPLICATE, "expected checksum digest value", 17},
                        {31, RuleId.DUPLICATE, "condition must always hold", 29},
                        {39, RuleId.DUPLICATE, "alpha test should pass", 36},
                        {40, RuleId.DUPLICATE, "beta test should pass", 37},
                        {49, RuleId.REDUNDANT_CLASS, "InputAllWarningTypes test failed", "InputAllWarningTypes",
                                "only filler words remain: test, failed"},
                        {54, RuleId.REDUNDANT_CLASS, "inputallwarningtypes error occurred", "InputAllWarningTypes",
                                "only filler words remain: error, occurred"},
                        {59, RuleId.REDUNDANT_CLASS, "Error in InputAllWarningTypes.method()", "InputAllWarningTypes",
                                "only filler words remain: Error, in, method"},
                        {68, RuleId.REDUNDANT_METHOD, "testRedundantMethodName assertion failed", "testRedundantMethodName",
                                "only filler words remain: assertion, failed"},
                        {73, RuleId.REDUNDANT_METHOD, "anotherMethodWithItsNameInMessage should not fail", "anotherMethodWithItsNameInMessage",
                                "only filler words remain: should, not, fail"},
                        {78, RuleId.REDUNDANT_METHOD, "VALIDATESOMETHING check", "validateSomething",
                                "only filler words remain: check"},
                        {87, RuleId.REDUNDANT_LINE, "error at line 42", "line 42"},
                        {89, RuleId.REDUNDANT_LINE, "failed at line #100", "line #100"},
                        {91, RuleId.REDUNDANT_LINE, "check L55 failed", "L55"},
                        {93, RuleId.REDUNDANT_LINE, "MyClass.java:123 error", ".java:123"},
                        {95, RuleId.REDUNDANT_LINE, "see line 999 for details", "line 999"},
                        {104, RuleId.REDUNDANT_CLASS, "InputAllWarningTypes failed at line 50", "InputAllWarningTypes",
                                "only filler words remain: failed, at, line, 50"},
                        {109, RuleId.REDUNDANT_LINE, "testCombinedMethodAndLine error at L99", "L99"},
                        {114, RuleId.REDUNDANT_CLASS, "InputAllWarningTypes.testTripleCombined at line 42", "InputAllWarningTypes",
                                "only 1 unique meaningful word(s): testTripleCombined (filler: at, line, 42)"},
                        {123, RuleId.MISSING_SUBJECT, "expected positive range value"},
                        {146, RuleId.WHITESPACE_RUN, "order  should persist", "  "},
                        {155, RuleId.MISSING_SUBJECT, "expected value should match"}
                }),
                arguments("TrivialSupplier", "InputTrivialSupplier.java", new Object[][]{
                        {16, RuleId.TRIVIAL_SUPPLIER, "expected match"},
                        {21, RuleId.TRIVIAL_SUPPLIER, "expected match"},
                        {26, RuleId.TRIVIAL_SUPPLIER, "abc"},
                        {31, RuleId.TRIVIAL_SUPPLIER, "condition should be true"},
                        {36, RuleId.TRIVIAL_SUPPLIER, "condition should be false"},
                        {49, RuleId.TRIVIAL_SUPPLIER, "expected {} ..."},
                        {55, RuleId.TRIVIAL_SUPPLIER, "expected value {} ..."},
                        {61, RuleId.TRIVIAL_SUPPLIER, "object: {} ..."}
                }),
                arguments("LowSignalMessages", "InputLowSignalMessages.java", new Object[][]{
                        {17, RuleId.GENERIC, "actual"},
                        {18, RuleId.GENERIC, "expected"},
                        {19, RuleId.GENERIC, "value"},
                        {20, RuleId.GENERIC, "result"},
                        {21, RuleId.GENERIC, "data"},
                        {22, RuleId.GENERIC, "object"},
                        {23, RuleId.GENERIC, "condition"},
                        {24, RuleId.GENERIC, "message"},
                        {25, RuleId.GENERIC, "msg"},
                        {26, RuleId.GENERIC, "ok"},
                        {35, RuleId.RESTATES_ASSERTION, "assertEquals"},
                        {36, RuleId.RESTATES_ASSERTION, "assertTrue"},
                        {37, RuleId.RESTATES_ASSERTION, "assertFalse"},
                        {38, RuleId.RESTATES_ASSERTION, "assertNull"},
                        {40, RuleId.RESTATES_ASSERTION, "assertNotNull"},
                        {41, RuleId.RESTATES_ASSERTION, "should be true"},
                        {42, RuleId.RESTATES_ASSERTION, "should be false"},
                        {43, RuleId.RESTATES_ASSERTION, "should be null"},
                        {44, RuleId.RESTATES_ASSERTION, "should not be null"},
                        {45, RuleId.RESTATES_ASSERTION, "must match"},
                        {46, RuleId.RESTATES_ASSERTION, "should match"},
                        {47, RuleId.RESTATES_ASSERTION, "equals"},
                        {48, RuleId.RESTATES_ASSERTION, "not null"},
                        {49, RuleId.RESTATES_ASSERTION, "is null"},
                        {50, RuleId.RESTATES_ASSERTION, "is true"},
                        {51, RuleId.RESTATES_ASSERTION, "is false"},
                        {59, RuleId.INDEX_ONLY, "0"},
                        {60, RuleId.INDEX_ONLY, "1"},
                        {61, RuleId.INDEX_ONLY, "[0]"},
                        {62, RuleId.INDEX_ONLY, "[5]"},
                        {63, RuleId.INDEX_ONLY, "index 0"},
                        {64, RuleId.INDEX_ONLY, "index 42"},
                        {65, RuleId.INDEX_ONLY, "element 0"},
                        {66, RuleId.INDEX_ONLY, "item 3"},
                        {67, RuleId.INDEX_ONLY, "#0"},
                        {68, RuleId.INDEX_ONLY, "i=5"},
                        {77, RuleId.CONTEXTLESS, "comparison"},
                        {78, RuleId.CONTEXTLESS, "validation"},
                        {79, RuleId.CONTEXTLESS, "equality"},
                        {80, RuleId.CONTEXTLESS, "mismatch"},
                        {81, RuleId.CONTEXTLESS, "failed"},
                        {82, RuleId.CONTEXTLESS, "failure"},
                        {90, RuleId.CONTEXTLESS, "test"},
                        {91, RuleId.CONTEXTLESS, "check"},
                        {92, RuleId.CONTEXTLESS, "error"},
                        {100, RuleId.CONTEXTLESS, "should be equal"},
                        {101, RuleId.CONTEXTLESS, "must be equal"},
                        {102, RuleId.CONTEXTLESS, "should equal"},
                        {103, RuleId.CONTEXTLESS, "must equal"},
                        {104, RuleId.CONTEXTLESS, "values should match"},
                        {105, RuleId.CONTEXTLESS, "value should match"}
                }),
                arguments("WordMetrics", "InputWordMetrics.java", new Object[][]{
                        {16, RuleId.TOO_SHORT, "short", 1, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {18, RuleId.TOO_SHORT, "too short", 2, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {27, RuleId.MISSING_SUBJECT, "expected result ok value"},
                        {36, RuleId.TOO_LONG, "one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen twenty twentyone twentytwo twentythree twentyfour twentyfive twentysix twentyseven twentyeight twentynine thirty thirtyone thirtytwo thirtythree thirtyfour thirtyfive thirtysix thirtyseven thirtyeight thirtynine forty fortyone fortytwo fortythree", 43, 42},
                        {45, RuleId.LONG_WORD, "error in SomeExtremelyVeryLongClassNameThatExceedsLimitHere here",
                                "SomeExtremelyVeryLongClassNameThatExceedsLimitHere", 42},
                        {47, RuleId.LONG_WORD, "problem with AnotherIncrediblyLongWordThatExceedsFortyTwoCharacters detected",
                                "AnotherIncrediblyLongWordThatExceedsFortyTwoCharacters", 42}
                }),
                arguments("DuplicateInputHeadingDerivedRules", "InputDuplicateInputHeadingDerivedRules.java", new Object[][]{
                        {20, RuleId.DUPLICATES_INPUT, "admin", "admin"},
                        {21, RuleId.GENERIC, "expected"},
                        {35, RuleId.ASSERTALL_HEADING, "assertAll"},
                        {39, RuleId.ASSERTALL_HEADING, "assertions"},
                        {43, RuleId.ASSERTALL_HEADING, "checks"},
                        {47, RuleId.ASSERTALL_HEADING, "validation"},
                        {51, RuleId.ASSERTALL_HEADING, "test"},
                        {69, RuleId.RESTATES_DERIVED, "empty", "empty"},
                        {70, RuleId.RESTATES_DERIVED, "is empty", "is empty"},
                        {71, RuleId.RESTATES_DERIVED, "not empty", "not empty"},
                        {72, RuleId.RESTATES_DERIVED, "blank", "blank"},
                        {73, RuleId.RESTATES_DERIVED, "present", "present"},
                        {74, RuleId.RESTATES_DERIVED, "not present", "not present"},
                        {75, RuleId.RESTATES_DERIVED, "contains", "contains"},
                        {76, RuleId.RESTATES_DERIVED, "matches", "matches"},
                        {77, RuleId.RESTATES_DERIVED, "size", "size"},
                        {78, RuleId.RESTATES_DERIVED, "zero", "zero"},
                        {79, RuleId.RESTATES_DERIVED, "positive", "positive"},
                        {80, RuleId.RESTATES_DERIVED, "negative", "negative"}
                }),
                arguments("AssertJAndSupplierRules", "InputAssertJAndSupplierRules.java", new Object[][]{
                        {21, RuleId.ASSERTJ_OVERRIDE, "value"},
                        {22, RuleId.ASSERTJ_OVERRIDE, "check"},
                        {23, RuleId.ASSERTJ_OVERRIDE, "test"},
                        {24, RuleId.ASSERTJ_OVERRIDE, "ok"},
                        {41, RuleId.TRIVIAL_SUPPLIER, "value: {} ..."},
                        {42, RuleId.TRIVIAL_SUPPLIER, "name"},
                        {43, RuleId.TRIVIAL_SUPPLIER, "name + suffix"},
                        {44, RuleId.TRIVIAL_SUPPLIER, "prefix {} suffix ..."},
                        {47, RuleId.TRIVIAL_SUPPLIER, "constant message"},
                        {50, RuleId.TOO_SHORT, "value: {}", 2, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {53, RuleId.TRIVIAL_SUPPLIER, "computed: {} ..."}
                }),
                arguments("AssertJMessageArgs", "InputAssertJMessageArgs.java", new Object[][]{
                        {12, RuleId.ASSERTJ_OVERRIDE, "expected %s"}
                }),
                arguments("HamcrestMessages", "InputHamcrestMessages.java", new Object[][]{}),
                arguments("MissingSubjectExamples", "InputMissingSubjectExamples.java", new Object[][]{
                        {11, RuleId.TOO_SHORT, "malformed input: partial character at end", 6, 10, 42,
                                FIX_TOO_SHORT_JAVADOC_CLASS},
                        {19, RuleId.MISSING_SUBJECT, "should emit missing message"},
                        {20, RuleId.MISSING_SUBJECT, "should use comment source"},
                        {21, RuleId.MISSING_SUBJECT, "should not emit unhandled warning"},
                        {22, RuleId.MISSING_SUBJECT, "should not emit missing message"},
                        {23, RuleId.MISSING_SUBJECT, "should throw npe for null"},
                        {24, RuleId.MISSING_SUBJECT, "should emit one message candidate"},
                        {25, RuleId.MISSING_SUBJECT, "should emit one candidate"},
                        {29, RuleId.TOO_SHORT, "skipped on windows/wsl", 4, 6, 42,
                                FIX_TOO_SHORT_ANNOTATION},
                        {34, RuleId.TOO_SHORT, "tradable", 1, 6, 42, FIX_TOO_SHORT_ANNOTATION},
                        {39, RuleId.TOO_SHORT, "ask indicative", 2, 6, 42, FIX_TOO_SHORT_ANNOTATION},
                        {44, RuleId.TOO_SHORT, "bid indicative", 2, 6, 42, FIX_TOO_SHORT_ANNOTATION},
                        {49, RuleId.TOO_SHORT, "assumes synchronous", 2, 6, 42, FIX_TOO_SHORT_ANNOTATION},
                        {54, RuleId.TOO_SHORT, "mid", 1, 6, 42, FIX_TOO_SHORT_ANNOTATION},
                        {59, RuleId.TOO_SHORT, "countpaused updated after pauses ({})", 4, 6, 42,
                                FIX_TOO_SHORT_ANNOTATION}
                }),
                arguments("AssertionMessageCandidates", "InputAssertionMessageCandidates.java", new Object[][]{
                        {10, RuleId.MISSING_SUBJECT, "should emit one candidate"},
                        {11, RuleId.TOO_FEW_MEANINGFUL, "result should not be empty",
                                "(none)", "result, should, not, be, empty", 0, 2,
                                FIX_TOO_FEW_MEANINGFUL_ASSERTION},
                        {12, RuleId.TOO_SHORT, "i: {}", 1, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {13, RuleId.TOO_SHORT, "offer i={}", 2, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {14, RuleId.MISSING_SUBJECT, "don't support index()"},
                        {22, RuleId.TOO_SHORT, "handler closed (priority={})", 3, 4, 42,
                                FIX_TOO_SHORT_ASSERTION},
                        {23, RuleId.TOO_FEW_MEANINGFUL, "null should return 0",
                                "0", "null, should, return", 1, 2,
                                FIX_TOO_FEW_MEANINGFUL_ASSERTION},
                        {24, RuleId.MISSING_SUBJECT, "don't support two queues yet"},
                        {25, RuleId.MISSING_SUBJECT, "should skip when multiple comments are present"},
                        {26, RuleId.TOO_SHORT, "adding {} {} to {}", 2, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {28, RuleId.MISSING_SUBJECT, "should emit unhandled warning"},
                        {29, RuleId.TOO_FEW_MEANINGFUL, "null should throw npe",
                                "(none)", "null, should, throw, npe", 0, 2,
                                FIX_TOO_FEW_MEANINGFUL_ASSERTION},
                        {30, RuleId.MISSING_SUBJECT, "should use return line"},
                        {33, RuleId.TOO_SHORT, "no error for {}", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {37, RuleId.TOO_SHORT, "no more messages", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {39, RuleId.TOO_SHORT, "i={}", 1, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {40, RuleId.TOO_SHORT, "`transacttime` not set", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {41, RuleId.TOO_FEW_MEANINGFUL, "textmethodtester output should match expected",
                                "textmethodtester", "output, should, match, expected", 1, 2,
                                FIX_TOO_FEW_MEANINGFUL_ASSERTION},
                        {42, RuleId.TOO_SHORT, "iter={}", 1, 4, 42, FIX_TOO_SHORT_ASSERTION}
                }),
                arguments("MethodCallMessageTemplates", "InputMethodCallMessageTemplates.java", new Object[][]{
                        {14, RuleId.GENERIC, "expected"},
                        {15, RuleId.CONTEXTLESS, "comparison"},
                        {16, RuleId.TOO_SHORT, "value %s", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {17, RuleId.TOO_SHORT, "ok %s", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {18, RuleId.GENERIC, "expected"}
                }),
                arguments("LocaleFormatExpressions", "InputLocaleFormatExpressions.java", new Object[][]{
                        {17, RuleId.TOO_SHORT, "alpha beta gamma", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {18, RuleId.TOO_SHORT, "delta epsilon zeta", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {19, RuleId.TOO_SHORT, "eta theta iota", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {20, RuleId.TOO_SHORT, "kappa lambda mu", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {21, RuleId.TOO_SHORT, "nu xi omicron", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {22, RuleId.TOO_SHORT, "pi rho sigma", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {23, RuleId.TOO_SHORT, "tau upsilon phi", 3, 4, 42, FIX_TOO_SHORT_ASSERTION}
                }),
                arguments("TemplateEdgeCases", "InputTemplateEdgeCases.java", new Object[][]{
                        {16, RuleId.CONTEXTLESS, "comparison"},
                        {17, RuleId.CONTEXTLESS, "comparison"},
                        {18, RuleId.TOO_SHORT, "value {0}", 3, 4, 42, FIX_TOO_SHORT_ASSERTION}
                }),
                arguments("TemplateVariablesInThrowStatements", "InputThrowTemplateVariables.java", new Object[][]{
                        {9, RuleId.MISSING_MESSAGE},
                        {13, RuleId.MISSING_MESSAGE}
                }),
                arguments("MissingComparisonValues", "InputComparisonValues.java", new Object[][]{
                        {8, RuleId.OVERUSED_WORD, "b(8/12)", 12},
                        {20, RuleId.MISSING_COMPARISON_VALUES, ">", "a", "b"},
                        {21, RuleId.MISSING_COMPARISON_VALUES, ">=", "a", "b"},
                        {22, RuleId.MISSING_COMPARISON_VALUES, "<", "a", "b"},
                        {23, RuleId.MISSING_COMPARISON_VALUES, "<=", "a", "b"},
                        {24, RuleId.MISSING_COMPARISON_VALUES, "==", "a", "b"},
                        {25, RuleId.MISSING_COMPARISON_VALUES, "!=", "a", "b"},
                        {26, RuleId.MISSING_COMPARISON_VALUES, ">", "a", "b"},
                        {39, RuleId.TOO_FEW_MEANINGFUL, "result should be valid",
                                "valid", "result, should, be", 1, 2, FIX_TOO_FEW_MEANINGFUL_ASSERTION},
                        {40, RuleId.TOO_FEW_MEANINGFUL, "collection should not be empty",
                                "collection", "should, not, be, empty", 1, 2,
                                FIX_TOO_FEW_MEANINGFUL_ASSERTION}
                }),
                arguments("LacksPurpose", "InputLacksPurpose.java", new Object[][]{
                        {8, RuleId.LACKS_PURPOSE, 0, 2, 12}
                }),
                arguments("MissingStringSearchValues", "InputStringSearchValues.java", new Object[][]{
                        {19, RuleId.MISSING_STRING_VALUE, "contains", "text", "\"@\""},
                        {20, RuleId.MISSING_STRING_VALUE, "startsWith", "name", "\"Mr\""},
                        {21, RuleId.MISSING_STRING_VALUE, "endsWith", "path", "\".txt\""},
                        {23, RuleId.MISSING_STRING_VALUE, "startsWith", "name", "\"Mrs\""},
                        {24, RuleId.MISSING_STRING_VALUE, "endsWith", "path", "\".tmp\""}
                }),
                arguments("AssertionEdgeCases", "InputAssertionEdgeCases.java", new Object[][]{
                        {11, RuleId.MISSING_COMPARISON_VALUES, ">", "count()", "limit"},
                        {16, RuleId.MISSING_COMPARISON_VALUES, ">=", "total", "limit"},
                        {20, RuleId.MISSING_STRING_VALUE, "startsWith", "\"token\"", "\"a\""},
                        {25, RuleId.MISSING_STRING_VALUE, "contains", "text", "token()"},
                        {33, RuleId.MISSING_MESSAGE}
                }),
                arguments("AssertionMessageVariables", "InputAssertionMessageVariables.java", new Object[][]{
                        {10, RuleId.TOO_SHORT, "dump: {}", 2, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {15, RuleId.TOO_SHORT, "snapshot: {}", 2, 4, 42, FIX_TOO_SHORT_ASSERTION}
                }),
                arguments("MissingLoopIndex", "InputMissingLoopIndex.java", new Object[][]{
                        {18, RuleId.MISSING_LOOP_INDEX, "i"},
                        {26, RuleId.MISSING_LOOP_INDEX, "idx"},
                        {33, RuleId.MISSING_LOOP_INDEX, "name"},
                        {41, RuleId.MISSING_LOOP_INDEX, "pos"},
                        {48, RuleId.MISSING_LOOP_INDEX, "k"}
                }),
                arguments("PriorityZeroConflicts", "InputPriorityZeroConflicts.java", new Object[][]{
                        {12, RuleId.MISSING_LOOP_INDEX, "i"},
                        {13, RuleId.MISSING_LOOP_INDEX, "i"},
                        {19, RuleId.MISSING_LOOP_INDEX, "name"}
                }),
                arguments("AnnotationMessages", "InputAnnotationMessages.java", new Object[][]{
                        {16, RuleId.TOO_SHORT, "cache ready", 2, 6, 42, FIX_TOO_SHORT_ANNOTATION},
                        {20, RuleId.GENERIC, "expected"},
                        {24, RuleId.REDUNDANT_LINE, "L42 run", "L42"},
                        {28, RuleId.RESTATES_ASSERTION, "should be true"},
                        {32, RuleId.REDUNDANT_LINE, "line 10", "line 10"},
                        {36, RuleId.GENERIC, "value"},
                        {40, RuleId.MISSING_MESSAGE},
                        {44, RuleId.MISSING_MESSAGE},
                        {53, RuleId.WHITESPACE_RUN, "order  should persist", "  "},
                        {57, RuleId.MISSING_MESSAGE},
                        {61, RuleId.TOO_FEW_MEANINGFUL,
                                "suffix behaviour under expected input and output conditions",
                                "suffix", "behaviour, under, expected, input, and, output, conditions", 1, 4,
                                FIX_TOO_FEW_MEANINGFUL_ANNOTATION}
                }),
                arguments("LogMessages", "InputLogMessages.java", new Object[][]{
                        {16, RuleId.MISSING_MESSAGE},
                        {18, RuleId.MISSING_MESSAGE},
                        {19, RuleId.MISSING_SUBJECT, "should retry"},
                        {20, RuleId.CONTEXTLESS, "comparison"},
                        {21, RuleId.INDEX_ONLY, "0"},
                        {22, RuleId.GENERIC, "value"},
                        {23, RuleId.WHITESPACE_RUN, "order  should persist", "  "},
                        {26, RuleId.MISSING_MESSAGE},
                        {28, RuleId.CONTEXTLESS, "indices should be valid"},
                        {32, RuleId.GENERIC, "value"},
                        {33, RuleId.MISSING_MESSAGE},
                        {35, RuleId.MISSING_SUBJECT, "should reconnect"},
                        {38, RuleId.MISSING_MESSAGE},
                        {40, RuleId.MISSING_SUBJECT, "expected value should match"},
                        {41, RuleId.TOO_SHORT, "", 1, 4, 42, FIX_TOO_SHORT_LOG},
                        {44, RuleId.MISSING_MESSAGE},
                        {46, RuleId.TOO_SHORT, "", 0, 4, 42, FIX_TOO_SHORT_LOG},
                        {48, RuleId.MISSING_SUBJECT, "should retry"},
                        {49, RuleId.CONTEXTLESS, "comparison"},
                        {50, RuleId.INDEX_ONLY, "0"},
                        {51, RuleId.GENERIC, "value"},
                        {52, RuleId.WHITESPACE_RUN, "order  should persist", "  "},
                        {53, RuleId.CONTEXTLESS, "comparison"},
                        {54, RuleId.CONTEXTLESS, "comparison"},
                        {55, RuleId.GENERIC, "value"},
                        {56, RuleId.MISSING_SUBJECT, "should retry"},
                        {57, RuleId.WHITESPACE_RUN, "order  should persist", "  "},
                        {58, RuleId.GENERIC, "value"},
                        {59, RuleId.CONTEXTLESS, "comparison"},
                        {60, RuleId.INDEX_ONLY, "0"},
                        {62, RuleId.MISSING_MESSAGE},
                        {72, RuleId.MISSING_MESSAGE},
                        {78, RuleId.MISSING_SUBJECT, "Expected more retries"},
                        {79, RuleId.MISSING_SUBJECT, "expected: more retries"},
                        {83, RuleId.MISSING_MESSAGE}
                }),
                arguments("LogMessageTypeResolutionGaps", "InputLogUnknownThrowableType.java", new Object[][]{
                        {13, RuleId.MISSING_MESSAGE}
                }),
                arguments("LogSupplierBranches", "InputLogSupplierBranches.java", new Object[][]{
                        {17, RuleId.TOO_SHORT, "", 1, 4, 42, FIX_TOO_SHORT_LOG},
                        {18, RuleId.TOO_SHORT, "", 1, 4, 42, FIX_TOO_SHORT_LOG},
                        {19, RuleId.TOO_SHORT, "", 1, 4, 42, FIX_TOO_SHORT_LOG},
                        {21, RuleId.TOO_SHORT, "", 1, 4, 42, FIX_TOO_SHORT_LOG}
                }),
                arguments("CheapSupplierDescriptions", "InputCheapSupplierDescriptions.java", new Object[][]{
                        {15, RuleId.TRIVIAL_SUPPLIER, "message"},
                        {16, RuleId.TRIVIAL_SUPPLIER, "prefix + suffix"},
                        {17, RuleId.TRIVIAL_SUPPLIER, "prefix + message"}
                }),
                arguments("MissingAssertionMessages", "InputMissingAssertionMessages.java", new Object[][]{
                        {9, RuleId.MISSING_MESSAGE},
                        {10, RuleId.MISSING_MESSAGE},
                        {11, RuleId.MISSING_MESSAGE},
                        {12, RuleId.MISSING_MESSAGE},
                        {13, RuleId.MISSING_MESSAGE},
                        {14, RuleId.MISSING_MESSAGE},
                        {15, RuleId.MISSING_MESSAGE},
                        {16, RuleId.MISSING_MESSAGE},
                        {17, RuleId.MISSING_MESSAGE},
                        {18, RuleId.MISSING_MESSAGE},
                        {19, RuleId.MISSING_MESSAGE},
                        {25, RuleId.MISSING_MESSAGE},
                        {26, RuleId.MISSING_MESSAGE},
                        {27, RuleId.MISSING_MESSAGE},
                        {28, RuleId.MISSING_MESSAGE},
                        {29, RuleId.MISSING_MESSAGE},
                        {30, RuleId.MISSING_MESSAGE},
                        {31, RuleId.MISSING_MESSAGE},
                        {32, RuleId.MISSING_MESSAGE},
                        {33, RuleId.MISSING_MESSAGE},
                        {34, RuleId.MISSING_MESSAGE},
                        {35, RuleId.MISSING_MESSAGE},
                        {37, RuleId.MISSING_MESSAGE},
                        {39, RuleId.MISSING_MESSAGE},
                        {43, RuleId.MISSING_MESSAGE},
                        {47, RuleId.MISSING_MESSAGE},
                        {54, RuleId.MISSING_MESSAGE},
                        {58, RuleId.MISSING_MESSAGE}
                }),
                arguments("LoopAssignmentIndex", "InputLoopAssignmentIndex.java", new Object[][]{
                        {14, RuleId.MISSING_LOOP_INDEX, "position"}
                }),
                arguments("ThrowNewMessages", "InputThrowNewMessages.java", new Object[][]{
                        {20, RuleId.GENERIC, "expected"},
                        {25, RuleId.DUPLICATE, "user id must be present", 24},
                        {33, RuleId.DUPLICATE, "configuration snapshot not loaded", 29},
                        {37, RuleId.REDUNDANT_LINE, "line 42", "line 42"},
                        {41, RuleId.MISSING_MESSAGE}
                }),
                arguments("ThrowNullLiteral", "InputThrowNull.java", new Object[][]{
                        {9, RuleId.THROW_NULL}
                }),
                arguments("ThrowCauseMessages", "InputThrowCauseMessages.java", new Object[][]{
                        {9, RuleId.MISSING_MESSAGE},
                        {13, RuleId.MISSING_MESSAGE},
                        {21, RuleId.MISSING_MESSAGE},
                        {25, RuleId.MISSING_MESSAGE}
                }),
                arguments("AssertionExtractorCoverage", "InputAssertionExtractorCoverage.java", new Object[][]{
                        {19, RuleId.MISSING_LOOP_INDEX, "idx"},
                        {24, RuleId.TRIVIAL_SUPPLIER, "cache entry should be ready for %s"}
                }),
                arguments("SuppressionScopes", "InputSuppressionScopes.java", new Object[][]{
                        {20, RuleId.CONTEXTLESS, "result should match expected"},
                        {37, RuleId.TOO_SHORT, "omicron pi rho", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {38, RuleId.CONTEXTLESS, "indices should be valid"}
                }),
                arguments("QualifiedSuppressions", "InputQualifiedSuppressions.java", new Object[][]{
                        {22, RuleId.TOO_SHORT, "eta theta iota", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {23, RuleId.CONTEXTLESS, "result should match expected"}
                }),
                arguments("TooShortSuppressWarnings", "InputSuppressWarningsTooShort.java", new Object[][]{
                        {19, RuleId.TOO_SHORT, "foxtrot golf hotel india juliet", 5, 6, 42, FIX_TOO_SHORT_ANNOTATION},
                        {22, RuleId.TOO_SHORT, "kilo lima mike", 3, 4, 42, FIX_TOO_SHORT_LOG}
                }),
                arguments("RuleCodeSuppressions", "InputRuleCodeSuppressions.java", new Object[][]{
                        {16, RuleId.TOO_SHORT, "delta epsilon zeta", 3, 4, 42, FIX_TOO_SHORT_ASSERTION},
                        {25, RuleId.CONTEXTLESS, "operation result should equal expected value"}
                }),
                arguments("PrefilteredMessages", "InputPrefilteredMessages.java", new Object[][]{
                        {21, RuleId.TOO_SHORT, "alpha beta gamma", 3, 4, 42, FIX_TOO_SHORT_ASSERTION}
                }),
                arguments("CoveragePaths", "InputCoveragePaths.java", new Object[][]{
                        {20, RuleId.MISSING_MESSAGE},
                        {26, RuleId.MISSING_MESSAGE},
                        {32, RuleId.MISSING_MESSAGE},
                        {48, RuleId.TRIVIAL_SUPPLIER, "this is a very long string that exceeds twelve characters for truncation test {} ..."},
                        {54, RuleId.MISSING_MESSAGE},
                        {60, RuleId.MISSING_MESSAGE},
                        {65, RuleId.MISSING_MESSAGE},
                        {70, RuleId.MISSING_SUBJECT, "expected comparison"},
                        {75, RuleId.MISSING_MESSAGE},
                        {80, RuleId.MISSING_MESSAGE},
                        {85, RuleId.MISSING_MESSAGE},
                        {91, RuleId.TRIVIAL_SUPPLIER, "index: {} is valid ..."},
                        {96, RuleId.TRIVIAL_SUPPLIER, "value is %d"},
                        {102, RuleId.MISSING_MESSAGE},
                        {107, RuleId.MISSING_MESSAGE},
                        {112, RuleId.TRIVIAL_SUPPLIER, "prefix: {} ..."},
                        {117, RuleId.TRIVIAL_SUPPLIER, "this string is definitely longer than twelve characters: {} ..."}
                }),
                arguments("DisplayNameMessages", "InputDisplayNameMessages.java", new Object[][]{
                        {18, RuleId.MISSING_DISPLAY_NAME, "testWithoutDisplayName"},
                        {29, RuleId.MISSING_DISPLAY_NAME, "parameterizedWithoutDisplayName"}
                }),
                arguments("TestAnnotationOrder", "InputTestAnnotationOrder.java", new Object[][]{
                        {16, RuleId.TEST_ANNOTATION_ORDER, "DisplayName", "testAnnotationOrderViolation"}
                }),
                arguments("JUnit4Migration", "InputJUnit4Migration.java", new Object[][]{
                        {11, RuleId.JUNIT4_ANNOTATION, "Before"},
                        {13, RuleId.JUNIT4_ASSERTION}
                }),
                arguments("JUnit4TestCommonIgnored", "InputJUnit4TestCommon.java", new Object[][]{}),
                arguments("GeneratedAtJavadocSkipped", "InputGeneratedAtJavadoc.java", new Object[][]{}),
                // Coverage-only input files - no expected violations
                arguments("JavadocEdgeCases", "InputJavadocEdgeCases.java", new Object[][]{}),
                arguments("AssertionRarePaths", "InputAssertionRarePaths.java", new Object[][]{}),
                arguments("MessageQualityGuideAfterExamples",
                        "InputMessageQualityGuideAfterExamples.java", new Object[][]{})
        );
    }

    @Override
    protected String getPackageLocation() {
        return "net/openhft/quality";
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("Test meaningful message scenario case detail")
    @MethodSource("provideStandardTestCases")
    void testMeaningfulMessage(String testName, String inputFile, Object[][] violations) throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(MeaningfulMessageCheck.class);
        checkConfig.addProperty("emitUnhandled", "false");

        final String[] expected = buildExpectedViolations(violations);
        verify(checkConfig, getPath(inputFile), expected);
    }

    private String[] buildExpectedViolations(Object[][] violations) {
        List<String> result = new ArrayList<>();
        for (Object[] v : violations) {
            int line = (Integer) v[0];
            RuleId rule = (RuleId) v[1];
            Object[] args = new Object[v.length - 2];
            System.arraycopy(v, 2, args, 0, args.length); // copy violation args for message
            if (rule == RuleId.MISSING_MESSAGE && args.length == 0) {
                args = new Object[]{FIX_MISSING_MESSAGE_DEFAULT};
            }
            result.add(line + ": " + getCheckMessage(rule.messageKey(), args));
        }
        return result.toArray(new String[0]);
    }

    @Test
    @DisplayName("Test message extraction file scenario case")
    public void testMessageExtractionFile() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(MeaningfulMessageCheck.class);
        Path output = Paths.get("target", "mm-extract-test.tsv");
        Files.deleteIfExists(output);
        checkConfig.addProperty("messageExtractionFile", output.toString());

        final String[] expected = {};

        verify(checkConfig, getPath("InputProcessorCoverage.java"), expected);

        assertTrue(Files.exists(output), "Extraction file missing: " + output);
        List<String> lines = Files.readAllLines(output, StandardCharsets.UTF_8);
        assertTrue(lines.size() >= 2, "Extraction file should contain header and data");
    }

    @Test
    @DisplayName("Test verbose property scenario case detail")
    public void testVerboseProperty() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(MeaningfulMessageCheck.class);
        checkConfig.addProperty("verbose", "true");

        final String[] expected = {
                "12: " + getCheckMessage(RuleId.REDUNDANT_CLASS.messageKey(),
                        "InputVerboseProperty should be created",
                        "InputVerboseProperty",
                        "only filler words remain: should, be, created "
                                + "Details: meaningful=[] filler=[should,be,created] "
                                + "removed=[InputVerboseProperty]")
        };

        verify(checkConfig, getPath("InputVerboseProperty.java"), expected);
    }

    @Test
    @DisplayName("Test unhandled cases logged by default")
    public void testUnhandledCasesLoggedByDefault() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(MeaningfulMessageCheck.class);

        final String[] expected = {
                "10: " + getCheckMessage(RuleId.UNHANDLED.messageKey(),
                        "Lambda message argument not recognised for assertTrue",
                        "InputUnhandledCases#unhandledLambdaMessage")
        };

        verify(checkConfig, getPath("InputUnhandledCases.java"), expected);
    }

    @Test
    @DisplayName("Test unhandled cases can be disabled")
    public void testUnhandledCasesCanBeDisabled() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(MeaningfulMessageCheck.class);
        checkConfig.addProperty("emitUnhandled", "false");

        final String[] expected = {};

        verify(checkConfig, getPath("InputUnhandledCases.java"), expected);
    }

    @Test
    @DisplayName("Test local helpers and supplier messages avoid unhandled warnings")
    public void testUnhandledCasesSkippedForLocalHelpers() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(MeaningfulMessageCheck.class);

        final String[] expected = {};

        verify(checkConfig, getPath("InputUnhandledSkipped.java"), expected);
    }

    @Test
    @DisplayName("Test extraction file failure is reported scenario")
    public void testExtractionFileFailureIsReported() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(MeaningfulMessageCheck.class);
        checkConfig.addProperty("messageExtractionFile", "target");

        final String[] expected = {
                "1: " + getCheckMessage("assert.message.extraction.failure",
                        "Unable to open message extraction file: target")
        };

        verify(checkConfig, getPath("InputGoodAssertMessages.java"), expected);
    }

    @Test
    @DisplayName("Test suppress warnings scenario case detail")
    public void testSuppressWarnings() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(MeaningfulMessageCheck.class);
        checkConfig.addProperty("id", "MeaningfulMessage");
        final DefaultConfiguration treeWalkerConfig =
                createModuleConfig(TreeWalker.class);
        final DefaultConfiguration suppressWarningsHolder =
                new DefaultConfiguration("SuppressWarningsHolder");
        suppressWarningsHolder.addProperty("aliasList",
                "net.openhft.quality.MeaningfulMessageCheck=MM-all");
        treeWalkerConfig.addChild(suppressWarningsHolder);
        treeWalkerConfig.addChild(checkConfig);

        final DefaultConfiguration rootConfig = createRootConfig(treeWalkerConfig);
        rootConfig.addChild(new DefaultConfiguration("SuppressWarningsFilter"));

        final String[] expected = {
                "16: " + getCheckMessage(RuleId.GENERIC.messageKey(), "expected"),
        };

        verify(rootConfig, getPath("InputSuppressWarnings.java"), expected);
    }

    @Test
    @DisplayName("Test ignored exception class names scenario")
    public void testIgnoredExceptionClassNames() throws Exception {
        final DefaultConfiguration checkConfig =
                createModuleConfig(MeaningfulMessageCheck.class);
        checkConfig.addProperty("ignoredExceptionClassNames",
                "IllegalArgumentException,UnsupportedOperationException");

        final String[] expected = {
                "21: " + getCheckMessage(RuleId.DUPLICATES_INPUT.messageKey(),
                        "IllegalStateException", "IllegalStateException"),
                "22: " + getCheckMessage(RuleId.TOO_SHORT.messageKey(),
                        "bad", 1, 2, 42, FIX_TOO_SHORT_THROW),
        };

        verify(checkConfig, getPath("InputAssertThrowsIgnoredExceptions.java"), expected);
    }

    @Test
    @DisplayName("Test rule code naming scenario case")
    public void testRuleCodeNaming() {
        final List<String> codes = new ArrayList<>();
        for (RuleId ruleId : RuleId.values()) {
            codes.add(ruleId.code());
        }

        assertFalse(codes.isEmpty(), "No rule codes found");
        final Set<String> uniqueCodes = new HashSet<>(codes);
        assertEquals(codes.size(), uniqueCodes.size(), "Duplicate rule codes detected");

        for (String code : codes) {
            assertTrue(code.matches("MM[A-Z][A-Za-z0-9]*"), "Rule code format invalid: " + code);
        }
    }
}
