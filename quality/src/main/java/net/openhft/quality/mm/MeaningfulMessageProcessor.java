/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
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
 *
 * <p><strong>Threading contract:</strong> instances are <em>not</em> thread-safe.
 * Checkstyle guarantees that {@code beginTree}, {@code visitToken},
 * {@code leaveToken}, and {@code finishTree} are called sequentially on the
 * same thread for each file. Do not share an instance across threads.
 *
 * <p>The {@code *ForTesting()} accessors expose internal state for unit testing.
 * They exist because the processor is a monolith that will be decomposed into
 * collaborators in a future refactoring. See god-class decomposition plan.
 */
public class MeaningfulMessageProcessor implements MessageCandidateSink {
    private static final int OVERUSED_WORD_MIN_MESSAGES = 12;
    private static final int PURPOSE_CUE_RATIO = 8;
    private static final double MIN_WORD_SHANNON_ENTROPY = 4.0;
    private static final String CONSEQUENT_ROOT = "consequent";
    private static final String GENERATED_JAVADOC_PREFIX = "Generated at ";
    private static final Set<String> PURPOSE_CUES = new HashSet<>(java.util.Arrays.asList(
            "because", "since", "due", "owing", "account", "reason", "for",
            "result", "therefore", "thus", "hence",
            "purpose",
            "if", "given", "assuming", "provided",
            "although", "despite", "spite", "regardless",
            "required", "workaround", "avoid", "otherwise"
    ));
    private ViolationCollector violationCollector;
    private AdviceCollector adviceCollector;
    private AdviceCandidateEmitter adviceEmitter;
    private AdviceReportManager adviceReportManager;
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
    private CommentMessageExtractor commentExtractor;

    private boolean verbose;
    private boolean dryRun;
    private String jsonlOutput;
    private String rankOut;
    private boolean warnLegacySuppressions = true;
    private boolean emitUnhandled = true;
    private String messageExtractionFile;
    private BufferedWriter messageExtractionWriter;
    private String messageExtractionTarget;
    private int messageExtractionFailureLine;
    private String messageExtractionFailureDetail;

    private Set<String> ignoredExceptionClassNames = java.util.Collections.emptySet();
    private List<String> excludedPathFragments = java.util.Collections.emptyList();
    private List<java.util.regex.Pattern> excludedPathPatterns = java.util.Collections.emptyList();
    private Map<String, Integer> overusedWordCounts = new HashMap<>();
    private Map<String, Integer> entropyWordCounts = new HashMap<>();
    private int fileMessageCount;
    private int overusedWordMessageCount;
    private int fileFirstMessageLine;
    private int purposeCueCount;
    private int entropyWordTotal;
    private Set<Integer> mapStringObjectLines = new HashSet<>();
    private boolean skipFile;
    private String currentFileName;

    /**
     * Create a processor with default settings.
     */
    public MeaningfulMessageProcessor() {
    }

    /**
     * Enable verbose evaluation details and message templates.
     *
     * @param verbose {@code true} to include verbose details in violations.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        // violationCollector is recreated at beginTree with the latest verbose value
    }

    /**
     * Enable dry-run mode to emit all advice and generate ranks.
     *
     * @param dryRun {@code true} for dry-run mode.
     */
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    /**
     * Return the suppression tracker for testing purposes.
     *
     * @return suppression tracker instance.
     */
    SuppressionTracker suppressionTrackerForTesting() {
        return suppressionTracker;
    }

    /**
     * Configure JSONL output path for aggregated advice.
     *
     * @param jsonlOutput JSONL output path, or {@code null} to disable.
     */
    public void setJsonlOutput(String jsonlOutput) {
        this.jsonlOutput = jsonlOutput;
    }

    /**
     * Configure the output path for rank generation.
     *
     * @param rankOut rank output path, or {@code null} to use defaults.
     */
    public void setRankOut(String rankOut) {
        this.rankOut = rankOut;
    }

    /**
     * Enable warnings for legacy RuleId suppressions.
     *
     * @param warnLegacySuppressions {@code true} to emit warnings.
     */
    public void setWarnLegacySuppressions(boolean warnLegacySuppressions) {
        this.warnLegacySuppressions = warnLegacySuppressions;
    }

    /**
     * Initialise run-level advice reporting resources.
     */
    public void beginRun() {
        adviceReportManager = new AdviceReportManager(verbose, dryRun, jsonlOutput, rankOut,
                warnLegacySuppressions);
        adviceReportManager.beginRun();
    }

    /**
     * Finalise run-level advice reporting resources.
     */
    public void finishRun() {
        if (adviceReportManager != null) {
            adviceReportManager.finishRun();
        }
    }

