/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link MessageExtractionContext}.
 */
public class MessageExtractionContextTest {

    private MessageExtractionContext context;

    @BeforeEach
    void setUp() {
        context = new MessageExtractionContext(new MessageAstSupport());
    }

    // --- Basic accessors ---

    @Test
    void astSupportNotNull() {
        assertNotNull(context.astSupport());
    }

    @Test
    void fileContentsNullInitially() {
        assertNull(context.fileContents());
    }

    @Test
    void templateExtractorNullInitially() {
        assertNull(context.templateExtractor());
    }

    @Test
    void setTemplateExtractor() {
        MessageTemplateExtractor extractor = new MessageTemplateExtractor(expr -> false);
        context.setTemplateExtractor(extractor);
        assertEquals(extractor, context.templateExtractor());
    }

    @Test
    void currentClassNameNullInitially() {
        assertNull(context.currentClassName());
    }

    @Test
    void currentMethodNameNullInitially() {
        assertNull(context.currentMethodName());
    }

    // --- reset ---

    @Test
    void resetClearsState() {
        context.setTemplateExtractor(new MessageTemplateExtractor(expr -> false));
        context.reset(null);
        assertNull(context.fileContents());
        assertNull(context.currentClassName());
        assertNull(context.currentMethodName());
    }

    // --- setIgnoredExceptionClassNames ---

    @Test
    void setIgnoredExceptionClassNamesNull() {
        context.setIgnoredExceptionClassNames(null);
        assertFalse(context.isIgnoredExceptionClass("IOException"));
    }

    @Test
    void setIgnoredExceptionClassNamesEmpty() {
        context.setIgnoredExceptionClassNames(Collections.emptySet());
        assertFalse(context.isIgnoredExceptionClass("IOException"));
    }

    @Test
    void setIgnoredExceptionClassNamesWithValues() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        ignored.add("RuntimeException");
        context.setIgnoredExceptionClassNames(ignored);

        assertTrue(context.isIgnoredExceptionClass("IOException"));
        assertTrue(context.isIgnoredExceptionClass("RuntimeException"));
        assertFalse(context.isIgnoredExceptionClass("IllegalStateException"));
    }

    @Test
    void setIgnoredExceptionClassNamesNormalisesFullyQualified() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        context.setIgnoredExceptionClassNames(ignored);

        // Fully qualified name should be normalised
        assertTrue(context.isIgnoredExceptionClass("java.io.IOException"));
    }

    // --- isIgnoredExceptionClass edge cases ---

    @Test
    void isIgnoredExceptionClassNull() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        context.setIgnoredExceptionClassNames(ignored);

        assertFalse(context.isIgnoredExceptionClass(null));
    }

    @Test
    void isIgnoredExceptionClassEmpty() {
        Set<String> ignored = new HashSet<>();
        ignored.add("IOException");
        context.setIgnoredExceptionClassNames(ignored);

        assertFalse(context.isIgnoredExceptionClass(""));
    }

    // --- JUnit static method detection ---

    @Test
    void isStaticJUnit4MethodFalseInitially() {
        assertFalse(context.isStaticJUnit4Method("assertEquals"));
    }

    @Test
    void isStaticJUnit5MethodFalseInitially() {
        assertFalse(context.isStaticJUnit5Method("assertEquals"));
    }

    // --- resolveTypeName ---

    @Test
    void resolveTypeNameSimple() {
        assertEquals("String", context.resolveTypeName("String"));
    }

    @Test
    void resolveTypeNameFullyQualified() {
        assertEquals("java.lang.String", context.resolveTypeName("java.lang.String"));
    }

    // --- getVariableType ---

    @Test
    void getVariableTypeUnknown() {
        assertNull(context.getVariableType("unknown"));
    }

    // --- importedClass ---

    @Test
    void importedClassUnknown() {
        assertNull(context.importedClass("Unknown"));
    }

    // --- leaveMethod ---

    @Test
    void leaveMethodClearsMethodName() {
        context.leaveMethod();
        assertNull(context.currentMethodName());
    }
}
