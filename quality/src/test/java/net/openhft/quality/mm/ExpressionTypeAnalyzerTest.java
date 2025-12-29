/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link ExpressionTypeAnalyzer}.
 *
 * <p>Tests focus on the type name checking methods which have simple string-based logic.
 * Expression analysis methods require AST nodes and are covered by integration tests.
 */
public class ExpressionTypeAnalyzerTest {

    private MessageAstSupport astSupport;
    private MessageExtractionContext context;
    private ExpressionTypeAnalyzer analyzer;

    @Before
    public void setUp() {
        astSupport = new MessageAstSupport();
        context = new MessageExtractionContext(astSupport);
        analyzer = new ExpressionTypeAnalyzer(context);
    }

    @Test
    public void isStringTypeName_String_returnsTrue() {
        assertTrue(analyzer.isStringTypeName("String"));
    }

    @Test
    public void isStringTypeName_javaLangString_returnsTrue() {
        assertTrue(analyzer.isStringTypeName("java.lang.String"));
    }

    @Test
    public void isStringTypeName_null_returnsFalse() {
        assertFalse(analyzer.isStringTypeName(null));
    }

    @Test
    public void isStringTypeName_Integer_returnsFalse() {
        assertFalse(analyzer.isStringTypeName("Integer"));
    }

    @Test
    public void isStringTypeName_javaLangInteger_returnsFalse() {
        assertFalse(analyzer.isStringTypeName("java.lang.Integer"));
    }

    @Test
    public void isStringTypeName_emptyString_returnsFalse() {
        assertFalse(analyzer.isStringTypeName(""));
    }

    @Test
    public void isSupplierTypeName_Supplier_returnsTrue() {
        assertTrue(analyzer.isSupplierTypeName("Supplier"));
    }

    @Test
    public void isSupplierTypeName_javaUtilFunctionSupplier_returnsTrue() {
        assertTrue(analyzer.isSupplierTypeName("java.util.function.Supplier"));
    }

    @Test
    public void isSupplierTypeName_customPackageSupplier_returnsTrue() {
        assertTrue(analyzer.isSupplierTypeName("com.example.Supplier"));
    }

    @Test
    public void isSupplierTypeName_null_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName(null));
    }

    @Test
    public void isSupplierTypeName_String_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName("String"));
    }

    @Test
    public void isSupplierTypeName_emptyString_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName(""));
    }

    @Test
    public void isSupplierTypeName_SupplierPrefix_returnsFalse() {
        assertFalse(analyzer.isSupplierTypeName("SupplierFactory"));
    }
}
