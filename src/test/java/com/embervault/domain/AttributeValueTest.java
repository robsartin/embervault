package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeValueTest {

    @Test
    @DisplayName("StringValue holds a string")
    void stringValue() {
        var val = new AttributeValue.StringValue("hello");
        assertEquals("hello", val.value());
    }

    @Test
    @DisplayName("NumberValue holds a double")
    void numberValue() {
        var val = new AttributeValue.NumberValue(3.14);
        assertEquals(3.14, val.value());
    }

    @Test
    @DisplayName("BooleanValue holds a boolean")
    void booleanValue() {
        var val = new AttributeValue.BooleanValue(true);
        assertEquals(true, val.value());
    }

    @Test
    @DisplayName("ColorValue holds a TbxColor")
    void colorValue() {
        TbxColor color = TbxColor.hex("#FF0000");
        var val = new AttributeValue.ColorValue(color);
        assertEquals(color, val.value());
    }

    @Test
    @DisplayName("DateValue holds an Instant")
    void dateValue() {
        Instant now = Instant.now();
        var val = new AttributeValue.DateValue(now);
        assertEquals(now, val.value());
    }

    @Test
    @DisplayName("IntervalValue holds a Duration")
    void intervalValue() {
        Duration dur = Duration.ofHours(2);
        var val = new AttributeValue.IntervalValue(dur);
        assertEquals(dur, val.value());
    }

    @Test
    @DisplayName("FileValue holds a path string")
    void fileValue() {
        var val = new AttributeValue.FileValue("/path/to/file");
        assertEquals("/path/to/file", val.path());
    }

    @Test
    @DisplayName("UrlValue holds a URL string")
    void urlValue() {
        var val = new AttributeValue.UrlValue("https://example.com");
        assertEquals("https://example.com", val.url());
    }

    @Test
    @DisplayName("ListValue holds a list of strings")
    void listValue() {
        var val = new AttributeValue.ListValue(List.of("a", "b", "c"));
        assertEquals(List.of("a", "b", "c"), val.values());
    }

    @Test
    @DisplayName("SetValue holds a set of strings")
    void setValue() {
        var val = new AttributeValue.SetValue(Set.of("x", "y"));
        assertEquals(Set.of("x", "y"), val.values());
    }

    @Test
    @DisplayName("ActionValue holds an expression string")
    void actionValue() {
        var val = new AttributeValue.ActionValue("$Name + ' suffix'");
        assertEquals("$Name + ' suffix'", val.expression());
    }

    @Test
    @DisplayName("Pattern matching works with switch on sealed interface")
    void patternMatching() {
        AttributeValue val = new AttributeValue.StringValue("test");
        String result = switch (val) {
            case AttributeValue.StringValue s -> "string:" + s.value();
            case AttributeValue.NumberValue n -> "number:" + n.value();
            case AttributeValue.BooleanValue b -> "bool:" + b.value();
            case AttributeValue.ColorValue c -> "color:" + c.value();
            case AttributeValue.DateValue d -> "date:" + d.value();
            case AttributeValue.IntervalValue i -> "interval:" + i.value();
            case AttributeValue.FileValue f -> "file:" + f.path();
            case AttributeValue.UrlValue u -> "url:" + u.url();
            case AttributeValue.ListValue l -> "list:" + l.values();
            case AttributeValue.SetValue sv -> "set:" + sv.values();
            case AttributeValue.ActionValue a -> "action:" + a.expression();
        };
        assertEquals("string:test", result);
    }

    @Test
    @DisplayName("of() infers StringValue from String")
    void of_infersString() {
        assertInstanceOf(AttributeValue.StringValue.class, AttributeValue.of("hello"));
    }

    @Test
    @DisplayName("of() infers NumberValue from Double")
    void of_infersDouble() {
        assertInstanceOf(AttributeValue.NumberValue.class, AttributeValue.of(3.14));
    }

    @Test
    @DisplayName("of() infers NumberValue from Integer")
    void of_infersInteger() {
        AttributeValue val = AttributeValue.of(42);
        assertInstanceOf(AttributeValue.NumberValue.class, val);
        assertEquals(42.0, ((AttributeValue.NumberValue) val).value());
    }

    @Test
    @DisplayName("of() infers BooleanValue from Boolean")
    void of_infersBoolean() {
        assertInstanceOf(AttributeValue.BooleanValue.class, AttributeValue.of(true));
    }

    @Test
    @DisplayName("of() infers ColorValue from TbxColor")
    void of_infersColor() {
        assertInstanceOf(AttributeValue.ColorValue.class,
                AttributeValue.of(TbxColor.hex("#FF0000")));
    }

    @Test
    @DisplayName("of() infers DateValue from Instant")
    void of_infersInstant() {
        assertInstanceOf(AttributeValue.DateValue.class, AttributeValue.of(Instant.now()));
    }

    @Test
    @DisplayName("of() infers IntervalValue from Duration")
    void of_infersDuration() {
        assertInstanceOf(AttributeValue.IntervalValue.class,
                AttributeValue.of(Duration.ofHours(1)));
    }

    @Test
    @DisplayName("of() rejects null")
    void of_rejectsNull() {
        assertThrows(NullPointerException.class, () -> AttributeValue.of(null));
    }

    @Test
    @DisplayName("of() rejects unknown types")
    void of_rejectsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> AttributeValue.of(new Object()));
    }

    @Test
    @DisplayName("ListValue makes a defensive copy")
    void listValue_defensiveCopy() {
        var mutable = new java.util.ArrayList<>(List.of("a", "b"));
        var val = new AttributeValue.ListValue(mutable);
        mutable.add("c");
        assertEquals(List.of("a", "b"), val.values());
    }

    @Test
    @DisplayName("SetValue makes a defensive copy")
    void setValue_defensiveCopy() {
        var mutable = new java.util.HashSet<>(Set.of("a", "b"));
        var val = new AttributeValue.SetValue(mutable);
        mutable.add("c");
        assertEquals(Set.of("a", "b"), val.values());
    }
}
