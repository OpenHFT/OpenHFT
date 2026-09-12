/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

/**
 * Occurrence of advice for reporting.
 */
public final class AdviceOccurrence {
    private final int lineNo;
    private final AdviceSource source;
    private final String messageLiteral;
    private final String messageExpr;
    private final String lineText;
    private final String snippet;

    public AdviceOccurrence(int lineNo, AdviceSource source,
                            String messageLiteral, String messageExpr,
                            String lineText, String snippet) {
        this.lineNo = lineNo;
        this.source = source;
        this.messageLiteral = messageLiteral;
        this.messageExpr = messageExpr;
        this.lineText = lineText;
        this.snippet = snippet;
    }

    public int lineNo() {
        return lineNo;
    }

    public AdviceSource source() {
        return source;
    }

    public String messageLiteral() {
        return messageLiteral;
    }

    public String messageExpr() {
        return messageExpr;
    }

    public String lineText() {
        return lineText;
    }

    public String snippet() {
        return snippet;
    }

    @Override
    public String toString() {
        return "AdviceOccurrence{lineNo=" + lineNo
                + ", source=" + source
                + ", messageLiteral='" + messageLiteral + '\''
                + '}';
    }
}
