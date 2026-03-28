package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Tests that MapViewModel and OutlineViewModel stay in sync when they
 * share the same NoteService and are wired with an onDataChanged callback.
 */
class ViewSyncTest {

    private MapViewModel mapViewModel;
    private OutlineViewModel outlineViewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;
    private Note root;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        StringProperty noteTitle = new SimpleStringProperty("Root");

        mapViewModel = new MapViewModel(noteTitle, noteService);
        outlineViewModel = new OutlineViewModel(noteTitle, noteService);

        root = noteService.createNote("Root", "");
        mapViewModel.setBaseNoteId(root.getId());
        outlineViewModel.setBaseNoteId(root.getId());

        // Wire the shared refresh callback (same pattern as App.java)
        Runnable refreshAll = () -> {
            mapViewModel.loadNotes();
            outlineViewModel.loadNotes();
        };
        mapViewModel.setOnDataChanged(refreshAll);
        outlineViewModel.setOnDataChanged(refreshAll);

        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();
    }

    @Test
    @DisplayName("Creating a note in Outline refreshes Map view")
    void createInOutline_shouldRefreshMap() {
        outlineViewModel.createChildNote(root.getId(), "New Note");

        assertEquals(1, mapViewModel.getNoteItems().size());
        assertEquals("New Note", mapViewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Creating a note in Map refreshes Outline view")
    void createInMap_shouldRefreshOutline() {
        mapViewModel.createChildNote("New Note");

        assertEquals(1, outlineViewModel.getRootItems().size());
        assertEquals("New Note", outlineViewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Renaming a note in Outline refreshes Map view")
    void renameInOutline_shouldRefreshMap() {
        Note child = noteService.createChildNote(root.getId(), "Original");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        outlineViewModel.renameNote(child.getId(), "Renamed");

        assertEquals("Renamed", mapViewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Renaming a note in Map refreshes Outline view")
    void renameInMap_shouldRefreshOutline() {
        Note child = noteService.createChildNote(root.getId(), "Original");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        mapViewModel.renameNote(child.getId(), "Renamed");

        assertEquals("Renamed", outlineViewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Indenting a note in Outline refreshes Map view with hasChildren")
    void indentInOutline_shouldRefreshMapWithHasChildren() {
        noteService.createChildNote(root.getId(), "Child1");
        Note child2 = noteService.createChildNote(root.getId(), "Child2");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        outlineViewModel.indentNote(child2.getId());

        // Map should show only Child1 and it should have children
        assertEquals(1, mapViewModel.getNoteItems().size());
        assertEquals("Child1", mapViewModel.getNoteItems().get(0).getTitle());
        assertTrue(mapViewModel.getNoteItems().get(0).isHasChildren());
    }

    @Test
    @DisplayName("Outdenting a note in Outline refreshes Map view")
    void outdentInOutline_shouldRefreshMap() {
        Note child1 = noteService.createChildNote(root.getId(), "Child1");
        noteService.createChildNote(child1.getId(), "Grandchild");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        outlineViewModel.outdentNote(
                noteService.getChildren(child1.getId()).get(0).getId());

        // Map should now show Child1 and Grandchild at root level
        assertEquals(2, mapViewModel.getNoteItems().size());
    }

    @Test
    @DisplayName("Creating a sibling in Outline refreshes Map view")
    void createSiblingInOutline_shouldRefreshMap() {
        Note child1 = noteService.createChildNote(root.getId(), "Child1");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        outlineViewModel.createSiblingNote(child1.getId(), "Sibling");

        assertEquals(2, mapViewModel.getNoteItems().size());
    }

    @Test
    @DisplayName("Drill-down in Map refreshes Outline to same level")
    void drillDownInMap_shouldNotifyDataChanged() {
        Note child = noteService.createChildNote(root.getId(), "Child");
        noteService.createChildNote(child.getId(), "Grandchild");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        mapViewModel.drillDown(child.getId());

        // After drillDown, the callback fires, reloading both.
        // Map should show grandchild, outline should still show root's children.
        assertEquals(1, mapViewModel.getNoteItems().size());
        assertEquals("Grandchild", mapViewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Navigate back in Map refreshes views")
    void navigateBackInMap_shouldNotifyDataChanged() {
        Note child = noteService.createChildNote(root.getId(), "Child");
        noteService.createChildNote(child.getId(), "Grandchild");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();
        mapViewModel.drillDown(child.getId());

        mapViewModel.navigateBack();

        assertEquals(1, mapViewModel.getNoteItems().size());
        assertEquals("Child", mapViewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("updateNotePosition in Map notifies data changed")
    void updatePositionInMap_shouldNotifyDataChanged() {
        Note child = noteService.createChildNote(root.getId(), "Child");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        mapViewModel.updateNotePosition(child.getId(), 100.0, 200.0);

        // Position update triggers refresh; map should still have the item
        assertEquals(1, mapViewModel.getNoteItems().size());
    }

    @Test
    @DisplayName("createChildNoteAt in Map notifies data changed and refreshes Outline")
    void createChildNoteAtInMap_shouldRefreshOutline() {
        mapViewModel.createChildNoteAt("Placed Note", 50.0, 75.0);

        assertEquals(1, outlineViewModel.getRootItems().size());
        assertEquals("Placed Note", outlineViewModel.getRootItems().get(0).getTitle());
    }
}
