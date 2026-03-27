package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OutlineViewModelTest {

    private OutlineViewModel viewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;
    private StringProperty noteTitle;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        noteTitle = new SimpleStringProperty("My Note");
        viewModel = new OutlineViewModel(noteTitle, noteService);
    }

    @Test
    @DisplayName("tabTitle reflects the note title with Outline prefix")
    void tabTitle_shouldReflectNoteTitleWithOutlinePrefix() {
        assertEquals("Outline: My Note", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle updates when note title changes")
    void tabTitle_shouldUpdateWhenNoteTitleChanges() {
        noteTitle.set("Updated");

        assertEquals("Outline: Updated", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("Constructor rejects null noteTitle")
    void constructor_shouldRejectNullNoteTitle() {
        assertThrows(NullPointerException.class,
                () -> new OutlineViewModel(null, noteService));
    }

    @Test
    @DisplayName("Constructor rejects null noteService")
    void constructor_shouldRejectNullNoteService() {
        assertThrows(NullPointerException.class,
                () -> new OutlineViewModel(noteTitle, null));
    }

    @Test
    @DisplayName("loadNotes() populates root items from base note children")
    void loadNotes_shouldPopulateRootItems() {
        Note parent = noteService.createNote("Parent", "");
        noteService.createChildNote(parent.getId(), "Child1");
        noteService.createChildNote(parent.getId(), "Child2");
        viewModel.setBaseNoteId(parent.getId());

        viewModel.loadNotes();

        assertEquals(2, viewModel.getRootItems().size());
        assertEquals("Child1", viewModel.getRootItems().get(0).getTitle());
        assertEquals("Child2", viewModel.getRootItems().get(1).getTitle());
    }

    @Test
    @DisplayName("loadNotes() clears items when baseNoteId is null")
    void loadNotes_shouldClearWhenBaseNoteIdNull() {
        viewModel.loadNotes();

        assertTrue(viewModel.getRootItems().isEmpty());
    }

    @Test
    @DisplayName("createChildNote() under base note adds to root items")
    void createChildNote_underBaseNote_shouldAddToRootItems() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());

        NoteDisplayItem item = viewModel.createChildNote(parent.getId(), "New Child");

        assertNotNull(item);
        assertEquals("New Child", item.getTitle());
        assertEquals(1, viewModel.getRootItems().size());
    }

    @Test
    @DisplayName("createChildNote() under non-base note does not add to root items")
    void createChildNote_underNonBaseNote_shouldNotAddToRootItems() {
        Note parent = noteService.createNote("Parent", "");
        Note child = noteService.createChildNote(parent.getId(), "Child");
        viewModel.setBaseNoteId(parent.getId());

        NoteDisplayItem grandchild = viewModel.createChildNote(child.getId(), "Grandchild");

        assertNotNull(grandchild);
        assertEquals("Grandchild", grandchild.getTitle());
        // Grandchild should not appear in root items since its parent is not the base note
        assertTrue(viewModel.getRootItems().isEmpty());
    }

    @Test
    @DisplayName("selectNote() sets selectedNoteId")
    void selectNote_shouldSetSelectedNoteId() {
        UUID noteId = UUID.randomUUID();

        viewModel.selectNote(noteId);

        assertEquals(noteId, viewModel.selectedNoteIdProperty().get());
    }

    @Test
    @DisplayName("selectNote(null) clears selection")
    void selectNote_null_shouldClearSelection() {
        viewModel.selectNote(UUID.randomUUID());

        viewModel.selectNote(null);

        assertNull(viewModel.selectedNoteIdProperty().get());
    }

    @Test
    @DisplayName("getChildren() returns display items for child notes")
    void getChildren_shouldReturnChildDisplayItems() {
        Note parent = noteService.createNote("Parent", "");
        noteService.createChildNote(parent.getId(), "Child1");
        noteService.createChildNote(parent.getId(), "Child2");

        ObservableList<NoteDisplayItem> children = viewModel.getChildren(parent.getId());

        assertEquals(2, children.size());
        assertEquals("Child1", children.get(0).getTitle());
        assertEquals("Child2", children.get(1).getTitle());
    }

    @Test
    @DisplayName("setBaseNoteId and getBaseNoteId work correctly")
    void baseNoteId_shouldBeSettableAndGettable() {
        UUID id = UUID.randomUUID();
        viewModel.setBaseNoteId(id);

        assertEquals(id, viewModel.getBaseNoteId());
    }
}
