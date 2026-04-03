package com.embervault.application.port.out;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.Stamp;

/**
 * Outbound port for exporting project data to a specific format.
 *
 * <p>Each export format (JSON, Markdown, OPML) provides its own
 * implementation of this port.</p>
 */
public interface ExportPort {

    /**
     * Exports the given project data to the specified output path.
     *
     * @param notes       the notes to export
     * @param links       the links to export
     * @param stamps      the stamps to export
     * @param projectName the project name
     * @param output      the output path
     * @throws IOException if writing fails
     */
    void export(List<Note> notes, List<Link> links,
            List<Stamp> stamps, String projectName,
            Path output) throws IOException;
}
