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
 * Tests that MapViewModel, OutlineViewModel, TreemapViewModel,
 * SearchViewModel, and SelectedNoteViewModel stay in sync when they
 * share the same NoteService and are wired with an onDataChanged callback.
 */
class ViewSyncTest {

    private MapViewModel mapViewModel;
    private OutlineViewModel outlineViewModel;
    private TreemapViewModel treemapViewModel;
    private SearchViewModel searchViewModel;
    private SelectedNoteViewModel selectedNoteViewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;
    private Note root;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        StringProperty noteTitle = new SimpleStringProperty("Root");

        AppState appState = new AppState();
        EventBus eventBus = new EventBus();
        new AppStateEventBridge(eventBus, appState);
        mapViewModel = new MapViewModel(noteTitle, noteService,
                noteService, noteService, appState, eventBus);
        outlineViewModel = new OutlineViewModel(
                noteTitle, noteService, noteService,
                noteService, noteService, noteService,
                noteService, appState, eventBus);
        treemapViewModel = new TreemapViewModel(
                noteTitle, noteService, noteService,
                appState, eventBus);
        searchViewModel = new SearchViewModel(noteService, appState);
        selectedNoteViewModel = new SelectedNoteViewModel(
                noteService, noteService, noteService,
                appState, eventBus);

        root = noteService.createNote("Root", "");
        mapViewModel.setBaseNoteId(root.getId());
        outlineViewModel.setBaseNoteId(root.getId());
        treemapViewModel.setBaseNoteId(root.getId());