    /**
     * Enable warnings for unhandled extraction cases.
     *
     * @param emitUnhandled {@code true} to emit unhandled warnings.
     */
    public void setEmitUnhandled(boolean emitUnhandled) {
        this.emitUnhandled = emitUnhandled;
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
     * Configure file exclusions by path fragment, glob, or regex (prefix with {@code regex:}).
     *
     * @param excludedPaths comma/whitespace separated path patterns.
     */
    public void setExcludedPaths(String excludedPaths) {
        if (excludedPaths == null || excludedPaths.trim().isEmpty()) {
            excludedPathFragments = java.util.Collections.emptyList();
            excludedPathPatterns = java.util.Collections.emptyList();
            return;
        }
        List<String> fragments = new ArrayList<>();
        List<java.util.regex.Pattern> patterns = new ArrayList<>();
        String[] tokens = excludedPaths.split("[,;\\s]+");
        for (String token : tokens) {
            if (token == null) {
                continue;
            }
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("regex:") || trimmed.startsWith("re:")) {
                String regex = trimmed.startsWith("regex:") ? trimmed.substring(6) : trimmed.substring(3);
                if (!regex.isEmpty()) {
                    patterns.add(java.util.regex.Pattern.compile(regex));
                }
                continue;
            }
            if (trimmed.indexOf('*') >= 0 || trimmed.indexOf('?') >= 0) {
                patterns.add(java.util.regex.Pattern.compile(globToRegex(normalizePath(trimmed))));
                continue;
            }
            fragments.add(normalizePath(trimmed));
        }
        excludedPathFragments = fragments.isEmpty()
                ? java.util.Collections.emptyList()
                : java.util.Collections.unmodifiableList(fragments);
        excludedPathPatterns = patterns.isEmpty()
                ? java.util.Collections.emptyList()
                : java.util.Collections.unmodifiableList(patterns);
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
                TokenTypes.LITERAL_RETURN,
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
        beginTree(fileContents, null);
    }

    /**
     * Initialise per-file state from the Checkstyle file contents.
     *
     * @param fileContents file contents for the current file.
     * @param rootAst      root AST node for the file, or {@code null} if unavailable.
     */
    public void beginTree(FileContents fileContents, DetailAST rootAst) {
        Map<String, Integer> messageOccurrences = new HashMap<>();
        suppressionTracker = new SuppressionTracker();
        violationCollector = new ViolationCollector(suppressionTracker, verbose);
        adviceCollector = new AdviceCollector();
        adviceEmitter = new AdviceCandidateEmitter(adviceCollector, suppressionTracker, fileContents);
        metricsCalculator = new MessageMetricsCalculator();
        MessageRuleSupport ruleSupport = new MessageRuleSupport(metricsCalculator);
        messagePrefilter = new MessagePrefilter();
        MessageAstSupport astSupport = new MessageAstSupport();
        context = new MessageExtractionContext(astSupport);
        MessageTemplateExtractor templateExtractor = new MessageTemplateExtractor(context::isLocaleExpression);
        context.setTemplateExtractor(templateExtractor);
        context.setIgnoredExceptionClassNames(ignoredExceptionClassNames);
        context.reset(fileContents);
        currentFileName = fileContents == null ? "unknown" : fileContents.getFileName();
        context.setDeclaredMethodNames(collectDeclaredMethods(rootAst));
        context.setDeclaredMethodArities(collectDeclaredMethodArities(rootAst));
        skipFile = shouldSkipFile(fileContents);
        if (!skipFile && suppressionTracker != null) {
            suppressionTracker.recordCommentSuppressions(fileContents);
        }
        ruleEngine = new RuleEngine(ruleSupport, messageOccurrences);
        assertionExtractor = new AssertionMessageExtractor(context, this);
        throwExtractor = new ThrowMessageExtractor(context, this);
        annotationExtractor = new AnnotationMessageExtractor(context, this);
        logExtractor = new LogMessageExtractor(context, this);
        javadocExtractor = new JavadocMessageExtractor(context, this);
        commentExtractor = new CommentMessageExtractor(context, this);
        javadocExtractor.reset();
        overusedWordCounts = new HashMap<>();
        entropyWordCounts = new HashMap<>();
        mapStringObjectLines = new HashSet<>();
        fileMessageCount = 0;
        overusedWordMessageCount = 0;
        fileFirstMessageLine = 0;
        purposeCueCount = 0;
        entropyWordTotal = 0;
        messageExtractionFailureLine = 0;
        messageExtractionFailureDetail = null;
        messageExtractionTarget = null;
        if (!skipFile) {
            recordFileNameWords(fileContents);
            openMessageExtractionWriter();
        }
    }

