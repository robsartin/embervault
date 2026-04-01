package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Edge-case tests for {@link OutlineViewModel} — null baseNoteId, drillDown
 * to leaf, navigateBack when empty, and rename edge cases.
 */
class OutlineViewModelEdgeCaseTest {

    private OutlineViewModel viewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;
    private StringProperty noteTitle;
    private AppState appState;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        noteTitle = new SimpleStringProperty("Root Title");
        appState = new AppState();
        viewModel = new OutlineViewModel(noteTitle, noteService, appState);
    }

    @Nested
    @DisplayName("loadNotes with null baseNoteId")
    class LoadNotesNull {

        @Test
        @DisplayName("loadNotes clears items when baseNoteId is never set")
        void neverSet_clearsItems() {
            // baseNoteId was never set (null)
            viewModel.loadNotes();

            assertTrue(viewModel.getRootItems().isEmpty());
        }

        @Test
        @DisplayName("loadNotes clears previous items when baseNoteId becomes null")
        void becomeNull_clearsPreviousItems() {
            Note parent = noteService.createNote("Parent", "");
            noteService.createChildNote(parent.getId(), "Child");
            viewModel.setBaseNoteId(parent.getId());
            viewModel.loadNotes();
            assertEquals(1, viewModel.getRootItems().size());

            viewModel.setBaseNoteId(null);
            viewModel.loadNotes();

            assertTrue(viewModel.getRootItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("drillDown to leaf note (no children)")
    class DrillDownToLeaf {

        @Test
        @DisplayName("drillDown to leaf shows empty root items")
        void drillDownToLeaf_emptyRootItems() {
            Note root = noteService.createNote("Root", "");
            Note leaf = noteService.createChildNote(root.getId(), "Leaf");
            viewModel.setBaseNoteId(root.getId());
            viewModel.loadNotes();

            viewModel.drillDown(leaf.getId());

            assertTrue(viewModel.getRootItems().isEmpty(),
                    "Leaf note has no children, so root items should be empty");
            assertEquals(leaf.getId(), viewModel.getBaseNoteId());
            assertEquals("Outline: Leaf", viewModel.tabTitleProperty().get());
        }

        @Test
        @DisplayName("navigateBack after drillDown to leaf restores previous state")
        void navigateBackAfterDrillDownToLeaf() {
            Note root = noteService.createNote("Root", "");
            Note leaf = noteService.createChildNote(root.getId(), "Leaf");
            viewModel.setBaseNoteId(root.getId());
            viewModel.loadNotes();

            viewModel.drillDown(leaf.getId());
            viewModel.navigateBack();

            assertEquals(root.getId(), viewModel.getBaseNoteId());
            assertEquals(1, viewModel.getRootItems().size());
            assertEquals("Leaf", viewModel.getRootItems().get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("navigateBack when empty")
    class NavigateBackEmpty {

        @Test
        @DisplayName("navigateBack with no history does nothing")
        void noHistory_doesNothing() {
            Note root = noteService.createNote("Root", "");
            viewModel.setBaseNoteId(root.getId());
            viewModel.loadNotes();

            viewModel.navigateBack();

            assertEquals(root.getId(), viewModel.getBaseNoteId());
            assertFalse(viewModel.canNavigateBackProperty().get());
        }

        @Test
        @DisplayName("navigateBack twice after single drillDown: second is no-op")
        void doublePop_secondIsNoOp() {
            Note root = noteService.createNote("Root", "");
            Note child = noteService.createChildNote(root.getId(), "Child");
            viewModel.setBaseNoteId(root.getId());
            viewModel.loadNotes();
            viewModel.drillDown(child.getId());

            viewModel.navigateBack();
            viewModel.navigateBack(); // no-op

            assertEquals(root.getId(), viewModel.getBaseNoteId());
            assertFalse(viewModel.canNavigateBackProperty().get());
        }
    }

    @Nested
    @DisplayName("renameNote edge cases")
    class RenameEdgeCases {

        @Test
        @DisplayName("renameNote for item not in rootItems still persists rename")
        void renameNonRootItem_stillPersists() {
            Note parent = noteService.createNote("Parent", "");
            Note child = noteService.createChildNote(parent.getId(), "Child");
            Note grandchild = noteService.createChildNote(
                    child.getId(), "Grandchild");
            viewModel.setBaseNoteId(parent.getId());
            viewModel.loadNotes();

            // Grandchild is not in rootItems (only Child is)
            boolean result = viewModel.renameNote(
                    grandchild.getId(), "Renamed GC");

            assertTrue(result);
            // rootItems should not be affected
            assertEquals("Child",
                    viewModel.getRootItems().get(0).getTitle());
            // But the note should be renamed in the service
            assertEquals("Renamed GC",
                    noteService.getNote(grandchild.getId())
                            .orElseThrow().getTitle());
        }

        @Test
        @DisplayName("renameNote with whitespace-only title returns false")
        void whitespaceTitle_returnsFalse() {
            Note parent = noteService.createNote("Parent", "");
            viewModel.setBaseNoteId(parent.getId());
            NoteDisplayItem item = viewModel.createChildNote(
                    parent.getId(), "Title");

            boolean result = viewModel.renameNote(item.getId(), "   ");

            assertFalse(result);
            assertEquals("Title",
                    viewModel.getRootItems().get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("deleteNote edge cases")
    class DeleteEdgeCases {

        @Test
        @DisplayName("deleteNote returns false for non-existent id")
        void nonExistentId_returnsFalse() {
            boolean result = viewModel.deleteNote(UUID.randomUUID());

            assertFalse(result);
        }

        @Test
        @DisplayName("deleteNote does not notify when deletion fails")
        void failedDeletion_doesNotNotify() {
            Note parent = noteService.createNote("Parent", "");
            Note child = noteService.createChildNote(parent.getId(), "Child");
            noteService.createChildNote(child.getId(), "Grandchild");
            viewModel.setBaseNoteId(parent.getId());
            viewModel.loadNotes();

            int versionBefore = appState.getDataVersion();

            viewModel.deleteNote(child.getId()); // has children, fails

            assertEquals(versionBefore, appState.getDataVersion());
        }
    }
}
