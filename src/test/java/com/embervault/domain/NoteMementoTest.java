package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

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

        NoteMemento memento = NoteMemento.capture(note);

        note.setAttribute(Attributes.NAME,
                new AttributeValue.StringValue("Changed Title"));
        note.setAttribute(Attributes.TEXT,
                new AttributeValue.StringValue("Changed content"));
        assertEquals("Changed Title", note.getTitle());

        memento.restore(note);

        assertEquals("Original Title", note.getTitle());
        assertEquals("Original content", note.getContent());
    }

    @Test
    @DisplayName("memento holds an independent copy of attributes")
    void mementoIsIndependentCopy() {
        Note note = Note.create("Title", "Content");

        NoteMemento memento = NoteMemento.capture(note);

        note.setAttribute(Attributes.COLOR,
                new AttributeValue.ColorValue(TbxColor.named("red")));

        memento.restore(note);

        assertEquals(Optional.empty(), note.getAttribute(Attributes.COLOR));
    }

    @Test
    @DisplayName("memento captures and restores prototype id")
    void capturesPrototypeId() {
        Note note = Note.create("Title", "Content");
        java.util.UUID protoId = java.util.UUID.randomUUID();
        note.setPrototypeId(protoId);

        NoteMemento memento = NoteMemento.capture(note);

        note.setPrototypeId(null);
        assertEquals(Optional.empty(), note.getPrototypeId());

        memento.restore(note);

        assertEquals(Optional.of(protoId), note.getPrototypeId());
    }
}
