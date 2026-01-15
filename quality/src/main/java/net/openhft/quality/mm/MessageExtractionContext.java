/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Holds per-file extraction state such as imports, variable types, and comments.
 */
public final class MessageExtractionContext {
    private final MessageAstSupport astSupport;
    private final Map<String, String> importedClasses = new HashMap<>();
    private final Map<String, String> fieldTypes = new HashMap<>();
    private final Map<String, String> methodVariableTypes = new HashMap<>();
    private final Map<String, MessageTemplate> fieldStringTemplates = new HashMap<>();
    private final Map<String, MessageTemplate> methodStringTemplates = new HashMap<>();
    private final Set<String> junit4StaticMethods = new HashSet<>();
    private final Set<String> junit5StaticMethods = new HashSet<>();
    private final Set<String> declaredMethodNames = new HashSet<>();
    private MessageTemplateExtractor templateExtractor;
    private FileContents fileContents;
    private boolean junit4StaticWildcard;
    private boolean junit5StaticWildcard;
    private boolean junit4ImportWildcard;
    private boolean junit5ImportWildcard;
    private boolean junit5ParamsImportWildcard;

    private Set<String> ignoredExceptionClassNames = java.util.Collections.emptySet();

    private String currentClassName;
    private String currentMethodName;
    private boolean currentMethodIsTest;
    private boolean currentMethodHasDisplayName;
    private int currentMethodLineNo;
    private String currentMethodFirstAnnotationName;
    private int currentMethodFirstAnnotationLine;
    private boolean currentMethodHasTestAnnotation;
    private int currentMethodTestAnnotationLine;
    private boolean junit4AnnotationUsage;
    private int junit4AnnotationLine;
    private String junit4AnnotationName;
    private boolean junit4AssertionUsage;
    private int junit4AssertionLine;

    /**
     * Create a context using the provided AST support helpers.
     *
     * @param astSupport AST helper utilities.
     */
    public MessageExtractionContext(MessageAstSupport astSupport) {
        this.astSupport = astSupport;
    }

    /**
     * Set the template extractor used for string parsing.
     *
     * @param templateExtractor template extractor to use.
     */
    public void setTemplateExtractor(MessageTemplateExtractor templateExtractor) {
        this.templateExtractor = templateExtractor;
    }

    /**
     * Return the current template extractor.
     *
     * @return template extractor, or {@code null} if not configured.
     */
    public MessageTemplateExtractor templateExtractor() {
        return templateExtractor;
    }

    /**
     * Return the AST support helper.
     *
     * @return AST support helper.
     */
    public MessageAstSupport astSupport() {
        return astSupport;
    }

    /**
     * Return the current file contents.
     *
     * @return file contents, or {@code null} if not initialised.
     */
    public FileContents fileContents() {
        return fileContents;
    }

    /**
     * Reset all per-file state and assign new file contents.
     *
     * @param fileContents file contents to associate with this context.
     */
    public void reset(FileContents fileContents) {
        this.fileContents = fileContents;
        importedClasses.clear();
        fieldTypes.clear();
        methodVariableTypes.clear();
        fieldStringTemplates.clear();
        methodStringTemplates.clear();
        junit4StaticMethods.clear();
        junit5StaticMethods.clear();
        junit4StaticWildcard = false;
        junit5StaticWildcard = false;
        junit4ImportWildcard = false;
        junit5ImportWildcard = false;
        junit5ParamsImportWildcard = false;
        declaredMethodNames.clear();
        currentClassName = null;
        currentMethodName = null;
        junit4AnnotationUsage = false;
        junit4AnnotationLine = 0;
        junit4AnnotationName = null;
        junit4AssertionUsage = false;
        junit4AssertionLine = 0;
    }

    /**
     * Record method names declared in the current file.
     *
     * @param methodNames method names declared in this file.
     */
    public void setDeclaredMethodNames(Set<String> methodNames) {
        declaredMethodNames.clear();
        if (methodNames != null && !methodNames.isEmpty()) {
            declaredMethodNames.addAll(methodNames);
        }
    }

    /**
     * Check whether a method name is declared in this file.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method name is declared.
     */
    public boolean isDeclaredMethodName(String methodName) {
        return methodName != null && declaredMethodNames.contains(methodName);
    }

