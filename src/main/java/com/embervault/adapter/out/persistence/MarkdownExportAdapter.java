package com.embervault.adapter.out.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.embervault.application.port.out.ExportPort;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.Stamp;

/**
 * Exports notes as individual Markdown files with YAML frontmatter.
 *
 * <p>Each note becomes a separate {@code .md} file in the output
 * directory, using the same serialization format as
 * {@link NoteFileSerializer} for consistency with the project's
 * native file format.</p>
 */
public final class MarkdownExportAdapter implements ExportPort {

    private final NoteFileSerializer serializer =
            new NoteFileSerializer();

    @Override
    public void export(List<Note> notes, List<Link> links,
            List<Stamp> stamps, String projectName,
            Path output) throws IOException {
        Files.createDirectories(output);
        for (Note note : notes) {
            String filename = sanitizeFilename(
                    note.getTitle()) + ".md";
            Path file = output.resolve(filename);
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
