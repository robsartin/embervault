package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StampTest {

    @Test
    @DisplayName("create() produces a Stamp with random UUID")
    void create_shouldProduceStampWithRandomId() {
        Stamp stamp = Stamp.create("Color:red", "$Color=red");

        assertNotNull(stamp.id());
        assertEquals("Color:red", stamp.name());
        assertEquals("$Color=red", stamp.action());
    }

    @Test
    @DisplayName("constructor validates non-null id")
    void constructor_shouldRejectNullId() {
        assertThrows(NullPointerException.class,
                () -> new Stamp(null, "name", "action"));
    }

    @Test
    @DisplayName("constructor validates non-null name")
    void constructor_shouldRejectNullName() {
        assertThrows(NullPointerException.class,
                () -> new Stamp(UUID.randomUUID(), null, "action"));
    }

    @Test
    @DisplayName("constructor validates non-null action")
    void constructor_shouldRejectNullAction() {
        assertThrows(NullPointerException.class,
                () -> new Stamp(UUID.randomUUID(), "name", null));
    }

    @Test
    @DisplayName("constructor rejects blank name")
    void constructor_shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stamp(UUID.randomUUID(), "   ", "$Color=red"));
    }

    @Test
    @DisplayName("constructor rejects blank action")
    void constructor_shouldRejectBlankAction() {
        assertThrows(IllegalArgumentException.class,
                () -> new Stamp(UUID.randomUUID(), "name", "   "));
    }

    @Test
    @DisplayName("record equals and hashCode work correctly")
    void equals_shouldWorkForRecords() {
        UUID id = UUID.randomUUID();
        Stamp a = new Stamp(id, "Color:red", "$Color=red");
        Stamp b = new Stamp(id, "Color:red", "$Color=red");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("toString includes name and action")
    void toString_shouldIncludeFields() {
        Stamp stamp = Stamp.create("Mark Done", "$Checked=true");
        String str = stamp.toString();

        assertNotNull(str);
        // Record toString includes field names
        assertEquals(true, str.contains("Mark Done"));
        assertEquals(true, str.contains("$Checked=true"));
    }
}