    /**
     * Set the ignored exception class names for throw checks.
     *
     * @param ignoredExceptionClassNames class names to ignore.
     */
    public void setIgnoredExceptionClassNames(Set<String> ignoredExceptionClassNames) {
        if (ignoredExceptionClassNames == null || ignoredExceptionClassNames.isEmpty()) {
            this.ignoredExceptionClassNames = java.util.Collections.emptySet();
        } else {
            this.ignoredExceptionClassNames = new HashSet<>(ignoredExceptionClassNames);
        }
    }

    /**
     * Check whether a class name matches the ignored exception list.
     *
     * @param className class name to check.
     * @return {@code true} if the class name is ignored.
     */
    public boolean isIgnoredExceptionClass(String className) {
        if (className == null || className.isEmpty() || ignoredExceptionClassNames.isEmpty()) {
            return false;
        }
        String normalized = normalizeClassName(className);
        return normalized != null && ignoredExceptionClassNames.contains(normalized);
    }

    /**
     * Enter a type declaration and update the current class name.
     *
     * @param typeAst type definition AST node.
     */
    public void enterType(DetailAST typeAst) {
        currentClassName = astSupport.extractName(typeAst);
    }

    /**
     * Enter a method declaration and reset method-local variables.
     *
     * @param methodAst method definition AST node.
     */
    public void enterMethod(DetailAST methodAst) {
        currentMethodName = astSupport.extractName(methodAst);
        currentMethodLineNo = methodAst.getLineNo();
        currentMethodIsTest = false;
        currentMethodHasDisplayName = false;
        currentMethodFirstAnnotationName = null;
        currentMethodFirstAnnotationLine = 0;
        currentMethodHasTestAnnotation = false;
        currentMethodTestAnnotationLine = 0;
        methodVariableTypes.clear();
        methodStringTemplates.clear();
    }

    /**
     * Leave the current method and clear method-local state.
     */
    public void leaveMethod() {
        currentMethodName = null;
        currentMethodIsTest = false;
        currentMethodHasDisplayName = false;
        currentMethodLineNo = 0;
        currentMethodFirstAnnotationName = null;
        currentMethodFirstAnnotationLine = 0;
        currentMethodHasTestAnnotation = false;
        currentMethodTestAnnotationLine = 0;
        methodVariableTypes.clear();
        methodStringTemplates.clear();
    }

    /**
     * Mark the current method as a JUnit 5 test method.
     */
    public void markCurrentMethodAsTest() {
        currentMethodIsTest = true;
    }

    /**
     * Mark the current method as having a @DisplayName annotation.
     */
    public void markCurrentMethodHasDisplayName() {
        currentMethodHasDisplayName = true;
    }

    /**
     * Record an annotation applied to the current method.
     *
     * @param annotationName annotation simple name.
     * @param lineNo         annotation line number.
     */
    public void recordMethodAnnotation(String annotationName, int lineNo) {
        if (currentMethodName == null || annotationName == null) {
            return;
        }
        if (currentMethodFirstAnnotationName == null) {
            currentMethodFirstAnnotationName = annotationName;
            currentMethodFirstAnnotationLine = lineNo;
        }
        if (isJUnit5TestAnnotation(annotationName)) {
            currentMethodHasTestAnnotation = true;
            currentMethodTestAnnotationLine = lineNo;
        }
    }

    /**
     * Check whether the current method is a JUnit 5 test method.
     *
     * @return {@code true} if the current method is a test method.
     */
    public boolean isCurrentMethodTest() {
        return currentMethodIsTest;
    }

    /**
     * Check whether the current method has a @DisplayName annotation.
     *
     * @return {@code true} if the current method has @DisplayName.
     */
    public boolean currentMethodHasDisplayName() {
        return currentMethodHasDisplayName;
    }

    /**
     * Check whether the current method declares a JUnit 5 @Test annotation.
     *
     * @return {@code true} if the current method has @Test.
     */
    public boolean currentMethodHasTestAnnotation() {
        return currentMethodHasTestAnnotation;
    }

    /**
     * Return the first annotation name recorded for the current method.
     *
     * @return first annotation name, or {@code null} if none recorded.
     */
    public String currentMethodFirstAnnotationName() {
        return currentMethodFirstAnnotationName;
    }

