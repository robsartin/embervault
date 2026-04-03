package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that NoteEditorViewModel publishes typed events via EventBus.
 */
class NoteEditorViewModelEventBusTest {

    private NoteEditorViewModel viewModel;
    private NoteService noteService;
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        var repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        AppState appState = new AppState();
        eventBus = new EventBus();
        viewModel = new NoteEditorViewModel(
                noteService, new AttributeSchemaRegistry(),
                appState, eventBus);
    }

    @Test
    @DisplayName("saveTitle publishes NoteRenamedEvent")
    void saveTitle_publishesNoteRenamedEvent() {
        Note note = noteService.createNote("Old", "text");
        viewModel.setNote(note.getId());
        List<NoteRenamedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteRenamedEvent.class, events::add);

        viewModel.saveTitle("New Title");

        assertEquals(1, events.size());
        assertEquals(note.getId(), events.get(0).noteId());
    }

    @Test
    @DisplayName("saveText publishes NoteUpdatedEvent")
    void saveText_publishesNoteUpdatedEvent() {
        Note note = noteService.createNote("Title", "old");
        viewModel.setNote(note.getId());
        List<NoteUpdatedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteUpdatedEvent.class, events::add);

        viewModel.saveText("new text");

        assertEquals(1, events.size());
        assertEquals(note.getId(), events.get(0).noteId());
    }

    @Test
    @DisplayName("saveAttribute publishes NoteUpdatedEvent")
    void saveAttribute_publishesNoteUpdatedEvent() {
        Note note = noteService.createNote("Title", "text");
        viewModel.setNote(note.getId());
        List<NoteUpdatedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteUpdatedEvent.class, events::add);

        viewModel.saveAttribute("$Color", "red");

        assertEquals(1, events.size());
        assertEquals(note.getId(), events.get(0).noteId());
    }
}
