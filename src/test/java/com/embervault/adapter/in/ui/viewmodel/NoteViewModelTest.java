package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteViewModelTest {

    private NoteViewModel viewModel;
    private NoteService noteService;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        viewModel = new NoteViewModel(noteService);
    }

    @Test
    @DisplayName("loadNotes() populates the observable list from the service")
    void loadNotes_shouldPopulateList() {
        noteService.createNote("A", "a");
        noteService.createNote("B", "b");

        viewModel.loadNotes();

        assertEquals(2, viewModel.getNotes().size());
    }

    @Test
    @DisplayName("addNote() creates a note and adds it to the list")
    void addNote_shouldCreateAndAppend() {
        viewModel.titleProperty().set("New Title");
        viewModel.contentProperty().set("New Content");

        viewModel.addNote();

        assertEquals(1, viewModel.getNotes().size());
        assertEquals("New Title", viewModel.getNotes().get(0).getTitle());
    }

    @Test
    @DisplayName("addNote() clears editor fields after creation")
    void addNote_shouldClearEditor() {
        viewModel.titleProperty().set("Title");
        viewModel.contentProperty().set("Content");

        viewModel.addNote();

        assertEquals("", viewModel.titleProperty().get());
        assertEquals("", viewModel.contentProperty().get());
    }

    @Test
    @DisplayName("selectNote() populates editor fields")
    void selectNote_shouldPopulateEditor() {
        viewModel.titleProperty().set("Title");
        viewModel.contentProperty().set("Content");
        viewModel.addNote();
        NoteDisplayItem item = viewModel.getNotes().get(0);

        viewModel.selectNote(item);

        assertEquals("Title", viewModel.titleProperty().get());
        assertEquals("Content", viewModel.contentProperty().get());
        assertEquals(item, viewModel.selectedNoteProperty().get());
    }

    @Test
    @DisplayName("selectNote(null) clears editor fields")
    void selectNote_null_shouldClearEditor() {
        viewModel.titleProperty().set("Residual");
        viewModel.contentProperty().set("Residual");

        viewModel.selectNote(null);

        assertEquals("", viewModel.titleProperty().get());
        assertEquals("", viewModel.contentProperty().get());
        assertNull(viewModel.selectedNoteProperty().get());
    }

    @Test
    @DisplayName("saveNote() updates the selected note in the list")
    void saveNote_shouldUpdateSelectedNote() {
        viewModel.titleProperty().set("Original");
        viewModel.contentProperty().set("Original");
        viewModel.addNote();

        NoteDisplayItem created = viewModel.getNotes().get(0);
        viewModel.selectNote(created);
        viewModel.titleProperty().set("Updated");
        viewModel.contentProperty().set("Updated");

        viewModel.saveNote();

        assertEquals("Updated", viewModel.getNotes().get(0).getTitle());
        assertEquals("Updated", viewModel.getNotes().get(0).getContent());
    }

    @Test
    @DisplayName("saveNote() does nothing when no note is selected")
    void saveNote_shouldDoNothingWhenNothingSelected() {
        viewModel.saveNote();
        // no exception
        assertTrue(viewModel.getNotes().isEmpty());
    }

    @Test
    @DisplayName("deleteNote() removes the selected note from the list")
    void deleteNote_shouldRemoveSelectedNote() {
        viewModel.titleProperty().set("ToDelete");
        viewModel.contentProperty().set("ToDelete");
        viewModel.addNote();

        NoteDisplayItem created = viewModel.getNotes().get(0);
        viewModel.selectNote(created);

        viewModel.deleteNote();

        assertTrue(viewModel.getNotes().isEmpty());
        assertNull(viewModel.selectedNoteProperty().get());
        assertEquals("", viewModel.titleProperty().get());
    }

    @Test
    @DisplayName("deleteNote() does nothing when no note is selected")
    void deleteNote_shouldDoNothingWhenNothingSelected() {
        viewModel.deleteNote();
        // no exception
        assertTrue(viewModel.getNotes().isEmpty());
    }

    @Test
    @DisplayName("saveNote() handles case where note is not in observable list")
    void saveNote_shouldHandleMissingNoteInList() {
        // Create a note via ViewModel
        viewModel.titleProperty().set("Title");
        viewModel.contentProperty().set("Content");
        viewModel.addNote();

        NoteDisplayItem created = viewModel.getNotes().get(0);
        viewModel.selectNote(created);

        // Clear the observable list directly, simulating an out-of-sync state
        viewModel.getNotes().clear();

        viewModel.titleProperty().set("Updated");
        viewModel.contentProperty().set("Updated");

        // Should not throw, even though the note is not in the list
        viewModel.saveNote();
        assertTrue(viewModel.getNotes().isEmpty());
    }
}