    /**
     * Return the line number where the first annotation was recorded.
     *
     * @return line number, or 0 if unknown.
     */
    public int currentMethodFirstAnnotationLine() {
        return currentMethodFirstAnnotationLine;
    }

    /**
     * Return the line number where @Test was recorded for the current method.
     *
     * @return line number, or 0 if @Test not recorded.
     */
    public int currentMethodTestAnnotationLine() {
        return currentMethodTestAnnotationLine;
    }

    /**
     * Return the line number of the current method definition.
     *
     * @return current method line number, or 0 if not in a method.
     */
    public int currentMethodLineNo() {
        return currentMethodLineNo;
    }

    /**
     * Check whether the given annotation name is a JUnit 5 test annotation.
     * Verifies that the annotation is from org.junit.jupiter package, not JUnit 4.
     *
     * @param annotationName simple annotation name like "Test".
     * @return {@code true} if this is a JUnit 5 test annotation.
     */
    public boolean isJUnit5TestAnnotation(String annotationName) {
        if (annotationName == null) {
            return false;
        }
        // ParameterizedTest, RepeatedTest, TestFactory, TestTemplate are JUnit 5 only
        if ("ParameterizedTest".equals(annotationName)
                || "RepeatedTest".equals(annotationName)
                || "TestFactory".equals(annotationName)
                || "TestTemplate".equals(annotationName)) {
            return true;
        }
        // For @Test, need to distinguish JUnit 4 from JUnit 5
        if ("Test".equals(annotationName)) {
            String fullName = importedClasses.get("Test");
            // JUnit 5: org.junit.jupiter.api.Test
            // JUnit 4: org.junit.Test
            return fullName != null && fullName.startsWith("org.junit.jupiter");
        }
        return false;
    }

    /**
     * Check whether the annotation refers to JUnit 4 test lifecycle usage.
     *
     * @param annotationName simple annotation name like "Test".
     * @param fullName       fully qualified name, if resolved.
     * @return {@code true} if this is a JUnit 4 test annotation.
     */
    public boolean isJUnit4TestAnnotation(String annotationName, String fullName) {
        if (annotationName == null) {
            return false;
        }
        if (!"Test".equals(annotationName)
                && !"Before".equals(annotationName)
                && !"After".equals(annotationName)
                && !"ParameterizedTest".equals(annotationName)) {
            return false;
        }
        if (fullName != null && !fullName.isEmpty()) {
            return fullName.startsWith("org.junit.")
                    && !fullName.startsWith("org.junit.jupiter.");
        }
        if ("Test".equals(annotationName)) {
            if (junit5ImportWildcard) {
                return false;
            }
            return junit4ImportWildcard;
        }
        if ("ParameterizedTest".equals(annotationName)) {
            if (junit5ParamsImportWildcard) {
                return false;
            }
            return junit4ImportWildcard;
        }
        return junit4ImportWildcard;
    }

    /**
     * Return the current class name.
     *
     * @return current class name, or {@code null} if unknown.
     */
    public String currentClassName() {
        return currentClassName;
    }

    /**
     * Return the current method name.
     *
     * @return current method name, or {@code null} if unknown.
     */
    public String currentMethodName() {
        return currentMethodName;
    }

    /**
     * Record a JUnit 4 annotation usage for the current file.
     *
     * @param annotationName annotation name.
     * @param lineNo         annotation line number.
     */
    public void recordJUnit4AnnotationUsage(String annotationName, int lineNo) {
        if (isJUnit4MigrationIgnored()) {
            return;
        }
        if (!junit4AnnotationUsage) {
            junit4AnnotationUsage = true;
            junit4AnnotationLine = lineNo;
            junit4AnnotationName = annotationName;
        }
    }

    /**
     * Record a JUnit 4 assertion usage for the current file.
     *
     * @param lineNo assertion line number.
     */
    public void recordJUnit4AssertionUsage(int lineNo) {
        if (isJUnit4MigrationIgnored()) {
            return;
        }
        if (!junit4AssertionUsage) {
            junit4AssertionUsage = true;
            junit4AssertionLine = lineNo;
        }
    }

