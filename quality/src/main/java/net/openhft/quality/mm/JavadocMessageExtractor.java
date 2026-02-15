/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Extracts the first paragraph from Javadoc comments.
 */
public final class JavadocMessageExtractor extends AbstractMessageExtractor {
    private static final Pattern INLINE_TAG_PATTERN = Pattern.compile("\\{@\\s*([^\\s}]+)[^}]*\\}");
    private static final String INLINE_TAG_PLACEHOLDER = "{@}";
    private static final Pattern PARAGRAPH_TAG_PATTERN = Pattern.compile("(?i)<p\\b[^>]*>");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private boolean classJavadocSeen;
    private final java.util.Set<Integer> emittedJavadocStartLines = new java.util.HashSet<>();

    /**
     * Create an extractor for Javadoc paragraphs.
     *
     * @param context extraction context with file contents.
     * @param sink    sink to receive message candidates.
     */
    public JavadocMessageExtractor(MessageExtractionContext context, MessageCandidateSink sink) {
        super(context, sink);
    }

    /**
     * Reset per-file state.
     */
    public void reset() {
        classJavadocSeen = false;
        emittedJavadocStartLines.clear();
    }

    /**
     * Handle a type definition Javadoc.
     *
     * @param typeAst  AST node for a type definition.
     * @param topLevel {@code true} if the type is top-level.
     */
    public void handleType(DetailAST typeAst, boolean topLevel) {
        TextBlock javadoc = findJavadoc(typeAst);
        if (javadoc == null) {
            return;
        }
        MessageSource source = MessageSource.JAVADOC_MEMBER;
        if (topLevel && !classJavadocSeen) {
            source = MessageSource.JAVADOC_CLASS;
            classJavadocSeen = true;
        }
        emitCandidate(javadoc, source);
    }

    /**
     * Handle a member Javadoc (method, ctor, field, enum constant).
     *
     * @param memberAst AST node for the member.
     */
    public void handleMember(DetailAST memberAst) {
        TextBlock javadoc = findJavadoc(memberAst);
        if (javadoc == null) {
            return;
        }
        emitCandidate(javadoc, MessageSource.JAVADOC_MEMBER);
    }

    /**
     * Handle a field declaration Javadoc, skipping locals.
     *
     * @param variableDef AST node for a variable definition.
     */
    public void handleField(DetailAST variableDef) {
        if (!isField(variableDef)) {
            return;
        }
        handleMember(variableDef);
    }

    private boolean isField(DetailAST varDef) {
        DetailAST parent = varDef.getParent();
        while (parent != null) {
            int type = parent.getType();
            if (type == TokenTypes.METHOD_DEF
                    || type == TokenTypes.CTOR_DEF
                    || type == TokenTypes.COMPACT_CTOR_DEF) {
                return false;
            }
            if (type == TokenTypes.CLASS_DEF
                    || type == TokenTypes.INTERFACE_DEF
                    || type == TokenTypes.ENUM_DEF
                    || type == TokenTypes.ANNOTATION_DEF
                    || type == TokenTypes.RECORD_DEF
                    || type == TokenTypes.OBJBLOCK) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private TextBlock findJavadoc(DetailAST ast) {
        FileContents contents = context().fileContents();
        if (contents == null) {
            return null;
        }
        return contents.getJavadocBefore(ast.getLineNo());
    }

    void emitCandidate(TextBlock javadoc, MessageSource source) {
        requireNonNull(javadoc);
        int startLine = javadoc.getStartLineNo();
        if (!emittedJavadocStartLines.add(startLine)) {
            return;
        }
        String paragraph = extractFirstParagraph(javadoc);
        if (paragraph == null) {
            return;
        }
        ExtractionResult extracted = normaliseParagraph(paragraph);
        if (extracted.message.isEmpty() && extracted.placeholderCount == 0) {
            return;
        }
        String normalised = MessageNormaliser.normalise(extracted.message);
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(source)
                .lineNo(javadoc.getStartLineNo())
                .message(extracted.message)
                .normalisedMessage(normalised)
                .placeholderCount(extracted.placeholderCount)
                .keyValueLabelCount(0)
                .build();
        sink().emitCandidate(candidate);
    }

    private ExtractionResult normaliseParagraph(String paragraph) {
        Matcher matcher = INLINE_TAG_PATTERN.matcher(paragraph);
        StringBuffer sb = new StringBuffer();
        int placeholderCount = 0;
        while (matcher.find()) {
            placeholderCount++;
            String tagName = matcher.group(1);
            String replacement = INLINE_TAG_PLACEHOLDER;
            if (tagName != null && !tagName.isEmpty()) {
                replacement = "{@" + tagName + "}";
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        String withoutInline = sb.toString();
        String withoutHtml = HTML_TAG_PATTERN.matcher(withoutInline).replaceAll(" ");
        String collapsed = collapseWhitespace(withoutHtml);
        return new ExtractionResult(collapsed, placeholderCount);
    }

    String extractFirstParagraph(TextBlock block) {
        String[] lines = block.getText();
        if (lines == null || lines.length == 0) {
            return "";
        }
        List<String> content = new ArrayList<>();
        for (String raw : lines) {
            String line = trimCommentLine(raw);
            if (line.isEmpty()) {
                if (!content.isEmpty()) {
                    break;
                }
                continue;
            }
            if (line.startsWith("@")) {
                break;
            }
            Matcher matcher = PARAGRAPH_TAG_PATTERN.matcher(line);
            if (matcher.find()) {
                String before = line.substring(0, matcher.start()).trim();
                if (!before.isEmpty()) {
                    content.add(before);
                }
                break;
            }
            content.add(line);
        }
        if (content.isEmpty()) {
            return "";
        }
        return String.join(" ", content);
    }

    private String trimCommentLine(String raw) {
        if (raw == null) {
            return "";
        }
        String line = raw.trim();
        if (line.startsWith("/**")) {
            line = line.substring(3).trim();
        }
        if (line.endsWith("*/")) {
            line = line.substring(0, line.length() - 2).trim();
        }
        if (line.startsWith("*")) {
            line = line.substring(1).trim();
        }
        return line;
    }

    private String collapseWhitespace(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(trimmed.length());
        boolean inWhitespace = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (Character.isWhitespace(ch)) {
                if (!inWhitespace) {
                    builder.append(' ');
                    inWhitespace = true;
                }
            } else {
                builder.append(ch);
                inWhitespace = false;
            }
        }
        return builder.toString();
    }

    private static final class ExtractionResult {
        final String message;
        final int placeholderCount;

        ExtractionResult(String message, int placeholderCount) {
            this.message = message;
            this.placeholderCount = placeholderCount;
        }
    }
}
