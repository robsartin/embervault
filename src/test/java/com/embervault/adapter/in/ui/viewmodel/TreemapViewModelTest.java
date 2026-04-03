package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TreemapViewModelTest {

    private TreemapViewModel viewModel;
    private NoteService noteService;
    private InMemoryNoteRepository repository;
    private StringProperty noteTitle;
    private AppState appState;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(repository);
        noteTitle = new SimpleStringProperty("My Note");
        appState = new AppState();
        GetNoteQuery getNoteQuery = noteService;
        CreateNoteUseCase createNoteUseCase = noteService;
        viewModel = new TreemapViewModel(
                noteTitle, getNoteQuery, createNoteUseCase, appState);
    }

    @Test
    @DisplayName("tabTitle reflects the note title with Treemap prefix")
    void tabTitle_shouldReflectNoteTitleWithTreemapPrefix() {
        assertEquals("Treemap: My Note",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle updates when note title changes")
    void tabTitle_shouldUpdateWhenNoteTitleChanges() {
        noteTitle.set("Updated");

        assertEquals("Treemap: Updated",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle truncates long note titles to 20 chars with ellipsis")
    void tabTitle_shouldTruncateLongTitles() {
        noteTitle.set("Welcome to EmberVault Application");

        assertEquals("Treemap: Welcome to EmberVaul\u2026",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle does not truncate titles at exactly 20 characters")
    void tabTitle_shouldNotTruncateExactly20Chars() {
        noteTitle.set("12345678901234567890");

        assertEquals("Treemap: 12345678901234567890",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("Constructor rejects null noteTitle")
    void constructor_shouldRejectNullNoteTitle() {
        assertThrows(NullPointerException.class,
                () -> new TreemapViewModel(
                        null, noteService, noteService, appState));
    }

    @Test
    @DisplayName("Constructor rejects null getNoteQuery")
    void constructor_shouldRejectNullGetNoteQuery() {
        assertThrows(NullPointerException.class,
                () -> new TreemapViewModel(
                        noteTitle, null, noteService, appState));
    }

    @Test
    @DisplayName("Constructor rejects null createNoteUseCase")
    void constructor_shouldRejectNullCreateNoteUseCase() {
        assertThrows(NullPointerException.class,
                () -> new TreemapViewModel(
                        noteTitle, noteService, null, appState));
    }

    @Test
    @DisplayName("loadNotes() populates items from base note children")
    void loadNotes_shouldPopulateFromBaseNoteChildren() {
        Note parent = noteService.createNote("Parent", "");
        noteService.createChildNote(parent.getId(), "Child1");
        noteService.createChildNote(parent.getId(), "Child2");
        viewModel.setBaseNoteId(parent.getId());

        viewModel.loadNotes();

        assertEquals(2, viewModel.getNoteItems().size());
        assertEquals("Child1",
                viewModel.getNoteItems().get(0).getTitle());
        assertEquals("Child2",
                viewModel.getNoteItems().get(1).getTitle());
    }

    @Test
    @DisplayName("loadNotes() clears items when baseNoteId is null")
    void loadNotes_shouldClearWhenBaseNoteIdNull() {
        viewModel.loadNotes();

        assertTrue(viewModel.getNoteItems().isEmpty());
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
    @DisplayName("setBaseNoteId and getBaseNoteId work correctly")
    void baseNoteId_shouldBeSettableAndGettable() {
        UUID id = UUID.randomUUID();
        viewModel.setBaseNoteId(id);

        assertEquals(id, viewModel.getBaseNoteId());
    }

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
        assertEquals(1, viewModel.getNoteItems().size());
        assertEquals("Grandchild",
                viewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("drillDown() updates tab title to drilled-down note")
    void drillDown_shouldUpdateTabTitle() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.drillDown(child.getId());

        assertEquals("Treemap: Child",
                viewModel.tabTitleProperty().get());
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

        assertEquals("Treemap: A Very Long Child No\u2026",
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
        assertEquals(1, viewModel.getNoteItems().size());
        assertEquals("Child",
                viewModel.getNoteItems().get(0).getTitle());
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
    @DisplayName("canNavigateBack() is false after navigating back")
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
    @DisplayName("navigateBack() on empty history does nothing")
    void navigateBack_onEmptyHistory_shouldDoNothing() {
        Note root = noteService.createNote("Root", "");
        viewModel.setBaseNoteId(root.getId());

        viewModel.navigateBack();

        assertEquals(root.getId(), viewModel.getBaseNoteId());
    }

    @Test
    @DisplayName("navigateBack() restores root tab title")
    void navigateBack_shouldRestoreRootTabTitle() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        viewModel.drillDown(child.getId());

        viewModel.navigateBack();

        assertEquals("Treemap: My Note",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("drillDown notifies data changed via AppState")
    void onDataChanged_shouldBeInvokedOnDrillDown() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        int versionBefore = appState.getDataVersion();

        viewModel.drillDown(child.getId());

        assertTrue(appState.getDataVersion() > versionBefore);
    }

    @Test
    @DisplayName("navigateBack notifies data changed via AppState")
    void onDataChanged_shouldBeInvokedOnNavigateBack() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        viewModel.drillDown(child.getId());
        int versionBefore = appState.getDataVersion();

        viewModel.navigateBack();

        assertTrue(appState.getDataVersion() > versionBefore);
    }

    @Test
    @DisplayName("createChildNote notifies data changed via AppState")
    void onDataChanged_shouldBeInvokedOnCreateChildNote() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        int versionBefore = appState.getDataVersion();

        viewModel.createChildNote("New");

        assertTrue(appState.getDataVersion() > versionBefore);
    }

    @Test
    @DisplayName("createChildNote() adds item to list and returns it")
    void createChildNote_shouldAddAndReturn() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());

        NoteDisplayItem item = viewModel.createChildNote("New Child");

        assertNotNull(item);
        assertEquals("New Child", item.getTitle());
        assertEquals(1, viewModel.getNoteItems().size());
    }

    @Test
    @DisplayName("getTreemapRects() returns positioned rectangles")
    void getTreemapRects_shouldReturnPositionedRects() {
        Note parent = noteService.createNote("Parent", "");
        noteService.createChildNote(parent.getId(), "Child1");
        noteService.createChildNote(parent.getId(), "Child2");
        viewModel.setBaseNoteId(parent.getId());
        viewModel.loadNotes();

        List<TreemapRect> rects = viewModel.getTreemapRects(800, 600);

        assertEquals(2, rects.size());
        double totalArea = rects.stream()
                .mapToDouble(r -> r.width() * r.height())
                .sum();
        assertEquals(800 * 600, totalArea, 1.0);
    }

    @Test
    @DisplayName("getTreemapRects() returns empty list when no notes")
    void getTreemapRects_shouldReturnEmptyWhenNoNotes() {
        viewModel.loadNotes();

        List<TreemapRect> rects = viewModel.getTreemapRects(800, 600);

        assertTrue(rects.isEmpty());
    }

    @Test
    @DisplayName("loadNotes() includes color hex from note attributes")
    void loadNotes_shouldIncludeColorHex() {
        Note parent = noteService.createNote("Parent", "");
        noteService.createChildNote(parent.getId(), "Child");
        viewModel.setBaseNoteId(parent.getId());

        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.getNoteItems().get(0);
        assertNotNull(item.getColorHex());
        assertFalse(item.getColorHex().isEmpty());
    }

    @Test
    @DisplayName("tabTitle does not update from root when drilled down")
    void tabTitle_shouldNotUpdateFromRootWhenDrilledDown() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();
        viewModel.drillDown(child.getId());

        noteTitle.set("Changed Root");

        assertEquals("Treemap: Child",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("Multi-level drillDown then navigateBack restores intermediate")
    void multiLevelDrillDown_navigateBack_shouldRestore() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        Note gc = noteService.createChildNote(child.getId(), "GC");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.drillDown(child.getId());
        viewModel.drillDown(gc.getId());

        assertTrue(viewModel.canNavigateBackProperty().get());

        viewModel.navigateBack();

        assertEquals("Treemap: Child",
                viewModel.tabTitleProperty().get());
        assertTrue(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("createChildNote() requires baseNoteId to be set")
    void createChildNote_shouldRequireBaseNoteId() {
        assertThrows(NullPointerException.class,
                () -> viewModel.createChildNote("Test"));
    }

    @Test
    @DisplayName("AppState is shared and always notified on mutation")
    void appState_shouldAlwaysBeNotified() {
        Note root = noteService.createNote("Root", "");
        viewModel.setBaseNoteId(root.getId());
        int versionBefore = appState.getDataVersion();

        viewModel.createChildNote("Test");

        assertTrue(appState.getDataVersion() > versionBefore);
    }

    @Test
    @DisplayName("toDisplayItem() resolves badge from $Badge attribute")
    void toDisplayItem_shouldResolveBadge() {
        Note parent = noteService.createNote("Parent", "");
        Note child = noteService.createChildNote(parent.getId(), "Child");
        child.setAttribute("$Badge", new AttributeValue.StringValue("check"));
        viewModel.setBaseNoteId(parent.getId());

        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.getNoteItems().get(0);
        assertEquals("\u2705", item.getBadge());
    }

    @Test
    @DisplayName("toDisplayItem() returns empty badge when $Badge not set")
    void toDisplayItem_shouldReturnEmptyBadgeWhenNotSet() {
        Note parent = noteService.createNote("Parent", "");
        noteService.createChildNote(parent.getId(), "Child");
        viewModel.setBaseNoteId(parent.getId());

        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.getNoteItems().get(0);
        assertEquals("", item.getBadge());
    }
}
