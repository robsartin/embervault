package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that SelectedNoteViewModel publishes typed events via EventBus.
 */
class SelectedNoteViewModelEventBusTest {

    private SelectedNoteViewModel viewModel;
    private NoteService noteService;
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        var repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        AppState appState = new AppState();
        eventBus = new EventBus();
        viewModel = new SelectedNoteViewModel(
                noteService, noteService, appState, eventBus);
    }

    @Test
    @DisplayName("saveTitle publishes NoteRenamedEvent")
    void saveTitle_publishesNoteRenamedEvent() {
        Note note = noteService.createNote("Old", "text");
        viewModel.setSelectedNoteId(note.getId());
        List<NoteRenamedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteRenamedEvent.class, events::add);

        viewModel.saveTitle("New Title");

        assertEquals(1, events.size());
        assertEquals(note.getId(), events.get(0).noteId());
        assertEquals("New Title", events.get(0).newTitle());
    }

    @Test
    @DisplayName("saveTitle does not publish event for blank title")
    void saveTitle_doesNotPublishForBlankTitle() {
        Note note = noteService.createNote("Old", "text");
        viewModel.setSelectedNoteId(note.getId());
        List<NoteRenamedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteRenamedEvent.class, events::add);

        viewModel.saveTitle("");

        assertTrue(events.isEmpty());
    }

    @Test
    @DisplayName("saveText publishes NoteUpdatedEvent")
    void saveText_publishesNoteUpdatedEvent() {
        Note note = noteService.createNote("Title", "old text");
        viewModel.setSelectedNoteId(note.getId());
        List<NoteUpdatedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteUpdatedEvent.class, events::add);

        viewModel.saveText("new text");

        assertEquals(1, events.size());
        assertEquals(note.getId(), events.get(0).noteId());
    }
}
