package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the search bar.
 *
 * <p>Manages the search query, results list, visibility state, and
 * result selection. Delegates searching to {@link NoteService#searchNotes}.</p>
 */
public final class SearchViewModel {

    private final StringProperty query = new SimpleStringProperty("");
    private final ObservableList<NoteDisplayItem> results =
            FXCollections.observableArrayList();
    private final BooleanProperty visible = new SimpleBooleanProperty(false);
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final NoteService noteService;

    /**
     * Constructs a SearchViewModel backed by the given NoteService.
     *
     * @param noteService the note service for searching
     */
    public SearchViewModel(NoteService noteService) {
        this.noteService = Objects.requireNonNull(noteService,
                "noteService must not be null");
    }

    /** Returns the query property bound to the search text field. */
    public StringProperty queryProperty() {
        return query;
    }

    /** Returns the observable list of search results. */
    public ObservableList<NoteDisplayItem> getResults() {
        return results;
    }

    /** Returns the visibility property for the search bar. */
    public BooleanProperty visibleProperty() {
        return visible;
    }

    /** Returns the selected note id property. */
    public ObjectProperty<UUID> selectedNoteIdProperty() {
        return selectedNoteId;
    }

    /**
     * Searches notes matching the given query and updates the results list.
     *
     * @param searchQuery the search query
     */
    public void search(String searchQuery) {
        results.setAll(
                noteService.searchNotes(searchQuery).stream()
                        .map(this::toDisplayItem)
                        .toList());
    }

    /**
     * Selects a search result by note id.
     *
     * @param noteId the note id to select, or null to clear
     */
    public void selectResult(UUID noteId) {
        selectedNoteId.set(noteId);
    }

    /** Toggles the search bar visibility. */
    public void toggleVisible() {
        visible.set(!visible.get());
    }

    /** Hides the search bar and clears query and results. */
    public void hide() {
        visible.set(false);
        query.set("");
        results.clear();
    }

    private NoteDisplayItem toDisplayItem(Note note) {
        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent());
    }
}
