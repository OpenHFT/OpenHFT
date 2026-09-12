/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Tracks @SuppressWarnings tokens and comment directives for MeaningfulMessage rule suppression.
 */
public class SuppressionTracker {

    private static final String SUPPRESS_WARNINGS = "SuppressWarnings";
    private static final String CHECKSTYLE_PREFIX = "checkstyle:";
    private static final String ALL_TOKEN = "MM-all";
    private static final String CHECK_ID = "MeaningfulMessage";
    private static final String CHECK_CLASS = "MeaningfulMessageCheck";
    private static final Pattern COMMENT_SUPPRESSION =
            Pattern.compile("^\\s*(//|#)\\s*(.+)\\s*:\\s*(OFF|ON)\\b.*$", Pattern.CASE_INSENSITIVE);
    private static final int NO_LINE = -1;

    private final Deque<SuppressionScope> scopes = new ArrayDeque<>();
    private final SuppressionScope fileScope = new SuppressionScope();
    private final Set<RuleId> legacySuppressedRuleIds = EnumSet.noneOf(RuleId.class);
    private final List<LineRange> commentSuppressAll = new ArrayList<>();
    private final Map<RuleId, List<LineRange>> commentSuppressedRules = new EnumMap<>(RuleId.class);
    private final Map<AdviceId, List<LineRange>> commentSuppressedAdviceIds = new EnumMap<>(AdviceId.class);

    /**
     * Create a suppression tracker.
     */
    public SuppressionTracker() {
    }

    /**
     * Enter a new suppression scope based on a type or method definition.
     *
     * @param scopeAst AST node that defines the scope.
     */
    public void enterScope(DetailAST scopeAst) {
        SuppressionScope parent = scopes.peek();
        SuppressionScope current = parent == null
                ? new SuppressionScope()
                : new SuppressionScope(parent);
        for (String token : extractSuppressWarnings(scopeAst)) {
            current.addToken(token);
        }
        scopes.push(current);
    }

    /**
     * Record suppressions declared on a top-level type for file-level checks.
     *
     * @param scopeAst AST node that defines the top-level type.
     */
    public void recordFileSuppressions(DetailAST scopeAst) {
        for (String token : extractSuppressWarnings(scopeAst)) {
            fileScope.addToken(token);
        }
    }

