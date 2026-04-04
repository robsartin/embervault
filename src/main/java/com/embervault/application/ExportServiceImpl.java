package com.embervault.application;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.embervault.application.port.in.ExportProjectUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.StampService;
import com.embervault.application.port.out.ExportPort;
import com.embervault.domain.ExportFormat;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.Project;

/**
 * Application service implementing project export use cases.
 *
 * <p>Delegates to format-specific {@link ExportPort} implementations
 * based on the requested {@link ExportFormat}.</p>
 */
public final class ExportServiceImpl
        implements ExportProjectUseCase {

    private final Project project;
    private final GetNoteQuery noteQuery;
    private final LinkService linkService;
    private final StampService stampService;
    private final Map<ExportFormat, ExportPort> exportPorts;

    /**
     * Creates an ExportServiceImpl.
     *
     * @param project      the project
     * @param noteQuery    the note query service
     * @param linkService  the link service
     * @param stampService the stamp service
     * @param exportPorts  map of format to export port
     */
    public ExportServiceImpl(Project project,
            GetNoteQuery noteQuery,
            LinkService linkService,
            StampService stampService,
            Map<ExportFormat, ExportPort> exportPorts) {
        this.project = project;
        this.noteQuery = noteQuery;
        this.linkService = linkService;
        this.stampService = stampService;
        this.exportPorts = exportPorts;
    }

    @Override
    public void exportProject(ExportFormat format, Path output)
            throws IOException {
        ExportPort port = exportPorts.get(format);
        if (port == null) {
            throw new IllegalArgumentException(
                    "Unsupported export format: " + format);
        }

        List<Note> notes = noteQuery.getAllNotes();
        List<Link> links = collectAllLinks(notes);

        port.export(notes, links,
                stampService.getAllStamps(),
                project.getName(), output);
    }

    private List<Link> collectAllLinks(List<Note> notes) {
        Set<UUID> seen = new HashSet<>();
        List<Link> allLinks = new ArrayList<>();
        for (Note note : notes) {
            for (Link link
                    : linkService.getLinksFrom(note.getId())) {
                if (seen.add(link.id())) {
                    allLinks.add(link);
                }
            }
        }
        return allLinks;
    }
}
