/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Coordinates extraction and evaluation of message candidates for a single file.
 */
public class MeaningfulMessageProcessor implements MessageCandidateSink {
    private ViolationCollector violationCollector;
    private SuppressionTracker suppressionTracker;
    private MessageMetricsCalculator metricsCalculator;
    private MessagePrefilter messagePrefilter;
    private RuleEngine ruleEngine;

    private MessageExtractionContext context;

    private AssertionMessageExtractor assertionExtractor;
    private ThrowMessageExtractor throwExtractor;
    private AnnotationMessageExtractor annotationExtractor;
    private LogMessageExtractor logExtractor;
    private JavadocMessageExtractor javadocExtractor;

    private boolean verbose;
    private String messageExtractionFile;
    private BufferedWriter messageExtractionWriter;

    private Set<String> ignoredExceptionClassNames = java.util.Collections.emptySet();

    /**
     * Create a processor with default settings.
     */
    public MeaningfulMessageProcessor() {
    }

    /**
     * Enable verbose evaluation details.
     *
     * @param verbose {@code true} to include verbose details in violations.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Configure the optional output file for extracted messages.
     *
     * @param messageExtractionFile path to write extraction data, or {@code null} to disable.
     */
    public void setMessageExtractionFile(String messageExtractionFile) {
        if (messageExtractionFile == null) {
            this.messageExtractionFile = null;
            return;
        }
        String trimmed = messageExtractionFile.trim();
        if (trimmed.isEmpty() || trimmed.contains("${")) {
            this.messageExtractionFile = null;
            return;
        }
        this.messageExtractionFile = trimmed;
    }

    /**
     * Configure exception class names that are ignored for message checks.
     *
     * @param ignoredExceptionClassNames comma or whitespace separated class names.
     */
    public void setIgnoredExceptionClassNames(String ignoredExceptionClassNames) {
        if (ignoredExceptionClassNames == null) {
            this.ignoredExceptionClassNames = java.util.Collections.emptySet();
        } else {
            Set<String> names = new HashSet<>();
            for (String token : ignoredExceptionClassNames.split("[,\\s]+")) {
                String normalized = normalizeClassName(token);
                if (normalized != null) {
                    names.add(normalized);
                }
            }
            this.ignoredExceptionClassNames = names.isEmpty()
                    ? java.util.Collections.emptySet()
                    : names;
        }
        if (context != null) {
            context.setIgnoredExceptionClassNames(this.ignoredExceptionClassNames);
        }
    }

    /**
     * Return the default tokens this processor consumes.
     *
     * @return the default tokens this processor consumes.
     */
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    /**
     * Return the acceptable tokens for this processor.
     *
     * @return the acceptable tokens for this processor.
     */
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    /**
     * Return the required tokens for this processor.
     *
     * @return the required tokens for this processor.
     */
    public int[] getRequiredTokens() {
        return new int[]{
                TokenTypes.IMPORT,
                TokenTypes.STATIC_IMPORT,
                TokenTypes.CLASS_DEF,
                TokenTypes.INTERFACE_DEF,
                TokenTypes.ENUM_DEF,
                TokenTypes.ANNOTATION_DEF,
                TokenTypes.RECORD_DEF,
                TokenTypes.METHOD_DEF,
                TokenTypes.CTOR_DEF,
                TokenTypes.COMPACT_CTOR_DEF,
                TokenTypes.PARAMETER_DEF,
                TokenTypes.VARIABLE_DEF,
                TokenTypes.ENUM_CONSTANT_DEF,
                TokenTypes.RECORD_COMPONENT_DEF,
                TokenTypes.LITERAL_ASSERT,
                TokenTypes.LITERAL_THROW,
                TokenTypes.ANNOTATION,
                TokenTypes.METHOD_CALL
        };
    }

    /**
     * Initialise per-file state from the Checkstyle file contents.
     *
     * @param fileContents file contents for the current file.
     */
    public void beginTree(FileContents fileContents) {
        Map<String, Integer> messageOccurrences = new HashMap<>();
        suppressionTracker = new SuppressionTracker();
        violationCollector = new ViolationCollector(suppressionTracker);
        metricsCalculator = new MessageMetricsCalculator();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(metricsCalculator);
        messagePrefilter = new MessagePrefilter();
        MessageAstSupport astSupport = new MessageAstSupport();
        context = new MessageExtractionContext(astSupport);
        MessageTemplateExtractor templateExtractor = new MessageTemplateExtractor(context::isLocaleExpression);
        context.setTemplateExtractor(templateExtractor);
        context.setIgnoredExceptionClassNames(ignoredExceptionClassNames);
        context.reset(fileContents);
        ruleEngine = new RuleEngine(ruleSupport, messageOccurrences);
        assertionExtractor = new AssertionMessageExtractor(context, this);
        throwExtractor = new ThrowMessageExtractor(context, this);
        annotationExtractor = new AnnotationMessageExtractor(context, this);
        logExtractor = new LogMessageExtractor(context, this);
        javadocExtractor = new JavadocMessageExtractor(context, this);
        javadocExtractor.reset();
        openMessageExtractionWriter();
    }

