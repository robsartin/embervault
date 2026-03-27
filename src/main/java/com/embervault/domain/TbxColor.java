package com.embervault.domain;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A domain value object representing a color in the Tinderbox attribute system.
 *
 * <p>Named {@code TbxColor} to avoid conflicts with {@code javafx.scene.paint.Color}.
 * Stores colors internally as a hex string and provides factory methods for
 * named, hex, RGB, and HSV formats.</p>
 */
public final class TbxColor {

    private static final Map<String, String> NAMED_COLORS = Map.ofEntries(
            Map.entry("red", "#FF0000"),
            Map.entry("green", "#00FF00"),
            Map.entry("blue", "#0000FF"),
            Map.entry("black", "#000000"),
            Map.entry("white", "#FFFFFF"),
            Map.entry("yellow", "#FFFF00"),
            Map.entry("cyan", "#00FFFF"),
            Map.entry("magenta", "#FF00FF"),
            Map.entry("orange", "#FFA500"),
            Map.entry("purple", "#800080"),
            Map.entry("warm gray", "#808080"),
            Map.entry("poppy", "#E35335")
    );

    private static final int HEX_LENGTH = 7;
    private static final int HEX_RADIX = 16;
    private static final int MAX_RGB = 255;
    private static final int MAX_HUE = 360;
    private static final int MAX_SV = 100;
    private static final double SV_SCALE = 100.0;
    private static final double HUE_SECTOR = 60.0;
    private static final int SECTOR_COUNT = 6;

    private final String hex;
    private final String name;

    private TbxColor(String hex, String name) {
        this.hex = hex;
        this.name = name;
    }

    /**
     * Creates a color from a hex string (e.g., "#A482BF").
     *
     * @param hexValue the hex color string starting with '#'
     * @return the color
     * @throws IllegalArgumentException if the format is invalid
     */
    public static TbxColor hex(String hexValue) {
        Objects.requireNonNull(hexValue, "hexValue must not be null");
        String upper = hexValue.toUpperCase();
        if (!upper.matches("#[0-9A-F]{6}")) {
            throw new IllegalArgumentException("Invalid hex color: " + hexValue);
        }
        return new TbxColor(upper, null);
    }

    /**
     * Creates a color from a known name (e.g., "red", "warm gray").
     *
     * @param colorName the color name
     * @return the color
     * @throws IllegalArgumentException if the name is blank or unknown
     */
    public static TbxColor named(String colorName) {
        Objects.requireNonNull(colorName, "colorName must not be null");
        if (colorName.isBlank()) {
            throw new IllegalArgumentException("colorName must not be blank");
        }
        String lower = colorName.toLowerCase();
        String hexValue = NAMED_COLORS.get(lower);
        if (hexValue == null) {
            throw new IllegalArgumentException("Unknown color name: " + colorName);
        }
        return new TbxColor(hexValue, colorName.toLowerCase());
    }

    /**
     * Creates a color from RGB components (0-255 each).
     *
     * @param red   the red component (0-255)
     * @param green the green component (0-255)
     * @param blue  the blue component (0-255)
     * @return the color
     * @throws IllegalArgumentException if any component is out of range
     */
    public static TbxColor rgb(int red, int green, int blue) {
        validateRange(red, 0, MAX_RGB, "red");
        validateRange(green, 0, MAX_RGB, "green");
        validateRange(blue, 0, MAX_RGB, "blue");
        String hexValue = String.format("#%02X%02X%02X", red, green, blue);
        return new TbxColor(hexValue, null);
    }

    /**
     * Creates a color from HSV components.
     *
     * @param hue        the hue (0-360)
     * @param saturation the saturation (0-100)
     * @param value      the value/brightness (0-100)
     * @return the color
     * @throws IllegalArgumentException if any component is out of range
     */
    public static TbxColor hsv(int hue, int saturation, int value) {
        validateRange(hue, 0, MAX_HUE, "hue");
        validateRange(saturation, 0, MAX_SV, "saturation");
        validateRange(value, 0, MAX_SV, "value");

        double s = saturation / SV_SCALE;
        double v = value / SV_SCALE;
        double c = v * s;
        double x = c * (1 - Math.abs((hue / HUE_SECTOR) % 2 - 1));
        double m = v - c;

        double r;
        double g;
        double b;
        int sector = (hue / (int) HUE_SECTOR) % SECTOR_COUNT;

        switch (sector) {
            case 0 -> {
                r = c;
                g = x;
                b = 0;
            }
            case 1 -> {
                r = x;
                g = c;
                b = 0;
            }
            case 2 -> {
                r = 0;
                g = c;
                b = x;
            }
            case 3 -> {
                r = 0;
                g = x;
                b = c;
            }
            case 4 -> {
                r = x;
                g = 0;
                b = c;
            }
            default -> {
                r = c;
                g = 0;
                b = x;
            }
        }

        int red = (int) Math.round((r + m) * MAX_RGB);
        int green = (int) Math.round((g + m) * MAX_RGB);
        int blue = (int) Math.round((b + m) * MAX_RGB);

        return rgb(red, green, blue);
    }

    /**
     * Returns the hex representation of this color (e.g., "#A482BF").
     *
     * @return the hex string
     */
    public String toHex() {
        return hex;
    }

    /**
     * Returns the name of this color if it was created via {@link #named(String)}.
     *
     * @return an optional containing the name, or empty
     */
    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TbxColor other)) {
            return false;
        }
        return hex.equals(other.hex);
    }

    @Override
    public int hashCode() {
        return hex.hashCode();
    }

    @Override
    public String toString() {
        if (name != null) {
            return "TbxColor{name='" + name + "', hex=" + hex + "}";
        }
        return "TbxColor{hex=" + hex + "}";
    }

    private static void validateRange(int value, int min, int max, String name) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    name + " must be between " + min + " and " + max + ", got: " + value);
        }
    }
}