    /**
     * Return whether JUnit 4 test annotations were used in the file.
     *
     * @return {@code true} if any JUnit 4 test annotation was recorded.
     */
    public boolean hasJUnit4AnnotationUsage() {
        return junit4AnnotationUsage;
    }

    /**
     * Return the first JUnit 4 annotation line.
     *
     * @return line number, or 0 when unknown.
     */
    public int junit4AnnotationLine() {
        return junit4AnnotationLine;
    }

    /**
     * Return the first JUnit 4 annotation name.
     *
     * @return annotation name, or {@code null} when unknown.
     */
    public String junit4AnnotationName() {
        return junit4AnnotationName;
    }

    /**
     * Return whether JUnit 4 assertions were used in the file.
     *
     * @return {@code true} if any JUnit 4 assertion was recorded.
     */
    public boolean hasJUnit4AssertionUsage() {
        return junit4AssertionUsage;
    }

    /**
     * Return the first JUnit 4 assertion line.
     *
     * @return line number, or 0 when unknown.
     */
    public int junit4AssertionLine() {
        return junit4AssertionLine;
    }

    private boolean isJUnit4MigrationIgnored() {
        return currentClassName != null && currentClassName.endsWith("TestCommon");
    }

    /**
     * Record a regular import for name resolution.
     *
     * @param importAst import AST node.
     */
    public void recordImport(DetailAST importAst) {
        String importText = requireNonNull(astSupport.extractImportText(importAst));
        if (importText.endsWith(".*")) {
            if ("org.junit.*".equals(importText)) {
                junit4ImportWildcard = true;
            } else if ("org.junit.jupiter.api.*".equals(importText)) {
                junit5ImportWildcard = true;
            } else if ("org.junit.jupiter.params.*".equals(importText)) {
                junit5ParamsImportWildcard = true;
            }
            return;
        }
        int lastDot = importText.lastIndexOf('.');
        if (lastDot < 0 || lastDot == importText.length() - 1) {
            return;
        }
        String simpleName = importText.substring(lastDot + 1);
        importedClasses.put(simpleName, importText);
    }

    /**
     * Record static imports to resolve JUnit assertion methods.
     *
     * @param importAst import AST node.
     */
    public void recordStaticImport(DetailAST importAst) {
        String importText = requireNonNull(astSupport.extractImportText(importAst));
        recordStaticJUnitImport(importText, "org.junit.Assert", true);
        recordStaticJUnitImport(importText, "org.junit.Assume", true);
        recordStaticJUnitImport(importText, "org.junit.jupiter.api.Assertions", false);
        recordStaticJUnitImport(importText, "org.junit.jupiter.api.Assumptions", false);
    }

    /**
     * Record the declared type of a variable.
     *
     * @param varDef variable definition AST node.
     */
    public void recordVariableType(DetailAST varDef) {
        requireNonNull(varDef);
        DetailAST type = requireNonNull(varDef.findFirstToken(TokenTypes.TYPE));
        DetailAST ident = requireNonNull(varDef.findFirstToken(TokenTypes.IDENT));
        String typeName = astSupport.extractTypeName(type);
        if (typeName == null) {
            return;
        }
        String name = ident.getText();
        if (name == null || name.isEmpty()) {
            return;
        }
        boolean inMethodOrCtor = isInMethodOrCtor(varDef);
        if (inMethodOrCtor) {
            methodVariableTypes.put(name, typeName);
        } else {
            fieldTypes.put(name, typeName);
        }
        recordStringTemplate(varDef, name, typeName, inMethodOrCtor);
    }

    /**
     * Check whether a name resolves to a JUnit 4 static assertion method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is statically imported from JUnit 4.
     */
    public boolean isStaticJUnit4Method(String methodName) {
        return junit4StaticWildcard || junit4StaticMethods.contains(methodName);
    }

    /**
     * Check whether a name resolves to a JUnit 5 static assertion method.
     *
     * @param methodName method name to check.
     * @return {@code true} if the method is statically imported from JUnit 5.
     */
    public boolean isStaticJUnit5Method(String methodName) {
        return junit5StaticWildcard || junit5StaticMethods.contains(methodName);
    }

