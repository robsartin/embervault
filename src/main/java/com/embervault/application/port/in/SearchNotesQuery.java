package com.embervault.application.port.in;

import java.util.List;

import com.embervault.domain.Note;
import com.embervault.domain.SearchFilter;

/**
 * Query interface for searching notes by text content.
 */
public interface SearchNotesQuery {

    /**
     * Searches all notes by case-insensitive substring matching on $Name and $Text.
     *
     * <p>Returns notes whose title or content contain the query string.
     * Title matches appear before text-only matches. Returns an empty list
     * if the query is null, empty, or blank.</p>
     *
     * @param query the search query
     * @return matching notes ordered by relevance (title matches first)
     */
    List<Note> searchNotes(String query);

    /**
     * Searches notes using structured filters.
     *
     * <p>All filters must match for a note to be included (AND semantics).
     * An empty filter list returns all notes.</p>
     *
     * @param filters the parsed search filters
     * @return notes matching all filters
     */
    List<Note> searchWithFilters(List<SearchFilter> filters);
}
