/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Extracts reason comments for non-idiomatic statements.
 */
public final class CommentMessageExtractor extends AbstractMessageExtractor {
    private static final String SYSTEM = "System";
    private static final String RUNTIME = "Runtime";
    private static final String THREAD = "Thread";
    private static final String THREAD_LOCAL = "ThreadLocal";
    private static final String INHERITABLE_THREAD_LOCAL = "InheritableThreadLocal";
    private static final String CLASS = "Class";
    private static final String PROCESS_BUILDER = "ProcessBuilder";
    private static final String STRING = "String";
    private static final String JAVA_LANG_PREFIX = "java.lang.";

    private final Set<Integer> processedLines = new HashSet<>();

    /**
     * Create a comment reason extractor.
     *
     * @param context extraction context with file contents.
     * @param sink    sink to receive message candidates.
     */
    public CommentMessageExtractor(MessageExtractionContext context, MessageCandidateSink sink) {
        super(context, sink);
    }

    /**
     * Process {@code return} statements that return {@code null}.
     *
     * @param returnAst AST node for the return statement.
     */
    public void handleReturnStatement(DetailAST returnAst) {
        DetailAST expr = returnAst.findFirstToken(TokenTypes.EXPR);
        if (expr == null || !astSupport().isNullLiteral(expr)) {
            return;
        }
        requireReasonComment(returnAst.getLineNo(), MissingMessageKind.RETURN_NULL);
    }

    /**
     * Process method calls that use {@code System} or {@code Runtime}.
     *
     * @param methodCall AST node for the method call.
     */
    public void handleMethodCall(DetailAST methodCall) {
        if (requiresSystemComment(methodCall)) {
            requireReasonComment(methodCall.getLineNo(), MissingMessageKind.SYSTEM_CALL);
        } else if (requiresRuntimeComment(methodCall)) {
            requireReasonComment(methodCall.getLineNo(), MissingMessageKind.RUNTIME_CALL);
        } else if (requiresThreadComment(methodCall)) {
            requireReasonComment(methodCall.getLineNo(), null);
        } else if (requiresThreadLocalComment(methodCall)) {
            requireReasonComment(methodCall.getLineNo(), null);
        } else if (requiresObjectMonitorComment(methodCall)) {
            requireReasonComment(methodCall.getLineNo(), null);
        } else if (requiresClassForNameComment(methodCall)) {
            requireReasonComment(methodCall.getLineNo(), null);
        } else if (requiresProcessBuilderComment(methodCall)) {
            requireReasonComment(methodCall.getLineNo(), null);
        } else if (requiresStringInternComment(methodCall)) {
            requireReasonComment(methodCall.getLineNo(), null);
        }
    }

    static final class ReasonComment {
        private static final ReasonComment MULTIPLE = new ReasonComment(null, true);
        private static final ReasonComment MISSING = new ReasonComment(null, false);

        private final String message;
        private final boolean multiple;

        private ReasonComment(String message, boolean multiple) {
            this.message = message;
            this.multiple = multiple;
        }

        static ReasonComment missing() {
            return MISSING;
        }

        static ReasonComment multiple() {
            return MULTIPLE;
        }

        static ReasonComment single(String message) {
            return new ReasonComment(message, false);
        }

        boolean isMissing() {
            return message == null && !multiple;
        }

        boolean isMultiple() {
            return multiple;
        }

        String message() {
            return message;
        }
    }

    boolean requiresSystemComment(DetailAST methodCall) {
        String member = findMemberAfterClass(methodCall, SYSTEM);
        if (member == null || member.isEmpty()) {
            return false;
        }
        return !isAllowedSystemMember(member);
    }

    boolean requiresRuntimeComment(DetailAST methodCall) {
        if (findMemberAfterClass(methodCall, RUNTIME) != null) {
            return true;
        }
        return isRuntimeInstanceCall(methodCall);
    }

    boolean requiresThreadComment(DetailAST methodCall) {
        String member = findMemberAfterClass(methodCall, THREAD);
        if (member != null && isThreadMethod(member)) {
            return true;
        }
        String methodName = astSupport().extractMethodName(methodCall);
        if (!isThreadMethod(methodName)) {
            return false;
        }
        return isInstanceMethodCall(methodCall, methodName, THREAD);
    }

