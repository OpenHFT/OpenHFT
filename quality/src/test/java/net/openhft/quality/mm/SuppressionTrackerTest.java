/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SuppressionTracker}.
 */
@SuppressWarnings("MMDisplayName")
class SuppressionTrackerTest {

    private SuppressionTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new SuppressionTracker();
    }

    @Test
    void constructorCreatesEmptyTracker() {
        assertNotNull(tracker, "tracker should not be null");
    }

    @Test
    void isSuppressed_emptyScope_returnsFalse() {
        assertFalse(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should return false when no scope entered");
    }

    @Test
    void isSuppressed_nullRuleId_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> tracker.isSuppressed(null),
                "should throw NPE for null ruleId");
    }

    @Test
    void leaveScope_emptyStack_doesNotThrow() {
        // Should not throw even with empty stack
        tracker.leaveScope();
        assertFalse(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should still return false after leaveScope on empty stack");
    }

    // --- Reflection tests for private methods ---

    @Test
    void stripQuotes_normalString() throws Exception {
        Method method = SuppressionTracker.class
                .getDeclaredMethod("stripQuotes", String.class);
        method.setAccessible(true);

        assertEquals("test", method.invoke(tracker, "\"test\""),
                "should strip surrounding quotes");
    }

    @Test
    void stripQuotes_noQuotes() throws Exception {
        Method method = SuppressionTracker.class
                .getDeclaredMethod("stripQuotes", String.class);
        method.setAccessible(true);

        assertEquals("test", method.invoke(tracker, "test"),
                "should return unchanged if no quotes");
    }

    @Test
    void stripQuotes_emptyQuotes() throws Exception {
        Method method = SuppressionTracker.class
                .getDeclaredMethod("stripQuotes", String.class);
        method.setAccessible(true);

        assertEquals("", method.invoke(tracker, "\"\""),
                "should return empty string for empty quotes");
    }

    @Test
    void stripQuotes_singleChar() throws Exception {
        Method method = SuppressionTracker.class
                .getDeclaredMethod("stripQuotes", String.class);
        method.setAccessible(true);

        assertEquals("x", method.invoke(tracker, "x"),
                "should return unchanged for single char");
    }

    // --- SuppressionScope inner class tests via reflection ---

    @Test
    void suppressionScope_addToken_mmAll_setsFlag() throws Exception {
        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);

        addToken.invoke(scope, "MM-all");

        Field suppressAll = scope.getClass().getDeclaredField("suppressAll");
        suppressAll.setAccessible(true);
        assertTrue((Boolean) suppressAll.get(scope),
                "MM-all should set suppressAll flag");
    }

    @Test
    void suppressionScope_addToken_meaningfulMessage_setsFlag() throws Exception {
        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);

        addToken.invoke(scope, "MeaningfulMessage");

        Field suppressAll = scope.getClass().getDeclaredField("suppressAll");
        suppressAll.setAccessible(true);
        assertTrue((Boolean) suppressAll.get(scope),
                "MeaningfulMessage should set suppressAll flag");
    }

    @Test
    void suppressionScope_addToken_meaningfulMessageCheck_setsFlag() throws Exception {
        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);

        addToken.invoke(scope, "MeaningfulMessageCheck");

        Field suppressAll = scope.getClass().getDeclaredField("suppressAll");
        suppressAll.setAccessible(true);
        assertTrue((Boolean) suppressAll.get(scope),
                "MeaningfulMessageCheck should set suppressAll flag");
    }

    @Test
    void suppressionScope_addToken_knownCode_addsToSet() throws Exception {
        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);

        addToken.invoke(scope, "MMTooShort");

        Field suppressedCodes = scope.getClass().getDeclaredField("suppressedCodes");
        suppressedCodes.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> codes = (Set<String>) suppressedCodes.get(scope);
        assertTrue(codes.contains("MMTooShort"),
                "known code should be added to suppressedCodes");
    }

    @Test
    void suppressionScope_addToken_unknownCode_notAdded() throws Exception {
        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);

        addToken.invoke(scope, "UnknownCode");

        Field suppressedCodes = scope.getClass().getDeclaredField("suppressedCodes");
        suppressedCodes.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> codes = (Set<String>) suppressedCodes.get(scope);
        assertFalse(codes.contains("UnknownCode"),
                "unknown code should not be added");
    }

    @Test
    void suppressionScope_addToken_emptyString_notAdded() throws Exception {
        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);

        addToken.invoke(scope, "");

        Field suppressedCodes = scope.getClass().getDeclaredField("suppressedCodes");
        suppressedCodes.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> codes = (Set<String>) suppressedCodes.get(scope);
        assertTrue(codes.isEmpty(),
                "empty string should not be added");
    }

    @Test
    void suppressionScope_addToken_checkstylePrefix_stripped() throws Exception {
        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);

        addToken.invoke(scope, "checkstyle:MMTooShort");

        Field suppressedCodes = scope.getClass().getDeclaredField("suppressedCodes");
        suppressedCodes.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> codes = (Set<String>) suppressedCodes.get(scope);
        assertTrue(codes.contains("MMTooShort"),
                "checkstyle: prefix should be stripped");
    }

    @Test
    void suppressionScope_addToken_whitespace_trimmed() throws Exception {
        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);

        addToken.invoke(scope, "  MMTooShort  ");

        Field suppressedCodes = scope.getClass().getDeclaredField("suppressedCodes");
        suppressedCodes.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> codes = (Set<String>) suppressedCodes.get(scope);
        assertTrue(codes.contains("MMTooShort"),
                "whitespace should be trimmed");
    }

    @Test
    void suppressionScope_copyConstructor_inheritsCodes() throws Exception {
        Object parentScope = createSuppressionScope();
        Method addToken = parentScope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);
        addToken.invoke(parentScope, "MMTooShort");

        Object childScope = createSuppressionScopeWithParent(parentScope);

        Field suppressedCodes = childScope.getClass().getDeclaredField("suppressedCodes");
        suppressedCodes.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<String> codes = (Set<String>) suppressedCodes.get(childScope);
        assertTrue(codes.contains("MMTooShort"),
                "child scope should inherit parent codes");
    }

    @Test
    void suppressionScope_copyConstructor_inheritsSuppressAll() throws Exception {
        Object parentScope = createSuppressionScope();
        Method addToken = parentScope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);
        addToken.invoke(parentScope, "MM-all");

        Object childScope = createSuppressionScopeWithParent(parentScope);

        Field suppressAll = childScope.getClass().getDeclaredField("suppressAll");
        suppressAll.setAccessible(true);
        assertTrue((Boolean) suppressAll.get(childScope),
                "child scope should inherit suppressAll flag");
    }

    @Test
    void cleanToken_stripsCheckstylePrefix() throws Exception {
        Class<?> scopeClass = getScopeClass();
        Object scope = createSuppressionScope();
        Method cleanToken = scopeClass.getDeclaredMethod("cleanToken", String.class);
        cleanToken.setAccessible(true);

        assertEquals("MMTooShort", cleanToken.invoke(scope, "checkstyle:MMTooShort"),
                "should strip checkstyle: prefix");
    }

    @Test
    void cleanToken_trimsWhitespace() throws Exception {
        Class<?> scopeClass = getScopeClass();
        Object scope = createSuppressionScope();
        Method cleanToken = scopeClass.getDeclaredMethod("cleanToken", String.class);
        cleanToken.setAccessible(true);

        assertEquals("MMTooShort", cleanToken.invoke(scope, "  MMTooShort  "),
                "should trim whitespace");
    }

    @Test
    void cleanToken_preservesNonPrefixed() throws Exception {
        Class<?> scopeClass = getScopeClass();
        Object scope = createSuppressionScope();
        Method cleanToken = scopeClass.getDeclaredMethod("cleanToken", String.class);
        cleanToken.setAccessible(true);

        assertEquals("MMTooShort", cleanToken.invoke(scope, "MMTooShort"),
                "should preserve non-prefixed token");
    }

    // --- Test isSuppressed with manually manipulated scope stack ---

    @Test
    void isSuppressed_withSuppressAll_returnsTrue() throws Exception {
        // Access the scopes field and add a scope with suppressAll = true
        Field scopesField = SuppressionTracker.class.getDeclaredField("scopes");
        scopesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Deque<Object> scopes = (Deque<Object>) scopesField.get(tracker);

        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);
        addToken.invoke(scope, "MM-all");

        scopes.push(scope);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should return true when suppressAll is set");
        assertTrue(tracker.isSuppressed(RuleId.TOO_LONG),
                "should return true for any rule when suppressAll is set");
    }

    @Test
    void isSuppressed_withSpecificCode_returnsTrueForMatch() throws Exception {
        Field scopesField = SuppressionTracker.class.getDeclaredField("scopes");
        scopesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Deque<Object> scopes = (Deque<Object>) scopesField.get(tracker);

        Object scope = createSuppressionScope();
        Method addToken = scope.getClass().getDeclaredMethod("addToken", String.class);
        addToken.setAccessible(true);
        addToken.invoke(scope, "MMTooShort");

        scopes.push(scope);

        assertTrue(tracker.isSuppressed(RuleId.TOO_SHORT),
                "should return true for suppressed rule");
        assertFalse(tracker.isSuppressed(RuleId.TOO_LONG),
                "should return false for non-suppressed rule");
    }

    // --- Helper methods ---

    private Object createSuppressionScope() throws Exception {
        Class<?> scopeClass = getScopeClass();
        Constructor<?> constructor = scopeClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private Object createSuppressionScopeWithParent(Object parent) throws Exception {
        Class<?> scopeClass = getScopeClass();
        Constructor<?> constructor = scopeClass.getDeclaredConstructor(scopeClass);
        constructor.setAccessible(true);
        return constructor.newInstance(parent);
    }

    private Class<?> getScopeClass() {
        for (Class<?> innerClass : SuppressionTracker.class.getDeclaredClasses()) {
            if (innerClass.getSimpleName().equals("SuppressionScope")) {
                return innerClass;
            }
        }
        throw new IllegalStateException("SuppressionScope inner class not found");
    }
}
