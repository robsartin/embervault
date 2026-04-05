package com.embervault.domain;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Matches notes against parsed search filters.
 *
 * <p>Evaluates {@link SearchFilter} instances against a {@link Note},
 * supporting substring, attribute, and relation filters.</p>
 */
public final class SearchFilterMatcher {

    private final Predicate<Note> hasChildrenCheck;

    /**
     * Creates a matcher with the given children-check predicate.
     *
     * @param hasChildrenCheck returns true if a note has children
     */
    public SearchFilterMatcher(Predicate<Note> hasChildrenCheck) {
        this.hasChildrenCheck = hasChildrenCheck;
    }

    /**
     * Returns notes from the list that match all filters (AND semantics).
     *
     * @param notes the candidate notes
     * @param filters the filters to apply
     * @return notes matching all filters
     */
    public List<Note> match(List<Note> notes,
            List<SearchFilter> filters) {
        if (filters.isEmpty()) {
            return notes;
        }
        return notes.stream()
                .filter(note -> filters.stream()
                        .allMatch(f -> matches(note, f)))
                .toList();
    }

    /**
     * Tests whether a single note matches a single filter.
     */
    public boolean matches(Note note, SearchFilter filter) {
        return switch (filter) {
            case SearchFilter.SubstringFilter sf ->
                matchesSubstring(note, sf);
            case SearchFilter.AttributeFilter af ->
                matchesAttribute(note, af);
            case SearchFilter.RelationFilter rf ->
                matchesRelation(note, rf);
        };
    }

    private boolean matchesSubstring(Note note,
            SearchFilter.SubstringFilter sf) {
        String lower = sf.text().toLowerCase(Locale.ROOT);
        return note.getTitle().toLowerCase(Locale.ROOT).contains(lower)
                || note.getContent().toLowerCase(Locale.ROOT)
                        .contains(lower);
    }

    private boolean matchesAttribute(Note note,
            SearchFilter.AttributeFilter af) {
        String key = af.attributeKey();
        String attrName = "$"
                + key.substring(0, 1).toUpperCase(Locale.ROOT)
                + key.substring(1);
        return note.getAttribute(attrName)
                .map(v -> matchesValue(v, af.value()))
                .orElse(false);
    }

    private boolean matchesRelation(Note note,
            SearchFilter.RelationFilter rf) {
        if ("children".equals(rf.relation())) {
            return hasChildrenCheck.test(note);
        }
        return false;
    }

    private boolean matchesValue(AttributeValue value, String expected) {
        return switch (value) {
            case AttributeValue.StringValue sv ->
                sv.value().equalsIgnoreCase(expected);
            case AttributeValue.BooleanValue bv ->
                String.valueOf(bv.value()).equalsIgnoreCase(expected);
            case AttributeValue.ColorValue cv ->
                cv.value().toHex().equalsIgnoreCase(expected)
                        || cv.value().getName()
                                .map(n -> n.equalsIgnoreCase(expected))
                                .orElse(false);
            case AttributeValue.NumberValue nv -> {
                try {
                    yield nv.value() == Double.parseDouble(expected);
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            default -> value.toString().contains(expected);
        };
    }

    private SearchFilterMatcher() {
        this.hasChildrenCheck = note -> false;
    }
}
