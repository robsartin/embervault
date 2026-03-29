package com.embervault.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Static registry of built-in color scheme presets.
 *
 * <p>Provides five presets: Standard, Dark, Solarized Light,
 * Solarized Dark, and High Contrast.</p>
 */
public final class ColorSchemeRegistry {

    private static final Map<String, ColorScheme> SCHEMES;

    static {
        Map<String, ColorScheme> map = new LinkedHashMap<>();

        map.put("Standard", new ColorScheme(
                "Standard",
                "#E8E8E8",  // canvas background — light gray
                "#FFFFFF",  // panel background — white
                "#1A1A1A",  // text color — near-black
                "#666666",  // secondary text
                "#CCCCCC",  // border color
                "#3399FF",  // selection color
                "#F5F5F5",  // toolbar background
                "#0066CC"   // accent color
        ));

        map.put("Dark", new ColorScheme(
                "Dark",
                "#1E1E1E",  // canvas background — charcoal
                "#2D2D2D",  // panel background — dark gray
                "#D4D4D4",  // text color — light gray
                "#808080",  // secondary text
                "#404040",  // border color
                "#264F78",  // selection color
                "#333333",  // toolbar background
                "#569CD6"   // accent color
        ));

        map.put("Solarized Light", new ColorScheme(
                "Solarized Light",
                "#FDF6E3",  // canvas background — warm cream
                "#EEE8D5",  // panel background
                "#657B83",  // text color — muted
                "#93A1A1",  // secondary text
                "#D3CBB8",  // border color
                "#268BD2",  // selection color
                "#EEE8D5",  // toolbar background
                "#2AA198"   // accent color — teal
        ));

        map.put("Solarized Dark", new ColorScheme(
                "Solarized Dark",
                "#002B36",  // canvas background — dark blue-gray
                "#073642",  // panel background
                "#839496",  // text color — light
                "#586E75",  // secondary text
                "#094959",  // border color
                "#268BD2",  // selection color
                "#073642",  // toolbar background
                "#2AA198"   // accent color — teal
        ));

        map.put("High Contrast", new ColorScheme(
                "High Contrast",
                "#FFFFFF",  // canvas background — white
                "#FFFFFF",  // panel background — white
                "#000000",  // text color — black
                "#333333",  // secondary text
                "#000000",  // border color — black
                "#FF6600",  // selection color — bold orange
                "#F0F0F0",  // toolbar background
                "#0000CC"   // accent color — bold blue
        ));

        SCHEMES = Collections.unmodifiableMap(map);
    }

    private ColorSchemeRegistry() {
        // utility class
    }

    /**
     * Returns the default color scheme (Standard).
     *
     * @return the default scheme
     */
    public static ColorScheme getDefault() {
        return SCHEMES.get("Standard");
    }

    /**
     * Returns all built-in color schemes in definition order.
     *
     * @return unmodifiable list of all schemes
     */
    public static List<ColorScheme> getAllSchemes() {
        return List.copyOf(SCHEMES.values());
    }

    /**
     * Looks up a scheme by name (case-sensitive).
     *
     * @param name the scheme name
     * @return the scheme, or empty if not found
     */
    public static Optional<ColorScheme> getScheme(String name) {
        Objects.requireNonNull(name, "name must not be null");
        return Optional.ofNullable(SCHEMES.get(name));
    }
}
