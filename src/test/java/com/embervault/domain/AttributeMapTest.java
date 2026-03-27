package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeMapTest {

    private AttributeMap map;

    @BeforeEach
    void setUp() {
        map = new AttributeMap();
    }

    @Test
    @DisplayName("get() returns empty for unset attribute")
    void get_returnsEmptyForUnset() {
        assertEquals(Optional.empty(), map.get("$Name"));
    }

    @Test
    @DisplayName("set() and get() round-trip a value")
    void set_andGet_roundTrip() {
        map.set("$Name", new AttributeValue.StringValue("Hello"));
        assertEquals(Optional.of(new AttributeValue.StringValue("Hello")), map.get("$Name"));
    }

    @Test
    @DisplayName("set() overwrites a previous value")
    void set_overwritesPrevious() {
        map.set("$Name", new AttributeValue.StringValue("Old"));
        map.set("$Name", new AttributeValue.StringValue("New"));
        assertEquals(Optional.of(new AttributeValue.StringValue("New")), map.get("$Name"));
    }

    @Test
    @DisplayName("remove() clears a local value")
    void remove_clearsValue() {
        map.set("$Name", new AttributeValue.StringValue("Hello"));
        map.remove("$Name");
        assertTrue(map.get("$Name").isEmpty());
    }

    @Test
    @DisplayName("remove() is safe for unset attribute")
    void remove_safeForUnset() {
        map.remove("$Name");
        // no exception
    }

    @Test
    @DisplayName("hasLocalValue() returns true for set attributes")
    void hasLocalValue_trueForSet() {
        map.set("$Name", new AttributeValue.StringValue("Hello"));
        assertTrue(map.hasLocalValue("$Name"));
    }

    @Test
    @DisplayName("hasLocalValue() returns false for unset attributes")
    void hasLocalValue_falseForUnset() {
        assertFalse(map.hasLocalValue("$Name"));
    }

    @Test
    @DisplayName("localEntries() returns only set attributes")
    void localEntries_returnsSetOnly() {
        map.set("$Name", new AttributeValue.StringValue("Hello"));
        map.set("$Color", new AttributeValue.ColorValue(TbxColor.named("red")));

        Map<String, AttributeValue> entries = map.localEntries();
        assertEquals(2, entries.size());
        assertTrue(entries.containsKey("$Name"));
        assertTrue(entries.containsKey("$Color"));
    }

    @Test
    @DisplayName("localEntries() returns an unmodifiable map")
    void localEntries_isUnmodifiable() {
        map.set("$Name", new AttributeValue.StringValue("Hello"));

        Map<String, AttributeValue> entries = map.localEntries();
        assertThrows(UnsupportedOperationException.class,
                () -> entries.put("$Test", new AttributeValue.StringValue("fail")));
    }

    @Test
    @DisplayName("size() returns the number of set attributes")
    void size_returnsCount() {
        assertEquals(0, map.size());
        map.set("$Name", new AttributeValue.StringValue("Hello"));
        assertEquals(1, map.size());
        map.set("$Text", new AttributeValue.StringValue("World"));
        assertEquals(2, map.size());
    }

    @Test
    @DisplayName("set() rejects null name")
    void set_rejectsNullName() {
        assertThrows(NullPointerException.class,
                () -> map.set(null, new AttributeValue.StringValue("x")));
    }

    @Test
    @DisplayName("set() rejects null value")
    void set_rejectsNullValue() {
        assertThrows(NullPointerException.class,
                () -> map.set("$Name", null));
    }

    @Test
    @DisplayName("Copy constructor creates independent copy")
    void copyConstructor_createsIndependentCopy() {
        map.set("$Name", new AttributeValue.StringValue("Hello"));
        AttributeMap copy = new AttributeMap(map);
        copy.set("$Name", new AttributeValue.StringValue("Changed"));

        assertEquals(Optional.of(new AttributeValue.StringValue("Hello")), map.get("$Name"));
        assertEquals(Optional.of(new AttributeValue.StringValue("Changed")), copy.get("$Name"));
    }
}
