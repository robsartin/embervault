package com.embervault;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.embervault.adapter.in.ui.viewmodel.AppState;
import com.embervault.adapter.in.ui.viewmodel.EventBus;
import com.embervault.adapter.in.ui.viewmodel.SelectedNoteViewModel;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeSchemaRegistry;
import javafx.beans.property.SimpleStringProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests that ViewPaneDeps carries EventBus reference.
 */
class ViewPaneDepsEventBusTest {

    @Test
    @DisplayName("ViewPaneDeps provides EventBus accessor")
    void viewPaneDeps_providesEventBus() {
        InMemoryNoteRepository noteRepo =
                new InMemoryNoteRepository();
        NoteService noteService = new NoteServiceImpl(noteRepo);
        AppState appState = new AppState();
        EventBus eventBus = new EventBus();
        SelectedNoteViewModel selectedNoteVm =
                new SelectedNoteViewModel(
                        noteService, noteService, appState);

        ViewPaneDeps deps = new ViewPaneDeps(
                noteService, null, new AttributeSchemaRegistry(),
                appState, eventBus, selectedNoteVm,
                new SimpleStringProperty("Root"));

        assertNotNull(deps.eventBus());
        assertSame(eventBus, deps.eventBus());
    }
}
