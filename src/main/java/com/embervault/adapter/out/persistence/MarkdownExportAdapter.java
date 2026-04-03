package com.embervault.adapter.out.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.embervault.domain.Note;

/**
 * Exports notes as individual Markdown files with YAML frontmatter.
 *
 * <p>Each note becomes a separate {@code .md} file in the output
 * directory, using the same serialization format as
 * {@link NoteFileSerializer} for consistency with the project's
 * native file format.</p>
 */
public final class MarkdownExportAdapter {

    private final NoteFileSerializer serializer =
            new NoteFileSerializer();

    /**
     * Exports the given notes to individual Markdown files.
     *
     * @param notes     the notes to export
     * @param outputDir the output directory
     * @throws IOException if writing fails
     */
    public void export(List<Note> notes, Path outputDir)
            throws IOException {
        Files.createDirectories(outputDir);
        for (Note note : notes) {
            String filename = sanitizeFilename(
                    note.getTitle()) + ".md";
            Path file = outputDir.resolve(filename);
            String content = serializer.serialize(note);
            Files.writeString(file, content,
                    StandardCharsets.UTF_8);
        }
    }

    private String sanitizeFilename(String name) {
        // Replace characters not allowed in filenames
        return name.replaceAll("[/\\\\:*?\"<>|]", "_");
    }
}
