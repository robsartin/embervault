package com.embervault.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of badge names and their corresponding Unicode symbols.
 *
 * <p>Provides a static mapping from human-readable badge names (e.g., "star")
 * to Unicode emoji symbols (e.g., "\u2B50"). Used by ViewModels to resolve
 * the {@code $Badge} attribute into a displayable symbol.</p>
 */
public final class BadgeRegistry {

    private static final Map<String, String> BADGES = new LinkedHashMap<>();

    static {
        BADGES.put("star", "\u2B50");
        BADGES.put("flag", "\uD83D\uDEA9");
        BADGES.put("check", "\u2705");
        BADGES.put("warning", "\u26A0\uFE0F");
        BADGES.put("book", "\uD83D\uDCD6");
        BADGES.put("person", "\uD83D\uDC64");
        BADGES.put("idea", "\uD83D\uDCA1");
        BADGES.put("heart", "\u2764\uFE0F");
        BADGES.put("pin", "\uD83D\uDCCC");
        BADGES.put("fire", "\uD83D\uDD25");
    }

    private BadgeRegistry() {
        // utility class
    }

    /**
     * Returns the Unicode symbol for the given badge name.
     *
     * @param name the badge name (e.g., "star"), or null
     * @return an optional containing the symbol, or empty if unknown
     */
    public static Optional<String> getBadgeSymbol(String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BADGES.get(name));
    }

    /**
     * Returns an unmodifiable list of all registered badge names.
     *
     * @return the list of badge names
     */
    public static List<String> getAllBadgeNames() {
        return List.copyOf(BADGES.keySet());
    }
}
