package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NoteMemento} — immutable snapshots of note state.
 */
class NoteMementoTest {

    @Test
    @DisplayName("memento captures note attributes and can restore them")
    void captureAndRestore() {
        Note note = Note.create("Original Title", "Original content");
        UUID noteId = note.getId();

        NoteMemento memento = NoteMemento.capture(note);

        // Mutate the note after capture
        note.setAttribute(Attributes.NAME,
                new AttributeValue.StringValue("Changed Title"));
        note.setAttribute(Attributes.TEXT,
                new AttributeValue.StringValue("Changed content"));

        assertEquals("Changed Title", note.getTitle());

        // Restore from memento
        memento.restore(note);

        assertEquals("Original Title", note.getTitle());
        assertEquals("Original content", note.getContent());
        assertEquals(noteId, note.getId());
    }

    @Test
    @DisplayName("memento holds an independent copy of attributes")
    void mementoIsIndependentCopy() {
        Note note = Note.create("Title", "Content");

        NoteMemento memento = NoteMemento.capture(note);

        // Mutate original note — memento should be unaffected
        note.setAttribute(Attributes.COLOR,
                new AttributeValue.ColorValue(TbxColor.named("red")));

        memento.restore(note);

        // The color we added after capture should be gone
        assertEquals(java.util.Optional.empty(),
                note.getAttribute(Attributes.COLOR));
    }
}
