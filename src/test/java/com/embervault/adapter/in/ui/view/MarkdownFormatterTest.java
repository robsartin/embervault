package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MarkdownFormatter} — text manipulation for formatting shortcuts.
 */
class MarkdownFormatterTest {

    @Test
    @DisplayName("toggleBold wraps selected text in **")
    void toggleBold_wrapsInStars() {
        MarkdownFormatter.FormatResult result =
                MarkdownFormatter.toggleBold("hello world", 6, 11);
        assertEquals("hello **world**", result.text());
        assertEquals(8, result.selectionStart());
        assertEquals(13, result.selectionEnd());
    }

    @Test
    @DisplayName("toggleBold removes ** from already bold text")
    void toggleBold_removesStars() {
        MarkdownFormatter.FormatResult result =
                MarkdownFormatter.toggleBold("hello **world**", 8, 13);
        assertEquals("hello world", result.text());
        assertEquals(6, result.selectionStart());
        assertEquals(11, result.selectionEnd());
    }

    @Test
    @DisplayName("toggleItalic wraps selected text in *")
    void toggleItalic_wrapsInStar() {
        MarkdownFormatter.FormatResult result =
                MarkdownFormatter.toggleItalic("hello world", 6, 11);
        assertEquals("hello *world*", result.text());
        assertEquals(7, result.selectionStart());
        assertEquals(12, result.selectionEnd());
    }

    @Test
    @DisplayName("toggleItalic removes * from already italic text")
    void toggleItalic_removesStar() {
        MarkdownFormatter.FormatResult result =
                MarkdownFormatter.toggleItalic("hello *world*", 7, 12);
        assertEquals("hello world", result.text());
        assertEquals(6, result.selectionStart());
        assertEquals(11, result.selectionEnd());
    }

    @Test
    @DisplayName("toggleCode wraps selected text in backticks")
    void toggleCode_wrapsInBackticks() {
        MarkdownFormatter.FormatResult result =
                MarkdownFormatter.toggleCode("hello world", 6, 11);
        assertEquals("hello `world`", result.text());
        assertEquals(7, result.selectionStart());
        assertEquals(12, result.selectionEnd());
    }

    @Test
    @DisplayName("toggleCode removes backticks from already code text")
    void toggleCode_removesBackticks() {
        MarkdownFormatter.FormatResult result =
                MarkdownFormatter.toggleCode("hello `world`", 7, 12);
        assertEquals("hello world", result.text());
        assertEquals(6, result.selectionStart());
        assertEquals(11, result.selectionEnd());
    }
}
