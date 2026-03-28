package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LinkTest {

    @Test
    @DisplayName("create(source, dest) generates id and uses default type")
    void create_shouldGenerateIdAndDefaultType() {
        UUID source = UUID.randomUUID();
        UUID dest = UUID.randomUUID();

        Link link = Link.create(source, dest);

        assertNotNull(link.id());
        assertEquals(source, link.sourceId());
        assertEquals(dest, link.destinationId());
        assertEquals("untitled", link.type());
    }

    @Test
    @DisplayName("create(source, dest, type) uses specified type")
    void create_withType_shouldUseSpecifiedType() {
        UUID source = UUID.randomUUID();
        UUID dest = UUID.randomUUID();

        Link link = Link.create(source, dest, "web");

        assertEquals("web", link.type());
        assertEquals(source, link.sourceId());
        assertEquals(dest, link.destinationId());
    }

    @Test
    @DisplayName("constructor rejects null id")
    void constructor_shouldRejectNullId() {
        assertThrows(NullPointerException.class,
                () -> new Link(null, UUID.randomUUID(), UUID.randomUUID(), "test"));
    }

    @Test
    @DisplayName("constructor rejects null sourceId")
    void constructor_shouldRejectNullSourceId() {
        assertThrows(NullPointerException.class,
                () -> new Link(UUID.randomUUID(), null, UUID.randomUUID(), "test"));
    }

    @Test
    @DisplayName("constructor rejects null destinationId")
    void constructor_shouldRejectNullDestinationId() {
        assertThrows(NullPointerException.class,
                () -> new Link(UUID.randomUUID(), UUID.randomUUID(), null, "test"));
    }

    @Test
    @DisplayName("constructor rejects null type")
    void constructor_shouldRejectNullType() {
        assertThrows(NullPointerException.class,
                () -> new Link(UUID.randomUUID(), UUID.randomUUID(),
                        UUID.randomUUID(), null));
    }

    @Test
    @DisplayName("record equality is based on all fields")
    void equality_shouldBeBasedOnAllFields() {
        UUID id = UUID.randomUUID();
        UUID source = UUID.randomUUID();
        UUID dest = UUID.randomUUID();

        Link link1 = new Link(id, source, dest, "untitled");
        Link link2 = new Link(id, source, dest, "untitled");

        assertEquals(link1, link2);
        assertEquals(link1.hashCode(), link2.hashCode());
    }
}
