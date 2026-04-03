package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that TreemapViewModel publishes typed events via EventBus.
 */
class TreemapViewModelEventBusTest {

    private TreemapViewModel viewModel;
    private NoteService noteService;
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        var repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        StringProperty noteTitle = new SimpleStringProperty("Root");
        AppState appState = new AppState();
        eventBus = new EventBus();
        viewModel = new TreemapViewModel(
                noteTitle, noteService, noteService,
                appState, eventBus);
    }

    @Test
    @DisplayName("createChildNote publishes NoteCreatedEvent")
    void createChildNote_publishesNoteCreatedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        List<NoteCreatedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteCreatedEvent.class, events::add);

        NoteDisplayItem item = viewModel.createChildNote("Child");

        assertEquals(1, events.size());
        assertEquals(item.getId(), events.get(0).noteId());
    }

    @Test
    @DisplayName("drillDown publishes NoteMovedEvent")
    void drillDown_publishesNoteMovedEvent() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        List<NoteMovedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteMovedEvent.class, events::add);

        viewModel.drillDown(child.getId());

        assertFalse(events.isEmpty());
    }

    @Test
    @DisplayName("navigateBack publishes NoteMovedEvent")
    void navigateBack_publishesNoteMovedEvent() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        viewModel.drillDown(child.getId());
        List<NoteMovedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteMovedEvent.class, events::add);

        viewModel.navigateBack();

        assertFalse(events.isEmpty());
    }
}
