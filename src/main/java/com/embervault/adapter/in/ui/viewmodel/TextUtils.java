package com.embervault.adapter.in.ui.viewmodel;

/**
 * Text utility methods for the viewmodel layer.
 */
final class TextUtils {

    private TextUtils() {
        // utility class
    }

    /**
     * Truncates text to the given maximum length, appending an ellipsis if truncated.
     *
     * @param text      the text to truncate, may be null
     * @param maxLength the maximum number of characters before truncation
     * @return the truncated text with ellipsis, or the original text if within limit
     */
    static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "\u2026";
    }

    /**
     * Computes a tab title of the form "Prefix: Name", truncating the name
     * if it exceeds the given maximum length.
     *
     * <p>If the name is null or empty, returns just the prefix.</p>
     *
     * @param prefix the view type prefix (e.g., "Map", "Outline")
     * @param name   the name to include, may be null or empty
     * @param maxLen the maximum length for the name portion before truncation
     * @return the formatted tab title
     */
    static String tabTitle(String prefix, String name, int maxLen) {
        if (name == null || name.isEmpty()) {
            return prefix;
        }
        return prefix + ": " + truncate(name, maxLen);
    }
}
