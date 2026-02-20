/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import java.util.Objects;

/**
 * Detector-emitted advice candidate prior to ranking and aggregation.
 */
public final class CandidateAdvice {
    private final String fileName;
    private final int lineNo;
    private final AdviceSource source;
    private final AdviceId adviceId;
    private final RuleId ruleId;
    private final String messageLiteral;
    private final String messageExpr;
    private final String snippet;
    private final AdviceMetrics metrics;

    private CandidateAdvice(Builder builder) {
        this.fileName = builder.fileName;
        this.lineNo = builder.lineNo;
        this.source = builder.source;
        this.adviceId = builder.adviceId;
        this.ruleId = builder.ruleId;
        this.messageLiteral = builder.messageLiteral;
        this.messageExpr = builder.messageExpr;
        this.snippet = builder.snippet;
        this.metrics = builder.metrics;
    }

    public String fileName() {
        return fileName;
    }

    public int lineNo() {
        return lineNo;
    }

    public AdviceSource source() {
        return source;
    }

    public AdviceId adviceId() {
        return adviceId;
    }

    public RuleId ruleId() {
        return ruleId;
    }

    public String messageLiteral() {
        return messageLiteral;
    }

    public String messageExpr() {
        return messageExpr;
    }

    public String snippet() {
        return snippet;
    }

    public AdviceMetrics metrics() {
        return metrics;
    }

    /**
     * Builder for {@link CandidateAdvice}.
     */
    public static final class Builder {
        private String fileName;
        private int lineNo;
        private AdviceSource source;
        private AdviceId adviceId;
        private RuleId ruleId;
        private String messageLiteral;
        private String messageExpr;
        private String snippet;
        private AdviceMetrics metrics;

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder lineNo(int lineNo) {
            this.lineNo = lineNo;
            return this;
        }

        public Builder source(AdviceSource source) {
            this.source = source;
            return this;
        }

        public Builder adviceId(AdviceId adviceId) {
            this.adviceId = adviceId;
            return this;
        }

        public Builder ruleId(RuleId ruleId) {
            this.ruleId = ruleId;
            return this;
        }

        public Builder messageLiteral(String messageLiteral) {
            this.messageLiteral = messageLiteral;
            return this;
        }

        public Builder messageExpr(String messageExpr) {
            this.messageExpr = messageExpr;
            return this;
        }

        public Builder snippet(String snippet) {
            this.snippet = snippet;
            return this;
        }

        public Builder metrics(AdviceMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public CandidateAdvice build() {
            Objects.requireNonNull(adviceId, "adviceId");
            Objects.requireNonNull(source, "source");
            return new CandidateAdvice(this);
        }
    }
}
