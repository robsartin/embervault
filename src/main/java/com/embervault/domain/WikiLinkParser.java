package com.embervault.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses {@code [[Note Title]]} wiki-link references from text.
 *
 * <p>Extracts all wiki-link targets in order of appearance.
 * Empty or whitespace-only targets are ignored. Titles are trimmed.</p>
 */
public final class WikiLinkParser {

    private static final Pattern WIKI_LINK = Pattern.compile("\\[\\[(.+?)]]");

    private WikiLinkParser() {
    }

    /**
     * Parses all wiki-link titles from the given text.
     *
     * @param text the text to parse (may be null)
     * @return an unmodifiable list of titles in order of appearance
     */
    public static List<String> parse(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        Matcher matcher = WIKI_LINK.matcher(text);
        List<String> titles = new ArrayList<>();
        while (matcher.find()) {
            String title = matcher.group(1).trim();
            if (!title.isEmpty()) {
                titles.add(title);
            }
        }
        return Collections.unmodifiableList(titles);
    }
}
