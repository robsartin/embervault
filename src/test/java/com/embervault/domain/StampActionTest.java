package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StampActionTest {

    // --- parse() format tests ---

    @Test
    @DisplayName("parse() extracts attribute name and string value")
    void parse_shouldExtractAttributeAndStringValue() {
        StampAction.ParsedAction result = StampAction.parse("$Priority=high");

        assertEquals("$Priority", result.attributeName());
        assertInstanceOf(AttributeValue.StringValue.class, result.value());
        assertEquals("high", ((AttributeValue.StringValue) result.value()).value());
    }

    @Test
    @DisplayName("parse() rejects action not starting with $")
    void parse_shouldRejectMissingDollarSign() {
        assertThrows(IllegalArgumentException.class,
                () -> StampAction.parse("Color=red"));
    }

    @Test
    @DisplayName("parse() rejects action without equals sign")
    void parse_shouldRejectMissingEquals() {
        assertThrows(IllegalArgumentException.class,
                () -> StampAction.parse("$Color"));
    }

    @Test
    @DisplayName("parse() rejects null action")
    void parse_shouldRejectNull() {
        assertThrows(NullPointerException.class,
                () -> StampAction.parse(null));
    }

    @Test
    @DisplayName("parse() rejects empty attribute name (just $=value)")
    void parse_shouldRejectEmptyAttributeName() {
        assertThrows(IllegalArgumentException.class,
                () -> StampAction.parse("$=value"));
    }

    @Test
    @DisplayName("parse() trims whitespace around attribute name and value")
    void parse_shouldTrimWhitespace() {
        StampAction.ParsedAction result = StampAction.parse("$Color = red");

        assertEquals("$Color", result.attributeName());
        // "red" is a named color, so it resolves to ColorValue
        assertInstanceOf(AttributeValue.ColorValue.class, result.value());
    }

    // --- Type inference: Boolean ---

    @Test
    @DisplayName("inferValue() parses 'true' as BooleanValue(true)")
    void inferValue_shouldParseTrueAsBoolean() {
        AttributeValue result = StampAction.inferValue("true");

        assertInstanceOf(AttributeValue.BooleanValue.class, result);
        assertEquals(true, ((AttributeValue.BooleanValue) result).value());
    }

    @Test
    @DisplayName("inferValue() parses 'false' as BooleanValue(false)")
    void inferValue_shouldParseFalseAsBoolean() {
        AttributeValue result = StampAction.inferValue("false");

        assertInstanceOf(AttributeValue.BooleanValue.class, result);
        assertEquals(false, ((AttributeValue.BooleanValue) result).value());
    }

    @Test
    @DisplayName("inferValue() parses 'TRUE' as BooleanValue (case-insensitive)")
    void inferValue_shouldParseTrueCaseInsensitive() {
        AttributeValue result = StampAction.inferValue("TRUE");

        assertInstanceOf(AttributeValue.BooleanValue.class, result);
        assertEquals(true, ((AttributeValue.BooleanValue) result).value());
    }

    @Test
    @DisplayName("inferValue() parses 'False' as BooleanValue (case-insensitive)")
    void inferValue_shouldParseFalseCaseInsensitive() {
        AttributeValue result = StampAction.inferValue("False");

        assertInstanceOf(AttributeValue.BooleanValue.class, result);
        assertEquals(false, ((AttributeValue.BooleanValue) result).value());
    }

    // --- Type inference: Number ---

    @Test
    @DisplayName("inferValue() parses integer string as NumberValue")
    void inferValue_shouldParseIntegerAsNumber() {
        AttributeValue result = StampAction.inferValue("5");

        assertInstanceOf(AttributeValue.NumberValue.class, result);
        assertEquals(5.0, ((AttributeValue.NumberValue) result).value());
    }

    @Test
    @DisplayName("inferValue() parses decimal string as NumberValue")
    void inferValue_shouldParseDecimalAsNumber() {
        AttributeValue result = StampAction.inferValue("3.14");

        assertInstanceOf(AttributeValue.NumberValue.class, result);
        assertEquals(3.14, ((AttributeValue.NumberValue) result).value(), 0.001);
    }

    @Test
    @DisplayName("inferValue() parses negative number as NumberValue")
    void inferValue_shouldParseNegativeNumber() {
        AttributeValue result = StampAction.inferValue("-42");

        assertInstanceOf(AttributeValue.NumberValue.class, result);
        assertEquals(-42.0, ((AttributeValue.NumberValue) result).value());
    }

    // --- Type inference: Color ---

    @Test
    @DisplayName("inferValue() parses named color as ColorValue")
    void inferValue_shouldParseNamedColor() {
        AttributeValue result = StampAction.inferValue("red");

        assertInstanceOf(AttributeValue.ColorValue.class, result);
        assertEquals(TbxColor.named("red"),
                ((AttributeValue.ColorValue) result).value());
    }

    @Test
    @DisplayName("inferValue() parses hex color as ColorValue")
    void inferValue_shouldParseHexColor() {
        AttributeValue result = StampAction.inferValue("#FF0000");

        assertInstanceOf(AttributeValue.ColorValue.class, result);
        assertEquals(TbxColor.hex("#FF0000"),
                ((AttributeValue.ColorValue) result).value());
    }

    @Test
    @DisplayName("inferValue() parses 'blue' as ColorValue")
    void inferValue_shouldParseBlueAsColor() {
        AttributeValue result = StampAction.inferValue("blue");

        assertInstanceOf(AttributeValue.ColorValue.class, result);
    }

    @Test
    @DisplayName("inferValue() parses 'green' as ColorValue")
    void inferValue_shouldParseGreenAsColor() {
        AttributeValue result = StampAction.inferValue("green");

        assertInstanceOf(AttributeValue.ColorValue.class, result);
    }

    // --- Type inference: String fallback ---

    @Test
    @DisplayName("inferValue() falls back to StringValue for unknown text")
    void inferValue_shouldFallbackToString() {
        AttributeValue result = StampAction.inferValue("some-text");

        assertInstanceOf(AttributeValue.StringValue.class, result);
        assertEquals("some-text",
                ((AttributeValue.StringValue) result).value());
    }

    @Test
    @DisplayName("inferValue() treats empty string as StringValue")
    void inferValue_shouldTreatEmptyAsString() {
        AttributeValue result = StampAction.inferValue("");

        assertInstanceOf(AttributeValue.StringValue.class, result);
        assertEquals("", ((AttributeValue.StringValue) result).value());
    }

    // --- Full parse integration tests ---

    @Test
    @DisplayName("parse() handles $Color=red producing ColorValue")
    void parse_shouldHandleColorAction() {
        StampAction.ParsedAction result = StampAction.parse("$Color=red");

        assertEquals("$Color", result.attributeName());
        assertInstanceOf(AttributeValue.ColorValue.class, result.value());
    }

    @Test
    @DisplayName("parse() handles $Checked=true producing BooleanValue")
    void parse_shouldHandleCheckedAction() {
        StampAction.ParsedAction result = StampAction.parse("$Checked=true");

        assertEquals("$Checked", result.attributeName());
        assertInstanceOf(AttributeValue.BooleanValue.class, result.value());
        assertEquals(true, ((AttributeValue.BooleanValue) result.value()).value());
    }

    @Test
    @DisplayName("parse() handles $Priority=5 producing NumberValue")
    void parse_shouldHandlePriorityAction() {
        StampAction.ParsedAction result = StampAction.parse("$Priority=5");

        assertEquals("$Priority", result.attributeName());
        assertInstanceOf(AttributeValue.NumberValue.class, result.value());
        assertEquals(5.0, ((AttributeValue.NumberValue) result.value()).value());
    }

    @Test
    @DisplayName("parse() handles $Color=#A482BF producing ColorValue")
    void parse_shouldHandleHexColorAction() {
        StampAction.ParsedAction result = StampAction.parse("$Color=#A482BF");

        assertEquals("$Color", result.attributeName());
        assertInstanceOf(AttributeValue.ColorValue.class, result.value());
    }
}
