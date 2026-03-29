package com.embervault.adapter.in.ui.viewmodel;

/**
 * Color configuration for views, derived from a domain ColorScheme.
 *
 * <p>This is a ViewModel-layer value object that carries scheme colors
 * as simple hex strings so that view controllers do not depend on the
 * domain layer directly (per ADR-0013).</p>
 *
 * <p>The contrast-text helper uses the W3C relative-luminance formula
 * (inlined here to avoid a domain dependency).</p>
 *
 * @param canvasBackground  hex color for canvas backgrounds
 * @param panelBackground   hex color for panel backgrounds
 * @param textColor         hex color for primary UI text
 * @param secondaryTextColor hex color for secondary text
 * @param borderColor       hex color for note borders
 * @param selectionColor    hex color for selection highlights
 * @param toolbarBackground hex color for toolbar areas
 * @param accentColor       hex color for accent/interactive elements
 */
public record ViewColorConfig(
        String canvasBackground,
        String panelBackground,
        String textColor,
        String secondaryTextColor,
        String borderColor,
        String selectionColor,
        String toolbarBackground,
        String accentColor
) {

    private static final double LUMINANCE_THRESHOLD = 0.179;
    private static final double LINEAR_THRESHOLD = 0.04045;
    private static final double LINEAR_DIVISOR = 12.92;
    private static final double GAMMA_OFFSET = 0.055;
    private static final double GAMMA_DIVISOR = 1.055;
    private static final double GAMMA_EXPONENT = 2.4;
    private static final double RED_COEFFICIENT = 0.2126;
    private static final double GREEN_COEFFICIENT = 0.7152;
    private static final double BLUE_COEFFICIENT = 0.0722;
    private static final double MAX_CHANNEL = 255.0;
    private static final int HEX_RADIX = 16;

    /**
     * Returns "#000000" or "#FFFFFF" for readable text on the given
     * background hex color.
     *
     * @param backgroundHex hex color string
     * @return contrast text color hex
     */
    public static String contrastTextColor(String backgroundHex) {
        String upper = backgroundHex.toUpperCase();
        int r = Integer.parseInt(upper.substring(1, 3), HEX_RADIX);
        int g = Integer.parseInt(upper.substring(3, 5), HEX_RADIX);
        int b = Integer.parseInt(upper.substring(5, 7), HEX_RADIX);

        double rs = linearize(r / MAX_CHANNEL);
        double gs = linearize(g / MAX_CHANNEL);
        double bs = linearize(b / MAX_CHANNEL);
        double luminance = RED_COEFFICIENT * rs
                + GREEN_COEFFICIENT * gs
                + BLUE_COEFFICIENT * bs;
        return luminance > LUMINANCE_THRESHOLD ? "#000000" : "#FFFFFF";
    }

    private static double linearize(double channel) {
        if (channel <= LINEAR_THRESHOLD) {
            return channel / LINEAR_DIVISOR;
        }
        return Math.pow((channel + GAMMA_OFFSET) / GAMMA_DIVISOR,
                GAMMA_EXPONENT);
    }
}
