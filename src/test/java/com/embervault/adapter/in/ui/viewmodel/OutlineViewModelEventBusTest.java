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
 * Tests that OutlineViewModel publishes typed events via EventBus.
 */
class OutlineViewModelEventBusTest {

    private OutlineViewModel viewModel;
    private NoteService noteService;
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        var repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        StringProperty noteTitle = new SimpleStringProperty("Root");
        AppState appState = new AppState();
        eventBus = new EventBus();
        viewModel = new OutlineViewModel(
                noteTitle, noteService, appState, eventBus);
    }

    @Test
    @DisplayName("createChildNote publishes NoteCreatedEvent")
    void createChildNote_publishesNoteCreatedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        List<NoteCreatedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteCreatedEvent.class, events::add);

        NoteDisplayItem item = viewModel.createChildNote(
                parent.getId(), "Child");

        assertEquals(1, events.size());
        assertEquals(item.getId(), events.get(0).noteId());
    }

    @Test
    @DisplayName("createSiblingNote publishes NoteCreatedEvent")
    void createSiblingNote_publishesNoteCreatedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem existing = viewModel.createChildNote(
                parent.getId(), "First");
        List<NoteCreatedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteCreatedEvent.class, events::add);

        viewModel.createSiblingNote(existing.getId(), "Second");

        assertEquals(1, events.size());
    }

    @Test
    @DisplayName("renameNote publishes NoteRenamedEvent")
    void renameNote_publishesNoteRenamedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote(
                parent.getId(), "Old");
        List<NoteRenamedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteRenamedEvent.class, events::add);

        viewModel.renameNote(item.getId(), "New");

        assertEquals(1, events.size());
        assertEquals("New", events.get(0).newTitle());
    }

    @Test
    @DisplayName("indentNote publishes NoteMovedEvent")
    void indentNote_publishesNoteMovedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        viewModel.createChildNote(parent.getId(), "First");
        NoteDisplayItem second = viewModel.createChildNote(
                parent.getId(), "Second");
        List<NoteMovedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteMovedEvent.class, events::add);

        viewModel.indentNote(second.getId());

        assertFalse(events.isEmpty());
    }

    @Test
    @DisplayName("outdentNote publishes NoteMovedEvent")
    void outdentNote_publishesNoteMovedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem first = viewModel.createChildNote(
                parent.getId(), "First");
        NoteDisplayItem child = viewModel.createChildNote(
                first.getId(), "Child");
        List<NoteMovedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteMovedEvent.class, events::add);

        viewModel.outdentNote(child.getId());

        assertFalse(events.isEmpty());
    }

    @Test
    @DisplayName("moveNoteToPosition publishes NoteMovedEvent")
    void moveNoteToPosition_publishesNoteMovedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem first = viewModel.createChildNote(
                parent.getId(), "First");
        viewModel.createChildNote(parent.getId(), "Second");
        List<NoteMovedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteMovedEvent.class, events::add);

        viewModel.moveNoteToPosition(
                first.getId(), parent.getId(), 1);

        assertFalse(events.isEmpty());
    }

    @Test
    @DisplayName("deleteNote publishes NoteDeletedEvent")
    void deleteNote_publishesNoteDeletedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote(
                parent.getId(), "Leaf");
        List<NoteDeletedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteDeletedEvent.class, events::add);

        viewModel.deleteNote(item.getId());

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
