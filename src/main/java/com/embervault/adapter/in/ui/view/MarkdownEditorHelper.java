package com.embervault.adapter.in.ui.view;

import java.util.List;

/**
 * Bridges {@link MarkdownSyntaxHighlighter} and {@link MarkdownFormatter}
 * for use by the text pane editor.
 *
 * <p>Provides a single entry point for computing highlights and
 * applying formatting operations. The controller delegates to this
 * helper so the logic remains testable without JavaFX.</p>
 */
public final class MarkdownEditorHelper {

    private MarkdownEditorHelper() {
    }

    /**
     * Computes syntax highlighting spans for the given text.
     *
     * @param text the Markdown text
     * @return style spans for highlighting
     */
    public static List<MarkdownSyntaxHighlighter.StyleSpan> computeHighlighting(
            String text) {
        return MarkdownSyntaxHighlighter.computeSpans(text);
    }

    /** Toggles bold around the selection. */
    public static MarkdownFormatter.FormatResult applyBold(
            String text, int selStart, int selEnd) {
        return MarkdownFormatter.toggleBold(text, selStart, selEnd);
    }

    /** Toggles italic around the selection. */
    public static MarkdownFormatter.FormatResult applyItalic(
            String text, int selStart, int selEnd) {
        return MarkdownFormatter.toggleItalic(text, selStart, selEnd);
    }

    /** Toggles code around the selection. */
    public static MarkdownFormatter.FormatResult applyCode(
            String text, int selStart, int selEnd) {
        return MarkdownFormatter.toggleCode(text, selStart, selEnd);
    }
}
