package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.embervault.domain.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link OpmlExportAdapter}.
 */
class OpmlExportAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("exports single note as valid OPML document")
    void exportSingleNote() throws IOException {
        Instant now = Instant.parse("2025-06-15T10:30:00Z");
        Note note = new Note(UUID.randomUUID(), "My Note",
                "Note body", now, now);

        OpmlExportAdapter adapter = new OpmlExportAdapter();
        Path outputFile = tempDir.resolve("export.opml");

        adapter.export(List.of(note), "Test Project",
                outputFile);

        assertTrue(Files.exists(outputFile));
        String opml = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(opml.contains("<?xml version="));
        assertTrue(opml.contains("<opml version=\"2.0\">"));
        assertTrue(opml.contains("<head>"));
        assertTrue(opml.contains("<title>Test Project</title>"));
        assertTrue(opml.contains("<body>"));
        assertTrue(opml.contains("text=\"My Note\""));
        assertTrue(opml.contains("</opml>"));
    }

    @Test
    @DisplayName("exports multiple notes as outline elements")
    void exportMultipleNotes() throws IOException {
        Instant now = Instant.now();
        Note note1 = new Note(UUID.randomUUID(), "Alpha", "",
                now, now);
        Note note2 = new Note(UUID.randomUUID(), "Beta",
                "Content", now, now);

        OpmlExportAdapter adapter = new OpmlExportAdapter();
        Path outputFile = tempDir.resolve("export.opml");

        adapter.export(List.of(note1, note2), "Project",
                outputFile);

        String opml = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(opml.contains("text=\"Alpha\""));
        assertTrue(opml.contains("text=\"Beta\""));
    }

    @Test
    @DisplayName("note text is included as _note attribute")
    void exportNoteTextAsNoteAttribute() throws IOException {
        Instant now = Instant.now();
        Note note = new Note(UUID.randomUUID(), "Title",
                "Some body text", now, now);

        OpmlExportAdapter adapter = new OpmlExportAdapter();
        Path outputFile = tempDir.resolve("export.opml");

        adapter.export(List.of(note), "Project",
                outputFile);

        String opml = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(opml.contains(
                "_note=\"Some body text\""));
    }

    @Test
    @DisplayName("escapes XML special characters in note title")
    void escapesXmlSpecialChars() throws IOException {
        Instant now = Instant.now();
        Note note = new Note(UUID.randomUUID(),
                "Notes & Ideas <2025>", "", now, now);

        OpmlExportAdapter adapter = new OpmlExportAdapter();
        Path outputFile = tempDir.resolve("export.opml");

        adapter.export(List.of(note), "Project",
                outputFile);

        String opml = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(opml.contains(
                "text=\"Notes &amp; Ideas &lt;2025&gt;\""));
    }

    @Test
    @DisplayName("note with created date includes it as attribute")
    void exportNoteWithCreatedDate() throws IOException {
        Instant now = Instant.parse("2025-06-15T10:30:00Z");
        Note note = new Note(UUID.randomUUID(), "Dated", "",
                now, now);

        OpmlExportAdapter adapter = new OpmlExportAdapter();
        Path outputFile = tempDir.resolve("export.opml");

        adapter.export(List.of(note), "Project",
                outputFile);

        String opml = Files.readString(outputFile,
                StandardCharsets.UTF_8);
        assertTrue(opml.contains("created="));
    }
}
