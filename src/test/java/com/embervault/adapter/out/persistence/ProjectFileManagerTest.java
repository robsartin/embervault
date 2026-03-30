package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

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

        // Root note file should exist (in base dir)
        assertTrue(Files.exists(
                projectDir.resolve("TestProject.md")),
                "Root note file should exist in base dir");
    }

    @Test
    @DisplayName("save creates root note in base dir, "
            + "not notes/")
    void save_rootNoteInBaseDir(@TempDir Path tmp) {
        Path projectDir = tmp.resolve("MyProject");
        var ctx = createContext();
        ctx.noteRepo.save(ctx.project.getRootNote());

        ProjectFileManager.save(projectDir, ctx.project,
                ctx.noteService, ctx.linkService,
                ctx.stampService, ctx.registry);

        // Root note in base directory
        assertTrue(Files.exists(
                projectDir.resolve("MyProject.md")),
                "Root note should be in base directory");

        // Root note should NOT be under notes/
        String shard = ctx.project.getRootNote().getId()
                .toString().substring(0, 8);
        assertFalse(Files.exists(
                projectDir.resolve("notes").resolve(shard)
                        .resolve(ctx.project.getRootNote()
                                .getId() + ".md")),
                "Root note should NOT be under notes/");
    }

    @Test
    @DisplayName("save does not create project.yaml")
    void save_noProjectYaml(@TempDir Path tmp) {
        Path projectDir = tmp.resolve("NoYaml");
        var ctx = createContext();
        ctx.noteRepo.save(ctx.project.getRootNote());

        ProjectFileManager.save(projectDir, ctx.project,
                ctx.noteService, ctx.linkService,
                ctx.stampService, ctx.registry);

        assertFalse(Files.exists(
                projectDir.resolve("project.yaml")),
                "project.yaml should not exist");
    }

    @Test
    @DisplayName("round-trip save then load preserves data")
    void roundTrip_saveThenLoad(@TempDir Path tmp) {
        Path projectDir = tmp.resolve("RoundTrip");
        var ctx = createContext();
        ctx.noteRepo.save(ctx.project.getRootNote());
        ctx.noteService.createChildNote(
                ctx.project.getRootNote().getId(), "Child A");
        ctx.noteService.createChildNote(
                ctx.project.getRootNote().getId(), "Child B");
        ctx.stampService.createStamp("Color:red",
                "$Color=red");

        ProjectFileManager.save(projectDir, ctx.project,
                ctx.noteService, ctx.linkService,
                ctx.stampService, ctx.registry);

        // Load into fresh services
        var ctx2 = createContext();
        ctx2.noteRepo.save(ctx2.project.getRootNote());
        UUID rootId = ProjectFileManager.load(projectDir,
                ctx2.noteService, ctx2.linkService,
                ctx2.stampService, ctx2.registry);

        // Root note loaded
        assertTrue(ctx2.noteService.getNote(rootId)
                        .isPresent(),
                "Root note should be loaded");
        assertEquals("RoundTrip",
                ctx2.noteService.getNote(rootId).get()
                        .getTitle());

        // Children loaded
        assertEquals(2,
                ctx2.noteService.getChildren(rootId).size(),
                "Should have 2 children");

        // Stamps loaded
        assertFalse(ctx2.stampService.getAllStamps().isEmpty(),
                "Stamps should be loaded");
    }

    @Test
    @DisplayName("load reads root note from base dir .md file")
    void load_readsRootFromBaseDir(@TempDir Path tmp) {
        Path projectDir = tmp.resolve("LoadTest");
        var ctx = createContext();
        ctx.noteRepo.save(ctx.project.getRootNote());

        ProjectFileManager.save(projectDir, ctx.project,
                ctx.noteService, ctx.linkService,
                ctx.stampService, ctx.registry);

        var ctx2 = createContext();
        ctx2.noteRepo.save(ctx2.project.getRootNote());
        UUID rootId = ProjectFileManager.load(projectDir,
                ctx2.noteService, ctx2.linkService,
                ctx2.stampService, ctx2.registry);

        assertTrue(ctx2.noteService.getNote(rootId)
                        .isPresent(),
                "Should load root note from base dir");
    }

    private TestContext createContext() {
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
        AttributeSchemaRegistry registry =
                new AttributeSchemaRegistry();
        return new TestContext(noteRepo, noteService,
                linkService, stampService, project, registry);
    }

    private record TestContext(
            InMemoryNoteRepository noteRepo,
            NoteService noteService,
            LinkService linkService,
            StampService stampService,
            Project project,
            AttributeSchemaRegistry registry) { }
}
