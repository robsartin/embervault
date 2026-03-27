package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MapViewModelTest {

    @Test
    @DisplayName("tabTitle reflects the note title with Map prefix")
    void tabTitle_shouldReflectNoteTitleWithMapPrefix() {
        StringProperty noteTitle = new SimpleStringProperty("My Note");
        MapViewModel viewModel = new MapViewModel(noteTitle);

        assertEquals("Map: My Note", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle updates when note title changes")
    void tabTitle_shouldUpdateWhenNoteTitleChanges() {
        StringProperty noteTitle = new SimpleStringProperty("Original");
        MapViewModel viewModel = new MapViewModel(noteTitle);

        noteTitle.set("Updated");

        assertEquals("Map: Updated", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("Constructor rejects null noteTitle")
    void constructor_shouldRejectNullNoteTitle() {
        assertThrows(NullPointerException.class,
                () -> new MapViewModel(null));
    }
}
