package com.embervault.application.port.in;

import java.util.List;

import com.embervault.domain.Note;
import com.embervault.domain.SearchFilter;

/**
 * Query interface for searching notes with attribute filters.
 */
public interface FilteredSearchQuery {

    /**
     * Searches notes using a parsed {@link SearchFilter} that may include
     * text terms, attribute filters, and relationship filters.
     *
     * <p>All filter criteria are combined with AND logic: a note must
     * match the text query, all attribute filters, and all relationship
     * filters to be included in results.</p>
     *
     * @param filter the parsed search filter
     * @return matching notes
     */
    List<Note> searchNotesFiltered(SearchFilter filter);
}
