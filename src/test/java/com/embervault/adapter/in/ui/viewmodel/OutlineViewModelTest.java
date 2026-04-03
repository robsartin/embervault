package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
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
    private AppState appState;
    private EventBus eventBus;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        noteTitle = new SimpleStringProperty("My Note");
        appState = new AppState();
        eventBus = new EventBus();
        new AppStateEventBridge(eventBus, appState);
        viewModel = new OutlineViewModel(
                noteTitle, noteService, appState, eventBus);
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
    @DisplayName("tabTitle truncates long note titles to 20 characters with ellipsis")
    void tabTitle_shouldTruncateLongTitles() {
        noteTitle.set("Welcome to EmberVault Application");

        assertEquals("Outline: Welcome to EmberVaul\u2026",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle does not truncate titles at exactly 20 characters")
    void tabTitle_shouldNotTruncateExactly20Chars() {
        noteTitle.set("12345678901234567890"); // exactly 20 chars

        assertEquals("Outline: 12345678901234567890",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("Constructor rejects null noteTitle")
    void constructor_shouldRejectNullNoteTitle() {
        assertThrows(NullPointerException.class,
                () -> new OutlineViewModel(null, noteService, appState));
    }

    @Test
    @DisplayName("Constructor rejects null noteService")
    void constructor_shouldRejectNullNoteService() {
        assertThrows(NullPointerException.class,
                () -> new OutlineViewModel(noteTitle, null, appState));
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

    @Test
    @DisplayName("renameNote() updates the display item title in root items")
    void renameNote_shouldUpdateDisplayItemTitle() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote(parent.getId(), "Old Title");

        boolean result = viewModel.renameNote(item.getId(), "New Title");

        assertTrue(result);
        assertEquals("New Title", viewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("renameNote() returns false for blank title")
    void renameNote_shouldReturnFalseForBlankTitle() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote(parent.getId(), "Title");

        boolean result = viewModel.renameNote(item.getId(), "");

        assertFalse(result);
        assertEquals("Title", viewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("renameNote() returns false for null title")
    void renameNote_shouldReturnFalseForNullTitle() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote(parent.getId(), "Title");

        boolean result = viewModel.renameNote(item.getId(), null);

        assertFalse(result);
    }

    // --- Drill-down navigation tests ---

    @Test
    @DisplayName("drillDown() changes baseNoteId and reloads children")
    void drillDown_shouldChangeBaseAndReload() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        noteService.createChildNote(child.getId(), "Grandchild");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.drillDown(child.getId());

        assertEquals(child.getId(), viewModel.getBaseNoteId());
        assertEquals(1, viewModel.getRootItems().size());
        assertEquals("Grandchild", viewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("drillDown() updates tab title to drilled-down note")
    void drillDown_shouldUpdateTabTitle() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.drillDown(child.getId());

        assertEquals("Outline: Child", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("drillDown() truncates long drilled-down note title")
    void drillDown_shouldTruncateLongDrilledDownTitle() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(),
                "A Very Long Child Note Title Here");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.drillDown(child.getId());

        assertEquals("Outline: A Very Long Child No\u2026",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("navigateBack() returns to previous base note")
    void navigateBack_shouldReturnToPreviousBase() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        noteService.createChildNote(child.getId(), "Grandchild");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        viewModel.drillDown(child.getId());

        viewModel.navigateBack();

        assertEquals(root.getId(), viewModel.getBaseNoteId());
        assertEquals(1, viewModel.getRootItems().size());
        assertEquals("Child", viewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("navigateBack() restores tab title to root note title")
    void navigateBack_shouldRestoreTabTitle() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        viewModel.drillDown(child.getId());

        viewModel.navigateBack();

        assertEquals("Outline: My Note", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("canNavigateBack() is false initially")
    void canNavigateBack_shouldBeFalseInitially() {
        assertFalse(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("canNavigateBack() is true after drillDown")
    void canNavigateBack_shouldBeTrueAfterDrillDown() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.drillDown(child.getId());

        assertTrue(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("canNavigateBack() is false after navigating back to root")
    void canNavigateBack_shouldBeFalseAfterBack() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        viewModel.drillDown(child.getId());

        viewModel.navigateBack();

        assertFalse(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("navigateBack() does nothing when history is empty")
    void navigateBack_shouldDoNothingWhenHistoryEmpty() {
        Note root = noteService.createNote("Root", "");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.navigateBack();

        assertEquals(root.getId(), viewModel.getBaseNoteId());
    }

    @Test
    @DisplayName("multiple drillDown and navigateBack traverses stack correctly")
    void drillDown_multipleLevels_shouldTraverseStackCorrectly() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        Note grandchild = noteService.createChildNote(child.getId(), "Grandchild");
        noteService.createChildNote(grandchild.getId(), "GreatGrandchild");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.drillDown(child.getId());
        viewModel.drillDown(grandchild.getId());

        assertEquals(grandchild.getId(), viewModel.getBaseNoteId());
        assertEquals("Outline: Grandchild", viewModel.tabTitleProperty().get());
        assertTrue(viewModel.canNavigateBackProperty().get());

        viewModel.navigateBack();

        assertEquals(child.getId(), viewModel.getBaseNoteId());
        assertEquals("Outline: Child", viewModel.tabTitleProperty().get());
        assertTrue(viewModel.canNavigateBackProperty().get());

        viewModel.navigateBack();

        assertEquals(root.getId(), viewModel.getBaseNoteId());
        assertEquals("Outline: My Note", viewModel.tabTitleProperty().get());
        assertFalse(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("tabTitle does not update from rootNoteTitle when drilled down")
    void tabTitle_shouldNotUpdateFromRootNoteTitleWhenDrilledDown() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        viewModel.drillDown(child.getId());

        noteTitle.set("Changed Root Title");

        assertEquals("Outline: Child", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle updates from rootNoteTitle when at root level")
    void tabTitle_shouldUpdateFromRootNoteTitleWhenAtRootLevel() {
        noteTitle.set("New Root Title");

        assertEquals("Outline: New Root Title", viewModel.tabTitleProperty().get());
    }

    // --- createSiblingNote tests ---

    @Test
    @DisplayName("createSiblingNote() creates sibling and reloads tree")
    void createSiblingNote_shouldCreateSiblingAndReload() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        Note child1 = noteService.createChildNote(parent.getId(), "Child1");
        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.createSiblingNote(child1.getId(), "Sibling");

        assertNotNull(item);
        assertEquals("Sibling", item.getTitle());
        // Root items should be reloaded
        assertEquals(2, viewModel.getRootItems().size());
        assertEquals("Child1", viewModel.getRootItems().get(0).getTitle());
        assertEquals("Sibling", viewModel.getRootItems().get(1).getTitle());
    }

    @Test
    @DisplayName("createSiblingNote() with empty title creates note with empty title")
    void createSiblingNote_emptyTitle_shouldWork() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        Note child1 = noteService.createChildNote(parent.getId(), "Child1");
        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.createSiblingNote(child1.getId(), "");

        assertNotNull(item);
        assertEquals("", item.getTitle());
    }

    // --- indentNote tests ---

    @Test
    @DisplayName("indentNote() indents note and reloads tree")
    void indentNote_shouldIndentAndReload() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        noteService.createChildNote(parent.getId(), "Child1");
        Note child2 = noteService.createChildNote(parent.getId(), "Child2");
        viewModel.loadNotes();

        viewModel.indentNote(child2.getId());

        // Only Child1 should remain at root level
        assertEquals(1, viewModel.getRootItems().size());
        assertEquals("Child1", viewModel.getRootItems().get(0).getTitle());
        // Child1 should now have children
        assertTrue(viewModel.getRootItems().get(0).isHasChildren());
    }

    // --- outdentNote tests ---

    @Test
    @DisplayName("outdentNote() outdents note and reloads tree")
    void outdentNote_shouldOutdentAndReload() {
        Note root = noteService.createNote("Root", "");
        Note parent = noteService.createChildNote(root.getId(), "Parent");
        Note child = noteService.createChildNote(parent.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.outdentNote(child.getId());

        // Root should now have Parent and Child
        assertEquals(2, viewModel.getRootItems().size());
        assertEquals("Parent", viewModel.getRootItems().get(0).getTitle());
        assertEquals("Child", viewModel.getRootItems().get(1).getTitle());
    }

    // --- hasChildren tests ---

    @Test
    @DisplayName("hasChildren() returns true when note has children")
    void hasChildren_shouldReturnTrue() {
        Note parent = noteService.createNote("Parent", "");
        Note child = noteService.createChildNote(parent.getId(), "Child");
        noteService.createChildNote(child.getId(), "Grandchild");

        assertTrue(viewModel.hasChildren(child.getId()));
    }

    @Test
    @DisplayName("hasChildren() returns false when note has no children")
    void hasChildren_shouldReturnFalse() {
        Note parent = noteService.createNote("Parent", "");
        Note child = noteService.createChildNote(parent.getId(), "Child");

        assertFalse(viewModel.hasChildren(child.getId()));
    }

    // --- getPreviousNoteId tests ---

    @Test
    @DisplayName("getPreviousNoteId() returns previous sibling id")
    void getPreviousNoteId_shouldReturnPreviousSiblingId() {
        Note parent = noteService.createNote("Parent", "");
        Note child1 = noteService.createChildNote(parent.getId(), "Child1");
        Note child2 = noteService.createChildNote(parent.getId(), "Child2");

        UUID previousId = viewModel.getPreviousNoteId(child2.getId());

        assertEquals(child1.getId(), previousId);
    }

    @Test
    @DisplayName("getPreviousNoteId() returns parent id when first child")
    void getPreviousNoteId_shouldReturnParentIdWhenFirstChild() {
        Note parent = noteService.createNote("Parent", "");
        Note child = noteService.createChildNote(parent.getId(), "Child");

        UUID previousId = viewModel.getPreviousNoteId(child.getId());

        assertEquals(parent.getId(), previousId);
    }

    @Test
    @DisplayName("getPreviousNoteId() returns null when no previous")
    void getPreviousNoteId_shouldReturnNullWhenNoPrevious() {
        Note root = noteService.createNote("Root", "");

        UUID previousId = viewModel.getPreviousNoteId(root.getId());

        assertNull(previousId);
    }

    // --- deleteNote tests ---

    @Test
    @DisplayName("deleteNote() deletes leaf note and reloads tree")
    void deleteNote_shouldDeleteLeafAndReload() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        noteService.createChildNote(parent.getId(), "Child1");
        Note child2 = noteService.createChildNote(parent.getId(), "Child2");
        viewModel.loadNotes();

        boolean result = viewModel.deleteNote(child2.getId());

        assertTrue(result);
        assertEquals(1, viewModel.getRootItems().size());
        assertEquals("Child1", viewModel.getRootItems().get(0).getTitle());
    }

    @Test
    @DisplayName("deleteNote() does not delete note with children")
    void deleteNote_shouldNotDeleteNoteWithChildren() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        Note child = noteService.createChildNote(parent.getId(), "Child");
        noteService.createChildNote(child.getId(), "Grandchild");
        viewModel.loadNotes();

        boolean result = viewModel.deleteNote(child.getId());

        assertFalse(result);
        assertEquals(1, viewModel.getRootItems().size());
    }

    @Test
    @DisplayName("deleteNote() notifies data changed on success")
    void deleteNote_shouldNotifyDataChanged() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        Note child = noteService.createChildNote(parent.getId(), "Child");
        viewModel.loadNotes();
        int versionBefore = appState.getDataVersion();

        viewModel.deleteNote(child.getId());

        assertTrue(appState.getDataVersion() > versionBefore);
    }

    @Test
    @DisplayName("toDisplayItem() resolves badge from $Badge attribute")
    void toDisplayItem_shouldResolveBadge() {
        Note parent = noteService.createNote("Parent", "");
        Note child = noteService.createChildNote(parent.getId(), "Child");
        child.setAttribute("$Badge", new AttributeValue.StringValue("flag"));
        viewModel.setBaseNoteId(parent.getId());

        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.getRootItems().get(0);
        assertEquals("\uD83D\uDEA9", item.getBadge());
    }

    @Test
    @DisplayName("toDisplayItem() returns empty badge when $Badge not set")
    void toDisplayItem_shouldReturnEmptyBadgeWhenNotSet() {
        Note parent = noteService.createNote("Parent", "");
        noteService.createChildNote(parent.getId(), "Child");
        viewModel.setBaseNoteId(parent.getId());

        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.getRootItems().get(0);
        assertEquals("", item.getBadge());
    }
}
