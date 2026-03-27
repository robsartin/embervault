package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OutlineViewModelTest {

    @Test
    @DisplayName("tabTitle reflects the note title with Outline prefix")
    void tabTitle_shouldReflectNoteTitleWithOutlinePrefix() {
        StringProperty noteTitle = new SimpleStringProperty("My Note");
        OutlineViewModel viewModel = new OutlineViewModel(noteTitle);

        assertEquals("Outline: My Note", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("tabTitle updates when note title changes")
    void tabTitle_shouldUpdateWhenNoteTitleChanges() {
        StringProperty noteTitle = new SimpleStringProperty("Original");
        OutlineViewModel viewModel = new OutlineViewModel(noteTitle);

        noteTitle.set("Updated");

        assertEquals("Outline: Updated", viewModel.tabTitleProperty().get());
    }

    @Test
    @DisplayName("Constructor rejects null noteTitle")
    void constructor_shouldRejectNullNoteTitle() {
        assertThrows(NullPointerException.class,
                () -> new OutlineViewModel(null));
    }
}
