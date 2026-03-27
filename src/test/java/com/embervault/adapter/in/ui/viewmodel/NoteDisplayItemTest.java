package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteDisplayItemTest {

    @Test
    @DisplayName("constructor stores id, title, and content")
    void constructor_shouldStoreFields() {
        UUID id = UUID.randomUUID();
        NoteDisplayItem item = new NoteDisplayItem(id, "Title", "Content");

        assertEquals(id, item.getId());
        assertEquals("Title", item.getTitle());
        assertEquals("Content", item.getContent());
    }

    @Test
    @DisplayName("constructor rejects null id")
    void constructor_shouldRejectNullId() {
        assertThrows(NullPointerException.class,
                () -> new NoteDisplayItem(null, "Title", "Content"));
    }

    @Test
    @DisplayName("constructor rejects null title")
    void constructor_shouldRejectNullTitle() {
        assertThrows(NullPointerException.class,
                () -> new NoteDisplayItem(UUID.randomUUID(), null, "Content"));
    }

    @Test
    @DisplayName("constructor rejects null content")
    void constructor_shouldRejectNullContent() {
        assertThrows(NullPointerException.class,
                () -> new NoteDisplayItem(UUID.randomUUID(), "Title", null));
    }

    @Test
    @DisplayName("toString() returns the title")
    void toString_shouldReturnTitle() {
        NoteDisplayItem item = new NoteDisplayItem(
                UUID.randomUUID(), "My Note", "Body");

        assertEquals("My Note", item.toString());
    }

    @Test
    @DisplayName("equals() returns true for same id")
    void equals_shouldReturnTrueForSameId() {
        UUID id = UUID.randomUUID();
        NoteDisplayItem a = new NoteDisplayItem(id, "A", "a");
        NoteDisplayItem b = new NoteDisplayItem(id, "B", "b");

        assertEquals(a, b);
    }

    @Test
    @DisplayName("equals() returns false for different ids")
    void equals_shouldReturnFalseForDifferentIds() {
        NoteDisplayItem a = new NoteDisplayItem(
                UUID.randomUUID(), "A", "a");
        NoteDisplayItem b = new NoteDisplayItem(
                UUID.randomUUID(), "A", "a");

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals() returns true for same instance")
    void equals_shouldReturnTrueForSameInstance() {
        NoteDisplayItem item = new NoteDisplayItem(
                UUID.randomUUID(), "A", "a");

        assertEquals(item, item);
    }

    @Test
    @DisplayName("equals() returns false for null")
    void equals_shouldReturnFalseForNull() {
        NoteDisplayItem item = new NoteDisplayItem(
                UUID.randomUUID(), "A", "a");

        assertNotEquals(item, null);
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    @DisplayName("equals() returns false for different type")
    void equals_shouldReturnFalseForDifferentType() {
        NoteDisplayItem item = new NoteDisplayItem(
                UUID.randomUUID(), "A", "a");

        assertNotEquals(item, "not a display item");
    }

    @Test
    @DisplayName("hashCode() is consistent with equals()")
    void hashCode_shouldBeConsistentWithEquals() {
        UUID id = UUID.randomUUID();
        NoteDisplayItem a = new NoteDisplayItem(id, "A", "a");
        NoteDisplayItem b = new NoteDisplayItem(id, "B", "b");

        assertEquals(a.hashCode(), b.hashCode());
    }
}