    boolean requiresThreadLocalComment(DetailAST methodCall) {
        String methodName = astSupport().extractMethodName(methodCall);
        if (!"set".equals(methodName)) {
            return false;
        }
        return isInstanceMethodCall(methodCall, methodName, THREAD_LOCAL, INHERITABLE_THREAD_LOCAL);
    }

    boolean requiresObjectMonitorComment(DetailAST methodCall) {
        String methodName = astSupport().extractMethodName(methodCall);
        int argumentCount = argumentCount(methodCall);
        if ("notify".equals(methodName) || "notifyAll".equals(methodName)) {
            return argumentCount == 0;
        }
        if (!"wait".equals(methodName)) {
            return false;
        }
        if (argumentCount == 0) {
            return true;
        }
        if (argumentCount > 2) {
            return false;
        }
        return !isCurrentClassHelperOverload(methodCall, methodName, argumentCount);
    }

    boolean requiresClassForNameComment(DetailAST methodCall) {
        String member = findMemberAfterClass(methodCall, CLASS);
        return "forName".equals(member);
    }

    boolean requiresProcessBuilderComment(DetailAST methodCall) {
        String methodName = astSupport().extractMethodName(methodCall);
        if (!"start".equals(methodName)) {
            return false;
        }
        return isInstanceMethodCall(methodCall, methodName, PROCESS_BUILDER);
    }

