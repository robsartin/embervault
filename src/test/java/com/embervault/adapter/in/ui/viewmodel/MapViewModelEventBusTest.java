package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Tests that MapViewModel publishes typed events via EventBus.
 */
class MapViewModelEventBusTest {

    private MapViewModel viewModel;
    private NoteService noteService;
    private EventBus eventBus;
    private AppState appState;

    @BeforeEach
    void setUp() {
        var repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        StringProperty noteTitle = new SimpleStringProperty("Root");
        appState = new AppState();
        eventBus = new EventBus();
        viewModel = new MapViewModel(
                noteTitle, noteService, appState, eventBus);
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
    @DisplayName("createChildNoteAt publishes NoteCreatedEvent")
    void createChildNoteAt_publishesNoteCreatedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        List<NoteCreatedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteCreatedEvent.class, events::add);

        NoteDisplayItem item = viewModel.createChildNoteAt(
                "Child", 100.0, 200.0);

        assertEquals(1, events.size());
        assertEquals(item.getId(), events.get(0).noteId());
    }

    @Test
    @DisplayName("createSiblingNote publishes NoteCreatedEvent")
    void createSiblingNote_publishesNoteCreatedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem existing = viewModel.createChildNote("First");
        List<NoteCreatedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteCreatedEvent.class, events::add);

        NoteDisplayItem sibling = viewModel.createSiblingNote(
                existing.getId(), "Second");

        assertEquals(1, events.size());
        assertEquals(sibling.getId(), events.get(0).noteId());
    }

    @Test
    @DisplayName("renameNote publishes NoteRenamedEvent")
    void renameNote_publishesNoteRenamedEvent() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote("Old");
        List<NoteRenamedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteRenamedEvent.class, events::add);

        viewModel.renameNote(item.getId(), "New");

        assertEquals(1, events.size());
        assertEquals(item.getId(), events.get(0).noteId());
        assertEquals("New", events.get(0).newTitle());
    }

    @Test
    @DisplayName("renameNote does not publish event for blank title")
    void renameNote_doesNotPublishForBlankTitle() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote("Title");
        List<NoteRenamedEvent> events = new ArrayList<>();
        eventBus.subscribe(NoteRenamedEvent.class, events::add);

        viewModel.renameNote(item.getId(), "");

        assertTrue(events.isEmpty());
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

    @Test
    @DisplayName("events trigger notifyDataChanged via bridge")
    void events_triggerNotifyDataChangedViaBridge() {
        new AppStateEventBridge(eventBus, appState);
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        int before = appState.getDataVersion();

        viewModel.createChildNote("Child");

        assertTrue(appState.getDataVersion() > before);
    }
}
