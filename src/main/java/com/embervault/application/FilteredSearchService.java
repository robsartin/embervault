package com.embervault.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.embervault.application.port.in.FilteredSearchQuery;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import com.embervault.domain.SearchFilter;
import com.embervault.domain.TbxColor;

/**
 * Application service for searching notes with attribute filters.
 *
 * <p>Implements the {@link FilteredSearchQuery} inbound port, applying
 * text, attribute, and relationship filters with AND logic.</p>
 */
public final class FilteredSearchService implements FilteredSearchQuery {

    private final NoteRepository repository;

    /**
     * Constructs a FilteredSearchService backed by the given repository.
     *
     * @param repository the note repository
     */
    public FilteredSearchService(NoteRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Note> searchNotesFiltered(SearchFilter filter) {
        if (filter == null || filter.isEmpty()) {
            return List.of();
        }
        List<Note> candidates = repository.findAll();
        candidates = applyTextFilter(candidates, filter.textQuery());
        candidates = applyAttributeFilters(candidates,
                filter.attributeFilters());
        candidates = applyRelationshipFilters(candidates,
                filter.relationshipFilters());
        return new ArrayList<>(candidates);
    }

    private List<Note> applyTextFilter(List<Note> candidates,
            String textQuery) {
        if (textQuery.isBlank()) {
            return candidates;
        }
        String lowerQuery = textQuery.toLowerCase(Locale.ROOT);
        List<Note> titleMatches = new ArrayList<>();
        List<Note> textOnly = new ArrayList<>();
        for (Note note : candidates) {
            boolean titleMatch = note.getTitle()
                    .toLowerCase(Locale.ROOT).contains(lowerQuery);
            boolean textMatch = note.getContent()
                    .toLowerCase(Locale.ROOT).contains(lowerQuery);
            if (titleMatch) {
                titleMatches.add(note);
            } else if (textMatch) {
                textOnly.add(note);
            }
        }
        List<Note> result = new ArrayList<>(
                titleMatches.size() + textOnly.size());
        result.addAll(titleMatches);
        result.addAll(textOnly);
        return result;
    }

    private List<Note> applyAttributeFilters(List<Note> candidates,
            Map<String, String> filters) {
        List<Note> result = candidates;
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String attrName = entry.getKey();
            String expected = entry.getValue();
            result = result.stream()
                    .filter(n -> matchesAttribute(
                            n, attrName, expected))
                    .toList();
        }
        return result;
    }

    private List<Note> applyRelationshipFilters(List<Note> candidates,
            List<String> filters) {
        List<Note> result = candidates;
        for (String rel : filters) {
            if ("children".equals(rel)) {
                Collection<UUID> ids = result.stream()
                        .map(Note::getId).toList();
                Set<UUID> withChildren =
                        repository.findNoteIdsWithChildren(ids);
                result = result.stream()
                        .filter(n -> withChildren.contains(
                                n.getId()))
                        .toList();
            }
        }
        return result;
    }

    private boolean matchesAttribute(Note note, String attrName,
            String expected) {
        Optional<AttributeValue> valueOpt =
                note.getAttribute(attrName);
        if (valueOpt.isEmpty()) {
            return false;
        }
        AttributeValue value = valueOpt.get();
        String expectedLower = expected.toLowerCase(Locale.ROOT);
        return switch (value) {
            case AttributeValue.StringValue sv ->
                    sv.value().toLowerCase(Locale.ROOT)
                            .equals(expectedLower);
            case AttributeValue.BooleanValue bv ->
                    String.valueOf(bv.value()).equals(expectedLower);
            case AttributeValue.ColorValue cv ->
                    matchesColor(cv.value(), expected);
            case AttributeValue.NumberValue nv ->
                    String.valueOf(nv.value()).equals(expected);
            default -> false;
        };
    }

    private boolean matchesColor(TbxColor color, String expected) {
        String expectedLower = expected.toLowerCase(Locale.ROOT);
        if (color.getName().isPresent()
                && color.getName().get().equals(expectedLower)) {
            return true;
        }
        return color.toHex().equalsIgnoreCase(expected);
    }
}