    boolean requiresStringInternComment(DetailAST methodCall) {
        String methodName = astSupport().extractMethodName(methodCall);
        if (!"intern".equals(methodName)) {
            return false;
        }
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return false;
        }
        DetailAST qualifier = dot.getFirstChild();
        if (qualifier == null) {
            return false;
        }
        if (qualifier.getType() == TokenTypes.STRING_LITERAL) {
            return true;
        }
        if (qualifier.getType() == TokenTypes.LITERAL_NEW) {
            String className = astSupport().extractNewClassName(qualifier);
            return isExpectedType(className, STRING);
        }
        return isInstanceMethodCall(methodCall, methodName, STRING);
    }

    private void requireReasonComment(int lineNo, MissingMessageKind missingMessageKind) {
        if (lineNo <= 0 || !processedLines.add(lineNo)) {
            return;
        }
        ReasonComment comment = findReasonComment(lineNo);
        if (comment.isMissing()) {
            sink().emitMissingMessage(lineNo, MessageSource.COMMENT, missingMessageKind);
            return;
        }
        if (comment.isMultiple()) {
            return;
        }
        String message = comment.message();
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(MessageSource.COMMENT)
                .lineNo(lineNo)
                .message(message)
                .normalisedMessage(MessageNormaliser.normalise(message))
                .placeholderCount(0)
                .keyValueLabelCount(0)
                .missingMessageKind(missingMessageKind)
                .build();
        sink().emitCandidate(candidate);
    }

    ReasonComment findReasonComment(int lineNo) {
        if (lineNo <= 0) {
            return ReasonComment.missing();
        }
        FileContents contents = context().fileContents();
        if (contents == null) {
            return ReasonComment.multiple();
        }
        List<TextBlock> comments = collectAdjacentComments(contents, lineNo);
        if (comments.isEmpty()) {
            return ReasonComment.missing();
        }
        if (comments.size() > 1) {
            return ReasonComment.multiple();
        }
        return ReasonComment.single(extractCommentMessage(comments.get(0)));
    }

    private List<TextBlock> collectAdjacentComments(FileContents contents, int lineNo) {
        List<TextBlock> comments = new ArrayList<>();
        int startLine = findCommentRegionStart(contents, lineNo);
        Map<Integer, TextBlock> singleLine = contents.getSingleLineComments();
        Map<Integer, List<TextBlock>> blockComments = contents.getBlockComments();
        for (int line = startLine; line <= lineNo; line++) {
            List<TextBlock> lineComments = new ArrayList<>();
            TextBlock single = singleLine.get(line);
            if (single != null) {
                lineComments.add(single);
            }
            List<TextBlock> blocks = blockComments.get(line);
            if (blocks != null) {
                for (TextBlock block : blocks) {
                    if (block != null) {
                        lineComments.add(block);
                    }
                }
            }
            if (lineComments.size() > 1) {
                lineComments.sort(Comparator.comparingInt(TextBlock::getStartColNo));
            }
            comments.addAll(lineComments);
        }
        return comments;
    }

    private int findCommentRegionStart(FileContents contents, int lineNo) {
        // FileContents line helpers use 0-based indices; TextBlock line numbers are 1-based.
        List<TextBlock> blockComments = flattenBlockComments(contents.getBlockComments());
        int scan = lineNo - 1;
        while (scan > 0) {
            int index = scan - 1;
            if (!contents.lineIsBlank(index) && !isCommentLine(contents, index, blockComments)) {
                break;
            }
            scan--;
        }
        return Math.max(1, scan + 1);
    }

    private List<TextBlock> flattenBlockComments(Map<Integer, List<TextBlock>> blockComments) {
        if (blockComments.isEmpty()) {
            return Collections.emptyList();
        }
        List<TextBlock> blocks = new ArrayList<>();
        for (List<TextBlock> group : blockComments.values()) {
            if (group == null) {
                continue;
            }
            for (TextBlock block : group) {
                if (block != null) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    boolean isCommentLine(FileContents contents, int lineIndex,
                          List<TextBlock> blockComments) {
        if (contents.lineIsComment(lineIndex)) {
            return true;
        }
        if (blockComments.isEmpty()) {
            return false;
        }
        String line = contents.getLine(lineIndex);
        if (line == null) {
            return false;
        }
        int lineNo = lineIndex + 1;
        for (TextBlock block : blockComments) {
            if (block == null) {
                continue;
            }
            if (lineNo < block.getStartLineNo() || lineNo > block.getEndLineNo()) {
                continue;
            }
            if (isBlockCommentOnlyLine(line, lineNo, block)) {
                return true;
            }
        }
        return false;
    }

    boolean isBlockCommentOnlyLine(String line, int lineNo, TextBlock block) {
        int startLine = block.getStartLineNo();
        int endLine = block.getEndLineNo();
        if (lineNo == startLine) {
            int startCol = clampColumn(block.getStartColNo(), line.length());
            if (!isWhitespace(line.substring(0, startCol))) {
                return false;
            }
            if (lineNo == endLine) {
                int endCol = clampColumn(block.getEndColNo() + 1, line.length());
                return isWhitespace(line.substring(endCol));
            }
            return true;
        }
        if (lineNo == endLine) {
            int endCol = clampColumn(block.getEndColNo() + 1, line.length());
            return isWhitespace(line.substring(endCol));
        }
        return true;
    }

    int clampColumn(int column, int length) {
        if (column < 0) {
            return 0;
        }
        return Math.min(column, length);
    }

    boolean isWhitespace(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String extractCommentMessage(TextBlock block) {
        requireNonNull(block);
        String[] lines = block.getText();
        if (lines == null || lines.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String cleaned = cleanCommentLine(line);
            if (cleaned.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(cleaned);
        }
        return builder.toString().trim();
    }

    private String cleanCommentLine(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("//")) {
            trimmed = trimmed.substring(2);
        } else if (trimmed.startsWith("/*")) {
            trimmed = trimmed.substring(2);
        } else if (trimmed.startsWith("*")) {
            trimmed = trimmed.substring(1);
        }
        trimmed = trimmed.trim();
        if (trimmed.endsWith("*/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 2).trim();
        }
        return trimmed;
    }

    private boolean isAllowedSystemMember(String member) {
        return "currentTimeMillis".equals(member)
                || "nanoTime".equals(member)
                || "getLogger".equals(member)
                || "Logger".equals(member)
                || "lineSeparator".equals(member)
                || "out".equals(member)
                || "err".equals(member);
    }

    private String findMemberAfterClass(DetailAST ast, String className) {
        if (ast == null) {
            return null;
        }
        DetailAST content = astSupport().unwrapExpr(ast);
        if (content == null) {
            return null;
        }
        if (content.getType() == TokenTypes.METHOD_CALL) {
            DetailAST dot = content.findFirstToken(TokenTypes.DOT);
            return dot == null ? null : findMemberAfterClass(dot, className);
        }
        if (content.getType() == TokenTypes.DOT) {
            DetailAST left = content.getFirstChild();
            DetailAST right = content.getLastChild();
            if (resolvesToClass(left, className)) {
                return right.getText();
            }
            return findMemberAfterClass(left, className);
        }
        if (content.getType() == TokenTypes.IDENT) {
            return className.equals(content.getText()) ? "" : null;
        }
        return null;
    }

    private boolean resolvesToClass(DetailAST ast, String className) {
        if (ast == null) {
            return false;
        }
        if (ast.getType() == TokenTypes.IDENT) {
            return className.equals(ast.getText());
        }
        if (ast.getType() == TokenTypes.DOT) {
            String flattened = astSupport().flattenDot(ast);
            return className.equals(flattened)
                    || (JAVA_LANG_PREFIX + className).equals(flattened);
        }
        return false;
    }

    private boolean isRuntimeInstanceCall(DetailAST methodCall) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return false;
        }
        DetailAST qualifier = dot.getFirstChild();
        String qualifierName = resolveQualifierName(qualifier);
        if (qualifierName == null || qualifierName.isEmpty()) {
            return false;
        }
        String typeName = context().getVariableType(qualifierName);
        if (typeName == null) {
            return false;
        }
        String resolved = context().resolveTypeName(typeName);
        return RUNTIME.equals(resolved) || (JAVA_LANG_PREFIX + RUNTIME).equals(resolved);
    }

    boolean isInstanceMethodCall(DetailAST methodCall, String methodName,
                                 String... classNames) {
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return false;
        }
        if (!methodName.equals(astSupport().extractMethodName(methodCall))) {
            return false;
        }
        DetailAST qualifier = dot.getFirstChild();
        if (qualifier == null) {
            return false;
        }
        if (qualifier.getType() == TokenTypes.LITERAL_NEW) {
            String className = astSupport().extractNewClassName(qualifier);
            return isExpectedType(className, classNames);
        }
        String qualifierName = resolveQualifierName(qualifier);
        if (qualifierName == null || qualifierName.isEmpty()) {
            return false;
        }
        String typeName = context().getVariableType(qualifierName);
        if (typeName == null) {
            return false;
        }
        String resolved = context().resolveTypeName(typeName);
        return isExpectedType(resolved, classNames);
    }

    private boolean isExpectedType(String typeName, String... classNames) {
        if (typeName == null) {
            return false;
        }
        for (String className : classNames) {
            if (className.equals(typeName)
                    || (JAVA_LANG_PREFIX + className).equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    private int argumentCount(DetailAST methodCall) {
        DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
        return elist == null ? 0 : astSupport().collectArguments(elist).size();
    }

    boolean isThreadMethod(String methodName) {
        return "stop".equals(methodName)
                || "suspend".equals(methodName)
                || "resume".equals(methodName)
                || "yield".equals(methodName)
                || "setPriority".equals(methodName)
                || "sleep".equals(methodName);
    }

    private boolean isCurrentClassHelperOverload(DetailAST methodCall, String methodName, int argumentCount) {
        if (!context().isDeclaredMethodSignature(methodName, argumentCount)) {
            return false;
        }
        DetailAST dot = methodCall.findFirstToken(TokenTypes.DOT);
        if (dot == null) {
            return true;
        }
        DetailAST qualifier = dot.getFirstChild();
        if (qualifier == null) {
            return false;
        }
        if (qualifier.getType() == TokenTypes.LITERAL_THIS) {
            return true;
        }
        String qualifierName = resolveQualifierName(qualifier);
        if (qualifierName == null || qualifierName.isEmpty()) {
            return false;
        }
        String typeName = context().getVariableType(qualifierName);
        if (typeName == null) {
            return false;
        }
        String currentClassName = context().currentClassName();
        if (currentClassName == null || currentClassName.isEmpty()) {
            return false;
        }
        String resolved = context().resolveTypeName(typeName);
        return currentClassName.equals(typeName) || currentClassName.equals(resolved);
    }

    private String resolveQualifierName(DetailAST qualifier) {
        if (qualifier == null) {
            return null;
        }
        if (qualifier.getType() == TokenTypes.IDENT) {
            return qualifier.getText();
        }
        if (qualifier.getType() == TokenTypes.DOT) {
            DetailAST ident = astSupport().findRightmostIdent(qualifier);
            return ident == null ? null : ident.getText();
        }
        return null;
    }
}