    /**
     * Finalise processing for the file and flush any pending violations.
     *
     * @param check owning check to report violations against.
     */
    public void finishTree(AbstractCheck check) {
        if (violationCollector != null) {
            if (verbose) {
                emitRuleSummary();
            }
            violationCollector.flush(check);
            violationCollector.clear();
        }
        closeMessageExtractionWriter();
        if (context != null) {
            context.reset(null);
        }
    }

    /**
     * Visit a token and route it to the appropriate extractor or scope tracker.
     *
     * @param ast token AST node.
     */
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.IMPORT:
                context.recordImport(ast);
                break;
            case TokenTypes.STATIC_IMPORT:
                context.recordStaticImport(ast);
                break;
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
            case TokenTypes.ANNOTATION_DEF:
            case TokenTypes.RECORD_DEF:
                suppressionTracker.enterScope(ast);
                context.enterType(ast);
                if (javadocExtractor != null) {
                    javadocExtractor.handleType(ast, isTopLevelType(ast));
                }
                break;
            case TokenTypes.METHOD_DEF:
            case TokenTypes.CTOR_DEF:
            case TokenTypes.COMPACT_CTOR_DEF:
                suppressionTracker.enterScope(ast);
                context.enterMethod(ast);
                if (javadocExtractor != null) {
                    javadocExtractor.handleMember(ast);
                }
                break;
            case TokenTypes.PARAMETER_DEF:
            case TokenTypes.VARIABLE_DEF:
                context.recordVariableType(ast);
                if (javadocExtractor != null && ast.getType() == TokenTypes.VARIABLE_DEF) {
                    javadocExtractor.handleField(ast);
                }
                break;
            case TokenTypes.ENUM_CONSTANT_DEF:
            case TokenTypes.RECORD_COMPONENT_DEF:
                if (javadocExtractor != null) {
                    javadocExtractor.handleMember(ast);
                }
                break;
            case TokenTypes.LITERAL_ASSERT:
                assertionExtractor.handleJavaAssert(ast);
                break;
            case TokenTypes.LITERAL_THROW:
                throwExtractor.handleThrowStatement(ast);
                break;
            case TokenTypes.ANNOTATION:
                annotationExtractor.handleAnnotation(ast);
                break;
            case TokenTypes.METHOD_CALL:
                assertionExtractor.handleMethodCall(ast);
                logExtractor.handleMethodCall(ast);
                break;
            default:
                break;
        }
    }

    /**
     * Leave a token and update scope tracking as required.
     *
     * @param ast token AST node.
     */
    public void leaveToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.METHOD_DEF:
            case TokenTypes.CTOR_DEF:
            case TokenTypes.COMPACT_CTOR_DEF:
                checkMissingDisplayName();
                context.leaveMethod();
                suppressionTracker.leaveScope();
                break;
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
            case TokenTypes.ANNOTATION_DEF:
            case TokenTypes.RECORD_DEF:
                suppressionTracker.leaveScope();
                break;
            default:
                break;
        }
    }

    private void checkMissingDisplayName() {
        if (context.isCurrentMethodTest() && !context.currentMethodHasDisplayName()) {
            String methodName = context.currentMethodName();
            int lineNo = context.currentMethodLineNo();
            if (methodName != null && lineNo > 0) {
                violationCollector.record(lineNo, RuleId.MISSING_DISPLAY_NAME, methodName);
            }
        }
    }

    private boolean isTopLevelType(DetailAST ast) {
        DetailAST parent = ast.getParent();
        return parent != null && parent.getType() == TokenTypes.COMPILATION_UNIT;
    }

    /**
     * Process a message candidate through metrics, filtering, and rule evaluation.
     *
     * @param candidate message candidate.
     */
    @Override
    public void emitCandidate(MessageCandidate candidate) {
        requireNonNull(candidate);
        String message = candidate.message();
        MessageMetrics metrics = null;
        if (message != null) {
            metrics = computeMessageMetrics(message, candidate.placeholderCount(),
                    candidate.keyValueLabelCount());
            writeMessageExtractionRecord(message, candidate.lineNo(), metrics,
                    candidate.placeholderCount(), candidate.keyValueLabelCount(),
                    candidate.source());
            if (messagePrefilter != null && messagePrefilter.shouldSkip(message)) {
                return;
            }
        }
        if (ruleEngine != null) {
            ruleEngine.evaluate(candidate, metrics, context.currentClassName(),
                    context.currentMethodName(), verbose, suppressionTracker,
                    violationCollector);
        }
    }

    /**
     * Emit a missing-message candidate for the given source and line.
     *
     * @param lineNo line number where the message is missing.
     * @param source source category of the missing message.
     */
    @Override
    public void emitMissingMessage(int lineNo, MessageSource source) {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(source)
                .lineNo(lineNo)
                .missingMessage(true)
                .build();
        emitCandidate(candidate);
    }

    private void openMessageExtractionWriter() {
        String outputValue = messageExtractionFile;
        if (outputValue == null || outputValue.trim().isEmpty()) {
            outputValue = System.getProperty("mm.extract.file");
        }
        if (outputValue == null) {
            return;
        }
        String trimmed = outputValue.trim();
        if (trimmed.isEmpty() || trimmed.contains("${")) {
            return;
        }
        Path outputPath = Paths.get(trimmed);
        boolean writeHeader;
        try {
            writeHeader = !Files.exists(outputPath) || Files.size(outputPath) == 0L;
            messageExtractionWriter = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (writeHeader) {
                messageExtractionWriter.write("file\tline\tsource\tchars\twords\tmeaningful\tplaceholders\tkeyValueLabels\ttotalWords\teffectiveMeaningful\tmessage");
                messageExtractionWriter.newLine();
                messageExtractionWriter.flush();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open message extraction file: " + outputPath, e);
        }
    }

    private void closeMessageExtractionWriter() {
        if (messageExtractionWriter == null) {
            return;
        }
        try {
            messageExtractionWriter.flush();
            messageExtractionWriter.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to close message extraction file: " + messageExtractionFile, e);
        } finally {
            messageExtractionWriter = null;
        }
    }

    private void writeMessageExtractionRecord(String message, int lineNo,
                                              MessageMetrics metrics, int placeholderCount,
                                              int keyValueLabelCount, MessageSource source) {
        if (messageExtractionWriter == null) {
            return;
        }
        MessageMetrics resolvedMetrics = metrics;
        if (resolvedMetrics == null) {
            resolvedMetrics = computeMessageMetrics(message, placeholderCount, keyValueLabelCount);
        }
        FileContents contents = context == null ? null : context.fileContents();
        String fileName = contents == null ? "unknown" : contents.getFileName();
        StringBuilder line = new StringBuilder(256);
        line.append(escapeForTsv(fileName)).append('\t')
                .append(lineNo).append('\t')
                .append(source.name()).append('\t')
                .append(resolvedMetrics.charCount()).append('\t')
                .append(resolvedMetrics.wordCount()).append('\t')
                .append(resolvedMetrics.meaningfulWordCount()).append('\t')
                .append(placeholderCount).append('\t')
                .append(keyValueLabelCount).append('\t')
                .append(resolvedMetrics.totalWordCount()).append('\t')
                .append(resolvedMetrics.effectiveMeaningfulWordCount()).append('\t')
                .append(escapeForTsv(message));
        try {
            messageExtractionWriter.write(line.toString());
            messageExtractionWriter.newLine();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write message extraction record", e);
        }
    }

    private void emitRuleSummary() {
        Map<RuleId, Integer> summary = violationCollector == null
                ? java.util.Collections.emptyMap()
                : violationCollector.summaryCounts();
        if (summary.isEmpty()) {
            return;
        }
        FileContents contents = context == null ? null : context.fileContents();
        String fileName = contents == null ? "unknown" : contents.getFileName();
        String summaryText = formatRuleSummary(summary);
        System.out.println("MeaningfulMessage summary: file=" + fileName + " " + summaryText);
    }

    private String formatRuleSummary(Map<RuleId, Integer> summary) {
        List<Map.Entry<RuleId, Integer>> entries = new ArrayList<>(summary.entrySet());
        entries.sort(Comparator
                .comparingInt((Map.Entry<RuleId, Integer> entry) -> entry.getValue())
                .reversed()
                .thenComparing(entry -> entry.getKey().code()));
        int total = 0;
        StringBuilder builder = new StringBuilder(128);
        for (Map.Entry<RuleId, Integer> entry : entries) {
            total += entry.getValue();
        }
        builder.append("total=").append(total).append(" rules=");
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<RuleId, Integer> entry = entries.get(i);
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey().code()).append('=').append(entry.getValue());
        }
        return builder.toString();
    }

    private MessageMetrics computeMessageMetrics(String message, int placeholderCount, int keyValueLabelCount) {
        if (metricsCalculator == null) {
            return new MessageMetrics(0, 0, 0, placeholderCount,
                    placeholderCount, java.util.Collections.emptyList(),
                    java.util.Collections.emptyList());
        }
        return metricsCalculator.calculate(message, placeholderCount, keyValueLabelCount);
    }

    String escapeForTsv(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String escaped = value.replace("\\", "\\\\");
        escaped = escaped.replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return escaped;
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
}
