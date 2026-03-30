package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.SearchViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.StampServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.Project;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link AppContext} record.
 */
class AppContextTest {

    private OutlineViewModel outlineVm;
    private SearchViewModel searchVm;
    private Project project;
    private StampService stampService;
    private ObjectProperty<UUID> selectedNoteId;
    private AtomicInteger refreshCount;
    private Runnable refreshAll;

    @BeforeEach
    void setUp() {
        InMemoryNoteRepository noteRepo = new InMemoryNoteRepository();
        NoteService noteService = new NoteServiceImpl(noteRepo);
        stampService = new StampServiceImpl(
                new InMemoryStampRepository(), noteRepo);
        outlineVm = new OutlineViewModel(
                new SimpleStringProperty("Root"), noteService);
        searchVm = new SearchViewModel(noteService);
        project = Project.createEmpty();
        selectedNoteId = new SimpleObjectProperty<>();
        refreshCount = new AtomicInteger(0);
        refreshAll = refreshCount::incrementAndGet;
    }

    private AppContext buildContext() {
        return new AppContext(
                outlineVm, searchVm,
                project, stampService, selectedNoteId,
                refreshAll, null);
    }

    @Test
    @DisplayName("record fields return constructed values")
    void fields_shouldReturnConstructedValues() {
        AppContext ctx = buildContext();
        assertSame(outlineVm, ctx.outlineViewModel());
        assertSame(searchVm, ctx.searchViewModel());
        assertSame(project, ctx.project());
        assertSame(stampService, ctx.stampService());
        assertSame(selectedNoteId, ctx.selectedNoteId());
        assertSame(refreshAll, ctx.refreshAll());
    }

    @Test
    @DisplayName("ownerStage accepts null")
    void ownerStage_shouldAcceptNull() {
        AppContext ctx = buildContext();
        assertNull(ctx.ownerStage());
    }

    @Test
    @DisplayName("refreshAll callback is invocable")
    void refreshAll_shouldBeInvocable() {
        AppContext ctx = buildContext();
        ctx.refreshAll().run();
        ctx.refreshAll().run();
        assertEquals(2, refreshCount.get());
    }

    @Test
    @DisplayName("project exposes root note")
    void project_shouldExposeRootNote() {
        AppContext ctx = buildContext();
        assertNotNull(ctx.project().getRootNote());
    }
}
