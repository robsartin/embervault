package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Edge-case tests for {@link AttributeMap} — remove, clear-like patterns,
 * copy constructor independence, and size tracking.
 */
class AttributeMapEdgeCaseTest {

    private AttributeMap map;

    @BeforeEach
    void setUp() {
        map = new AttributeMap();
    }

    @Test
    @DisplayName("remove() on populated map decrements size")
    void remove_decrementsSize() {
        map.set("$Name", new AttributeValue.StringValue("A"));
        map.set("$Text", new AttributeValue.StringValue("B"));
        assertEquals(2, map.size());

        map.remove("$Name");

        assertEquals(1, map.size());
        assertFalse(map.hasLocalValue("$Name"));
        assertTrue(map.hasLocalValue("$Text"));
    }

    @Test
    @DisplayName("removing all entries leaves map empty")
    void removeAll_leavesMapEmpty() {
        map.set("$Name", new AttributeValue.StringValue("A"));
        map.set("$Text", new AttributeValue.StringValue("B"));
        map.set("$Color", new AttributeValue.ColorValue(TbxColor.named("red")));

        map.remove("$Name");
        map.remove("$Text");
        map.remove("$Color");

        assertEquals(0, map.size());
        assertTrue(map.localEntries().isEmpty());
    }

    @Test
    @DisplayName("copy constructor copies all entries")
    void copyConstructor_copiesAllEntries() {
        map.set("$Name", new AttributeValue.StringValue("Hello"));
        map.set("$Color", new AttributeValue.ColorValue(TbxColor.named("blue")));

        AttributeMap copy = new AttributeMap(map);

        assertEquals(2, copy.size());
        assertEquals(map.get("$Name"), copy.get("$Name"));
        assertEquals(map.get("$Color"), copy.get("$Color"));
    }

    @Test
    @DisplayName("copy constructor from empty map produces empty copy")
    void copyConstructor_emptyMap_producesEmptyCopy() {
        AttributeMap copy = new AttributeMap(map);

        assertEquals(0, copy.size());
        assertTrue(copy.localEntries().isEmpty());
    }

    @Test
    @DisplayName("set same key twice does not increase size")
    void setSameKeyTwice_doesNotIncreaseSize() {
        map.set("$Name", new AttributeValue.StringValue("First"));
        map.set("$Name", new AttributeValue.StringValue("Second"));

        assertEquals(1, map.size());
        assertEquals(Optional.of(new AttributeValue.StringValue("Second")),
                map.get("$Name"));
    }

    @Test
    @DisplayName("get returns empty after remove even if value was previously set")
    void getAfterRemove_returnsEmpty() {
        map.set("$Name", new AttributeValue.StringValue("Hello"));
        map.remove("$Name");

        assertEquals(Optional.empty(), map.get("$Name"));
        assertFalse(map.hasLocalValue("$Name"));
    }
}
