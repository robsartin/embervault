package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.StampServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.ColorScheme;
import com.embervault.domain.Project;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link AppContext} record.
 *
 * <p>Verifies that all record fields are accessible and hold
 * the values passed at construction time. These tests run
 * headless without requiring a JavaFX stage.</p>
 */
class AppContextTest {

    private MapViewModel mapVm;
    private HyperbolicViewModel hyperbolicVm;
    private SearchViewModel searchVm;
    private Project project;
    private StampService stampService;
    private ObjectProperty<UUID> selectedNoteId;
    private AtomicInteger refreshCount;
    private Runnable refreshAll;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository noteRepo =
                new InMemoryNoteRepository();
        NoteService noteService =
                new NoteServiceImpl(noteRepo);
        LinkService linkService =
                new LinkServiceImpl(
                        new InMemoryLinkRepository());
        stampService = new StampServiceImpl(
                new InMemoryStampRepository(), noteRepo);

        mapVm = new MapViewModel(
                new SimpleStringProperty("Root"),
                noteService);
        hyperbolicVm = new HyperbolicViewModel(
                noteService, linkService);
        searchVm = new SearchViewModel(noteService);
        project = Project.createEmpty();
        selectedNoteId = new SimpleObjectProperty<>();
        refreshCount = new AtomicInteger(0);
        refreshAll = refreshCount::incrementAndGet;
    }

    private AppContext buildContext() {
        return new AppContext(
                mapVm, hyperbolicVm, searchVm,
                null, null, null,
                project, stampService, selectedNoteId,
                refreshAll, null, cs -> { });
    }

    @Test
    @DisplayName("record fields return the values passed "
            + "at construction")
    void fields_shouldReturnConstructedValues() {
        AppContext ctx = buildContext();

        assertSame(mapVm, ctx.mapViewModel());
        assertSame(hyperbolicVm, ctx.hyperbolicViewModel());
        assertSame(searchVm, ctx.searchViewModel());
        assertSame(project, ctx.project());
        assertSame(stampService, ctx.stampService());
        assertSame(selectedNoteId, ctx.selectedNoteId());
        assertSame(refreshAll, ctx.refreshAll());
    }

    @Test
    @DisplayName("nullable fields accept null without error")
    void nullableFields_shouldAcceptNull() {
        AppContext ctx = buildContext();

        assertNull(ctx.mainSplitPane());
        assertNull(ctx.browserEditorPane());
        assertNull(ctx.hyperbolicContainer());
        assertNull(ctx.ownerStage());
    }

    @Test
    @DisplayName("refreshAll callback is invocable from "
            + "the record")
    void refreshAll_shouldBeInvocable() {
        AppContext ctx = buildContext();

        ctx.refreshAll().run();
        ctx.refreshAll().run();

        assertEquals(2, refreshCount.get());
    }

    @Test
    @DisplayName("colorSchemeApplier callback is invocable")
    void colorSchemeApplier_shouldBeInvocable() {
        AtomicInteger applyCount = new AtomicInteger(0);
        AppContext ctx = new AppContext(
                mapVm, hyperbolicVm, searchVm,
                null, null, null,
                project, stampService, selectedNoteId,
                refreshAll, null,
                cs -> applyCount.incrementAndGet());

        ctx.colorSchemeApplier().accept(
                new ColorScheme("Test", "#000", "#111",
                        "#222", "#333", "#444", "#555",
                        "#666", "#777"));

        assertEquals(1, applyCount.get());
    }

    @Test
    @DisplayName("project field exposes root note")
    void project_shouldExposeRootNote() {
        AppContext ctx = buildContext();

        assertNotNull(ctx.project().getRootNote());
        assertNotNull(ctx.project().getRootNote().getId());
    }
}
