package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link WikiLinkParser} — extracts [[Note Title]] references from text.
 */
class WikiLinkParserTest {

    @Test
    @DisplayName("empty text returns no links")
    void emptyText_returnsNoLinks() {
        assertTrue(WikiLinkParser.parse("").isEmpty());
    }

    @Test
    @DisplayName("null text returns no links")
    void nullText_returnsNoLinks() {
        assertTrue(WikiLinkParser.parse(null).isEmpty());
    }

    @Test
    @DisplayName("text without wiki-links returns no links")
    void noLinks_returnsEmpty() {
        assertTrue(WikiLinkParser.parse("Just plain text").isEmpty());
    }

    @Test
    @DisplayName("single wiki-link is extracted")
    void singleLink() {
        List<String> links = WikiLinkParser.parse("See [[My Note]] for details");
        assertEquals(List.of("My Note"), links);
    }

    @Test
    @DisplayName("multiple wiki-links are extracted in order")
    void multipleLinks() {
        List<String> links = WikiLinkParser.parse(
                "See [[First]] and [[Second]] and [[Third]]");
        assertEquals(List.of("First", "Second", "Third"), links);
    }

    @Test
    @DisplayName("wiki-link at start of text")
    void linkAtStart() {
        List<String> links = WikiLinkParser.parse("[[Start]] of text");
        assertEquals(List.of("Start"), links);
    }

    @Test
    @DisplayName("wiki-link at end of text")
    void linkAtEnd() {
        List<String> links = WikiLinkParser.parse("End of [[text]]");
        assertEquals(List.of("text"), links);
    }

    @Test
    @DisplayName("empty brackets are ignored")
    void emptyBrackets_ignored() {
        assertTrue(WikiLinkParser.parse("See [[]] here").isEmpty());
    }

    @Test
    @DisplayName("whitespace-only brackets are ignored")
    void whitespaceOnlyBrackets_ignored() {
        assertTrue(WikiLinkParser.parse("See [[  ]] here").isEmpty());
    }

    @Test
    @DisplayName("single brackets are not treated as links")
    void singleBrackets_notLinks() {
        assertTrue(WikiLinkParser.parse("See [not a link] here").isEmpty());
    }

    @Test
    @DisplayName("duplicate titles are preserved")
    void duplicateTitles_preserved() {
        List<String> links = WikiLinkParser.parse("[[A]] then [[A]] again");
        assertEquals(List.of("A", "A"), links);
    }

    @Test
    @DisplayName("link titles are trimmed")
    void linkTitles_trimmed() {
        List<String> links = WikiLinkParser.parse("[[  Spaces  ]]");
        assertEquals(List.of("Spaces"), links);
    }
}