        // Wire the shared refresh via appState listener (same pattern as App.java)
        appState.dataVersionProperty().addListener((obs, oldVal, newVal) -> {
            mapViewModel.loadNotes();
            outlineViewModel.loadNotes();
            treemapViewModel.loadNotes();
            searchViewModel.refreshResults();
            var selId = selectedNoteViewModel.selectedNoteIdProperty().get();
            if (selId != null) {
                selectedNoteViewModel.setSelectedNoteId(selId);
            }
        });

        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();
        treemapViewModel.loadNotes();
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
    @DisplayName("Indent middle child in Outline removes it from Map's children (issue #118)")
    void indentMiddleChild_shouldRemoveFromMapChildren() {
        // Reproduce exact scenario from issue #118:
        // root -> A, B, C; indent B under A
        Note childA = noteService.createChildNote(root.getId(), "A");
        noteService.createChildNote(root.getId(), "B");
        Note childC = noteService.createChildNote(root.getId(), "C");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        // Verify initial state: Map shows 3 children
        assertEquals(3, mapViewModel.getNoteItems().size());

        // Indent B (goes under A, the note above it)
        outlineViewModel.indentNote(
                noteService.getChildren(root.getId()).get(1).getId());

        // After indent: Map should show only A and C (2 items), NOT B
        assertEquals(2, mapViewModel.getNoteItems().size(),
                "Map should show only 2 top-level notes after indent of B under A");
        assertEquals("A", mapViewModel.getNoteItems().get(0).getTitle());
        assertEquals("C", mapViewModel.getNoteItems().get(1).getTitle());

        // A should now have children flag set
        assertTrue(mapViewModel.getNoteItems().get(0).isHasChildren(),
                "A should report hasChildren=true after B was indented under it");

        // Verify getChildren at service level too
        assertEquals(1, noteService.getChildren(childA.getId()).size());
        assertEquals("B", noteService.getChildren(childA.getId()).get(0).getTitle());
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
        assertEquals("Placed Note",
                outlineViewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Creating a note in Map refreshes Treemap view")
    void createInMap_shouldRefreshTreemap() {
        mapViewModel.createChildNote("New Note");

        assertEquals(1, treemapViewModel.getNoteItems().size());
        assertEquals("New Note",
                treemapViewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Creating a note in Treemap refreshes Map and Outline")
    void createInTreemap_shouldRefreshMapAndOutline() {
        treemapViewModel.createChildNote("Treemap Note");

        assertEquals(1, mapViewModel.getNoteItems().size());
        assertEquals("Treemap Note",
                mapViewModel.getNoteItems().get(0).getTitle());
        assertEquals(1, outlineViewModel.getRootItems().size());
        assertEquals("Treemap Note",
                outlineViewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("DrillDown in Treemap notifies data changed")
    void drillDownInTreemap_shouldNotifyDataChanged() {
        Note child = noteService.createChildNote(root.getId(), "Child");
        noteService.createChildNote(child.getId(), "Grandchild");
        treemapViewModel.loadNotes();

        treemapViewModel.drillDown(child.getId());

        assertEquals(1, treemapViewModel.getNoteItems().size());
        assertEquals("Grandchild",
                treemapViewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Renaming a note in Map refreshes visible search results")
    void renameInMap_shouldRefreshSearchResults() {
        Note child = noteService.createChildNote(root.getId(), "Original");
        mapViewModel.loadNotes();

        // Simulate open search with matching query
        searchViewModel.toggleVisible();
        searchViewModel.queryProperty().set("original");
        searchViewModel.search("original");
        assertEquals(1, searchViewModel.getResults().size());

        // Rename the note via map
        mapViewModel.renameNote(child.getId(), "Renamed");

        // Search results should have been refreshed — "original" no longer matches
        assertEquals(0, searchViewModel.getResults().size());
    }

    @Test
    @DisplayName("Creating a note refreshes search results when search is visible")
    void createInOutline_shouldRefreshSearchResults() {
        // Open search for "child"
        searchViewModel.toggleVisible();
        searchViewModel.queryProperty().set("child");
        searchViewModel.search("child");
        assertEquals(0, searchViewModel.getResults().size());

        // Create a matching note
        outlineViewModel.createChildNote(root.getId(), "Child Note");

        // Search should now find it
        assertEquals(1, searchViewModel.getResults().size());
        assertEquals("Child Note", searchViewModel.getResults().get(0).getTitle());
    }

    @Test
    @DisplayName("Renaming a note in Outline refreshes SelectedNoteViewModel title")
    void renameInOutline_shouldRefreshSelectedNote() {
        Note child = noteService.createChildNote(root.getId(), "Original");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        // Select the note in the text pane
        selectedNoteViewModel.setSelectedNoteId(child.getId());
        assertEquals("Original", selectedNoteViewModel.titleProperty().get());

        // Rename via outline
        outlineViewModel.renameNote(child.getId(), "Renamed");

        // Text pane should reflect the new title
        assertEquals("Renamed", selectedNoteViewModel.titleProperty().get());
    }

    @Test
    @DisplayName("Saving title in SelectedNoteViewModel refreshes Map and Outline")
    void saveTitleInSelectedNote_shouldRefreshMapAndOutline() {
        Note child = noteService.createChildNote(root.getId(), "Original");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();
        selectedNoteViewModel.setSelectedNoteId(child.getId());

        selectedNoteViewModel.saveTitle("Updated Title");

        assertEquals("Updated Title", mapViewModel.getNoteItems().get(0).getTitle());
        assertEquals("Updated Title", outlineViewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("Deleting a note in Outline refreshes all views including search")
    void deleteInOutline_shouldRefreshAllViews() {
        Note child = noteService.createChildNote(root.getId(), "ToDelete");
        mapViewModel.loadNotes();
        outlineViewModel.loadNotes();

        searchViewModel.toggleVisible();
        searchViewModel.queryProperty().set("delete");
        searchViewModel.search("delete");
        assertEquals(1, searchViewModel.getResults().size());

        outlineViewModel.deleteNote(child.getId());

        assertEquals(0, mapViewModel.getNoteItems().size());
        assertEquals(0, treemapViewModel.getNoteItems().size());
        assertEquals(0, searchViewModel.getResults().size());
    }
}
