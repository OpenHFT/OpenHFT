/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import net.openhft.quality.mm.MeaningfulMessageProcessor;
import net.openhft.quality.mm.RuleId;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checkstyle check that enforces unique and meaningful messages in assertions,
 * preconditions, thrown exceptions, and JUnit annotation descriptions within each Java file
 * because reports should explain intent so that triage stays clear.
 * <p>
 * This check flags:
 * <ul>
 *     <li>Duplicate messages within the same file</li>
 *     <li>Messages containing the class name (redundant - shown in stack trace)</li>
 *     <li>Messages containing the method/test name (redundant - shown in stack trace)</li>
 *     <li>Messages containing line numbers (redundant - shown in stack trace)</li>
 *     <li>Trivial Supplier&lt;String&gt; lambdas that return constants or simple concatenations (defeats lazy evaluation)</li>
 * </ul>
 * <p>
 * Configure in checkstyle.xml:
 * <pre>
 * &lt;module name="net.openhft.quality.MeaningfulMessageCheck"/&gt;
 * </pre>
 */
public class MeaningfulMessageCheck extends AbstractCheck {
    private static final Logger LOG = Logger.getLogger(MeaningfulMessageCheck.class.getName());
    private final MeaningfulMessageProcessor processor;
    private boolean verbose;
    private boolean dryRun;
    private String jsonlOutput;
    private String rankOut;
    private boolean warnLegacySuppressions = true;
    private String excludedPaths;

    /**
     * Create the check with default processor settings.
     */
    public MeaningfulMessageCheck() {
        processor = new MeaningfulMessageProcessor();
    }

    /**
     * Create the check with a caller-supplied processor to allow injection and configuration control.
     *
     * @param processor meaningful message processor.
     */
    public MeaningfulMessageCheck(MeaningfulMessageProcessor processor) {
        this.processor = Objects.requireNonNull(processor, "processor");
    }

    /**
     * Enable verbose reporting during rule evaluation and message template rendering.
     *
     * @param verbose {@code true} to include verbose details in violations.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        processor.setVerbose(verbose);
    }

    /**
     * Enable dry-run mode to generate advice ranking.
     *
     * @param dryRun {@code true} for dry-run mode.
     */
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
        processor.setDryRun(dryRun);
    }

    /**
     * Configure JSONL output to emit aggregated advice.
     *
     * @param jsonlOutput output path, or {@code null} to disable.
     */
    public void setJsonl(String jsonlOutput) {
        this.jsonlOutput = jsonlOutput;
        processor.setJsonlOutput(jsonlOutput);
    }

    /**
     * Configure the output path used to generate advice ranks.
     *
     * @param rankOut output path, or {@code null} to use defaults.
     */
    public void setRankOut(String rankOut) {
        this.rankOut = rankOut;
        processor.setRankOut(rankOut);
    }

    /**
     * Enable warnings about legacy RuleId suppressions.
     *
     * @param warnLegacySuppressions {@code true} to emit warnings.
     */
    public void setWarnLegacySuppressions(boolean warnLegacySuppressions) {
        this.warnLegacySuppressions = warnLegacySuppressions;
        processor.setWarnLegacySuppressions(warnLegacySuppressions);
    }

    /**
     * Enable warnings about unhandled extraction cases.
     *
     * @param emitUnhandled {@code true} to emit unhandled warnings.
     */
    public void setEmitUnhandled(boolean emitUnhandled) {
        processor.setEmitUnhandled(emitUnhandled);
    }

    /**
     * Configure an optional output file to capture extracted message data.
     *
     * @param messageExtractionFile path to the extraction file, or {@code null} to disable.
     */
    public void setMessageExtractionFile(String messageExtractionFile) {
        processor.setMessageExtractionFile(messageExtractionFile);
    }

    /**
     * Configure exception class names that should be ignored during message checks.
     *
     * @param ignoredExceptionClassNames comma or whitespace separated class names.
     */
    public void setIgnoredExceptionClassNames(String ignoredExceptionClassNames) {
        processor.setIgnoredExceptionClassNames(ignoredExceptionClassNames);
    }

    /**
     * Configure file exclusions by path fragment, glob, or regex (prefix with {@code regex:}).
     *
     * @param excludedPaths comma/whitespace separated path patterns.
     */
    public void setExcludedPaths(String excludedPaths) {
        this.excludedPaths = excludedPaths;
        processor.setExcludedPaths(excludedPaths);
    }

    @Override
    public int[] getDefaultTokens() {
        return processor.getDefaultTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return processor.getAcceptableTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return processor.getRequiredTokens();
    }

    @Override
    public void init() {
        super.init();
        processor.setVerbose(verbose);
        processor.setDryRun(dryRun);
        processor.setJsonlOutput(jsonlOutput);
        processor.setRankOut(rankOut);
        processor.setWarnLegacySuppressions(warnLegacySuppressions);
        processor.setExcludedPaths(excludedPaths);
        processor.beginRun();
    }

    @Override
    public void destroy() {
        processor.finishRun();
        super.destroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void beginTree(DetailAST rootAST) {
        try {
            processor.beginTree(getFileContents(), rootAST);
        } catch (RuntimeException e) {
            logUnexpected(rootAST, e);
        }
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        try {
            processor.finishTree(this);
        } catch (RuntimeException e) {
            logUnexpected(rootAST, e);
        }
    }

    @Override
    public void visitToken(DetailAST ast) {
        try {
            processor.visitToken(ast);
        } catch (RuntimeException e) {
            logUnexpected(ast, e);
        }
    }

    @Override
    public void leaveToken(DetailAST ast) {
        try {
            processor.leaveToken(ast);
        } catch (RuntimeException e) {
            logUnexpected(ast, e);
        }
    }

    void logUnexpected(DetailAST ast, RuntimeException exception) {
        LOG.log(Level.SEVERE, "Unexpected exception in MeaningfulMessageCheck", exception);
        String message = exception.toString();
        int lineNo = ast == null ? 0 : ast.getLineNo();
        int resolvedLine = lineNo > 0 ? lineNo : 1;
        log(resolvedLine, RuleId.messageKey("assert.message.unexpected.exception", verbose), message);
    }
}
