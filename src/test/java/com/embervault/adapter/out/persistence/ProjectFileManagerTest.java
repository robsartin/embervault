package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.ProjectServiceImpl;
import com.embervault.application.StampServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link ProjectFileManager}.
 */
class ProjectFileManagerTest {

    @Test
    @DisplayName("save sets root note name to directory name")
    void save_setsRootNoteNameToDirectoryName(
            @TempDir Path tmp) {
        Path projectDir = tmp.resolve("My Project");
        InMemoryNoteRepository noteRepo =
                new InMemoryNoteRepository();
        NoteService noteService =
                new NoteServiceImpl(noteRepo);
        LinkService linkService = new LinkServiceImpl(
                new InMemoryLinkRepository());
        StampService stampService = new StampServiceImpl(
                new InMemoryStampRepository(), noteRepo);
        Project project =
                new ProjectServiceImpl().createEmptyProject();
        noteRepo.save(project.getRootNote());
        AttributeSchemaRegistry registry =
                new AttributeSchemaRegistry();

        ProjectFileManager.save(projectDir, project,
                noteService, linkService, stampService,
                registry);

        assertEquals("My Project",
                project.getRootNote().getTitle());
    }

    @Test
    @DisplayName("save stores stamps in root note, "
            + "not stamps.yaml")
    void save_storesStampsInRootNote(@TempDir Path tmp) {
        Path projectDir = tmp.resolve("TestProject");
        InMemoryNoteRepository noteRepo =
                new InMemoryNoteRepository();
        NoteService noteService =
                new NoteServiceImpl(noteRepo);
        LinkService linkService = new LinkServiceImpl(
                new InMemoryLinkRepository());
        StampService stampService = new StampServiceImpl(
                new InMemoryStampRepository(), noteRepo);
        Project project =
                new ProjectServiceImpl().createEmptyProject();
        noteRepo.save(project.getRootNote());
        AttributeSchemaRegistry registry =
                new AttributeSchemaRegistry();

        stampService.createStamp("Color:red", "$Color=red");
        stampService.createStamp("Mark Done",
                "$Checked=true");

        ProjectFileManager.save(projectDir, project,
                noteService, linkService, stampService,
                registry);

        // stamps.yaml should NOT exist
        assertFalse(Files.exists(
                projectDir.resolve("stamps.yaml")),
                "stamps.yaml should not exist");

        // Root note should contain stamps in its file
        Path rootFile = projectDir.resolve("notes")
                .resolve(project.getRootNote().getId()
                        .toString().substring(0, 8))
                .resolve(project.getRootNote().getId()
                        + ".md");
        assertTrue(Files.exists(rootFile),
                "Root note file should exist");
    }
}
