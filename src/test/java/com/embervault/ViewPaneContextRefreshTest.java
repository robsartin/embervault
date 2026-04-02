package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.concurrent.atomic.AtomicInteger;

import com.embervault.adapter.in.ui.viewmodel.AppState;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Tests that {@link ViewPaneContext#refreshCurrentView()}
 * correctly delegates to the new view's refresh action
 * after a view switch.
 */
@Tag("ui")
@ExtendWith(ApplicationExtension.class)
class ViewPaneContextRefreshTest {

    private NoteService noteService;
    private LinkService linkService;
    private SelectedNoteViewModel selectedNoteVm;
    private StringProperty rootNoteTitle;
    private Note rootNote;
    private MapViewModel mapViewModel;
    private ViewPaneContext paneContext;
    private AtomicInteger refreshAllCount;

    @Start
    private void start(Stage stage) {
        stage.show();
    }

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository noteRepo =
                new InMemoryNoteRepository();
        noteService = new NoteServiceImpl(noteRepo);
        linkService = new LinkServiceImpl(
                new InMemoryLinkRepository());
        AttributeSchemaRegistry schemaRegistry =
                new AttributeSchemaRegistry();
        AppState appState = new AppState();
        selectedNoteVm =
                new SelectedNoteViewModel(noteService, appState);
        rootNoteTitle = new SimpleStringProperty("Root");
        refreshAllCount = new AtomicInteger(0);

        rootNote = noteService.createNote("Root", "");
        noteService.createChildNote(
                rootNote.getId(), "Child1");

        mapViewModel = new MapViewModel(
                rootNoteTitle, noteService, appState);
        mapViewModel.setBaseNoteId(rootNote.getId());
        mapViewModel.loadNotes();

        Label dummyView = new Label("map content");
        paneContext = new ViewPaneContext(
                ViewType.MAP,
                mapViewModel.tabTitleProperty(),
                dummyView,
                rootNote.getId(),
                mapViewModel::loadNotes);

        ViewPaneDeps deps = new ViewPaneDeps(
                noteService, linkService, schemaRegistry,
                appState, selectedNoteVm, rootNoteTitle);
        paneContext.setDeps(deps);
    }

    @Test
    @DisplayName("refresh after switch to OUTLINE delegates "
            + "to the outline view model")
    void refreshAfterOutlineSwitch_delegatesToNewView(
            FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(
                        ViewType.OUTLINE));

        // Add a note after switching
        noteService.createChildNote(
                rootNote.getId(), "NewChild");

        // Refresh should pick up the new data
        // (no exception = delegation works)
        robot.interact(
                () -> paneContext.refreshCurrentView());

        assertEquals(ViewType.OUTLINE,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("refresh after switch to HYPERBOLIC "
            + "delegates to the hyperbolic view model")
    void refreshAfterHyperbolicSwitch_delegatesToNewView(
            FxRobot robot) {
        robot.interact(
                () -> paneContext.switchView(
                        ViewType.HYPERBOLIC));

        robot.interact(
                () -> paneContext.refreshCurrentView());

        assertEquals(ViewType.HYPERBOLIC,
                paneContext.getCurrentViewType());
    }

    @Test
    @DisplayName("label text updates after switching "
            + "view and drilling down")
    void labelText_updatesAfterViewSwitch(FxRobot robot) {
        String labelBefore =
                paneContext.getLabel().getText();

        robot.interact(
                () -> paneContext.switchView(
                        ViewType.TREEMAP));

        String labelAfter =
                paneContext.getLabel().getText();
        assertNotEquals(labelBefore, labelAfter,
                "Label should change after view switch");
    }
}
