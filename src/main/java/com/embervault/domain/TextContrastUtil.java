package com.embervault.domain;

import java.util.Objects;

/**
 * Utility for choosing readable text color on arbitrary backgrounds.
 *
 * <p>Uses the W3C relative luminance formula to decide whether black
 * or white text provides better contrast against a given background.</p>
 */
public final class TextContrastUtil {

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

    private TextContrastUtil() {
        // utility class
    }

    /**
     * Returns "#000000" (black) or "#FFFFFF" (white) depending on which
     * provides better contrast against the given background hex color.
     *
     * @param backgroundHex hex color string (e.g. "#FF8800")
     * @return "#000000" for light backgrounds, "#FFFFFF" for dark
     * @throws NullPointerException     if backgroundHex is null
     * @throws IllegalArgumentException if format is invalid
     */
    public static String contrastTextColor(String backgroundHex) {
        Objects.requireNonNull(backgroundHex, "backgroundHex must not be null");
        String upper = backgroundHex.toUpperCase();
        if (!upper.matches("#[0-9A-F]{6}")) {
            throw new IllegalArgumentException(
                    "Invalid hex color: " + backgroundHex);
        }

        int r = Integer.parseInt(upper.substring(1, 3), HEX_RADIX);
        int g = Integer.parseInt(upper.substring(3, 5), HEX_RADIX);
        int b = Integer.parseInt(upper.substring(5, 7), HEX_RADIX);

        double luminance = relativeLuminance(r, g, b);
        return luminance > LUMINANCE_THRESHOLD ? "#000000" : "#FFFFFF";
    }

    private static double relativeLuminance(int r, int g, int b) {
        double rs = linearize(r / MAX_CHANNEL);
        double gs = linearize(g / MAX_CHANNEL);
        double bs = linearize(b / MAX_CHANNEL);
        return RED_COEFFICIENT * rs
                + GREEN_COEFFICIENT * gs
                + BLUE_COEFFICIENT * bs;
    }

    private static double linearize(double channel) {
        if (channel <= LINEAR_THRESHOLD) {
            return channel / LINEAR_DIVISOR;
        }
        return Math.pow((channel + GAMMA_OFFSET) / GAMMA_DIVISOR,
                GAMMA_EXPONENT);
    }
}
