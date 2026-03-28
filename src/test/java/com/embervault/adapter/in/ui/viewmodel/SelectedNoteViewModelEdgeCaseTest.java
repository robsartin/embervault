package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Edge-case tests for {@link SelectedNoteViewModel}.
 */
class SelectedNoteViewModelEdgeCaseTest {

    private SelectedNoteViewModel viewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        viewModel = new SelectedNoteViewModel(noteService);
    }

    @Nested
    @DisplayName("saveTitle edge cases")
    class SaveTitle {

        @Test
        @DisplayName("saveTitle with empty string does not persist")
        void emptyString_doesNotPersist() {
            Note note = noteService.createNote("Original", "Content");
            viewModel.setSelectedNoteId(note.getId());

            viewModel.saveTitle("");

            assertEquals("Original", viewModel.titleProperty().get());
        }

        @Test
        @DisplayName("saveTitle does not notify when no note selected")
        void noNoteSelected_doesNotNotify() {
            boolean[] notified = {false};
            viewModel.setOnDataChanged(() -> notified[0] = true);

            viewModel.saveTitle("Something");

            assertFalse(notified[0]);
        }

        @Test
        @DisplayName("saveTitle does not notify for blank title")
        void blankTitle_doesNotNotify() {
            Note note = noteService.createNote("Title", "Content");
            viewModel.setSelectedNoteId(note.getId());
            boolean[] notified = {false};
            viewModel.setOnDataChanged(() -> notified[0] = true);

            viewModel.saveTitle("   ");

            assertFalse(notified[0]);
        }
    }

    @Nested
    @DisplayName("saveText edge cases")
    class SaveText {

        @Test
        @DisplayName("saveText with empty string saves empty content")
        void emptyString_savesEmptyContent() {
            Note note = noteService.createNote("Title", "Original");
            viewModel.setSelectedNoteId(note.getId());

            viewModel.saveText("");

            assertEquals("", viewModel.textProperty().get());
            assertEquals("", noteService.getNote(note.getId())
                    .orElseThrow().getContent());
        }

        @Test
        @DisplayName("saveText does not notify when no note selected")
        void noNoteSelected_doesNotNotify() {
            boolean[] notified = {false};
            viewModel.setOnDataChanged(() -> notified[0] = true);

            viewModel.saveText("Something");

            assertFalse(notified[0]);
        }
    }

    @Nested
    @DisplayName("setSelectedNoteId edge cases")
    class SetSelectedNoteId {

        @Test
        @DisplayName("selecting same note twice reloads properties")
        void sameNoteTwice_reloadsProperties() {
            Note note = noteService.createNote("Title", "Content");
            viewModel.setSelectedNoteId(note.getId());

            // Modify the note externally
            noteService.renameNote(note.getId(), "Modified Title");

            // Re-select the same note
            viewModel.setSelectedNoteId(note.getId());

            assertEquals("Modified Title", viewModel.titleProperty().get());
        }

        @Test
        @DisplayName("selecting deleted note clears properties")
        void deletedNote_clearsProperties() {
            Note note = noteService.createNote("Title", "Content");
            noteService.deleteNote(note.getId());

            viewModel.setSelectedNoteId(note.getId());

            assertEquals("", viewModel.titleProperty().get());
            assertEquals("", viewModel.textProperty().get());
        }
    }
}
