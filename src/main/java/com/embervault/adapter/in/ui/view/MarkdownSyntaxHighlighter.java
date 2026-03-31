package com.embervault.adapter.in.ui.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Computes syntax highlighting style spans for Markdown text.
 *
 * <p>Returns a list of {@link StyleSpan} records indicating which ranges
 * of the text should receive which CSS style class. This is a pure
 * function with no UI dependencies.</p>
 */
public final class MarkdownSyntaxHighlighter {

    private static final Pattern HEADER = Pattern.compile("^#{1,6}\\s+.+$",
            Pattern.MULTILINE);
    private static final Pattern BOLD = Pattern.compile("\\*\\*.+?\\*\\*");
    private static final Pattern ITALIC = Pattern.compile(
            "(?<!\\*)\\*(?!\\*).+?(?<!\\*)\\*(?!\\*)");
    private static final Pattern CODE = Pattern.compile("`[^`]+`");
    private static final Pattern LINK = Pattern.compile("\\[.+?]\\(.+?\\)");
    private static final Pattern WIKI_LINK = Pattern.compile("\\[\\[.+?]]");

    private MarkdownSyntaxHighlighter() {
    }

    /**
     * A range of text with an associated style class.
     *
     * @param styleClass the CSS style class name
     * @param start      the start index (inclusive)
     * @param end        the end index (exclusive)
     */
    public record StyleSpan(String styleClass, int start, int end) {
    }

    /**
     * Computes style spans for the given Markdown text.
     *
     * @param text the Markdown text
     * @return an unmodifiable list of style spans
     */
    public static List<StyleSpan> computeSpans(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        List<StyleSpan> spans = new ArrayList<>();
        addSpans(spans, HEADER, text, "header");
        addSpans(spans, BOLD, text, "bold");
        addSpans(spans, ITALIC, text, "italic");
        addSpans(spans, CODE, text, "code");
        addSpans(spans, LINK, text, "link");
        addSpans(spans, WIKI_LINK, text, "link");
        return Collections.unmodifiableList(spans);
    }

    private static void addSpans(List<StyleSpan> spans, Pattern pattern,
            String text, String styleClass) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            spans.add(new StyleSpan(styleClass, matcher.start(), matcher.end()));
        }
    }
}