    /**
     * Record comment-based suppression ranges for this file.
     *
     * @param fileContents file contents for the current file.
     */
    public void recordCommentSuppressions(FileContents fileContents) {
        commentSuppressAll.clear();
        commentSuppressedRules.clear();
        commentSuppressedAdviceIds.clear();
        if (fileContents == null) {
            return;
        }
        String[] lines = fileContents.getLines();
        if (lines == null || lines.length == 0) {
            return;
        }
        int openAll = NO_LINE;
        Map<RuleId, Integer> openRules = new EnumMap<>(RuleId.class);
        Map<AdviceId, Integer> openAdviceIds = new EnumMap<>(AdviceId.class);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line == null) {
                continue;
            }
            Matcher matcher = COMMENT_SUPPRESSION.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            String token = matcher.group(2);
            if (token == null) {
                continue;
            }
            String cleaned = cleanToken(token);
            if (cleaned.isEmpty()) {
                continue;
            }
            boolean off = "OFF".equalsIgnoreCase(matcher.group(3));
            int lineNo = i + 1;
            if (ALL_TOKEN.equals(cleaned) || CHECK_ID.equals(cleaned) || CHECK_CLASS.equals(cleaned)) {
                if (off) {
                    if (openAll == NO_LINE) {
                        openAll = lineNo;
                    }
                } else if (openAll != NO_LINE) {
                    commentSuppressAll.add(new LineRange(openAll, lineNo));
                    openAll = NO_LINE;
                }
                continue;
            }
            AdviceId adviceId = AdviceId.forName(cleaned);
            if (adviceId != AdviceId.UNKNOWN) {
                if (off) {
                    openAdviceIds.putIfAbsent(adviceId, lineNo);
                } else {
                    Integer start = openAdviceIds.remove(adviceId);
                    if (start != null) {
                        addAdviceRange(adviceId, start, lineNo);
                    }
                }
                continue;
            }
            RuleId ruleId = RuleRegistry.forCode(cleaned);
            if (ruleId != null) {
                legacySuppressedRuleIds.add(ruleId);
                if (off) {
                    openRules.putIfAbsent(ruleId, lineNo);
                } else {
                    Integer start = openRules.remove(ruleId);
                    if (start != null) {
                        addRuleRange(ruleId, start, lineNo);
                    }
                }
            }
        }
        int lastLine = lines.length;
        // Close any still-open suppression ranges at EOF: the loops below intentionally
        // close every open range so that a suppression started without a matching ON
        // directive still covers the rest of the file.
        if (openAll != NO_LINE) {
            commentSuppressAll.add(new LineRange(openAll, lastLine));
        }
        for (Map.Entry<RuleId, Integer> entry : openRules.entrySet()) {
            addRuleRange(entry.getKey(), entry.getValue(), lastLine);
        }
        for (Map.Entry<AdviceId, Integer> entry : openAdviceIds.entrySet()) {
            addAdviceRange(entry.getKey(), entry.getValue(), lastLine);
        }
    }

    /**
     * Leave the current suppression scope.
     */
    public void leaveScope() {
        if (scopes.isEmpty()) {
            throw new IllegalStateException("leaveScope without matching enterScope");
        }
        scopes.pop();
    }

    /**
     * Check whether a rule is suppressed in the current scope.
     *
     * @param ruleId rule identifier to check.
     * @return {@code true} if the rule is suppressed.
     */
    public boolean isSuppressed(RuleId ruleId) {
        requireNonNull(ruleId);
        SuppressionScope scope = scopes.peek();
        if (scope == null) {
            return false;
        }
        if (scope.suppressAll) {
            return true;
        }
        if (scope.suppressedRules.contains(ruleId)) {
            return true;
        }
        // Also check if any suppressed AdviceId has this RuleId
        for (AdviceId adviceId : scope.suppressedAdviceIds) {
            if (adviceId.ruleId() == ruleId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether a rule is suppressed for the given line.
     *
     * @param ruleId rule identifier to check.
     * @param lineNo line number to check.
     * @return {@code true} if the rule is suppressed.
     */
    public boolean isSuppressed(RuleId ruleId, int lineNo) {
        if (isSuppressed(ruleId)) {
            return true;
        }
        return isCommentSuppressed(ruleId, lineNo);
    }

    /**
     * Check whether a rule is suppressed at the file level.
     *
     * @param ruleId rule identifier to check.
     * @return {@code true} if the rule is suppressed for the file.
     */
    public boolean isSuppressedInFile(RuleId ruleId) {
        requireNonNull(ruleId);
        if (fileScope.suppressAll) {
            return true;
        }
        if (fileScope.suppressedRules.contains(ruleId)) {
            return true;
        }
        // Also check if any suppressed AdviceId has this RuleId
        for (AdviceId adviceId : fileScope.suppressedAdviceIds) {
            if (adviceId.ruleId() == ruleId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether a rule is suppressed at the file level for the given line.
     *
     * @param ruleId rule identifier to check.
     * @param lineNo line number to check.
     * @return {@code true} if the rule is suppressed for the file.
     */
    public boolean isSuppressedInFile(RuleId ruleId, int lineNo) {
        if (isSuppressedInFile(ruleId)) {
            return true;
        }
        return isCommentSuppressed(ruleId, lineNo);
    }

    /**
     * Check whether an advice identifier is suppressed in the current scope.
     *
     * @param adviceId advice identifier to check.
     * @return {@code true} if the advice is suppressed.
     */
    public boolean isSuppressed(AdviceId adviceId) {
        requireNonNull(adviceId);
        SuppressionScope scope = scopes.peek();
        if (scope == null) {
            return false;
        }
        if (scope.suppressAll) {
            return true;
        }
        return scope.suppressedAdviceIds.contains(adviceId);
    }

    /**
     * Check whether an advice identifier is suppressed for the given line.
     *
     * @param adviceId advice identifier to check.
     * @param lineNo   line number to check.
     * @return {@code true} if the advice is suppressed.
     */
    public boolean isSuppressed(AdviceId adviceId, int lineNo) {
        if (isSuppressed(adviceId)) {
            return true;
        }
        return isCommentSuppressed(adviceId, lineNo);
    }

    /**
     * Check whether an advice identifier is suppressed at the file level.
     *
     * @param adviceId advice identifier to check.
     * @return {@code true} if the advice is suppressed for the file.
     */
    public boolean isSuppressedInFile(AdviceId adviceId) {
        requireNonNull(adviceId);
        if (fileScope.suppressAll) {
            return true;
        }
        return fileScope.suppressedAdviceIds.contains(adviceId);
    }

    /**
     * Check whether an advice identifier is suppressed at the file level for the given line.
     *
     * @param adviceId advice identifier to check.
     * @param lineNo   line number to check.
     * @return {@code true} if the advice is suppressed for the file.
     */
    public boolean isSuppressedInFile(AdviceId adviceId, int lineNo) {
        if (isSuppressedInFile(adviceId)) {
            return true;
        }
        return isCommentSuppressed(adviceId, lineNo);
    }

    /**
     * Return any legacy RuleId suppressions encountered in the file.
     *
     * @return unmodifiable set of legacy suppressed rules.
     */
    public Set<RuleId> legacySuppressedRuleIds() {
        return Collections.unmodifiableSet(legacySuppressedRuleIds);
    }

    private List<String> extractSuppressWarnings(DetailAST scopeAst) {
        requireNonNull(scopeAst);
        DetailAST modifiers = scopeAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return java.util.Collections.emptyList();
        }
        List<String> tokens = new ArrayList<>();
        DetailAST child = modifiers.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.ANNOTATION) {
                String name = extractAnnotationName(child);
                if (name != null && name.endsWith(SUPPRESS_WARNINGS)) {
                    DetailAST valueExpr = findAnnotationValue(child);
                    if (valueExpr != null) {
                        collectStringValues(valueExpr, tokens);
                    }
                }
            }
            child = child.getNextSibling();
        }
        return tokens;
    }

    void collectStringValues(DetailAST expr, List<String> tokens) {
        DetailAST content = unwrapExpr(expr);
        requireNonNull(content);
        if (content.getType() == TokenTypes.EXPR) {
            DetailAST child = content.getFirstChild();
            while (child != null) {
                collectStringValues(child, tokens);
                child = child.getNextSibling();
            }
            return;
        }
        if (content.getType() == TokenTypes.STRING_LITERAL) {
            String literal = stripQuotes(content.getText());
            if (literal != null) {
                tokens.add(literal);
            }
            return;
        }
        if (content.getType() == TokenTypes.ARRAY_INIT
                || content.getType() == TokenTypes.ANNOTATION_ARRAY_INIT) {
            DetailAST child = content.getFirstChild();
            while (child != null) {
                collectStringValues(child, tokens);
                child = child.getNextSibling();
            }
        }
    }

    DetailAST unwrapExpr(DetailAST expr) {
        requireNonNull(expr);
        if (expr.getType() == TokenTypes.EXPR && expr.getChildCount() == 1) {
            return expr.getFirstChild();
        }
        return expr;
    }

    DetailAST findAnnotationValue(DetailAST annotationAst) {
        DetailAST child = annotationAst.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
                DetailAST ident = child.findFirstToken(TokenTypes.IDENT);
                if (ident != null && "value".equals(ident.getText())) {
                    DetailAST expr = child.findFirstToken(TokenTypes.EXPR);
                    if (expr != null) {
                        return expr;
                    }
                    DetailAST arrayInit = child.findFirstToken(TokenTypes.ANNOTATION_ARRAY_INIT);
                    if (arrayInit != null) {
                        return arrayInit;
                    }
                    DetailAST literal = child.findFirstToken(TokenTypes.STRING_LITERAL);
                    if (literal != null) {
                        return literal;
                    }
                }
            } else if (child.getType() == TokenTypes.EXPR
                    || child.getType() == TokenTypes.ANNOTATION_ARRAY_INIT
                    || child.getType() == TokenTypes.ARRAY_INIT
                    || child.getType() == TokenTypes.STRING_LITERAL) {
                return child;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private void addRuleRange(RuleId ruleId, int startLine, int endLine) {
        commentSuppressedRules
                .computeIfAbsent(ruleId, key -> new ArrayList<>())
                .add(new LineRange(startLine, endLine));
    }

    private void addAdviceRange(AdviceId adviceId, int startLine, int endLine) {
        commentSuppressedAdviceIds
                .computeIfAbsent(adviceId, key -> new ArrayList<>())
                .add(new LineRange(startLine, endLine));
    }

    private boolean isCommentSuppressed(RuleId ruleId, int lineNo) {
        requireNonNull(ruleId);
        if (lineNo <= 0) {
            return false;
        }
        if (isLineSuppressed(commentSuppressAll, lineNo)) {
            return true;
        }
        if (isLineSuppressed(commentSuppressedRules.get(ruleId), lineNo)) {
            return true;
        }
        for (Map.Entry<AdviceId, List<LineRange>> entry : commentSuppressedAdviceIds.entrySet()) {
            AdviceId adviceId = entry.getKey();
            if (adviceId.ruleId() == ruleId && isLineSuppressed(entry.getValue(), lineNo)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCommentSuppressed(AdviceId adviceId, int lineNo) {
        requireNonNull(adviceId);
        if (lineNo <= 0) {
            return false;
        }
        if (isLineSuppressed(commentSuppressAll, lineNo)) {
            return true;
        }
        return isLineSuppressed(commentSuppressedAdviceIds.get(adviceId), lineNo);
    }

    private boolean isLineSuppressed(List<LineRange> ranges, int lineNo) {
        if (ranges == null || ranges.isEmpty()) {
            return false;
        }
        for (LineRange range : ranges) {
            if (range.contains(lineNo)) {
                return true;
            }
        }
        return false;
    }

    private String extractAnnotationName(DetailAST annotationAst) {
        DetailAST dot = annotationAst.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            DetailAST ident = findRightmostIdent(dot);
            if (ident != null) {
                return ident.getText();
            }
        }
        DetailAST ident = annotationAst.findFirstToken(TokenTypes.IDENT);
        return ident != null ? ident.getText() : null;
    }

    private DetailAST findRightmostIdent(DetailAST dot) {
        DetailAST current = dot;
        while (current != null && current.getType() == TokenTypes.DOT) {
            current = current.getLastChild();
        }
        return current != null && current.getType() == TokenTypes.IDENT ? current : null;
    }

    String stripQuotes(String text) {
        requireNonNull(text);
        if (text.length() >= 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    private String cleanToken(String token) {
        String trimmed = token.trim();
        if (trimmed.startsWith(CHECKSTYLE_PREFIX)) {
            return trimmed.substring(CHECKSTYLE_PREFIX.length());
        }
        return trimmed;
    }

    final class SuppressionScope {
        final Set<RuleId> suppressedRules = EnumSet.noneOf(RuleId.class);
        final Set<AdviceId> suppressedAdviceIds = EnumSet.noneOf(AdviceId.class);
        boolean suppressAll;

        SuppressionScope() {
        }

        SuppressionScope(SuppressionScope parent) {
            suppressedRules.addAll(parent.suppressedRules);
            suppressedAdviceIds.addAll(parent.suppressedAdviceIds);
            suppressAll = parent.suppressAll;
        }

        void addToken(String token) {
            requireNonNull(token);
            String cleaned = cleanToken(token);
            if (cleaned.isEmpty()) {
                return;
            }
            if (ALL_TOKEN.equals(cleaned) || CHECK_ID.equals(cleaned) || CHECK_CLASS.equals(cleaned)) {
                suppressAll = true;
                return;
            }
            AdviceId adviceId = AdviceId.forName(cleaned);
            if (adviceId != AdviceId.UNKNOWN) {
                suppressedAdviceIds.add(adviceId);
                return;
            }
            RuleId ruleId = RuleRegistry.forCode(cleaned);
            if (ruleId != null) {
                suppressedRules.add(ruleId);
                legacySuppressedRuleIds.add(ruleId);
            }
        }

        String cleanToken(String token) {
            return SuppressionTracker.this.cleanToken(token);
        }
    }

    private static final class LineRange {
        private final int start;
        private final int end;

        private LineRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        private boolean contains(int lineNo) {
            return lineNo >= start && lineNo <= end;
        }
    }

    // visible for testing
    void pushScopeForTest(SuppressionScope scope) {
        scopes.push(scope);
    }

    /**
     * Add file-level suppression tokens for testing purposes.
     *
     * @param tokens suppression tokens to add.
     */
    // visible for testing
    void addFileSuppressionsForTest(String... tokens) {
        for (String token : tokens) {
            fileScope.addToken(token);
        }
    }
}
