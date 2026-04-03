package com.embervault.application.port.in;

import java.io.IOException;
import java.nio.file.Path;

import com.embervault.domain.ExportFormat;

/**
 * Inbound port for exporting project data to various formats.
 */
public interface ExportProjectUseCase {

    /**
     * Exports the current project to the specified format.
     *
     * @param format the export format
     * @param output the output path (file for JSON/OPML, directory for Markdown)
     * @throws IOException if writing fails
     */
    void exportProject(ExportFormat format, Path output)
            throws IOException;
}
