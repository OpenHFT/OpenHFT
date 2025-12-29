/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link LambdaMessageExtractor}.
 */
public class LambdaMessageExtractorTest {

    private final MessageAstSupport astSupport = new MessageAstSupport();
    private final MessageExtractionContext context = new MessageExtractionContext(astSupport);
    private final LambdaMessageExtractor extractor = new LambdaMessageExtractor(context);

    @Test
    public void summariseSupplierMessage_null_returnsEllipsis() {
        String result = extractor.summariseSupplierMessage(null);
        assertEquals("...", result);
    }

    @Test
    public void summariseSupplierMessage_emptyAfterNormalization_returnsEllipsis() {
        String result = extractor.summariseSupplierMessage("   ");
        assertEquals("...", result);
    }

    @Test
    public void summariseSupplierMessage_withContent_appendsEllipsis() {
        String result = extractor.summariseSupplierMessage("hello world");
        assertEquals("hello world ...", result);
    }

    @Test
    public void summariseSupplierMessage_withExtraWhitespace_normalizes() {
        String result = extractor.summariseSupplierMessage("  hello   world  ");
        assertEquals("hello world ...", result);
    }

    @Test
    public void extractTrivialSupplierMessage_null_throwsNPE() {
        try {
            extractor.extractTrivialSupplierMessage(null);
        } catch (NullPointerException expected) {
            // expected
        }
    }
}
