package com.embervault.adapter.in.ui.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MarkdownSyntaxHighlighter} — computes style spans for Markdown text.
 */
class MarkdownSyntaxHighlighterTest {

    @Test
    @DisplayName("empty text returns no spans")
    void emptyText_noSpans() {
        assertTrue(MarkdownSyntaxHighlighter.computeSpans("").isEmpty());
    }

    @Test
    @DisplayName("header line gets header style")
    void headerLine_getsHeaderStyle() {
        List<MarkdownSyntaxHighlighter.StyleSpan> spans =
                MarkdownSyntaxHighlighter.computeSpans("# Header");
        assertEquals(1, spans.size());
        assertEquals("header", spans.getFirst().styleClass());
        assertEquals(0, spans.getFirst().start());
        assertEquals(8, spans.getFirst().end());
    }

    @Test
    @DisplayName("bold text gets bold style")
    void boldText_getBoldStyle() {
        List<MarkdownSyntaxHighlighter.StyleSpan> spans =
                MarkdownSyntaxHighlighter.computeSpans("some **bold** text");
        assertTrue(spans.stream().anyMatch(s ->
                s.styleClass().equals("bold") && s.start() == 5 && s.end() == 13));
    }

    @Test
    @DisplayName("italic text gets italic style")
    void italicText_getsItalicStyle() {
        List<MarkdownSyntaxHighlighter.StyleSpan> spans =
                MarkdownSyntaxHighlighter.computeSpans("some *italic* text");
        assertTrue(spans.stream().anyMatch(s ->
                s.styleClass().equals("italic") && s.start() == 5 && s.end() == 13));
    }

    @Test
    @DisplayName("inline code gets code style")
    void inlineCode_getsCodeStyle() {
        List<MarkdownSyntaxHighlighter.StyleSpan> spans =
                MarkdownSyntaxHighlighter.computeSpans("use `code` here");
        assertTrue(spans.stream().anyMatch(s ->
                s.styleClass().equals("code") && s.start() == 4 && s.end() == 10));
    }

    @Test
    @DisplayName("link gets link style")
    void link_getsLinkStyle() {
        List<MarkdownSyntaxHighlighter.StyleSpan> spans =
                MarkdownSyntaxHighlighter.computeSpans("[text](url)");
        assertTrue(spans.stream().anyMatch(s ->
                s.styleClass().equals("link")));
    }

    @Test
    @DisplayName("wiki-link gets link style")
    void wikiLink_getsLinkStyle() {
        List<MarkdownSyntaxHighlighter.StyleSpan> spans =
                MarkdownSyntaxHighlighter.computeSpans("see [[My Note]]");
        assertTrue(spans.stream().anyMatch(s ->
                s.styleClass().equals("link")));
    }

    @Test
    @DisplayName("plain text returns no spans")
    void plainText_noSpans() {
        assertTrue(MarkdownSyntaxHighlighter.computeSpans(
                "just plain text").isEmpty());
    }
}
