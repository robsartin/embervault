package com.embervault.adapter.out.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Note;
import com.embervault.domain.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Saves and loads an entire project to/from a directory.
 *
 * <p>Coordinates the file-based repositories to persist all
 * notes, links, stamps, and project metadata.</p>
 */
public final class ProjectFileManager {

    private static final Logger LOG =
            LoggerFactory.getLogger(ProjectFileManager.class);

    private ProjectFileManager() { }

    /**
     * Saves the current in-memory state to the given directory.
     *
     * @param dir          the target directory
     * @param project      the project metadata
     * @param noteService  the note service with current data
     * @param linkService  the link service with current data
     * @param stampService the stamp service with current data
     * @param registry     the attribute schema registry
     */
    public static void save(Path dir, Project project,
            NoteService noteService, LinkService linkService,
            StampService stampService,
            AttributeSchemaRegistry registry) {
        try {
            Files.createDirectories(dir);

            // project.yaml
            String projectYaml = "id: \""
                    + project.getId() + "\"\n"
                    + "name: \"" + project.getName() + "\"\n"
                    + "rootNoteId: \""
                    + project.getRootNote().getId() + "\"\n";
            Files.writeString(dir.resolve("project.yaml"),
                    projectYaml, StandardCharsets.UTF_8);

            // Notes
            FileNoteRepository noteRepo =
                    new FileNoteRepository(dir, registry);
            for (Note note : noteService.getAllNotes()) {
                noteRepo.save(note);
            }

            // Links
            FileLinkRepository linkRepo =
                    new FileLinkRepository(dir);
            // Links are already loaded; save via the repo
            UUID rootId = project.getRootNote().getId();
            for (Note note : noteService.getAllNotes()) {
                for (var link : linkService
                        .getLinksFrom(note.getId())) {
                    linkRepo.save(link);
                }
            }

            // Stamps
            FileStampRepository stampRepo =
                    new FileStampRepository(dir);
            for (var stamp : stampService.getAllStamps()) {
                stampRepo.save(stamp);
            }

            LOG.info("Project saved to {}", dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Loads a project from the given directory into in-memory
     * services.
     *
     * @param dir          the source directory
     * @param noteService  the note service to populate
     * @param linkService  the link service to populate
     * @param stampService the stamp service to populate
     * @param registry     the attribute schema registry
     * @return the loaded project root note ID
     */
    public static UUID load(Path dir, NoteService noteService,
            LinkService linkService, StampService stampService,
            AttributeSchemaRegistry registry) {
        try {
            // Read project.yaml for root note ID
            String projectYaml = Files.readString(
                    dir.resolve("project.yaml"),
                    StandardCharsets.UTF_8);
            UUID rootNoteId = parseRootNoteId(projectYaml);

            // Load notes
            FileNoteRepository fileNotes =
                    new FileNoteRepository(dir, registry);
            for (Note note : fileNotes.findAll()) {
                noteService.updateNote(note.getId(),
                        note.getTitle(), note.getContent());
            }

            // Load links
            FileLinkRepository fileLinks =
                    new FileLinkRepository(dir);
            // Links loaded automatically by constructor

            // Load stamps
            FileStampRepository fileStamps =
                    new FileStampRepository(dir);
            for (var stamp : fileStamps.findAll()) {
                stampService.createStamp(
                        stamp.name(), stamp.action());
            }

            LOG.info("Project loaded from {}", dir);
            return rootNoteId;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static UUID parseRootNoteId(String yaml) {
        for (String line : yaml.split("\n")) {
            if (line.startsWith("rootNoteId:")) {
                String value = line.substring(
                        "rootNoteId:".length()).trim();
                value = value.replace("\"", "");
                return UUID.fromString(value);
            }
        }
        throw new IllegalArgumentException(
                "No rootNoteId in project.yaml");
    }
}
