package com.embervault.adapter.in.ui.view;

/**
 * Pure text manipulation for Markdown formatting shortcuts.
 *
 * <p>Each toggle method wraps/unwraps the selected text region with
 * the appropriate Markdown delimiter (**, *, `). Returns a
 * {@link FormatResult} with the new text and adjusted selection bounds.</p>
 */
public final class MarkdownFormatter {

    private MarkdownFormatter() {
    }

    /**
     * Result of a formatting operation.
     *
     * @param text           the full text after formatting
     * @param selectionStart the new selection start
     * @param selectionEnd   the new selection end
     */
    public record FormatResult(String text, int selectionStart,
            int selectionEnd) {
    }

    /** Toggles bold (**) around the selection. */
    public static FormatResult toggleBold(String text,
            int selStart, int selEnd) {
        return toggle(text, selStart, selEnd, "**");
    }

    /** Toggles italic (*) around the selection. */
    public static FormatResult toggleItalic(String text,
            int selStart, int selEnd) {
        return toggle(text, selStart, selEnd, "*");
    }

    /** Toggles inline code (`) around the selection. */
    public static FormatResult toggleCode(String text,
            int selStart, int selEnd) {
        return toggle(text, selStart, selEnd, "`");
    }

    private static FormatResult toggle(String text,
            int selStart, int selEnd, String delimiter) {
        int delimLen = delimiter.length();

        // Check if already wrapped
        int beforeStart = selStart - delimLen;
        int afterEnd = selEnd + delimLen;
        if (beforeStart >= 0 && afterEnd <= text.length()
                && text.substring(beforeStart, selStart).equals(delimiter)
                && text.substring(selEnd, afterEnd).equals(delimiter)) {
            // Remove delimiters
            String newText = text.substring(0, beforeStart)
                    + text.substring(selStart, selEnd)
                    + text.substring(afterEnd);
            return new FormatResult(newText, beforeStart,
                    beforeStart + (selEnd - selStart));
        }

        // Add delimiters
        String newText = text.substring(0, selStart)
                + delimiter
                + text.substring(selStart, selEnd)
                + delimiter
                + text.substring(selEnd);
        return new FormatResult(newText, selStart + delimLen,
                selEnd + delimLen);
    }
}
