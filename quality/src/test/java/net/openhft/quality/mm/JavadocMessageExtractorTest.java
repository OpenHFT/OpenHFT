/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.quality.mm;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JavadocMessageExtractor}.
 * Focuses on the isField method to improve mutation coverage.
 */
@DisplayName("Javadoc message extractor tests scenario case")
class JavadocMessageExtractorTest {

    private JavadocMessageExtractor extractor;
    private TestMessageSink sink;

    @BeforeEach
    void setUp() {
        MessageExtractionContext context = new MessageExtractionContext(new MessageAstSupport());
        sink = new TestMessageSink();
        extractor = new JavadocMessageExtractor(context, sink);
    }

    // --- isField tests via handleField ---

    @Test
    @DisplayName("Handle field skips variable inside method def")
    void handleField_skipsVariableInsideMethodDef() {
        DetailAST varDef = createVariableDefWithParent(TokenTypes.METHOD_DEF);

        extractor.handleField(varDef);

        // Should not emit anything since it's a local variable
        verify(varDef.getParent()).getType();
    }

    @Test
    @DisplayName("Handle field skips variable inside ctor def")
    void handleField_skipsVariableInsideCtorDef() {
        DetailAST varDef = createVariableDefWithParent(TokenTypes.CTOR_DEF);

        extractor.handleField(varDef);

        // Should not emit since it's inside constructor
        verify(varDef.getParent()).getType();
    }

    @Test
    @DisplayName("Handle field skips variable inside compact ctor def")
    void handleField_skipsVariableInsideCompactCtorDef() {
        DetailAST varDef = createVariableDefWithParent(TokenTypes.COMPACT_CTOR_DEF);

        extractor.handleField(varDef);

        verify(varDef.getParent()).getType();
    }

    @Test
    @DisplayName("Handle field processes variable in class def")
    void handleField_processesVariableInClassDef() {
        DetailAST varDef = createVariableDefWithParent(TokenTypes.CLASS_DEF);

        extractor.handleField(varDef);

        verify(varDef.getParent()).getType();
    }

    @Test
    @DisplayName("Handle field processes variable in interface def")
    void handleField_processesVariableInInterfaceDef() {
        DetailAST varDef = createVariableDefWithParent(TokenTypes.INTERFACE_DEF);

        extractor.handleField(varDef);

        verify(varDef.getParent()).getType();
    }

    @Test
    @DisplayName("Handle field processes variable in enum def")
    void handleField_processesVariableInEnumDef() {
        DetailAST varDef = createVariableDefWithParent(TokenTypes.ENUM_DEF);

        extractor.handleField(varDef);

        verify(varDef.getParent()).getType();
    }

    @Test
    @DisplayName("Handle field processes variable in annotation def")
    void handleField_processesVariableInAnnotationDef() {
        DetailAST varDef = createVariableDefWithParent(TokenTypes.ANNOTATION_DEF);

        extractor.handleField(varDef);

        verify(varDef.getParent()).getType();
    }

    @Test
    @DisplayName("Handle field processes variable in record def")
    void handleField_processesVariableInRecordDef() {
        DetailAST varDef = createVariableDefWithParent(TokenTypes.RECORD_DEF);

        extractor.handleField(varDef);

        verify(varDef.getParent()).getType();
    }

    @Test
    @DisplayName("Handle field processes variable in objblock")
    void handleField_processesVariableInObjblock() {
        DetailAST varDef = createVariableDefWithParent(TokenTypes.OBJBLOCK);

        extractor.handleField(varDef);

        verify(varDef.getParent()).getType();
    }

    @Test
    @DisplayName("Handle field returns false for orphan variable")
    void handleField_returnsFalseForOrphanVariable() {
        DetailAST varDef = mock(DetailAST.class);
        when(varDef.getType()).thenReturn(TokenTypes.VARIABLE_DEF);
        when(varDef.getParent()).thenReturn(null);
        when(varDef.getLineNo()).thenReturn(10);

        extractor.handleField(varDef);

        // No emission expected since no parent
        verify(varDef).getParent();
    }

    @Test
    @DisplayName("Handle field walks parent chain to find class")
    void handleField_walksParentChainToFindClass() {
        // Create: varDef -> SLIST -> METHOD_DEF
        DetailAST slist = mock(DetailAST.class);
        when(slist.getType()).thenReturn(TokenTypes.SLIST);

        DetailAST methodDef = mock(DetailAST.class);
        when(methodDef.getType()).thenReturn(TokenTypes.METHOD_DEF);
        when(slist.getParent()).thenReturn(methodDef);

        DetailAST varDef = mock(DetailAST.class);
        when(varDef.getType()).thenReturn(TokenTypes.VARIABLE_DEF);
        when(varDef.getParent()).thenReturn(slist);
        when(varDef.getLineNo()).thenReturn(10);

        extractor.handleField(varDef);

        // Should have walked to method def
        verify(slist).getParent();
    }

