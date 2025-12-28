/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import net.openhft.quality.mm.MeaningfulMessageProcessor;

/**
 * Checkstyle check that enforces unique and meaningful messages for assertions,
 * preconditions, thrown exceptions, and JUnit annotation descriptions within each Java file.
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
    private final MeaningfulMessageProcessor processor = new MeaningfulMessageProcessor();

    /**
     * Create the check with default processor settings.
     */
    public MeaningfulMessageCheck() {
    }

    /**
     * Enable verbose reporting for rule evaluation.
     *
     * @param verbose {@code true} to include verbose details in violations.
     */
    public void setVerbose(boolean verbose) {
        processor.setVerbose(verbose);
    }

    /**
     * Configure an optional output file for extracted message data.
     *
     * @param messageExtractionFile path to the extraction file, or {@code null} to disable.
     */
    public void setMessageExtractionFile(String messageExtractionFile) {
        processor.setMessageExtractionFile(messageExtractionFile);
    }

    /**
     * Configure exception class names that should be ignored for message checks.
     *
     * @param ignoredExceptionClassNames comma or whitespace separated class names.
     */
    public void setIgnoredExceptionClassNames(String ignoredExceptionClassNames) {
        processor.setIgnoredExceptionClassNames(ignoredExceptionClassNames);
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

    @SuppressWarnings("deprecation")
    @Override
    public void beginTree(DetailAST rootAST) {
        processor.beginTree(getFileContents());
    }

    @Override
    public void finishTree(DetailAST rootAST) {
        processor.finishTree(this);
    }

    @Override
    public void visitToken(DetailAST ast) {
        processor.visitToken(ast);
    }

    @Override
    public void leaveToken(DetailAST ast) {
        processor.leaveToken(ast);
    }
}
