package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NoteFileDeserializer}.
 */
class NoteFileDeserializerTest {

    private NoteFileDeserializer deserializer;
    private AttributeSchemaRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AttributeSchemaRegistry();
        deserializer = new NoteFileDeserializer(registry);
    }

    @Nested
    @DisplayName("deserialize")
    class DeserializeTests {

        @Test
        @DisplayName("minimal front matter creates note")
        void deserialize_minimal() {
            UUID id = UUID.randomUUID();
            String input = "---\n"
                    + "id: \"" + id + "\"\n"
                    + Attributes.NAME + ": \"Hello\"\n"
                    + "---\n";

            Note note = deserializer.deserialize(input, id);

            assertEquals(id, note.getId());
            assertEquals("Hello", note.getTitle());
        }

        @Test
        @DisplayName("markdown body sets $Text")
        void deserialize_withBody() {
            UUID id = UUID.randomUUID();
            String input = "---\n"
                    + "id: \"" + id + "\"\n"
                    + Attributes.NAME + ": \"Title\"\n"
                    + "---\n"
                    + "Body **bold** text\n";

            Note note = deserializer.deserialize(input, id);

            assertEquals("Body **bold** text", note.getContent());
        }

        @Test
        @DisplayName("ColorValue via registry lookup")
        void deserialize_color() {
            UUID id = UUID.randomUUID();
            String input = "---\n"
                    + "id: \"" + id + "\"\n"
                    + Attributes.COLOR + ": \"#A482BF\"\n"
                    + "---\n";

            Note note = deserializer.deserialize(input, id);
            AttributeValue val = note.getAttribute(
                    Attributes.COLOR).orElseThrow();

            assertTrue(val instanceof AttributeValue.ColorValue);
            assertEquals("#A482BF",
                    ((AttributeValue.ColorValue) val)
                            .value().toHex());
        }

        @Test
        @DisplayName("DateValue from ISO-8601")
        void deserialize_date() {
            UUID id = UUID.randomUUID();
            String input = "---\n"
                    + "id: \"" + id + "\"\n"
                    + Attributes.CREATED
                    + ": \"2025-01-15T10:30:00Z\"\n"
                    + "---\n";

            Note note = deserializer.deserialize(input, id);
            AttributeValue val = note.getAttribute(
                    Attributes.CREATED).orElseThrow();

            assertTrue(val instanceof AttributeValue.DateValue);
            assertEquals(Instant.parse("2025-01-15T10:30:00Z"),
                    ((AttributeValue.DateValue) val).value());
        }

        @Test
        @DisplayName("NumberValue and BooleanValue")
        void deserialize_numberAndBoolean() {
            UUID id = UUID.randomUUID();
            String input = "---\n"
                    + "id: \"" + id + "\"\n"
                    + Attributes.OUTLINE_ORDER + ": 3.5\n"
                    + Attributes.CHECKED + ": true\n"
                    + "---\n";

            Note note = deserializer.deserialize(input, id);

            AttributeValue order = note.getAttribute(
                    Attributes.OUTLINE_ORDER).orElseThrow();
            assertTrue(order instanceof AttributeValue.NumberValue);
            assertEquals(3.5,
                    ((AttributeValue.NumberValue) order).value());

            AttributeValue checked = note.getAttribute(
                    Attributes.CHECKED).orElseThrow();
            assertTrue(
                    checked instanceof AttributeValue.BooleanValue);
            assertEquals(true,
                    ((AttributeValue.BooleanValue) checked)
                            .value());
        }

        @Test
        @DisplayName("SetValue from YAML list")
        void deserialize_set() {
            UUID id = UUID.randomUUID();
            String input = "---\n"
                    + "id: \"" + id + "\"\n"
                    + Attributes.DISPLAYED_ATTRIBUTES
                    + ": [\"$Color\", \"$Name\"]\n"
                    + "---\n";

            Note note = deserializer.deserialize(input, id);
            AttributeValue val = note.getAttribute(
                    Attributes.DISPLAYED_ATTRIBUTES).orElseThrow();

            assertTrue(val instanceof AttributeValue.SetValue);
            assertEquals(Set.of("$Color", "$Name"),
                    ((AttributeValue.SetValue) val).values());
        }

        @Test
        @DisplayName("unknown attribute defaults to StringValue")
        void deserialize_unknownAttr() {
            UUID id = UUID.randomUUID();
            String input = "---\n"
                    + "id: \"" + id + "\"\n"
                    + "$MyCustom: \"some value\"\n"
                    + "---\n";

            Note note = deserializer.deserialize(input, id);
            AttributeValue val = note.getAttribute(
                    "$MyCustom").orElseThrow();

            assertTrue(val instanceof AttributeValue.StringValue);
            assertEquals("some value",
                    ((AttributeValue.StringValue) val).value());
        }

        @Test
        @DisplayName("prototypeId is set on note")
        void deserialize_prototypeId() {
            UUID id = UUID.randomUUID();
            UUID protoId = UUID.randomUUID();
            String input = "---\n"
                    + "id: \"" + id + "\"\n"
                    + "prototypeId: \"" + protoId + "\"\n"
                    + "---\n";

            Note note = deserializer.deserialize(input, id);

            assertTrue(note.getPrototypeId().isPresent());
            assertEquals(protoId,
                    note.getPrototypeId().get());
        }

        @Test
        @DisplayName("round-trip preserves all attribute types")
        void roundTrip_allTypes() {
            Note original = Note.create("Test", "Body text");
            original.setAttribute(Attributes.COLOR,
                    new AttributeValue.ColorValue(
                            TbxColor.hex("#FF0000")));
            original.setAttribute(Attributes.OUTLINE_ORDER,
                    new AttributeValue.NumberValue(2.0));
            original.setAttribute(Attributes.CHECKED,
                    new AttributeValue.BooleanValue(false));
            original.setAttribute(Attributes.DISPLAYED_ATTRIBUTES,
                    new AttributeValue.SetValue(
                            Set.of("$Name", "$Color")));
            original.setPrototypeId(UUID.randomUUID());

            NoteFileSerializer serializer =
                    new NoteFileSerializer();
            String serialized = serializer.serialize(original);
            Note restored = deserializer.deserialize(
                    serialized, original.getId());

            assertEquals(original.getId(), restored.getId());
            assertEquals(original.getTitle(),
                    restored.getTitle());
            assertEquals(original.getContent(),
                    restored.getContent());
            assertEquals(
                    original.getPrototypeId(),
                    restored.getPrototypeId());

            assertNotNull(restored.getAttribute(
                    Attributes.COLOR).orElse(null));
            assertNotNull(restored.getAttribute(
                    Attributes.OUTLINE_ORDER).orElse(null));
        }
    }
}
