package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link MarkdownExportAdapter}.
 */
class MarkdownExportAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("exports single note as .md file with YAML frontmatter")
    void exportSingleNote() throws IOException {
        UUID noteId = UUID.randomUUID();
        Instant now = Instant.parse("2025-06-15T10:30:00Z");
        Note note = new Note(noteId, "My Note",
                "Hello **world**", now, now);

        MarkdownExportAdapter adapter =
                new MarkdownExportAdapter();
        adapter.export(List.of(note), tempDir);

        Path expectedFile = tempDir.resolve("My Note.md");
        assertTrue(Files.exists(expectedFile));

        String content = Files.readString(expectedFile,
                StandardCharsets.UTF_8);
        assertTrue(content.startsWith("---\n"));
        assertTrue(content.contains(
                "id: \"" + noteId + "\""));
        assertTrue(content.contains("Hello **world**"));
    }

    @Test
    @DisplayName("exports multiple notes as separate .md files")
    void exportMultipleNotes() throws IOException {
        Instant now = Instant.now();
        Note note1 = new Note(UUID.randomUUID(), "Alpha", "",
                now, now);
        Note note2 = new Note(UUID.randomUUID(), "Beta",
                "Content", now, now);

        MarkdownExportAdapter adapter =
                new MarkdownExportAdapter();
        adapter.export(List.of(note1, note2), tempDir);

        assertTrue(Files.exists(tempDir.resolve("Alpha.md")));
        assertTrue(Files.exists(tempDir.resolve("Beta.md")));
    }

    @Test
    @DisplayName("note with color attribute includes it in frontmatter")
    void exportNoteWithColorAttribute() throws IOException {
        Instant now = Instant.now();
        Note note = new Note(UUID.randomUUID(), "Colored", "",
                now, now);
        note.setAttribute(Attributes.COLOR,
                new AttributeValue.ColorValue(
                        TbxColor.hex("#FF0000")));

        MarkdownExportAdapter adapter =
                new MarkdownExportAdapter();
        adapter.export(List.of(note), tempDir);

        String content = Files.readString(
                tempDir.resolve("Colored.md"),
                StandardCharsets.UTF_8);
        assertTrue(content.contains(
                Attributes.COLOR + ": \"#FF0000\""));
    }

    @Test
    @DisplayName("sanitizes filename for notes with special characters")
    void exportNoteWithSpecialCharsInName() throws IOException {
        Instant now = Instant.now();
        Note note = new Note(UUID.randomUUID(),
                "Notes/Ideas: 2025", "body", now, now);

        MarkdownExportAdapter adapter =
                new MarkdownExportAdapter();
        adapter.export(List.of(note), tempDir);

        // File should exist with sanitized name
        long mdCount;
        try (var files = Files.list(tempDir)) {
            mdCount = files
                    .filter(p -> p.toString().endsWith(".md"))
                    .count();
        }
        assertEquals(1, mdCount,
                "Should produce exactly one .md file");
    }

    @Test
    @DisplayName("uses NoteFileSerializer format for consistency")
    void usesNoteFileSerializerFormat() throws IOException {
        Instant now = Instant.parse("2025-06-15T10:30:00Z");
        Note note = new Note(UUID.randomUUID(),
                "Consistent", "Body text", now, now);

        MarkdownExportAdapter adapter =
                new MarkdownExportAdapter();
        adapter.export(List.of(note), tempDir);

        String content = Files.readString(
                tempDir.resolve("Consistent.md"),
                StandardCharsets.UTF_8);

        // Should match NoteFileSerializer output format
        NoteFileSerializer serializer =
                new NoteFileSerializer();
        String expected = serializer.serialize(note);
        assertEquals(expected, content);
    }
}
