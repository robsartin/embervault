package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteTest {

    @Test
    @DisplayName("create() produces a Note with non-null id and timestamps")
    void create_shouldPopulateIdAndTimestamps() {
        Note note = Note.create("My Title", "Some content");

        assertNotNull(note.getId());
        assertEquals("My Title", note.getTitle());
        assertEquals("Some content", note.getContent());
        assertNotNull(note.getCreatedAt());
        assertNotNull(note.getUpdatedAt());
    }

    @Test
    @DisplayName("create() sets createdAt equal to updatedAt initially")
    void create_shouldSetEqualTimestamps() {
        Note note = Note.create("Title", "Content");

        assertEquals(note.getCreatedAt(), note.getUpdatedAt());
    }

    @Test
    @DisplayName("Constructor rejects null id")
    void constructor_shouldRejectNullId() {
        Instant now = Instant.now();
        assertThrows(NullPointerException.class,
                () -> new Note(null, "Title", "Content", now, now));
    }

    @Test
    @DisplayName("Constructor rejects null title")
    void constructor_shouldRejectNullTitle() {
        Instant now = Instant.now();
        assertThrows(NullPointerException.class,
                () -> new Note(UUID.randomUUID(), null, "Content", now, now));
    }

    @Test
    @DisplayName("Constructor rejects blank title")
    void constructor_shouldRejectBlankTitle() {
        Instant now = Instant.now();
        assertThrows(IllegalArgumentException.class,
                () -> new Note(UUID.randomUUID(), "   ", "Content", now, now));
    }

    @Test
    @DisplayName("Constructor rejects null content")
    void constructor_shouldRejectNullContent() {
        Instant now = Instant.now();
        assertThrows(NullPointerException.class,
                () -> new Note(UUID.randomUUID(), "Title", null, now, now));
    }

    @Test
    @DisplayName("Constructor rejects null createdAt")
    void constructor_shouldRejectNullCreatedAt() {
        Instant now = Instant.now();
        assertThrows(NullPointerException.class,
                () -> new Note(UUID.randomUUID(), "Title", "Content", null, now));
    }

    @Test
    @DisplayName("Constructor rejects null updatedAt")
    void constructor_shouldRejectNullUpdatedAt() {
        Instant now = Instant.now();
        assertThrows(NullPointerException.class,
                () -> new Note(UUID.randomUUID(), "Title", "Content", now, null));
    }

    @Test
    @DisplayName("update() rejects null content")
    void update_shouldRejectNullContent() {
        Note note = Note.create("Title", "Content");

        assertThrows(NullPointerException.class,
                () -> note.update("Title", null));
    }

    @Test
    @DisplayName("update() changes title, content, and updatedAt")
    void update_shouldChangeFieldsAndTimestamp() {
        Note note = Note.create("Old", "Old content");
        Instant before = note.getUpdatedAt();

        note.update("New", "New content");

        assertEquals("New", note.getTitle());
        assertEquals("New content", note.getContent());
        assertTrue(note.getUpdatedAt().compareTo(before) >= 0);
    }

    @Test
    @DisplayName("update() rejects blank title")
    void update_shouldRejectBlankTitle() {
        Note note = Note.create("Title", "Content");

        assertThrows(IllegalArgumentException.class,
                () -> note.update("  ", "Content"));
    }

    @Test
    @DisplayName("update() rejects null title")
    void update_shouldRejectNullTitle() {
        Note note = Note.create("Title", "Content");

        assertThrows(NullPointerException.class,
                () -> note.update(null, "Content"));
    }

    @Test
    @DisplayName("equals() returns true for same instance")
    void equals_shouldReturnTrueForSameInstance() {
        Note note = Note.create("Title", "Content");

        assertEquals(note, note);
    }

    @Test
    @DisplayName("equals() is based on id")
    void equals_shouldBeBasedOnId() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Note noteA = new Note(id, "A", "a", now, now);
        Note noteB = new Note(id, "B", "b", now, now);

        assertEquals(noteA, noteB);
    }

    @Test
    @DisplayName("equals() returns false for different ids")
    void equals_shouldReturnFalseForDifferentIds() {
        Note noteA = Note.create("Same", "Same");
        Note noteB = Note.create("Same", "Same");

        assertNotEquals(noteA, noteB);
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    @DisplayName("equals() returns false for null")
    void equals_shouldReturnFalseForNull() {
        Note note = Note.create("Title", "Content");

        assertNotEquals(note, null);
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    @DisplayName("equals() returns false for different type")
    void equals_shouldReturnFalseForDifferentType() {
        Note note = Note.create("Title", "Content");

        assertNotEquals(note, "not a note");
    }

    @Test
    @DisplayName("hashCode() is consistent with equals()")
    void hashCode_shouldBeConsistentWithEquals() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Note noteA = new Note(id, "A", "a", now, now);
        Note noteB = new Note(id, "B", "b", now, now);

        assertEquals(noteA.hashCode(), noteB.hashCode());
    }

    @Test
    @DisplayName("toString() includes id and title")
    void toString_shouldIncludeIdAndTitle() {
        Note note = Note.create("Test", "Content");
        String str = note.toString();

        assertTrue(str.contains(note.getId().toString()));
        assertTrue(str.contains("Test"));
    }

    // --- Child note tests ---

    @Test
    @DisplayName("new note has no children")
    void newNote_shouldHaveNoChildren() {
        Note note = Note.create("Title", "Content");

        assertTrue(note.getChildIds().isEmpty());
        assertFalse(note.hasChildren());
    }

    @Test
    @DisplayName("addChild() adds a child id to the list")
    void addChild_shouldAddChildId() {
        Note note = Note.create("Parent", "");
        UUID childId = UUID.randomUUID();

        note.addChild(childId);

        assertEquals(1, note.getChildIds().size());
        assertEquals(childId, note.getChildIds().get(0));
        assertTrue(note.hasChildren());
    }

    @Test
    @DisplayName("addChild() preserves order")
    void addChild_shouldPreserveOrder() {
        Note note = Note.create("Parent", "");
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        note.addChild(first);
        note.addChild(second);

        assertEquals(List.of(first, second), note.getChildIds());
    }

    @Test
    @DisplayName("addChild() rejects null")
    void addChild_shouldRejectNull() {
        Note note = Note.create("Parent", "");

        assertThrows(NullPointerException.class, () -> note.addChild(null));
    }

    @Test
    @DisplayName("removeChild() removes a child id")
    void removeChild_shouldRemoveChildId() {
        Note note = Note.create("Parent", "");
        UUID childId = UUID.randomUUID();
        note.addChild(childId);

        note.removeChild(childId);

        assertTrue(note.getChildIds().isEmpty());
        assertFalse(note.hasChildren());
    }

    @Test
    @DisplayName("removeChild() is safe for unknown ids")
    void removeChild_shouldBeSafeForUnknownId() {
        Note note = Note.create("Parent", "");

        note.removeChild(UUID.randomUUID());

        assertTrue(note.getChildIds().isEmpty());
    }

    @Test
    @DisplayName("getChildIds() returns unmodifiable list")
    void getChildIds_shouldReturnUnmodifiableList() {
        Note note = Note.create("Parent", "");
        note.addChild(UUID.randomUUID());

        List<UUID> children = note.getChildIds();

        assertThrows(UnsupportedOperationException.class,
                () -> children.add(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Note created with AttributeMap constructor has empty children")
    void attributeMapConstructor_shouldHaveEmptyChildren() {
        Note note = new Note(UUID.randomUUID(), new AttributeMap());

        assertTrue(note.getChildIds().isEmpty());
        assertFalse(note.hasChildren());
    }
}
