package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.embervault.SharedServices;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration test verifying UndoRedoService is wired into SharedServices.
 */
class UndoRedoServiceIntegrationTest {

    private SharedServices services;

    @BeforeEach
    void setUp() {
        services = SharedServices.create();
    }

    @Test
    @DisplayName("SharedServices exposes an UndoRedoService")
    void sharedServicesHasUndoRedo() {
        assertNotNull(services.undoRedoService());
    }

    @Test
    @DisplayName("undo via SharedServices restores note state")
    void undoViaSharedServices() {
        Note note = services.noteService().createNote("Original", "");
        services.undoRedoService().recordChange(note);

        note.setAttribute(Attributes.NAME,
                new AttributeValue.StringValue("Changed"));
        services.noteRepository().save(note);

        assertTrue(services.undoRedoService().canUndo());
        services.undoRedoService().undo(services.noteRepository());

        assertEquals("Original",
                services.noteRepository().findById(note.getId())
                        .orElseThrow().getTitle());
        assertFalse(services.undoRedoService().canUndo());
    }
}
