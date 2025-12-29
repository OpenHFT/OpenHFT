/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Base class for message extractors providing common context and sink access.
 */
public abstract class AbstractMessageExtractor {
    private final MessageExtractionContext context;
    private final MessageCandidateSink sink;
    private final MessageAstSupport astSupport;

    /**
     * Create an extractor with the given context and sink.
     *
     * @param context extraction context with imports and type information.
     * @param sink    sink to receive message candidates.
     */
    protected AbstractMessageExtractor(MessageExtractionContext context,
                                       MessageCandidateSink sink) {
        this.context = context;
        this.sink = sink;
        this.astSupport = context.astSupport();
    }

    /**
     * Return the extraction context.
     *
     * @return the extraction context.
     */
    protected final MessageExtractionContext context() {
        return context;
    }

    /**
     * Return the candidate sink.
     *
     * @return the candidate sink.
     */
    protected final MessageCandidateSink sink() {
        return sink;
    }

    /**
     * Return AST support utilities.
     *
     * @return AST support utilities.
     */
    protected final MessageAstSupport astSupport() {
        return astSupport;
    }
}
