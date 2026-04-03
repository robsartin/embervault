package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Edge-case tests for {@link MapViewModel} — null baseNoteId, drillDown
 * to leaf, navigateBack when empty, zoom boundaries.
 */
class MapViewModelEdgeCaseTest {

    private MapViewModel viewModel;
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
        viewModel = new MapViewModel(noteTitle, noteService,
                noteService, noteService, appState, new EventBus());
    }

    @Nested
    @DisplayName("loadNotes with null baseNoteId")
    class LoadNotesNull {

        @Test
        @DisplayName("loadNotes clears items when baseNoteId is null")
        void nullBaseNoteId_clearsItems() {
            viewModel.loadNotes();

            assertTrue(viewModel.getNoteItems().isEmpty());
        }

        @Test
        @DisplayName("loadNotes clears items after baseNoteId set to null")
        void resetToNull_clearsItems() {
            Note parent = noteService.createNote("Parent", "");
            noteService.createChildNote(parent.getId(), "Child");
            viewModel.setBaseNoteId(parent.getId());
            viewModel.loadNotes();
            assertEquals(1, viewModel.getNoteItems().size());

            viewModel.setBaseNoteId(null);
            viewModel.loadNotes();

            assertTrue(viewModel.getNoteItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("drillDown to leaf note")
    class DrillDownToLeaf {

        @Test
        @DisplayName("drillDown to leaf shows empty note items")
        void drillDownToLeaf_emptyItems() {
            Note root = noteService.createNote("Root", "");
            Note leaf = noteService.createChildNote(root.getId(), "Leaf");
            viewModel.setBaseNoteId(root.getId());
            viewModel.loadNotes();

            viewModel.drillDown(leaf.getId());

            assertTrue(viewModel.getNoteItems().isEmpty());
            assertEquals(leaf.getId(), viewModel.getBaseNoteId());
            assertEquals("Map: Leaf", viewModel.tabTitleProperty().get());
        }
    }

    @Nested
    @DisplayName("navigateBack when empty")
    class NavigateBackEmpty {

        @Test
        @DisplayName("navigateBack with no history is no-op")
        void noHistory_noOp() {
            Note root = noteService.createNote("Root", "");
            viewModel.setBaseNoteId(root.getId());

            viewModel.navigateBack();

            assertEquals(root.getId(), viewModel.getBaseNoteId());
            assertFalse(viewModel.canNavigateBackProperty().get());
        }
    }

    @Nested
    @DisplayName("createChildNote without baseNoteId")
    class CreateChildNoteNoBase {

        @Test
        @DisplayName("createChildNote throws when baseNoteId is null")
        void nullBaseNoteId_throws() {
            assertThrows(NullPointerException.class,
                    () -> viewModel.createChildNote("Title"));
        }
    }

    @Nested
    @DisplayName("updateNotePosition for non-existent item")
    class UpdatePositionEdge {

        @Test
        @DisplayName("updateNotePosition for unknown id does not crash")
        void unknownId_noCrash() {
            Note parent = noteService.createNote("Parent", "");
            viewModel.setBaseNoteId(parent.getId());
            viewModel.createChildNote("Child");

            // Update position for a note not in the list — should not throw
            viewModel.updateNotePosition(
                    java.util.UUID.randomUUID(), 50.0, 50.0);

            // Existing item should be unchanged
            assertEquals(1, viewModel.getNoteItems().size());
        }
    }

    @Nested
    @DisplayName("renameNote edge cases")
    class RenameEdgeCases {

        @Test
        @DisplayName("renameNote with null returns false")
        void nullTitle_returnsFalse() {
            Note parent = noteService.createNote("Parent", "");
            viewModel.setBaseNoteId(parent.getId());
            NoteDisplayItem item = viewModel.createChildNote("Title");

            boolean result = viewModel.renameNote(item.getId(), null);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Zoom edge cases")
    class ZoomEdgeCases {

        @Test
        @DisplayName("repeated zoomOut stops at minimum")
        void repeatedZoomOut_stopsAtMin() {
            for (int i = 0; i < 100; i++) {
                viewModel.zoomOut();
            }

            assertEquals(0.1, viewModel.zoomLevelProperty().get(), 0.001);
        }

        @Test
        @DisplayName("repeated zoomIn stops at maximum")
        void repeatedZoomIn_stopsAtMax() {
            for (int i = 0; i < 100; i++) {
                viewModel.zoomIn();
            }

            assertEquals(5.0, viewModel.zoomLevelProperty().get(), 0.001);
        }

        @Test
        @DisplayName("getCurrentTier returns OVERVIEW at min zoom")
        void minZoom_overviewTier() {
            viewModel.setZoomLevel(0.1);

            assertEquals(ZoomTier.OVERVIEW, viewModel.getCurrentTier());
        }

        @Test
        @DisplayName("getCurrentTier returns DETAILED at max zoom")
        void maxZoom_detailedTier() {
            viewModel.setZoomLevel(5.0);

            assertEquals(ZoomTier.DETAILED, viewModel.getCurrentTier());
        }
    }
}
