package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WindowBuilderTest {

    private WindowSetupContext setupCtx;
    private SharedServices services;
    private WindowManager windowManager;

    @BeforeEach
    void setUp() {
        services = SharedServices.create();
        windowManager = new WindowManager();
        setupCtx = new WindowSetupContext(services, windowManager);
    }

    @Test
    void shouldCreateAppState() {
        WindowSetupResult result = WindowBuilder.build(setupCtx);

        assertNotNull(result.appState());
    }

    @Test
    void shouldCreateEventBus() {
        WindowSetupResult result = WindowBuilder.build(setupCtx);

        assertNotNull(result.eventBus());
    }

    @Test
    void shouldCreateSelectedNoteViewModel() {
        WindowSetupResult result = WindowBuilder.build(setupCtx);

        assertNotNull(result.selectedNoteVm());
    }

    @Test
    void shouldCreateViewPaneDeps() {
        WindowSetupResult result = WindowBuilder.build(setupCtx);

        assertNotNull(result.paneDeps());
        assertSame(services.noteService(),
                result.paneDeps().noteService());
        assertSame(services.linkService(),
                result.paneDeps().linkService());
        assertSame(services.schemaRegistry(),
                result.paneDeps().schemaRegistry());
        assertSame(result.appState(),
                result.paneDeps().appState());
        assertSame(result.eventBus(),
                result.paneDeps().eventBus());
        assertSame(result.selectedNoteVm(),
                result.paneDeps().selectedNoteVm());
    }

    @Test
    void shouldCreateRootNoteTitleFromProject() {
        WindowSetupResult result = WindowBuilder.build(setupCtx);

        assertEquals(services.project().getRootNote().getTitle(),
                result.rootNoteTitle().get());
    }

    @Test
    void shouldWireDataVersionToWindowManager() {
        WindowSetupResult result = WindowBuilder.build(setupCtx);
        boolean[] refreshed = {false};
        windowManager.addRefreshListener(() -> refreshed[0] = true);

        result.appState().notifyDataChanged();

        assertEquals(true, refreshed[0]);
    }

    @Test
    void wireSelectionShouldForwardNoteId() {
        WindowSetupResult result = WindowBuilder.build(setupCtx);
        ObjectProperty<UUID> source = new SimpleObjectProperty<>();
        SelectedNoteViewModel target = result.selectedNoteVm();

        UUID childId = services.noteService().createChildNote(
                services.project().getRootNote().getId(),
                "Test Note").getId();
        WindowBuilder.wireSelection(source, target);
        source.set(childId);

        assertEquals(childId,
                target.selectedNoteIdProperty().get());
    }
}
