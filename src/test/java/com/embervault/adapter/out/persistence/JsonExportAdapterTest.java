package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.Stamp;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link JsonExportAdapter}.
 */
class JsonExportAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("exports single note to JSON with attributes")
    void exportSingleNote() throws IOException {
        UUID noteId = UUID.randomUUID();
        Instant now = Instant.parse("2025-06-15T10:30:00Z");
        Note note = new Note(noteId, "Test Note", "Note body",
                now, now);

        JsonExportAdapter adapter = new JsonExportAdapter();
        Path outputFile = tempDir.resolve("export.json");

        adapter.export(List.of(note), List.of(), List.of(),
                "Test", outputFile);

        assertTrue(Files.exists(outputFile));
        String json = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(json.contains("\"" + noteId + "\""));
        assertTrue(json.contains("\"Test Note\""));
        assertTrue(json.contains("\"Note body\""));
    }

    @Test
    @DisplayName("exports notes with links")
    void exportNotesWithLinks() throws IOException {
        UUID noteId1 = UUID.randomUUID();
        UUID noteId2 = UUID.randomUUID();
        Instant now = Instant.now();
        Note note1 = new Note(noteId1, "Note A", "", now,
                now);
        Note note2 = new Note(noteId2, "Note B", "", now,
                now);
        Link link = Link.create(noteId1, noteId2, "web");

        JsonExportAdapter adapter = new JsonExportAdapter();
        Path outputFile = tempDir.resolve("export.json");

        adapter.export(List.of(note1, note2), List.of(link),
                List.of(), "Test", outputFile);

        String json = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(json.contains("\"links\""));
        assertTrue(json.contains("\"" + noteId1 + "\""));
        assertTrue(json.contains("\"" + noteId2 + "\""));
        assertTrue(json.contains("\"web\""));
    }

    @Test
    @DisplayName("exports notes with stamps")
    void exportNotesWithStamps() throws IOException {
        Instant now = Instant.now();
        Note note = new Note(UUID.randomUUID(), "N", "", now,
                now);
        Stamp stamp = Stamp.create("Color Red",
                "$Color=red");

        JsonExportAdapter adapter = new JsonExportAdapter();
        Path outputFile = tempDir.resolve("export.json");

        adapter.export(List.of(note), List.of(),
                List.of(stamp), "Test", outputFile);

        String json = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(json.contains("\"stamps\""));
        assertTrue(json.contains("\"Color Red\""));
    }

    @Test
    @DisplayName("exports note with typed attributes")
    void exportNoteWithTypedAttributes() throws IOException {
        Instant now = Instant.now();
        Note note = new Note(UUID.randomUUID(), "N", "", now,
                now);
        note.setAttribute(Attributes.COLOR,
                new AttributeValue.ColorValue(
                        TbxColor.hex("#FF0000")));
        note.setAttribute(Attributes.CHECKED,
                new AttributeValue.BooleanValue(true));
        note.setAttribute(Attributes.OUTLINE_ORDER,
                new AttributeValue.NumberValue(3.0));

        JsonExportAdapter adapter = new JsonExportAdapter();
        Path outputFile = tempDir.resolve("export.json");

        adapter.export(List.of(note), List.of(), List.of(),
                "Test", outputFile);

        String json = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(json.contains("\"#FF0000\""));
        assertTrue(json.contains("true"));
    }

    @Test
    @DisplayName("produces valid JSON structure with top-level keys")
    void validJsonStructure() throws IOException {
        Instant now = Instant.now();
        Note note = new Note(UUID.randomUUID(), "N", "", now,
                now);

        JsonExportAdapter adapter = new JsonExportAdapter();
        Path outputFile = tempDir.resolve("export.json");

        adapter.export(List.of(note), List.of(), List.of(),
                "Test", outputFile);

        String json = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(json.contains("\"notes\""));
        assertTrue(json.contains("\"links\""));
        assertTrue(json.contains("\"stamps\""));
        assertTrue(json.startsWith("{"));
        assertTrue(json.trim().endsWith("}"));
    }
}