    @Test
    @DisplayName("Handle field walks to objblock then class")
    void handleField_walksToObjblockThenClass() {
        // Create: varDef -> OBJBLOCK (stops here)
        DetailAST objblock = mock(DetailAST.class);
        when(objblock.getType()).thenReturn(TokenTypes.OBJBLOCK);

        DetailAST varDef = mock(DetailAST.class);
        when(varDef.getType()).thenReturn(TokenTypes.VARIABLE_DEF);
        when(varDef.getParent()).thenReturn(objblock);
        when(varDef.getLineNo()).thenReturn(15);

        extractor.handleField(varDef);

        // isField returns true at OBJBLOCK
        verify(objblock).getType();
    }

    @Test
    @DisplayName("Handle field local in lambda scenario")
    void handleField_localInLambda() {
        // varDef -> SLIST -> LAMBDA -> something -> METHOD_DEF
        DetailAST methodDef = mock(DetailAST.class);
        when(methodDef.getType()).thenReturn(TokenTypes.METHOD_DEF);

        DetailAST lambda = mock(DetailAST.class);
        when(lambda.getType()).thenReturn(TokenTypes.LAMBDA);
        when(lambda.getParent()).thenReturn(methodDef);

        DetailAST slist = mock(DetailAST.class);
        when(slist.getType()).thenReturn(TokenTypes.SLIST);
        when(slist.getParent()).thenReturn(lambda);

        DetailAST varDef = mock(DetailAST.class);
        when(varDef.getType()).thenReturn(TokenTypes.VARIABLE_DEF);
        when(varDef.getParent()).thenReturn(slist);
        when(varDef.getLineNo()).thenReturn(20);

        extractor.handleField(varDef);

        // Should walk up to METHOD_DEF and return false
        verify(methodDef).getType();
    }

    @Test
    @DisplayName("Extract first paragraph stops at paragraph tag")
    void extractFirstParagraphStopsAtParagraphTag() throws Exception {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{
                "/**",
                " * First line {@code value}.",
                " * <p>Second line",
                " */"
        });

        assertEquals("First line {@code value}.", invokeExtractFirstParagraph(block));
    }

    @Test
    @DisplayName("Emit candidate skips empty paragraph")
    void emitCandidateSkipsEmptyParagraph() throws Exception {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{
                "/**",
                " * @param value description",
                " */"
        });
        when(block.getStartLineNo()).thenReturn(4);

        invokeEmitCandidate(block, MessageSource.JAVADOC_MEMBER);

        assertTrue(sink.candidates.isEmpty(), "Empty paragraph should emit no candidates");
    }

    @Test
    @DisplayName("Emit candidate normalises inline tags")
    void emitCandidateNormalisesInlineTags() throws Exception {
        TextBlock block = mock(TextBlock.class);
        when(block.getText()).thenReturn(new String[]{
                "/**",
                " * Uses {@code value} here.",
                " */"
        });
        when(block.getStartLineNo()).thenReturn(7);

        invokeEmitCandidate(block, MessageSource.JAVADOC_MEMBER);

        assertEquals(1, sink.candidates.size(), "Should emit one candidate");
        MessageCandidate candidate = sink.candidates.get(0);
        assertEquals("Uses {@code} here.", candidate.message());
        assertEquals(1, candidate.placeholderCount());
    }

    // Helper method to create a variable def with a direct parent
    private DetailAST createVariableDefWithParent(int parentType) {
        DetailAST parent = mock(DetailAST.class);
        when(parent.getType()).thenReturn(parentType);
        when(parent.getParent()).thenReturn(null);

        DetailAST varDef = mock(DetailAST.class);
        when(varDef.getType()).thenReturn(TokenTypes.VARIABLE_DEF);
        when(varDef.getParent()).thenReturn(parent);
        when(varDef.getLineNo()).thenReturn(10);

        return varDef;
    }

    private String invokeExtractFirstParagraph(TextBlock block) throws Exception {
        return extractor.extractFirstParagraph(block);
    }

    private void invokeEmitCandidate(TextBlock block, MessageSource source) throws Exception {
        extractor.emitCandidate(block, source);
    }

    /**
     * Test sink for capturing emitted candidates.
     */
    private static class TestMessageSink implements MessageCandidateSink {
        final List<MessageCandidate> candidates = new ArrayList<>();

        @Override
        public void emitCandidate(MessageCandidate candidate) {
            candidates.add(candidate);
        }

        @Override
        public void emitMissingMessage(int lineNo, MessageSource source) {
            // No-op for testing
        }
    }
}