    /**
     * Return the fully qualified name for an imported simple name.
     *
     * @param name simple class name.
     * @return fully qualified import, or {@code null} if unknown.
     */
    public String importedClass(String name) {
        return importedClasses.get(name);
    }

    /**
     * Resolve a type name using recorded imports.
     *
     * @param typeName type name to resolve.
     * @return resolved fully qualified name, or the input if unchanged.
     */
    public String resolveTypeName(String typeName) {
        requireNonNull(typeName);
        if (typeName.contains(".")) {
            return typeName;
        }
        String imported = importedClasses.get(typeName);
        return imported != null ? imported : typeName;
    }

    private void recordStringTemplate(DetailAST varDef, String name, String typeName, boolean inMethodOrCtor) {
        if (templateExtractor == null) {
            return;
        }
        if (!isStringTypeName(typeName)) {
            return;
        }
        DetailAST assign = varDef.findFirstToken(TokenTypes.ASSIGN);
        if (assign == null) {
            return;
        }
        DetailAST expr = assign.getLastChild();
        if (expr == null) {
            return;
        }
        MessageTemplate template = templateExtractor.extractMessageTemplate(expr);
        if (template == null) {
            return;
        }
        if (inMethodOrCtor) {
            methodStringTemplates.put(name, template);
        } else {
            fieldStringTemplates.put(name, template);
        }
    }

    private MessageTemplate resolveStringTemplate(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        MessageTemplate template = methodStringTemplates.get(name);
        if (template != null) {
            return template;
        }
        return fieldStringTemplates.get(name);
    }

    /**
     * Resolve a message template from a string variable reference.
     *
     * @param expr expression node.
     * @return resolved message template, or {@code null} if not available.
     */
    public MessageTemplate resolveMessageTemplate(DetailAST expr) {
        if (expr == null) {
            return null;
        }
        DetailAST content = astSupport.unwrapExpr(expr);
        if (content == null) {
            return null;
        }
        if (content.getType() == TokenTypes.IDENT) {
            return resolveStringTemplate(content.getText());
        }
        if (content.getType() == TokenTypes.DOT) {
            DetailAST ident = astSupport.findRightmostIdent(content);
            return ident == null ? null : resolveStringTemplate(ident.getText());
        }
        return null;
    }

    /**
     * Extract a message template using variable templates and direct expression parsing.
     *
     * @param expr expression node.
     * @return extracted message template, or {@code null} if not available.
     */
    public MessageTemplate extractMessageTemplate(DetailAST expr) {
        MessageTemplate resolved = resolveMessageTemplate(expr);
        if (resolved != null) {
            return resolved;
        }
        if (templateExtractor == null) {
            return null;
        }
        return templateExtractor.extractMessageTemplate(expr);
    }

    /**
     * Look up the type of a variable by name.
     *
     * @param name variable name.
     * @return type name, or {@code null} if unknown.
     */
    public String getVariableType(String name) {
        String type = methodVariableTypes.get(name);
        if (type != null) {
            return type;
        }
        return fieldTypes.get(name);
    }

    /**
     * Determine whether an expression resolves to a Locale.
     *
     * @param expr expression to inspect.
     * @return {@code true} if the expression represents a Locale.
     */
    public boolean isLocaleExpression(DetailAST expr) {
        DetailAST content = requireNonNull(astSupport.unwrapExpr(expr));
        if (content.getType() == TokenTypes.IDENT) {
            return isLocaleTypeName(getVariableType(content.getText()));
        }
        if (content.getType() == TokenTypes.DOT) {
            String flattened = astSupport.flattenDot(content);
            if (flattened != null
                    && (flattened.startsWith("java.util.Locale.")
                    || flattened.startsWith("Locale."))) {
                return true;
            }
            DetailAST left = content.getFirstChild();
            if (left != null && left.getType() == TokenTypes.IDENT) {
                return isLocaleTypeName(getVariableType(left.getText()));
            }
            return false;
        }
        if (content.getType() == TokenTypes.LITERAL_NEW) {
            return isLocaleTypeName(astSupport.extractNewClassName(content));
        }
        if (content.getType() == TokenTypes.METHOD_CALL) {
            DetailAST dot = content.findFirstToken(TokenTypes.DOT);
            if (dot == null) {
                return false;
            }
            String qualifier = astSupport.extractQualifierIdent(dot);
            if (qualifier == null) {
                return false;
            }
            if (isLocaleTypeName(qualifier)) {
                return true;
            }
            String typeName = getVariableType(qualifier);
            return isLocaleTypeName(typeName);
        }
        return false;
    }

