/*
 * Copyright 2016-2025 Higher Frequency Trading; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link LambdaMessageExtractor}.
 */
@SuppressWarnings("MMDisplayName")
class LambdaMessageExtractorTest {

    private final MessageAstSupport astSupport = new MessageAstSupport();
    private final MessageExtractionContext context = new MessageExtractionContext(astSupport);
    private final LambdaMessageExtractor extractor = new LambdaMessageExtractor(context);

    @Test
    void summariseSupplierMessage_null_returnsEllipsis() {
        String result = extractor.summariseSupplierMessage(null);
        assertEquals("...", result);
    }

    @Test
    void summariseSupplierMessage_emptyAfterNormalization_returnsEllipsis() {
        String result = extractor.summariseSupplierMessage("   ");
        assertEquals("...", result);
    }

    @Test
    void summariseSupplierMessage_withContent_appendsEllipsis() {
        String result = extractor.summariseSupplierMessage("hello world");
        assertEquals("hello world ...", result);
    }

    @Test
    void summariseSupplierMessage_withExtraWhitespace_normalizes() {
        String result = extractor.summariseSupplierMessage("  hello   world  ");
        assertEquals("hello world ...", result);
    }

    @Test
    void extractTrivialSupplierMessage_null_throwsNPE() {
        assertThrows(NullPointerException.class, () -> extractor.extractTrivialSupplierMessage(null));
    }
}
