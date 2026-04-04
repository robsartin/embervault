package com.embervault.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses a search query string into a {@link SearchFilter}.
 *
 * <p>Supports three kinds of tokens:</p>
 * <ul>
 *   <li><b>Attribute filters</b>: {@code color:red}, {@code badge:star},
 *       {@code checked:true} — mapped to Tinderbox attribute names</li>
 *   <li><b>Relationship filters</b>: {@code has:children} — checks
 *       structural relationships</li>
 *   <li><b>Text terms</b>: everything else is treated as plain-text
 *       search</li>
 * </ul>
 */
public final class SearchQueryParser {

    private static final Map<String, String> FILTER_KEY_MAP = Map.ofEntries(
            Map.entry("color", Attributes.COLOR),
            Map.entry("badge", Attributes.BADGE),
            Map.entry("checked", Attributes.CHECKED),
            Map.entry("name", Attributes.NAME),
            Map.entry("shape", Attributes.SHAPE),
            Map.entry("text", Attributes.TEXT),
            Map.entry("url", Attributes.URL),
            Map.entry("prototype", Attributes.PROTOTYPE)
    );

    private SearchQueryParser() {
        // utility class
    }

    /**
     * Parses a search query string into a {@link SearchFilter}.
     *
     * @param input the raw search string (may be null)
     * @return the parsed filter
     */
    public static SearchFilter parse(String input) {
        if (input == null || input.isBlank()) {
            return new SearchFilter("", Map.of(), List.of());
        }

        String[] tokens = input.trim().split("\\s+");
        List<String> textParts = new ArrayList<>();
        Map<String, String> attributeFilters = new HashMap<>();
        List<String> relationshipFilters = new ArrayList<>();

        for (String token : tokens) {
            int colonIndex = token.indexOf(':');
            if (colonIndex > 0 && colonIndex < token.length() - 1) {
                String key = token.substring(0, colonIndex)
                        .toLowerCase(Locale.ROOT);
                String value = token.substring(colonIndex + 1);

                if ("has".equals(key)) {
                    relationshipFilters.add(
                            value.toLowerCase(Locale.ROOT));
                } else {
                    String attrName = FILTER_KEY_MAP.get(key);
                    if (attrName != null) {
                        attributeFilters.put(attrName, value);
                    } else {
                        textParts.add(token);
                    }
                }
            } else {
                textParts.add(token);
            }
        }

        String textQuery = String.join(" ", textParts);
        return new SearchFilter(textQuery, attributeFilters,
                relationshipFilters);
    }
}
