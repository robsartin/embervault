package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HyperbolicViewModelTest {

    private HyperbolicViewModel viewModel;
    private NoteService noteService;
    private LinkService linkService;
    private AppState appState;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository noteRepo = new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(noteRepo);
        InMemoryLinkRepository linkRepo = new InMemoryLinkRepository();
        linkService = new LinkServiceImpl(linkRepo);
        appState = new AppState();
        viewModel = new HyperbolicViewModel(noteService, linkService, appState);
    }

    @Test
    @DisplayName("constructor rejects null noteService")
    void constructor_shouldRejectNullNoteService() {
        assertThrows(NullPointerException.class,
                () -> new HyperbolicViewModel(null, linkService, appState));
    }

    @Test
    @DisplayName("constructor rejects null linkService")
    void constructor_shouldRejectNullLinkService() {
        assertThrows(NullPointerException.class,
                () -> new HyperbolicViewModel(noteService, null, appState));
    }

    @Test
    @DisplayName("initial tab title is 'Hyperbolic'")
    void tabTitle_shouldBeHyperbolicInitially() {
        assertEquals("Hyperbolic", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("setFocusNote updates tab title")
    void setFocusNote_shouldUpdateTabTitle() {
        Note note = noteService.createNote("My Focus", "");

        viewModel.setFocusNote(note.getId());

        assertEquals("Hyperbolic: My Focus",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("setFocusNote truncates long titles")
    void setFocusNote_shouldTruncateLongTitles() {
        Note note = noteService.createNote(
                "A Very Long Note Title That Should Be Truncated", "");

        viewModel.setFocusNote(note.getId());

        assertEquals("Hyperbolic: A Very Long Note Tit\u2026",
                viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("setFocusNote updates focusNoteId property")
    void setFocusNote_shouldUpdateFocusNoteIdProperty() {
        Note note = noteService.createNote("Focus", "");

        viewModel.setFocusNote(note.getId());

        assertEquals(note.getId(), viewModel.getFocusNoteId());
        assertEquals(note.getId(), viewModel.focusNoteIdProperty().get());
    }

    @Test
    @DisplayName("setFocusNote with no links produces single node")
    void setFocusNote_noLinks_shouldProduceSingleNode() {
        Note note = noteService.createNote("Solo", "");

        viewModel.setFocusNote(note.getId());

        assertEquals(1, viewModel.getNodes().size());
        assertEquals(note.getId(), viewModel.getNodes().get(0).noteId());
        assertTrue(viewModel.getEdges().isEmpty());
    }

    @Test
    @DisplayName("setFocusNote with links produces nodes and edges")
    void setFocusNote_withLinks_shouldProduceNodesAndEdges() {
        Note n1 = noteService.createNote("N1", "");
        Note n2 = noteService.createNote("N2", "");
        Note n3 = noteService.createNote("N3", "");
        linkService.createLink(n1.getId(), n2.getId());
        linkService.createLink(n1.getId(), n3.getId());

        viewModel.setFocusNote(n1.getId());

        assertEquals(3, viewModel.getNodes().size());
        assertEquals(2, viewModel.getEdges().size());
    }

    @Test
    @DisplayName("web and prototype links are ignored")
    void setFocusNote_shouldIgnoreWebAndPrototypeLinks() {
        Note n1 = noteService.createNote("N1", "");
        Note n2 = noteService.createNote("N2", "");
        Note n3 = noteService.createNote("N3", "");
        linkService.createLink(n1.getId(), n2.getId(), "web");
        linkService.createLink(n1.getId(), n3.getId(), "prototype");

        viewModel.setFocusNote(n1.getId());

        assertEquals(1, viewModel.getNodes().size());
        assertTrue(viewModel.getEdges().isEmpty());
    }

    @Test
    @DisplayName("selectNote sets selectedNoteId")
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
    @DisplayName("canNavigateBack is false initially")
    void canNavigateBack_shouldBeFalseInitially() {
        assertFalse(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("drillDown pushes history and enables back navigation")
    void drillDown_shouldPushHistoryAndEnableBack() {
        Note n1 = noteService.createNote("N1", "");
        Note n2 = noteService.createNote("N2", "");
        viewModel.setFocusNote(n1.getId());

        viewModel.drillDown(n2.getId());

        assertEquals(n2.getId(), viewModel.getFocusNoteId());
        assertTrue(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("navigateBack restores previous focus")
    void navigateBack_shouldRestorePreviousFocus() {
        Note n1 = noteService.createNote("N1", "");
        Note n2 = noteService.createNote("N2", "");
        viewModel.setFocusNote(n1.getId());
        viewModel.drillDown(n2.getId());

        viewModel.navigateBack();

        assertEquals(n1.getId(), viewModel.getFocusNoteId());
        assertFalse(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("navigateBack with empty history is a no-op")
    void navigateBack_emptyHistory_shouldBeNoOp() {
        Note n1 = noteService.createNote("N1", "");
        viewModel.setFocusNote(n1.getId());

        viewModel.navigateBack();

        assertEquals(n1.getId(), viewModel.getFocusNoteId());
    }

    @Test
    @DisplayName("createLink adds link and updates layout")
    void createLink_shouldAddLinkAndUpdateLayout() {
        Note n1 = noteService.createNote("N1", "");
        Note n2 = noteService.createNote("N2", "");
        viewModel.setFocusNote(n1.getId());
        assertEquals(1, viewModel.getNodes().size());

        viewModel.createLink(n1.getId(), n2.getId());

        assertEquals(2, viewModel.getNodes().size());
        assertEquals(1, viewModel.getEdges().size());
    }

    @Test
    @DisplayName("createLink notifies data changed via AppState")
    void createLink_shouldNotifyDataChanged() {
        Note n1 = noteService.createNote("N1", "");
        Note n2 = noteService.createNote("N2", "");
        viewModel.setFocusNote(n1.getId());
        int versionBefore = appState.getDataVersion();

        viewModel.createLink(n1.getId(), n2.getId());

        assertTrue(appState.getDataVersion() > versionBefore);
    }

    @Test
    @DisplayName("setFocusNote rejects null")
    void setFocusNote_shouldRejectNull() {
        assertThrows(NullPointerException.class,
                () -> viewModel.setFocusNote(null));
    }

    @Test
    @DisplayName("multi-level navigation history works correctly")
    void multiLevel_navigationHistory_shouldWorkCorrectly() {
        Note n1 = noteService.createNote("N1", "");
        Note n2 = noteService.createNote("N2", "");
        Note n3 = noteService.createNote("N3", "");
        viewModel.setFocusNote(n1.getId());

        viewModel.drillDown(n2.getId());
        viewModel.drillDown(n3.getId());

        assertTrue(viewModel.canNavigateBackProperty().get());

        viewModel.navigateBack();
        assertEquals(n2.getId(), viewModel.getFocusNoteId());
        assertTrue(viewModel.canNavigateBackProperty().get());

        viewModel.navigateBack();
        assertEquals(n1.getId(), viewModel.getFocusNoteId());
        assertFalse(viewModel.canNavigateBackProperty().get());
    }

    @Test
    @DisplayName("AppState always notified on createLink")
    void appState_shouldAlwaysBeNotified() {
        Note n1 = noteService.createNote("N1", "");
        Note n2 = noteService.createNote("N2", "");
        viewModel.setFocusNote(n1.getId());
        int versionBefore = appState.getDataVersion();

        viewModel.createLink(n1.getId(), n2.getId());

        assertTrue(appState.getDataVersion() > versionBefore);
    }

    @Test
    @DisplayName("transitive links are followed during BFS")
    void transitiveLinks_shouldBeFollowed() {
        Note n1 = noteService.createNote("N1", "");
        Note n2 = noteService.createNote("N2", "");
        Note n3 = noteService.createNote("N3", "");
        linkService.createLink(n1.getId(), n2.getId());
        linkService.createLink(n2.getId(), n3.getId());

        viewModel.setFocusNote(n1.getId());

        assertEquals(3, viewModel.getNodes().size());
        assertEquals(2, viewModel.getEdges().size());
    }

    @Test
    @DisplayName("getNodes returns HyperbolicNode with noteId")
    void getNodes_shouldReturnNodesWithNoteId() {
        Note n1 = noteService.createNote("N1", "");
        viewModel.setFocusNote(n1.getId());

        assertNotNull(viewModel.getNodes());
        assertFalse(viewModel.getNodes().isEmpty());
        assertEquals(n1.getId(), viewModel.getNodes().get(0).noteId());
    }

    @Test
    @DisplayName("getNoteBadge() returns badge symbol when $Badge is set")
    void getNoteBadge_shouldReturnSymbolWhenSet() {
        Note note = noteService.createNote("N1", "");
        note.setAttribute("$Badge", new AttributeValue.StringValue("star"));

        String badge = viewModel.getNoteBadge(note.getId());

        assertEquals("\u2B50", badge);
    }

    @Test
    @DisplayName("getNoteBadge() returns empty when $Badge not set")
    void getNoteBadge_shouldReturnEmptyWhenNotSet() {
        Note note = noteService.createNote("N1", "");

        String badge = viewModel.getNoteBadge(note.getId());

        assertEquals("", badge);
    }

    @Test
    @DisplayName("getNoteBadge() returns empty for unknown badge name")
    void getNoteBadge_shouldReturnEmptyForUnknownName() {
        Note note = noteService.createNote("N1", "");
        note.setAttribute("$Badge", new AttributeValue.StringValue("nonexistent"));

        String badge = viewModel.getNoteBadge(note.getId());

        assertEquals("", badge);
    }

    @Test
    @DisplayName("getNoteTitle() returns note title")
    void getNoteTitle_shouldReturnTitle() {
        Note note = noteService.createNote("My Title", "");

        String title = viewModel.getNoteTitle(note.getId());

        assertEquals("My Title", title);
    }

    @Test
    @DisplayName("getNoteTitle() returns empty for nonexistent note")
    void getNoteTitle_shouldReturnEmptyForNonexistent() {
        String title = viewModel.getNoteTitle(UUID.randomUUID());

        assertEquals("", title);
    }
}
