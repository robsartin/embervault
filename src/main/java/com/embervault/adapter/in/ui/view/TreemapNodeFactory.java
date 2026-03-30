package com.embervault.adapter.in.ui.view;

/**
 * Factory for creating Treemap view JavaFX nodes.
 *
 * <p>Extracts node construction logic from {@link TreemapViewController}
 * so it can be tested without a full scene graph.</p>
 */
final class TreemapNodeFactory {

    static final double TITLE_FONT_SIZE = 13.0;
    static final double RECT_PADDING = 2.0;
    static final double NORMAL_BORDER_WIDTH = 1.0;
    private static final int MAX_LABEL_LENGTH = 30;

    private TreemapNodeFactory() { }

    static String truncateLabel(String title, double availableWidth) {
        double charsPerWidth = availableWidth / (TITLE_FONT_SIZE * 0.6);
        int maxChars = Math.min(MAX_LABEL_LENGTH, (int) charsPerWidth);
        if (maxChars <= 0) {
            return "";
        }
        if (title.length() <= maxChars) {
            return title;
        }
        return title.substring(0, maxChars) + "\u2026";
    }
}
