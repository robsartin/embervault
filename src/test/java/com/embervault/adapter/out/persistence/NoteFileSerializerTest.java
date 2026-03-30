package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.embervault.domain.AttributeMap;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NoteFileSerializer}.
 */
class NoteFileSerializerTest {

    private NoteFileSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new NoteFileSerializer();
    }

    @Nested
    @DisplayName("serialize")
    class SerializeTests {

        @Test
        @DisplayName("minimal note produces YAML front matter")
        void serialize_minimalNote_producesFrontMatter() {
            UUID id = UUID.randomUUID();
            Note note = new Note(id,
                    new AttributeMap());
            note.setAttribute(Attributes.NAME,
                    new AttributeValue.StringValue("Hello"));

            String result = serializer.serialize(note);

            assertTrue(result.startsWith("---\n"));
            assertTrue(result.contains("id: \"" + id + "\""));
            assertTrue(result.contains(
                    Attributes.NAME + ": \"Hello\""));
        }

        @Test
        @DisplayName("note with $Text puts body after front matter")
        void serialize_noteWithText_producesBody() {
            Note note = Note.create("Title", "Body **bold**");
            String result = serializer.serialize(note);

            assertFalse(result.contains(
                    Attributes.TEXT + ":"),
                    "$Text should not be in front matter");
            assertTrue(result.endsWith(
                    "---\nBody **bold**\n"));
        }

        @Test
        @DisplayName("ColorValue serializes as hex string")
        void serialize_colorValue_writesHex() {
            Note note = Note.create("N", "");
            note.setAttribute(Attributes.COLOR,
                    new AttributeValue.ColorValue(
                            TbxColor.hex("#A482BF")));

            String result = serializer.serialize(note);

            assertTrue(result.contains(
                    Attributes.COLOR + ": \"#A482BF\""));
        }

        @Test
        @DisplayName("DateValue serializes as ISO-8601")
        void serialize_dateValue_writesIso8601() {
            Instant ts = Instant.parse("2025-01-15T10:30:00Z");
            Note note = Note.create("N", "");
            note.setAttribute(Attributes.CREATED,
                    new AttributeValue.DateValue(ts));

            String result = serializer.serialize(note);

            assertTrue(result.contains(
                    Attributes.CREATED
                            + ": \"2025-01-15T10:30:00Z\""));
        }

        @Test
        @DisplayName("NumberValue and BooleanValue")
        void serialize_numberAndBoolean() {
            Note note = Note.create("N", "");
            note.setAttribute(Attributes.OUTLINE_ORDER,
                    new AttributeValue.NumberValue(3.5));
            note.setAttribute(Attributes.CHECKED,
                    new AttributeValue.BooleanValue(true));

            String result = serializer.serialize(note);

            assertTrue(result.contains(
                    Attributes.OUTLINE_ORDER + ": 3.5"));
            assertTrue(result.contains(
                    Attributes.CHECKED + ": true"));
        }

        @Test
        @DisplayName("SetValue serializes as sorted YAML list")
        void serialize_setValue_writesSortedList() {
            Note note = Note.create("N", "");
            note.setAttribute(Attributes.DISPLAYED_ATTRIBUTES,
                    new AttributeValue.SetValue(
                            Set.of("$Name", "$Color")));

            String result = serializer.serialize(note);

            assertTrue(result.contains(
                    Attributes.DISPLAYED_ATTRIBUTES
                            + ": [\"$Color\", \"$Name\"]"));
        }

        @Test
        @DisplayName("ListValue serializes as YAML list")
        void serialize_listValue_writesList() {
            Note note = Note.create("N", "");
            note.setAttribute(Attributes.FLAGS,
                    new AttributeValue.ListValue(
                            List.of("a", "b", "c")));

            String result = serializer.serialize(note);

            assertTrue(result.contains(
                    Attributes.FLAGS
                            + ": [\"a\", \"b\", \"c\"]"));
        }

        @Test
        @DisplayName("prototypeId serialized when set")
        void serialize_prototypeId() {
            UUID protoId = UUID.randomUUID();
            Note note = Note.create("N", "");
            note.setPrototypeId(protoId);

            String result = serializer.serialize(note);

            assertTrue(result.contains(
                    "prototypeId: \"" + protoId + "\""));
        }

        @Test
        @DisplayName("multiline $Text preserved")
        void serialize_multilineText() {
            Note note = Note.create("N",
                    "Line 1\nLine 2\n\nLine 4");
            String result = serializer.serialize(note);

            assertTrue(result.endsWith(
                    "---\nLine 1\nLine 2\n\nLine 4\n"));
        }

        @Test
        @DisplayName("YAML-special chars in $Name are quoted")
        void serialize_specialCharsQuoted() {
            Note note = Note.create("Project: Alpha", "");
            String result = serializer.serialize(note);

            assertTrue(result.contains(
                    Attributes.NAME
                            + ": \"Project: Alpha\""));
        }

        @Test
        @DisplayName("empty $Text produces no body")
        void serialize_emptyText_noBody() {
            Note note = Note.create("N", "");
            String result = serializer.serialize(note);

            assertTrue(result.endsWith("---\n"));
        }
    }
}