    /**
     * Finalise processing for the file and flush any pending violations.
     *
     * @param check owning check to report violations against.
     */
    public void finishTree(AbstractCheck check) {
        if (skipFile) {
            closeMessageExtractionWriter();
            if (context != null) {
                context.reset(null);
            }
            return;
        }
        if (violationCollector != null) {
            emitJUnit4MigrationWarnings();
            emitOverusedWordWarning();
            emitLacksPurposeWarning();
            emitLowEntropyWarning();
            if (verbose) {
                emitRuleSummary();
            }
            violationCollector.flush(check);
            violationCollector.clear();
        }
        if (adviceReportManager != null && adviceCollector != null) {
            adviceReportManager.reportFile(currentFileName, adviceCollector, context, suppressionTracker);
            adviceCollector.clearAll();
        }
        closeMessageExtractionWriter();
        emitMessageExtractionFailure(check);
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
        if (skipFile) {
            return;
        }
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
                if (isTopLevelType(ast)) {
                    suppressionTracker.recordFileSuppressions(ast);
                }
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
                if (ast.getType() == TokenTypes.VARIABLE_DEF) {
                    if (javadocExtractor != null) {
                        javadocExtractor.handleField(ast);
                    }
                    checkMapStringObjectUsage(ast);
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
            case TokenTypes.LITERAL_RETURN:
                commentExtractor.handleReturnStatement(ast);
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
                commentExtractor.handleMethodCall(ast);
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
        if (skipFile) {
            return;
        }
        switch (ast.getType()) {
            case TokenTypes.METHOD_DEF:
            case TokenTypes.CTOR_DEF:
            case TokenTypes.COMPACT_CTOR_DEF:
                checkMissingDisplayName();
                checkTestAnnotationOrder();
                context.leaveMethod();
                suppressionTracker.leaveScope();
                break;
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF:
            case TokenTypes.ANNOTATION_DEF:
            case TokenTypes.RECORD_DEF:
                checkMissingDisplayNameForClass();
                context.leaveType();
                suppressionTracker.leaveScope();
                break;
            default:
                break;
        }
    }

    private boolean shouldSkipFile(FileContents fileContents) {
        if (fileContents == null) {
            return false;
        }
        if (isExcludedPath(fileContents.getFileName())) {
            return true;
        }
        String[] lines = fileContents.getLines();
        if (lines == null) {
            return false;
        }
        boolean inJavadoc = false;
        for (String raw : lines) {
            if (raw == null) {
                continue;
            }
            String line = raw;
            if (!inJavadoc) {
                int start = line.indexOf("/**");
                if (start < 0) {
                    continue;
                }
                inJavadoc = true;
                line = line.substring(start + 3);
            }
            int end = line.indexOf("*/");
            String contentLine = end >= 0 ? line.substring(0, end) : line;
            String content = trimJavadocLine(contentLine);
            if (!content.isEmpty()) {
                return content.startsWith(GENERATED_JAVADOC_PREFIX);
            }
            if (end >= 0) {
                return false;
            }
        }
        return false;
    }

    private boolean isExcludedPath(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        if (excludedPathFragments.isEmpty() && excludedPathPatterns.isEmpty()) {
            return false;
        }
        String normalized = normalizePath(fileName);
        for (String fragment : excludedPathFragments) {
            if (normalized.contains(fragment)) {
                return true;
            }
        }
        for (java.util.regex.Pattern pattern : excludedPathPatterns) {
            if (pattern.matcher(normalized).find()) {
                return true;
            }
        }
        return false;
    }

    private String normalizePath(String path) {
        return path == null ? "" : path.replace('\\', '/');
    }

    String globToRegex(String glob) {
        StringBuilder builder = new StringBuilder();
        int start = 0;
        for (int i = 0; i < glob.length(); i++) {
            char ch = glob.charAt(i);
            if (ch == '*' || ch == '?') {
                if (i > start) {
                    builder.append(java.util.regex.Pattern.quote(glob.substring(start, i)));
                }
                builder.append(ch == '*' ? ".*" : ".");
                start = i + 1;
            }
        }
        if (start < glob.length()) {
            builder.append(java.util.regex.Pattern.quote(glob.substring(start)));
        }
        return builder.toString();
    }

    Set<String> collectDeclaredMethods(DetailAST rootAst) {
        if (rootAst == null) {
            return java.util.Collections.emptySet();
        }
        Set<String> names = new HashSet<>();
        ArrayDeque<DetailAST> stack = new ArrayDeque<>();
        stack.push(rootAst);
        while (!stack.isEmpty()) {
            DetailAST current = stack.pop();
            if (current.getType() == TokenTypes.METHOD_DEF) {
                DetailAST ident = current.findFirstToken(TokenTypes.IDENT);
                if (ident != null) {
                    names.add(ident.getText());
                }
            }
            DetailAST child = current.getFirstChild();
            while (child != null) {
                stack.push(child);
                child = child.getNextSibling();
            }
        }
        return names;
    }

    Map<String, Set<Integer>> collectDeclaredMethodArities(DetailAST rootAst) {
        if (rootAst == null) {
            return java.util.Collections.emptyMap();
        }
        Map<String, Set<Integer>> arities = new HashMap<>();
        ArrayDeque<DetailAST> stack = new ArrayDeque<>();
        stack.push(rootAst);
        while (!stack.isEmpty()) {
            DetailAST current = stack.pop();
            if (current.getType() == TokenTypes.METHOD_DEF) {
                DetailAST ident = current.findFirstToken(TokenTypes.IDENT);
                if (ident != null) {
                    arities.computeIfAbsent(ident.getText(), key -> new HashSet<>())
                            .add(parameterCount(current));
                }
            }
            DetailAST child = current.getFirstChild();
            while (child != null) {
                stack.push(child);
                child = child.getNextSibling();
            }
        }
        return arities;
    }

    private int parameterCount(DetailAST methodDef) {
        DetailAST parameters = methodDef.findFirstToken(TokenTypes.PARAMETERS);
        if (parameters == null) {
            return 0;
        }
        int count = 0;
        DetailAST child = parameters.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.PARAMETER_DEF) {
                count++;
            }
            child = child.getNextSibling();
        }
        return count;
    }

    private String trimJavadocLine(String raw) {
        String line = raw.trim();
        if (line.startsWith("*")) {
            line = line.substring(1).trim();
        }
        return line;
    }

    private void checkMissingDisplayName() {
        if (context.isCurrentMethodTest()) {
            context.markCurrentClassHasJUnit5Tests();
        }
        if (context.isCurrentMethodTest() && !context.currentMethodHasDisplayName()) {
            String methodName = context.currentMethodName();
            int lineNo = context.currentMethodLineNo();
            if (methodName != null && lineNo > 0) {
                violationCollector.record(lineNo, RuleId.MISSING_DISPLAY_NAME, methodName);
                recordManualAdvice(lineNo, RuleId.MISSING_DISPLAY_NAME,
                        AdviceSource.ANNOTATION_DISPLAY_NAME, null, null);
            }
        }
    }

    private void checkMissingDisplayNameForClass() {
        if (context.currentClassHasJUnit5Tests() && !context.currentClassHasDisplayName()) {
            int lineNo = context.currentClassLineNo();
            if (lineNo <= 0) {
                lineNo = 1;
            }
            String className = context.currentClassName();
            String name = className == null ? "unknown" : className;
            violationCollector.record(lineNo, RuleId.MISSING_DISPLAY_NAME, name);
            recordManualAdvice(lineNo, RuleId.MISSING_DISPLAY_NAME,
                    AdviceSource.ANNOTATION_DISPLAY_NAME, null, null);
        }
    }

    private void checkTestAnnotationOrder() {
        if (!context.currentMethodHasTestAnnotation()) {
            return;
        }
        String firstAnnotation = context.currentMethodFirstAnnotationName();
        if (firstAnnotation == null || context.isJUnit5TestAnnotation(firstAnnotation)) {
            return;
        }
        int lineNo = context.currentMethodTestAnnotationLine();
        if (lineNo <= 0) {
            lineNo = context.currentMethodLineNo();
        }
        if (lineNo <= 0) {
            lineNo = 1;
        }
        String methodName = context.currentMethodName();
        String name = methodName == null ? "unknown" : methodName;
        violationCollector.record(lineNo, RuleId.TEST_ANNOTATION_ORDER,
                firstAnnotation, name);
        recordManualAdvice(lineNo, RuleId.TEST_ANNOTATION_ORDER,
                AdviceSource.ANNOTATION_TEST_ORDER, null, null);
    }

    private void checkMapStringObjectUsage(DetailAST varDef) {
        if (varDef == null || context == null || commentExtractor == null) {
            return;
        }
        DetailAST type = varDef.findFirstToken(TokenTypes.TYPE);
        if (type == null) {
            return;
        }
        String mapType = matchMapStringObjectType(type);
        if (mapType == null) {
            return;
        }
        int lineNo = varDef.getLineNo();
        if (lineNo <= 0) {
            lineNo = 1;
        }
        if (!mapStringObjectLines.add(lineNo)) {
            return;
        }
        CommentMessageExtractor.ReasonComment comment = commentExtractor.findReasonComment(lineNo);
        if (comment.isMultiple()) {
            return;
        }
        if (comment.message() != null) {
            String message = comment.message();
            MessageCandidate candidate = new MessageCandidate.Builder()
                    .source(MessageSource.COMMENT)
                    .lineNo(lineNo)
                    .message(message)
                    .normalisedMessage(MessageNormaliser.normalise(message))
                    .placeholderCount(0)
                    .keyValueLabelCount(0)
                    .build();
            emitCandidate(candidate);
            return;
        }
        String name = extractVariableName(varDef);
        if (name == null || name.isEmpty()) {
            name = "map";
        }
        if (violationCollector != null) {
            violationCollector.record(lineNo, RuleId.MAP_STRING_OBJECT, name, mapType);
        }
        recordManualAdvice(lineNo, RuleId.MAP_STRING_OBJECT, AdviceSource.COMMENT, name, mapType);
    }

    private String extractVariableName(DetailAST varDef) {
        DetailAST child = varDef.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.IDENT) {
                return child.getText();
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private String matchMapStringObjectType(DetailAST typeAst) {
        String typeName = context.astSupport().extractTypeName(typeAst);
        if (typeName == null) {
            return null;
        }
        String resolved = context.resolveTypeName(typeName);
        if (!"java.util.Map".equals(resolved) && !"Map".equals(resolved)) {
            return null;
        }
        DetailAST typeArgs = typeAst.findFirstToken(TokenTypes.TYPE_ARGUMENTS);
        if (typeArgs == null) {
            return null;
        }
        List<DetailAST> args = collectTypeArguments(typeArgs);
        if (args.size() != 2) {
            return null;
        }
        if (!isStringTypeArgument(args.get(0))) {
            return null;
        }
        if (isWildcardTypeArgument(args.get(1))) {
            return "Map<String, ?>";
        }
        if (isStringTypeArgument(args.get(1))) {
            return "Map<String, String>";
        }
        if (isObjectTypeArgument(args.get(1))) {
            return "Map<String, Object>";
        }
        return null;
    }

    private List<DetailAST> collectTypeArguments(DetailAST typeArgs) {
        List<DetailAST> args = new ArrayList<>();
        DetailAST child = typeArgs.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.TYPE_ARGUMENT) {
                args.add(child);
            }
            child = child.getNextSibling();
        }
        return args;
    }

    private boolean isStringTypeArgument(DetailAST typeArg) {
        String name = extractTypeArgumentName(typeArg);
        if (name == null) {
            return false;
        }
        String resolved = context.resolveTypeName(name);
        return "java.lang.String".equals(resolved) || "String".equals(resolved);
    }

    private boolean isObjectTypeArgument(DetailAST typeArg) {
        String name = extractTypeArgumentName(typeArg);
        if (name == null) {
            return false;
        }
        String resolved = context.resolveTypeName(name);
        return "java.lang.Object".equals(resolved) || "Object".equals(resolved);
    }

    private boolean isWildcardTypeArgument(DetailAST typeArg) {
        if (typeArg.getType() == TokenTypes.WILDCARD_TYPE
                || typeArg.getType() == TokenTypes.QUESTION) {
            return true;
        }
        return typeArg.findFirstToken(TokenTypes.WILDCARD_TYPE) != null
                || typeArg.findFirstToken(TokenTypes.QUESTION) != null;
    }

    private String extractTypeArgumentName(DetailAST typeArg) {
        DetailAST dot = typeArg.findFirstToken(TokenTypes.DOT);
        if (dot != null) {
            return context.astSupport().flattenDot(dot);
        }
        DetailAST ident = typeArg.findFirstToken(TokenTypes.IDENT);
        return ident != null ? ident.getText() : null;
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
            String role = resolveExtractionRole(candidate);
            writeMessageExtractionRecord(message, candidate.lineNo(), metrics,
                    candidate.placeholderCount(), candidate.keyValueLabelCount(),
                    candidate.source(), role);
            if (messagePrefilter != null && messagePrefilter.shouldSkip(message)) {
                return;
            }
            recordOverusedWordUsage(metrics, candidate.lineNo());
            recordPurposeCueUsage(message);
            recordEntropyWordUsage(candidate, message);
        }
        if (ruleEngine != null) {
            ruleEngine.evaluate(candidate, metrics, context.currentClassName(),
                    context.currentMethodName(), verbose, suppressionTracker,
                    violationCollector, adviceEmitter);
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
        emitMissingMessage(lineNo, source, null, null);
    }

    @Override
    public void emitMissingMessage(int lineNo, MessageSource source,
                                   MissingMessageKind missingMessageKind) {
        emitMissingMessage(lineNo, source, null, missingMessageKind);
    }

    @Override
    public void emitMissingMessage(int lineNo, MessageSource source,
                                   AdviceSource adviceSource,
                                   MissingMessageKind missingMessageKind) {
        MessageCandidate candidate = new MessageCandidate.Builder()
                .source(source)
                .adviceSource(adviceSource)
                .lineNo(lineNo)
                .missingMessage(true)
                .missingMessageKind(missingMessageKind)
                .build();
        emitCandidate(candidate);
    }

    @Override
    public void emitUnhandled(DetailAST ast, String reason) {
        if (!emitUnhandled || violationCollector == null) {
            return;
        }
        int lineNo = ast == null ? 0 : ast.getLineNo();
        if (lineNo <= 0 && context != null) {
            lineNo = context.currentMethodLineNo();
        }
        if (lineNo <= 0) {
            lineNo = 1;
        }
        String detail = reason == null || reason.trim().isEmpty() ? "unknown" : reason;
        violationCollector.record(lineNo, RuleId.UNHANDLED, detail, buildScope());
        if (suppressionTracker == null
                || (!suppressionTracker.isSuppressed(RuleId.UNHANDLED, lineNo)
                && !suppressionTracker.isSuppressed(AdviceId.MMUnhandled, lineNo))) {
            java.util.List<String> items = new java.util.ArrayList<>(1);
            items.add(detail);
            recordFileAdvice(new FileAdviceDetails(AdviceId.MMUnhandled, lineNo, items,
                    null, null, null, null, null, null));
        }
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
        messageExtractionTarget = outputPath.toString();
        try {
            // Always open in APPEND mode — appending to a new file is safe
            messageExtractionWriter = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            if (Files.size(outputPath) == 0L) {
                messageExtractionWriter.write("file\tline\tsource\trole\tchars\twords\tmeaningful\tplaceholders\tkeyValueLabels\ttotalWords\teffectiveMeaningful\tmessage");
                messageExtractionWriter.newLine();
                messageExtractionWriter.flush();
            }
        } catch (IOException e) {
            recordMessageExtractionFailure(1, "Unable to open message extraction file: " + outputPath);
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
            recordMessageExtractionFailure(1, "Unable to close message extraction file: " + extractionTarget());
        } finally {
            messageExtractionWriter = null;
        }
    }

    void writeMessageExtractionRecord(String message, int lineNo,
                                      MessageMetrics metrics, int placeholderCount,
                                      int keyValueLabelCount, MessageSource source,
                                      String role) {
        if (messageExtractionWriter == null || messageExtractionFailureDetail != null) {
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
                .append(role == null ? "unknown" : role).append('\t')
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
            recordMessageExtractionFailure(lineNo,
                    "Unable to write message extraction record: " + extractionTarget());
            closeMessageExtractionWriter();
        }
    }

    private void recordMessageExtractionFailure(int lineNo, String detail) {
        if (messageExtractionFailureDetail != null) {
            return;
        }
        String resolvedDetail = detail == null || detail.trim().isEmpty() ? "unknown" : detail;
        messageExtractionFailureDetail = resolvedDetail;
        messageExtractionFailureLine = lineNo > 0 ? lineNo : 1;
        messageExtractionWriter = null;
    }

    private void emitMessageExtractionFailure(AbstractCheck check) {
        if (messageExtractionFailureDetail == null || check == null) {
            return;
        }
        int lineNo = messageExtractionFailureLine > 0 ? messageExtractionFailureLine : 1;
        check.log(lineNo, RuleId.messageKey("assert.message.extraction.failure", verbose),
                messageExtractionFailureDetail);
        messageExtractionFailureDetail = null;
        messageExtractionFailureLine = 0;
    }

    String extractionTarget() {
        if (messageExtractionTarget != null && !messageExtractionTarget.isEmpty()) {
            return messageExtractionTarget;
        }
        if (messageExtractionFile != null && !messageExtractionFile.trim().isEmpty()) {
            return messageExtractionFile.trim();
        }
        String fromProperty = System.getProperty("mm.extract.file");
        return fromProperty == null || fromProperty.trim().isEmpty() ? "unknown" : fromProperty.trim();
    }

    String resolveExtractionRole(MessageCandidate candidate) {
        if (candidate == null || candidate.source() == null) {
            return "unknown";
        }
        if (candidate.source() == MessageSource.ASSERTION) {
            if (candidate.assertAllHeading()) {
                return "assert_all_heading";
            }
            if (candidate.assertJOverride()) {
                return "assert_override";
            }
            if (candidate.trivialSupplierDescription() != null) {
                return "assert_supplier";
            }
            return "assert_message";
        }
        if (candidate.source() == MessageSource.PRECONDITION) {
            return "precondition_message";
        }
        if (candidate.source() == MessageSource.THROW) {
            return "throw_message";
        }
        if (candidate.source() == MessageSource.LOG) {
            return "log_message";
        }
        if (candidate.source() == MessageSource.ANNOTATION) {
            return "annotation_value";
        }
        if (candidate.source() == MessageSource.COMMENT) {
            return "comment_text";
        }
        if (candidate.source() == MessageSource.JAVADOC_CLASS) {
            return "javadoc_class";
        }
        if (candidate.source() == MessageSource.JAVADOC_MEMBER) {
            return "javadoc_member";
        }
        return "unknown";
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

    String formatRuleSummary(Map<RuleId, Integer> summary) {
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

    private void recordOverusedWordUsage(MessageMetrics metrics, int lineNo) {
        if (metrics == null) {
            return;
        }
        fileMessageCount++;
        overusedWordMessageCount++;
        if (fileFirstMessageLine == 0 && lineNo > 0) {
            fileFirstMessageLine = lineNo;
        }
        List<String> words = metrics.meaningfulWords();
        if (words.isEmpty()) {
            return;
        }
        for (String word : words) {
            if (word == null || word.isEmpty()) {
                continue;
            }
            String normalised = word.toLowerCase(java.util.Locale.ROOT);
            overusedWordCounts.merge(normalised, 1, Integer::sum);
        }
    }

    private void recordPurposeCueUsage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        purposeCueCount += countPurposeCues(message);
    }

    private void recordFileNameWords(FileContents contents) {
        if (contents == null || metricsCalculator == null) {
            return;
        }
        String fileName = contents.getFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            return;
        }
        String baseName = baseFileName(fileName);
        if (baseName.isEmpty()) {
            return;
        }
        String[] words = metricsCalculator.splitWords(splitFileNameWords(baseName));
        boolean hasTokens = false;
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            hasTokens = true;
            String token = word.toLowerCase(java.util.Locale.ROOT);
            entropyWordCounts.merge(token, 1, Integer::sum);
            entropyWordTotal++;
            if (!metricsCalculator.isFillerWord(word)) {
                overusedWordCounts.merge(token, 1, Integer::sum);
            }
        }
        if (hasTokens) {
            overusedWordMessageCount++;
        }
    }

    private String baseFileName(String fileName) {
        int slash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        String name = slash >= 0 ? fileName.substring(slash + 1) : fileName;
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            return name.substring(0, dot);
        }
        return name;
    }

    private String splitFileNameWords(String baseName) {
        StringBuilder builder = new StringBuilder(baseName.length() + 8);
        char prev = 0;
        for (int i = 0; i < baseName.length(); i++) {
            char ch = baseName.charAt(i);
            if (!Character.isLetterOrDigit(ch)) {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != ' ') {
                    builder.append(' ');
                }
                prev = 0;
                continue;
            }
            if (prev != 0 && Character.isLowerCase(prev) && Character.isUpperCase(ch)) {
                builder.append(' ');
            }
            builder.append(ch);
            prev = ch;
        }
        return builder.toString();
    }

    private void recordEntropyWordUsage(MessageCandidate candidate, String message) {
        if (candidate == null || candidate.argumentNameMessage()) {
            return;
        }
        if (metricsCalculator == null || message == null || message.trim().isEmpty()) {
            return;
        }
        String[] words = metricsCalculator.splitWords(message);
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            String token = word.toLowerCase(java.util.Locale.ROOT);
            entropyWordCounts.merge(token, 1, Integer::sum);
            entropyWordTotal++;
        }
    }

    int countPurposeCues(String message) {
        if (metricsCalculator == null) {
            return 0;
        }
        String[] words = metricsCalculator.splitWords(message);
        List<String> tokens = new ArrayList<>(words.length);
        for (String word : words) {
            if (word == null || word.isEmpty()) {
                continue;
            }
            tokens.add(word.toLowerCase(java.util.Locale.ROOT));
        }
        if (tokens.size() < 3) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if ("so".equals(token) && i + 1 < tokens.size()
                    && "that".equals(tokens.get(i + 1))) {
                count++;
                i++;
                continue;
            }
            if ("in".equals(token) && i + 1 < tokens.size()
                    && "order".equals(tokens.get(i + 1))) {
                count++;
                i++;
                continue;
            }
            if (i == tokens.size() - 1) {
                continue;
            }
            if (isPurposeCue(token)) {
                count++;
            }
        }
        return count;
    }

    private boolean isPurposeCue(String token) {
        if (token.startsWith(CONSEQUENT_ROOT)) {
            return true;
        }
        return PURPOSE_CUES.contains(token);
    }

    private void emitOverusedWordWarning() {
        if (violationCollector == null || overusedWordCounts.isEmpty()) {
            return;
        }
        if (fileMessageCount < OVERUSED_WORD_MIN_MESSAGES) {
            return;
        }
        int lineNo = fileFirstMessageLine > 0 ? fileFirstMessageLine : 1;
        if (suppressionTracker != null
                && suppressionTracker.isSuppressedInFile(RuleId.OVERUSED_WORD, lineNo)) {
            return;
        }
        int messageCount = overusedWordMessageCount == 0 ? fileMessageCount : overusedWordMessageCount;
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : overusedWordCounts.entrySet()) {
            if (entry.getValue() * 2 > messageCount) {
                entries.add(entry);
            }
        }
        if (entries.isEmpty()) {
            return;
        }
        entries.sort(Comparator
                .comparingInt((Map.Entry<String, Integer> entry) -> entry.getValue())
                .reversed()
                .thenComparing(Map.Entry::getKey));
        StringBuilder builder = new StringBuilder(128);
        for (Map.Entry<String, Integer> entry : entries) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey())
                    .append('(')
                    .append(entry.getValue())
                    .append('/')
                    .append(messageCount)
                    .append(')');
        }
        violationCollector.record(lineNo, RuleId.OVERUSED_WORD,
                builder.toString(), messageCount);
        List<String> overusedWords = new ArrayList<>(entries.size());
        for (Map.Entry<String, Integer> entry : entries) {
            overusedWords.add(entry.getKey());
        }
        recordFileAdvice(new FileAdviceDetails(AdviceId.MMOverusedWord, lineNo, overusedWords,
                messageCount, null, null, null, null, null));
    }

    private void emitLacksPurposeWarning() {
        if (violationCollector == null) {
            return;
        }
        if (fileMessageCount == 0) {
            return;
        }
        int lineNo = fileFirstMessageLine > 0 ? fileFirstMessageLine : 1;
        if (suppressionTracker != null
                && suppressionTracker.isSuppressedInFile(RuleId.LACKS_PURPOSE, lineNo)) {
            return;
        }
        int expectedMin = Math.max(1, (int) Math.round((double) fileMessageCount / PURPOSE_CUE_RATIO));
        if (purposeCueCount >= expectedMin) {
            return;
        }
        violationCollector.record(lineNo, RuleId.LACKS_PURPOSE,
                purposeCueCount, expectedMin, fileMessageCount);
        recordFileAdvice(new FileAdviceDetails(AdviceId.MMLacksPurpose, lineNo,
                java.util.Collections.emptyList(), null, purposeCueCount,
                expectedMin, fileMessageCount, null, null));
    }

    /**
     * Emits a low-entropy warning only when no other violations exist for the file.
     * <p>
     * Low entropy (Shannon word entropy below {@link #MIN_WORD_SHANNON_ENTROPY}) is a
     * soft signal that messages across the file lack vocabulary diversity. It is deliberately
     * suppressed when harder violations are already present, because those violations
     * produce more actionable advice and low entropy is usually a symptom rather than a
     * root cause. This coupling is intentional: fixing the real violations typically
     * raises entropy as a side effect.
     */
    private void emitLowEntropyWarning() {
        if (violationCollector == null) {
            return;
        }
        if (violationCollector.hasViolations()) {
            return;
        }
        if (fileMessageCount == 0) {
            return;
        }
        if (entropyWordTotal == 0) {
            return;
        }
        int lineNo = fileFirstMessageLine > 0 ? fileFirstMessageLine : 1;
        if (suppressionTracker != null
                && suppressionTracker.isSuppressedInFile(RuleId.LOW_ENTROPY, lineNo)) {
            return;
        }
        double entropy = shannonEntropy(entropyWordCounts, entropyWordTotal);
        if (entropy >= MIN_WORD_SHANNON_ENTROPY) {
            return;
        }
        violationCollector.record(lineNo, RuleId.LOW_ENTROPY,
                formatEntropy(entropy), formatEntropy(MIN_WORD_SHANNON_ENTROPY));
        recordFileAdvice(new FileAdviceDetails(AdviceId.MMLowEntropy, lineNo,
                java.util.Collections.emptyList(), null, null, null, null,
                entropy, MIN_WORD_SHANNON_ENTROPY));
    }

    private void emitJUnit4MigrationWarnings() {
        if (violationCollector == null || context == null) {
            return;
        }
        if (context.hasJUnit4AnnotationUsage()) {
            int lineNo = context.junit4AnnotationLine();
            if (lineNo <= 0) {
                lineNo = 1;
            }
            String name = context.junit4AnnotationName();
            if (name == null || name.isEmpty()) {
                name = "Test";
            }
            violationCollector.record(lineNo, RuleId.JUNIT4_ANNOTATION, name);
            recordManualAdvice(lineNo, RuleId.JUNIT4_ANNOTATION,
                    AdviceSource.ANNOTATION_JUNIT4, name, null);
        }
        if (context.hasJUnit4AssertionUsage()) {
            int lineNo = context.junit4AssertionLine();
            if (lineNo <= 0) {
                lineNo = 1;
            }
            violationCollector.record(lineNo, RuleId.JUNIT4_ASSERTION);
            recordManualAdvice(lineNo, RuleId.JUNIT4_ASSERTION,
                    AdviceSource.ANNOTATION_JUNIT4, null, null);
        }
    }

    private MessageMetrics computeMessageMetrics(String message, int placeholderCount, int keyValueLabelCount) {
        if (metricsCalculator == null) {
            return new MessageMetrics(0, 0, 0, placeholderCount,
                    placeholderCount, java.util.Collections.emptyList(),
                    java.util.Collections.emptyList());
        }
        return metricsCalculator.calculate(message, placeholderCount, keyValueLabelCount);
    }

    private double shannonEntropy(Map<String, Integer> counts, int total) {
        if (total <= 0) {
            return 0.0;
        }
        double entropy = 0.0;
        for (Integer count : counts.values()) {
            double p = (double) count / total;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    private String formatEntropy(double entropy) {
        return String.format(java.util.Locale.ROOT, "%.2f", entropy);
    }

    String buildScope() {
        if (context == null) {
            return "unknown";
        }
        String className = context.currentClassName();
        String methodName = context.currentMethodName();
        if (className == null || className.isEmpty()) {
            return methodName == null || methodName.isEmpty() ? "unknown" : methodName;
        }
        if (methodName == null || methodName.isEmpty()) {
            return className;
        }
        return className + "#" + methodName;
    }

    private void recordManualAdvice(int lineNo, RuleId ruleId, AdviceSource adviceSource,
                                    String messageLiteral, String messageExpr) {
        if (adviceCollector == null || ruleId == null || adviceSource == null) {
            return;
        }
        AdviceId adviceId = AdviceId.forRule(ruleId, adviceSource);
        if (adviceId == AdviceId.UNKNOWN) {
            throw new IllegalStateException("Missing AdviceId mapping for " + ruleId + " and " + adviceSource);
        }
        if (suppressionTracker != null) {
            if (suppressionTracker.isSuppressed(ruleId, lineNo)
                    || suppressionTracker.isSuppressed(adviceId, lineNo)) {
                return;
            }
        }
        CandidateAdvice advice = new CandidateAdvice.Builder()
                .fileName(currentFileName)
                .lineNo(lineNo)
                .source(adviceSource)
                .adviceId(adviceId)
                .ruleId(ruleId)
                .messageLiteral(messageLiteral)
                .messageExpr(messageExpr)
                .build();
        adviceCollector.record(advice);
    }

    private void recordFileAdvice(FileAdviceDetails details) {
        if (adviceCollector == null || details == null) {
            return;
        }
        AdviceId adviceId = details.adviceId();
        if (adviceId == AdviceId.UNKNOWN) {
            throw new IllegalStateException("Missing AdviceId mapping for file advice");
        }
        if (suppressionTracker != null) {
            RuleId ruleId = adviceId.ruleId();
            int lineNo = details.lineNo();
            if (ruleId != null && suppressionTracker.isSuppressedInFile(ruleId, lineNo)) {
                return;
            }
            if (suppressionTracker.isSuppressedInFile(adviceId, lineNo)) {
                return;
            }
        }
        adviceCollector.recordFileAdvice(currentFileName, details);
    }

    MessageExtractionContext contextForTesting() {
        return context;
    }

    void recordViolationForTesting(int lineNo, RuleId ruleId) {
        if (violationCollector != null) {
            violationCollector.record(lineNo, ruleId);
        }
    }

    void setMessageExtractionWriterForTesting(BufferedWriter writer) {
        this.messageExtractionWriter = writer;
    }

    BufferedWriter messageExtractionWriterForTesting() {
        return messageExtractionWriter;
    }

    void setMessageExtractionTargetForTesting(String target) {
        this.messageExtractionTarget = target;
    }

    void setMessageExtractionFailureDetailForTesting(String detail) {
        this.messageExtractionFailureDetail = detail;
    }

    String messageExtractionFailureDetailForTesting() {
        return messageExtractionFailureDetail;
    }

    Map<String, Integer> overusedWordCountsForTesting() {
        return java.util.Collections.unmodifiableMap(overusedWordCounts);
    }

    Map<String, Integer> entropyWordCountsForTesting() {
        return java.util.Collections.unmodifiableMap(entropyWordCounts);
    }

    int entropyWordTotalForTesting() {
        return entropyWordTotal;
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
        return MessageAstSupport.normalizeClassName(className);
    }
}
