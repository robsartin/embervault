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
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.RenameNoteUseCase;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapViewModelTest {

    private MapViewModel viewModel;
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
        RenameNoteUseCase renameNoteUseCase = noteService;
        viewModel = new MapViewModel(noteTitle, getNoteQuery,
                createNoteUseCase, renameNoteUseCase, appState);
    }

    @Test
    @DisplayName("tabTitle reflects the note title with Map prefix")
    void tabTitle_shouldReflectNoteTitleWithMapPrefix() {
        assertEquals("Map: My Note", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle updates when note title changes")
    void tabTitle_shouldUpdateWhenNoteTitleChanges() {
        noteTitle.set("Updated");

        assertEquals("Map: Updated", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle truncates long note titles to 20 characters with ellipsis")
    void tabTitle_shouldTruncateLongTitles() {
        noteTitle.set("Welcome to EmberVault Application");

        assertEquals("Map: Welcome to EmberVaul\u2026", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle does not truncate titles at exactly 20 characters")
    void tabTitle_shouldNotTruncateExactly20Chars() {
        noteTitle.set("12345678901234567890"); // exactly 20 chars

        assertEquals("Map: 12345678901234567890", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("Constructor rejects null noteTitle")
    void constructor_shouldRejectNullNoteTitle() {
        assertThrows(NullPointerException.class,
                () -> new MapViewModel(null, noteService,
                        noteService, noteService, appState));
    }

    @Test
    @DisplayName("Constructor rejects null getNoteQuery")
    void constructor_shouldRejectNullGetNoteQuery() {
        assertThrows(NullPointerException.class,
                () -> new MapViewModel(noteTitle, null,
                        noteService, noteService, appState));
    }

    @Test
    @DisplayName("Constructor rejects null appState")
    void constructor_shouldRejectNullAppState() {
        assertThrows(NullPointerException.class,
                () -> new MapViewModel(noteTitle, noteService,
                        noteService, noteService, null));
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
        assertEquals("Child1", viewModel.getNoteItems().get(0).getTitle());
        assertEquals("Child2", viewModel.getNoteItems().get(1).getTitle());
    }

    @Test
    @DisplayName("loadNotes() clears items when baseNoteId is null")
    void loadNotes_shouldClearWhenBaseNoteIdNull() {
        viewModel.loadNotes();

        assertTrue(viewModel.getNoteItems().isEmpty());
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
        assertEquals(item, viewModel.getNoteItems().get(0));
    }

    @Test
    @DisplayName("createChildNote() sets position values on display item")
    void createChildNote_shouldSetPosition() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());

        NoteDisplayItem item = viewModel.createChildNote("Child");

        // Positions should be set (scaled from attribute values)
        assertTrue(item.getXpos() >= 0);
        assertTrue(item.getYpos() >= 0);
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
    @DisplayName("updateNotePosition() updates display item position")
    void updateNotePosition_shouldUpdateDisplayItem() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote("Child");

        viewModel.updateNotePosition(item.getId(), 100.0, 200.0);

        NoteDisplayItem updated = viewModel.getNoteItems().get(0);
        assertEquals(100.0, updated.getXpos(), 0.01);
        assertEquals(200.0, updated.getYpos(), 0.01);
    }

    @Test
    @DisplayName("setBaseNoteId and getBaseNoteId work correctly")
    void baseNoteId_shouldBeSettableAndGettable() {
        UUID id = UUID.randomUUID();
        viewModel.setBaseNoteId(id);

        assertEquals(id, viewModel.getBaseNoteId());
    }

    @Test
    @DisplayName("loadNotes() includes color hex from note attributes")
    void loadNotes_shouldIncludeColorHex() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        viewModel.createChildNote("Child");

        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.getNoteItems().get(0);
        assertNotNull(item.getColorHex());
        assertFalse(item.getColorHex().isEmpty());
    }

    @Test
    @DisplayName("renameNote() updates the display item title")
    void renameNote_shouldUpdateDisplayItemTitle() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote("Old Title");

        viewModel.renameNote(item.getId(), "New Title");

        assertEquals("New Title", viewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("renameNote() returns false for blank title")
    void renameNote_shouldReturnFalseForBlankTitle() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote("Title");

        boolean result = viewModel.renameNote(item.getId(), "");

        assertFalse(result);
        assertEquals("Title", viewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("renameNote() returns true on success")
    void renameNote_shouldReturnTrueOnSuccess() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem item = viewModel.createChildNote("Title");

        boolean result = viewModel.renameNote(item.getId(), "New");

        assertTrue(result);
    }

    @Test
    @DisplayName("createSiblingNote() delegates to service and adds item to list")
    void createSiblingNote_shouldDelegateAndAddItem() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem existing = viewModel.createChildNote("First");

        NoteDisplayItem sibling = viewModel.createSiblingNote(
                existing.getId(), "Second");

        assertNotNull(sibling);
        assertEquals("Second", sibling.getTitle());
        assertEquals(2, viewModel.getNoteItems().size());
    }

    @Test
    @DisplayName("createSiblingNote() positions new note near the original sibling")
    void createSiblingNote_shouldPositionNearSibling() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem existing = viewModel.createChildNote("First");
        viewModel.updateNotePosition(existing.getId(), 100.0, 50.0);
        // Re-fetch the updated item after position change
        NoteDisplayItem updated = viewModel.getNoteItems().get(0);

        NoteDisplayItem sibling = viewModel.createSiblingNote(
                updated.getId(), "Second");

        // New note should share the sibling's x position
        assertEquals(100.0, sibling.getXpos(), 0.01);
        // New note should be offset below the sibling
        assertTrue(sibling.getYpos() > updated.getYpos(),
                "Sibling should be positioned below the original note");
    }

    @Test
    @DisplayName("createSiblingNote() notifies data changed via AppState")
    void createSiblingNote_shouldNotifyDataChanged() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem existing = viewModel.createChildNote("First");
        int versionBefore = appState.getDataVersion();

        viewModel.createSiblingNote(existing.getId(), "Second");

        assertTrue(appState.getDataVersion() > versionBefore);
    }

    @Test
    @DisplayName("createSiblingNote() with empty title creates note")
    void createSiblingNote_withEmptyTitle_shouldCreateNote() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());
        NoteDisplayItem existing = viewModel.createChildNote("First");

        NoteDisplayItem sibling = viewModel.createSiblingNote(
                existing.getId(), "");

        assertNotNull(sibling);
        assertEquals(2, viewModel.getNoteItems().size());
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
        assertEquals("Grandchild", viewModel.getNoteItems().get(0).getTitle());
    }

    @Test
    @DisplayName("drillDown() updates tab title to drilled-down note")
    void drillDown_shouldUpdateTabTitle() {
        Note root = noteService.createNote("Root", "");
        Note child = noteService.createChildNote(root.getId(), "Child");
        viewModel.setBaseNoteId(root.getId());
        viewModel.loadNotes();

        viewModel.drillDown(child.getId());

        assertEquals("Map: Child", viewModel.tabTitleProperty().get());
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

        assertEquals("Map: A Very Long Child No\u2026",
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
        assertEquals("Child", viewModel.getNoteItems().get(0).getTitle());
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
    @DisplayName("toDisplayItem() resolves badge from $Badge attribute")
    void toDisplayItem_shouldResolveBadgeFromAttribute() {
        Note parent = noteService.createNote("Parent", "");
        Note child = noteService.createChildNote(parent.getId(), "Child");
        child.setAttribute("$Badge", new AttributeValue.StringValue("star"));
        viewModel.setBaseNoteId(parent.getId());

        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.getNoteItems().get(0);
        assertEquals("\u2B50", item.getBadge());
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

    @Test
    @DisplayName("toDisplayItem() returns empty badge for unknown badge name")
    void toDisplayItem_shouldReturnEmptyBadgeForUnknown() {
        Note parent = noteService.createNote("Parent", "");
        Note child = noteService.createChildNote(parent.getId(), "Child");
        child.setAttribute("$Badge", new AttributeValue.StringValue("nonexistent"));
        viewModel.setBaseNoteId(parent.getId());

        viewModel.loadNotes();

        NoteDisplayItem item = viewModel.getNoteItems().get(0);
        assertEquals("", item.getBadge());
    }

    // --- Zoom support tests ---

    @Test
    @DisplayName("zoomLevel defaults to 1.0")
    void zoomLevel_shouldDefaultTo1() {
        assertEquals(1.0, viewModel.zoomLevelProperty().get(), 0.001);
    }

    @Test
    @DisplayName("currentTier defaults to NORMAL at zoom 1.0")
    void currentTier_shouldDefaultToNormal() {
        assertEquals(ZoomTier.NORMAL, viewModel.currentTierProperty().get());
    }

    @Test
    @DisplayName("setZoomLevel updates zoomLevel property")
    void setZoomLevel_shouldUpdateProperty() {
        viewModel.setZoomLevel(2.0);

        assertEquals(2.0, viewModel.zoomLevelProperty().get(), 0.001);
    }

    @Test
    @DisplayName("setZoomLevel clamps to minimum 0.1")
    void setZoomLevel_shouldClampToMinimum() {
        viewModel.setZoomLevel(0.01);

        assertEquals(0.1, viewModel.zoomLevelProperty().get(), 0.001);
    }

    @Test
    @DisplayName("setZoomLevel clamps to maximum 5.0")
    void setZoomLevel_shouldClampToMaximum() {
        viewModel.setZoomLevel(10.0);

        assertEquals(5.0, viewModel.zoomLevelProperty().get(), 0.001);
    }

    @Test
    @DisplayName("setZoomLevel updates currentTier")
    void setZoomLevel_shouldUpdateCurrentTier() {
        viewModel.setZoomLevel(0.2);
        assertEquals(ZoomTier.OVERVIEW, viewModel.currentTierProperty().get());

        viewModel.setZoomLevel(0.5);
        assertEquals(ZoomTier.TITLES_ONLY, viewModel.currentTierProperty().get());

        viewModel.setZoomLevel(1.0);
        assertEquals(ZoomTier.NORMAL, viewModel.currentTierProperty().get());

        viewModel.setZoomLevel(2.0);
        assertEquals(ZoomTier.DETAILED, viewModel.currentTierProperty().get());
    }

    @Test
    @DisplayName("zoomIn multiplies zoom by 1.25")
    void zoomIn_shouldMultiplyBy1Point25() {
        viewModel.setZoomLevel(1.0);

        viewModel.zoomIn();

        assertEquals(1.25, viewModel.zoomLevelProperty().get(), 0.001);
    }

    @Test
    @DisplayName("zoomIn clamps at 5.0")
    void zoomIn_shouldClampAtMaximum() {
        viewModel.setZoomLevel(4.5);

        viewModel.zoomIn();

        assertEquals(5.0, viewModel.zoomLevelProperty().get(), 0.001);
    }

    @Test
    @DisplayName("zoomOut multiplies zoom by 0.8")
    void zoomOut_shouldMultiplyByPoint8() {
        viewModel.setZoomLevel(1.0);

        viewModel.zoomOut();

        assertEquals(0.8, viewModel.zoomLevelProperty().get(), 0.001);
    }

    @Test
    @DisplayName("zoomOut clamps at 0.1")
    void zoomOut_shouldClampAtMinimum() {
        viewModel.setZoomLevel(0.12);

        viewModel.zoomOut();

        assertEquals(0.1, viewModel.zoomLevelProperty().get(), 0.001);
    }

    @Test
    @DisplayName("fitAll calculates zoom to fit all notes in viewport")
    void fitAll_shouldCalculateZoomToFitNotes() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());

        // Create notes at known positions
        NoteDisplayItem item1 = viewModel.createChildNote("A");
        viewModel.updateNotePosition(item1.getId(), 0, 0);
        NoteDisplayItem item2 = viewModel.createChildNote("B");
        viewModel.updateNotePosition(item2.getId(), 400, 300);

        // Viewport of 800x600 should fit content at roughly 1:1
        viewModel.fitAll(800, 600);

        double zoom = viewModel.zoomLevelProperty().get();
        assertTrue(zoom > 0.1 && zoom <= 5.0,
                "Zoom should be within valid range, was: " + zoom);
    }

    @Test
    @DisplayName("fitAll with empty note list does not change zoom")
    void fitAll_shouldNotChangeZoom_whenNoNotes() {
        viewModel.setZoomLevel(1.5);

        viewModel.fitAll(800, 600);

        assertEquals(1.5, viewModel.zoomLevelProperty().get(), 0.001);
    }

    @Test
    @DisplayName("fitAll clamps result within valid range")
    void fitAll_shouldClampResult() {
        Note parent = noteService.createNote("Parent", "");
        viewModel.setBaseNoteId(parent.getId());

        // Single note at origin - huge viewport would want very large zoom
        viewModel.createChildNote("Solo");

        viewModel.fitAll(10000, 10000);

        assertTrue(viewModel.zoomLevelProperty().get() <= 5.0,
                "fitAll should clamp zoom to maximum 5.0");
    }
}
