package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SelectedNoteViewModelTest {

    private SelectedNoteViewModel viewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        viewModel = new SelectedNoteViewModel(noteService);
    }

    @Test
    @DisplayName("Constructor rejects null noteService")
    void constructor_shouldRejectNullNoteService() {
        assertThrows(NullPointerException.class,
                () -> new SelectedNoteViewModel(null));
    }

    @Test
    @DisplayName("Initial state has empty title and text")
    void initialState_shouldHaveEmptyTitleAndText() {
        assertEquals("", viewModel.titleProperty().get());
        assertEquals("", viewModel.textProperty().get());
        assertNull(viewModel.selectedNoteIdProperty().get());
    }

    @Test
    @DisplayName("setSelectedNoteId loads note title and text")
    void setSelectedNoteId_shouldLoadTitleAndText() {
        Note note = noteService.createNote("Test Title", "Test Content");

        viewModel.setSelectedNoteId(note.getId());

        assertEquals("Test Title", viewModel.titleProperty().get());
        assertEquals("Test Content", viewModel.textProperty().get());
        assertEquals(note.getId(), viewModel.selectedNoteIdProperty().get());
    }

    @Test
    @DisplayName("setSelectedNoteId(null) clears title and text")
    void setSelectedNoteId_null_shouldClearTitleAndText() {
        Note note = noteService.createNote("Test", "Content");
        viewModel.setSelectedNoteId(note.getId());

        viewModel.setSelectedNoteId(null);

        assertEquals("", viewModel.titleProperty().get());
        assertEquals("", viewModel.textProperty().get());
        assertNull(viewModel.selectedNoteIdProperty().get());
    }

    @Test
    @DisplayName("setSelectedNoteId with unknown id clears properties")
    void setSelectedNoteId_unknownId_shouldClearProperties() {
        Note note = noteService.createNote("Test", "Content");
        viewModel.setSelectedNoteId(note.getId());

        viewModel.setSelectedNoteId(UUID.randomUUID());

        assertEquals("", viewModel.titleProperty().get());
        assertEquals("", viewModel.textProperty().get());
    }

    @Test
    @DisplayName("Changing selection loads new note data")
    void changingSelection_shouldLoadNewNoteData() {
        Note note1 = noteService.createNote("Note One", "Content One");
        Note note2 = noteService.createNote("Note Two", "Content Two");

        viewModel.setSelectedNoteId(note1.getId());
        assertEquals("Note One", viewModel.titleProperty().get());

        viewModel.setSelectedNoteId(note2.getId());
        assertEquals("Note Two", viewModel.titleProperty().get());
        assertEquals("Content Two", viewModel.textProperty().get());
    }

    @Test
    @DisplayName("saveTitle persists and updates title property")
    void saveTitle_shouldPersistAndUpdateProperty() {
        Note note = noteService.createNote("Old Title", "Content");
        viewModel.setSelectedNoteId(note.getId());

        viewModel.saveTitle("New Title");

        assertEquals("New Title", viewModel.titleProperty().get());
        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        assertEquals("New Title", reloaded.getTitle());
    }

    @Test
    @DisplayName("saveTitle does nothing when no note is selected")
    void saveTitle_shouldDoNothingWhenNoNote() {
        viewModel.saveTitle("Something");
        assertEquals("", viewModel.titleProperty().get());
    }

    @Test
    @DisplayName("saveTitle does nothing for blank title")
    void saveTitle_shouldDoNothingForBlankTitle() {
        Note note = noteService.createNote("Original", "Content");
        viewModel.setSelectedNoteId(note.getId());

        viewModel.saveTitle("   ");

        assertEquals("Original", viewModel.titleProperty().get());
    }

    @Test
    @DisplayName("saveTitle does nothing for null title")
    void saveTitle_shouldDoNothingForNullTitle() {
        Note note = noteService.createNote("Original", "Content");
        viewModel.setSelectedNoteId(note.getId());

        viewModel.saveTitle(null);

        assertEquals("Original", viewModel.titleProperty().get());
    }

    @Test
    @DisplayName("saveText persists and updates text property")
    void saveText_shouldPersistAndUpdateProperty() {
        Note note = noteService.createNote("Title", "Old Content");
        viewModel.setSelectedNoteId(note.getId());

        viewModel.saveText("New Content");

        assertEquals("New Content", viewModel.textProperty().get());
        Note reloaded = noteService.getNote(note.getId()).orElseThrow();
        assertEquals("New Content", reloaded.getContent());
    }

    @Test
    @DisplayName("saveText does nothing when no note is selected")
    void saveText_shouldDoNothingWhenNoNote() {
        viewModel.saveText("Something");
        assertEquals("", viewModel.textProperty().get());
    }

    @Test
    @DisplayName("saveText does nothing for null text")
    void saveText_shouldDoNothingForNull() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setSelectedNoteId(note.getId());

        viewModel.saveText(null);

        assertEquals("Content", viewModel.textProperty().get());
    }

    @Test
    @DisplayName("saveTitle notifies data changed")
    void saveTitle_shouldNotifyDataChanged() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setSelectedNoteId(note.getId());
        boolean[] notified = {false};
        viewModel.setOnDataChanged(() -> notified[0] = true);

        viewModel.saveTitle("New Title");

        assertTrue(notified[0]);
    }

    @Test
    @DisplayName("saveText notifies data changed")
    void saveText_shouldNotifyDataChanged() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setSelectedNoteId(note.getId());
        boolean[] notified = {false};
        viewModel.setOnDataChanged(() -> notified[0] = true);

        viewModel.saveText("New Content");

        assertTrue(notified[0]);
    }

    @Test
    @DisplayName("onDataChanged callback can be cleared with null")
    void onDataChanged_shouldBeCleared() {
        Note note = noteService.createNote("Title", "Content");
        viewModel.setSelectedNoteId(note.getId());
        boolean[] notified = {false};
        viewModel.setOnDataChanged(() -> notified[0] = true);
        viewModel.setOnDataChanged(null);

        viewModel.saveTitle("New Title");

        // notified should still be false since callback was cleared
        assertTrue(!notified[0]);
    }

    @Test
    @DisplayName("setSelectedNoteId loads note with $Text attribute")
    void setSelectedNoteId_shouldLoadNoteWithTextAttribute() {
        Note note = noteService.createNote("Title", "");
        note.setAttribute("$Text",
                new AttributeValue.StringValue("Attribute text"));

        viewModel.setSelectedNoteId(note.getId());

        assertEquals("Attribute text", viewModel.textProperty().get());
    }
}