    /**
     * Check for a block comment with words inside the argument list range.
     *
     * @param nodeWithParens call or annotation node that contains parentheses.
     * @return {@code true} if a suitable inline reason comment is found.
     */
    public boolean hasInlineReasonComment(DetailAST nodeWithParens) {
        requireNonNull(nodeWithParens);
        requireNonNull(fileContents);
        int[] range = findArgumentListRange(nodeWithParens, fileContents);
        if (range == null) {
            return false;
        }
        int startLine = range[0];
        int startCol = range[1];
        int endLine = range[2];
        int endCol = range[3];
        Map<Integer, List<TextBlock>> blockComments = fileContents.getBlockComments();
        if (blockComments.isEmpty()) {
            return false;
        }

        for (List<TextBlock> commentBlocks : blockComments.values()) {
            for (TextBlock block : commentBlocks) {
                requireNonNull(block);
                if (intersectsRange(block, startLine, startCol, endLine, endCol)
                        && blockCommentHasWord(block)) {
                    return true;
                }
            }
        }
        return false;
    }

    void recordStaticJUnitImport(String importText, String prefix, boolean junit4) {
        if (!importText.startsWith(prefix + ".")) {
            return;
        }
        String suffix = importText.substring(prefix.length() + 1);
        if (suffix.equals("*")) {
            if (junit4) {
                junit4StaticWildcard = true;
            } else {
                junit5StaticWildcard = true;
            }
            return;
        }
        if (suffix.isEmpty()) {
            return;
        }
        if (junit4) {
            junit4StaticMethods.add(suffix);
        } else {
            junit5StaticMethods.add(suffix);
        }
    }

