package com.embervault;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.AppState;
import com.embervault.adapter.in.ui.viewmodel.EventBus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that each {@link ViewFactory} produces a valid
 * {@link ViewCreationResult}.
 */
class ViewFactoryCreateTest {

    private ViewPaneDeps deps;
    private UUID baseNoteId;
    private ViewFactoryRegistry registry;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository noteRepo =
                new InMemoryNoteRepository();
        NoteService noteService = new NoteServiceImpl(noteRepo);
        LinkService linkService = new LinkServiceImpl(
                new InMemoryLinkRepository());
        AttributeSchemaRegistry schemaRegistry =
                new AttributeSchemaRegistry();
        AppState appState = new AppState();
        SelectedNoteViewModel selectedNoteVm =
                new SelectedNoteViewModel(
                        noteService, noteService, noteService,
                        appState, new EventBus());
        StringProperty rootNoteTitle =
                new SimpleStringProperty("Root");

        Note root = noteService.createNote("Root", "");
        baseNoteId = root.getId();

        deps = new ViewPaneDeps(
                noteService, linkService, schemaRegistry,
                appState, new EventBus(), selectedNoteVm,
                rootNoteTitle);
        registry = new ViewFactoryRegistry();
    }

    @Test
    @DisplayName("MapViewFactory returns result with all fields non-null")
    void mapFactory_shouldReturnCompleteResult() {
        ViewCreationResult result =
                registry.getFactory(ViewType.MAP)
                        .create(deps, baseNoteId, name -> { });
        assertNotNull(result.tabTitle());
        assertNotNull(result.viewRefresh());
        assertNotNull(result.initialLoad());
        assertNotNull(result.controllerInitializer());
        assertNotNull(result.selectedNoteIdProperty(),
                "Map supports selection");
    }

    @Test
    @DisplayName("OutlineViewFactory returns result with all fields non-null")
    void outlineFactory_shouldReturnCompleteResult() {
        ViewCreationResult result =
                registry.getFactory(ViewType.OUTLINE)
                        .create(deps, baseNoteId, name -> { });
        assertNotNull(result.tabTitle());
        assertNotNull(result.viewRefresh());
        assertNotNull(result.initialLoad());
        assertNotNull(result.controllerInitializer());
        assertNotNull(result.selectedNoteIdProperty(),
                "Outline supports selection");
    }

    @Test
    @DisplayName("TreemapViewFactory returns result with all fields non-null")
    void treemapFactory_shouldReturnCompleteResult() {
        ViewCreationResult result =
                registry.getFactory(ViewType.TREEMAP)
                        .create(deps, baseNoteId, name -> { });
        assertNotNull(result.tabTitle());
        assertNotNull(result.viewRefresh());
        assertNotNull(result.initialLoad());
        assertNotNull(result.controllerInitializer());
        assertNotNull(result.selectedNoteIdProperty(),
                "Treemap supports selection");
    }

    @Test
    @DisplayName("HyperbolicViewFactory returns result with all fields non-null")
    void hyperbolicFactory_shouldReturnCompleteResult() {
        ViewCreationResult result =
                registry.getFactory(ViewType.HYPERBOLIC)
                        .create(deps, baseNoteId, name -> { });
        assertNotNull(result.tabTitle());
        assertNotNull(result.viewRefresh());
        assertNotNull(result.initialLoad());
        assertNotNull(result.controllerInitializer());
        assertNotNull(result.selectedNoteIdProperty(),
                "Hyperbolic supports selection");
    }

    @Test
    @DisplayName("AttributeBrowserViewFactory returns null selectedNoteIdProperty")
    void browserFactory_shouldReturnNullSelectedNoteIdProperty() {
        ViewCreationResult result =
                registry.getFactory(ViewType.BROWSER)
                        .create(deps, baseNoteId, name -> { });
        assertNotNull(result.tabTitle());
        assertNotNull(result.viewRefresh());
        assertNotNull(result.initialLoad());
        assertNotNull(result.controllerInitializer());
        assertNull(result.selectedNoteIdProperty(),
                "Browser does not support selection");
    }
}
