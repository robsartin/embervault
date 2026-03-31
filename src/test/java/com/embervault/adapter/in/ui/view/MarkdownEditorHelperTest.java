package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MarkdownEditorHelper} — bridges highlighter/formatter
 * with editor text operations.
 */
class MarkdownEditorHelperTest {

    @Test
    @DisplayName("computeHighlighting returns spans for markdown text")
    void computeHighlighting_returnsSpans() {
        List<MarkdownSyntaxHighlighter.StyleSpan> spans =
                MarkdownEditorHelper.computeHighlighting("# Hello");
        assertFalse(spans.isEmpty());
        assertEquals("header", spans.getFirst().styleClass());
    }

    @Test
    @DisplayName("applyBold wraps selection and returns result")
    void applyBold_wrapsSelection() {
        MarkdownFormatter.FormatResult result =
                MarkdownEditorHelper.applyBold("hello world", 6, 11);
        assertEquals("hello **world**", result.text());
    }

    @Test
    @DisplayName("applyItalic wraps selection and returns result")
    void applyItalic_wrapsSelection() {
        MarkdownFormatter.FormatResult result =
                MarkdownEditorHelper.applyItalic("hello world", 6, 11);
        assertEquals("hello *world*", result.text());
    }

    @Test
    @DisplayName("applyCode wraps selection and returns result")
    void applyCode_wrapsSelection() {
        MarkdownFormatter.FormatResult result =
                MarkdownEditorHelper.applyCode("hello world", 6, 11);
        assertEquals("hello `world`", result.text());
    }

    @Test
    @DisplayName("empty text returns no highlights")
    void emptyText_noHighlights() {
        assertTrue(MarkdownEditorHelper.computeHighlighting("").isEmpty());
    }
}