    boolean isInMethodOrCtor(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            int type = parent.getType();
            if (type == TokenTypes.METHOD_DEF || type == TokenTypes.CTOR_DEF) {
                return true;
            }
            if (type == TokenTypes.CLASS_DEF
                    || type == TokenTypes.INTERFACE_DEF
                    || type == TokenTypes.ENUM_DEF
                    || type == TokenTypes.OBJBLOCK) {
                return false;
            }
            parent = parent.getParent();
        }
        return false;
    }

    boolean isLocaleTypeName(String typeName) {
        if (typeName == null) {
            return false;
        }
        String resolved = resolveTypeName(typeName);
        return "java.util.Locale".equals(resolved) || "Locale".equals(resolved);
    }

    private boolean isStringTypeName(String typeName) {
        if (typeName == null) {
            return false;
        }
        String resolved = resolveTypeName(typeName);
        return "java.lang.String".equals(resolved) || "String".equals(resolved);
    }

    String normalizeClassName(String className) {
        requireNonNull(className);
        String trimmed = className.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        int lastDot = trimmed.lastIndexOf('.');
        return lastDot >= 0 ? trimmed.substring(lastDot + 1) : trimmed;
    }

    int[] findArgumentListRange(DetailAST nodeWithParens, FileContents contents) {
        DetailAST lparen = nodeWithParens.findFirstToken(TokenTypes.LPAREN);
        DetailAST rparen = nodeWithParens.findFirstToken(TokenTypes.RPAREN);
        if (lparen != null && rparen != null) {
            int startLine = lparen.getLineNo();
            int endLine = rparen.getLineNo();
            if (startLine <= 0 || endLine <= 0) {
                return null;
            }
            int startCol = Math.max(0, lparen.getColumnNo() + 1);
            int endCol = rparen.getColumnNo() - 1;
            if (startLine == endLine && startCol > endCol) {
                return null;
            }
            return new int[]{startLine, startCol, endLine, endCol};
        }
        return findArgumentListRangeByScan(nodeWithParens, contents);
    }

    int[] findArgumentListRangeByScan(DetailAST nodeWithParens, FileContents contents) {
        String[] lines = contents.getLines();
        if (lines.length == 0) {
            return null;
        }
        int scanLine = nodeWithParens.getLineNo();
        int scanCol = nodeWithParens.getColumnNo();
        if (nodeWithParens.getType() == TokenTypes.METHOD_CALL) {
            DetailAST dot = nodeWithParens.findFirstToken(TokenTypes.DOT);
            DetailAST ident = dot != null ? astSupport.findRightmostIdent(dot)
                    : nodeWithParens.findFirstToken(TokenTypes.IDENT);
            if (ident != null) {
                scanLine = ident.getLineNo();
                scanCol = ident.getColumnNo() + ident.getText().length();
            }
        } else if (nodeWithParens.getType() == TokenTypes.ANNOTATION) {
            DetailAST dot = nodeWithParens.findFirstToken(TokenTypes.DOT);
            DetailAST ident = dot != null ? astSupport.findRightmostIdent(dot)
                    : nodeWithParens.findFirstToken(TokenTypes.IDENT);
            if (ident != null) {
                scanLine = ident.getLineNo();
                scanCol = ident.getColumnNo() + ident.getText().length();
            }
        }
        if (scanLine <= 0 || scanLine > lines.length) {
            return null;
        }
        int startIndex = Math.max(0, scanCol);
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inChar = false;
        boolean foundOpen = false;
        int parenDepth = 0;
        int openLine = 0;
        int openCol = 0;

        for (int line = scanLine - 1; line < lines.length; line++) {
            String text = lines[line];
            requireNonNull(text);
            int index = line == scanLine - 1 ? Math.min(startIndex, text.length()) : 0;
            while (index < text.length()) {
                char current = text.charAt(index);
                char next = index + 1 < text.length() ? text.charAt(index + 1) : '\0';

                if (inBlockComment) {
                    if (current == '*' && next == '/') {
                        inBlockComment = false;
                        index += 2;
                        continue;
                    }
                    index++;
                    continue;
                }
                if (inString) {
                    if (current == '\\' && index + 1 < text.length()) {
                        index += 2;
                        continue;
                    }
                    if (current == '"') {
                        inString = false;
                    }
                    index++;
                    continue;
                }
                if (inChar) {
                    if (current == '\\' && index + 1 < text.length()) {
                        index += 2;
                        continue;
                    }
                    if (current == '\'') {
                        inChar = false;
                    }
                    index++;
                    continue;
                }

                if (current == '/' && next == '/') {
                    break;
                }
                if (current == '/' && next == '*') {
                    inBlockComment = true;
                    index += 2;
                    continue;
                }
                if (current == '"') {
                    inString = true;
                    index++;
                    continue;
                }
                if (current == '\'') {
                    inChar = true;
                    index++;
                    continue;
                }

                if (current == '(') {
                    if (!foundOpen) {
                        foundOpen = true;
                        parenDepth = 1;
                        openLine = line + 1;
                        openCol = index + 1;
                    } else {
                        parenDepth++;
                    }
                    index++;
                    continue;
                }
                if (current == ')') {
                    if (foundOpen) {
                        parenDepth--;
                        if (parenDepth == 0) {
                            int closeLine = line + 1;
                            int closeCol = index - 1;
                            if (openLine == closeLine && openCol > closeCol) {
                                return null;
                            }
                            return new int[]{openLine, openCol, closeLine, closeCol};
                        }
                    }
                    index++;
                    continue;
                }
                index++;
            }
        }
        return null;
    }

    private boolean intersectsRange(TextBlock block, int startLine, int startCol,
                                    int endLine, int endCol) {
        int blockStartLine = block.getStartLineNo();
        int blockStartCol = block.getStartColNo();
        int blockEndLine = block.getEndLineNo();
        int blockEndCol = block.getEndColNo();

        if (isBefore(blockEndLine, blockEndCol, startLine, startCol)) {
            return false;
        }
        return !isBefore(endLine, endCol, blockStartLine, blockStartCol);
    }

    boolean isBefore(int leftLine, int leftCol, int rightLine, int rightCol) {
        return leftLine < rightLine || (leftLine == rightLine && leftCol < rightCol);
    }

    boolean blockCommentHasWord(TextBlock block) {
        String[] text = block.getText();
        requireNonNull(text);
        for (String line : text) {
            requireNonNull(line);
            for (int i = 0; i < line.length(); i++) {
                if (Character.isLetter(line.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }
}
