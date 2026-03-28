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
}
