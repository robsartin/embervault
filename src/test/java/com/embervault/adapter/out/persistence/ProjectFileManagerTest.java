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
import com.embervault.domain.Note;
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

        // Load into fresh services with fresh repo
        InMemoryNoteRepository loadRepo =
                new InMemoryNoteRepository();
        NoteService loadNoteService =
                new NoteServiceImpl(loadRepo);
        StampService loadStampService =
                new StampServiceImpl(
                        new InMemoryStampRepository(),
                        loadRepo);
        LinkService loadLinkService = new LinkServiceImpl(
                new InMemoryLinkRepository());

        UUID rootId = ProjectFileManager.load(projectDir,
                loadRepo, loadNoteService, loadLinkService,
                loadStampService, ctx.registry);

        // Root note loaded
        assertTrue(loadNoteService.getNote(rootId)
                        .isPresent(),
                "Root note should be loaded");
        assertEquals("RoundTrip",
                loadNoteService.getNote(rootId).get()
                        .getTitle());

        // Children loaded
        assertEquals(2,
                loadNoteService.getChildren(rootId).size(),
                "Should have 2 children");

        // Stamps loaded
        assertFalse(
                loadStampService.getAllStamps().isEmpty(),
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

        InMemoryNoteRepository loadRepo =
                new InMemoryNoteRepository();
        NoteService loadNoteService =
                new NoteServiceImpl(loadRepo);
        UUID rootId = ProjectFileManager.load(projectDir,
                loadRepo, loadNoteService, ctx.linkService,
                ctx.stampService, ctx.registry);

        assertTrue(loadNoteService.getNote(rootId)
                        .isPresent(),
                "Should load root note from base dir");
    }

    @Test
    @DisplayName("round-trip preserves grandchildren")
    void roundTrip_preservesGrandchildren(@TempDir Path tmp) {
        Path projectDir = tmp.resolve("Deep");
        var ctx = createContext();
        ctx.noteRepo.save(ctx.project.getRootNote());
        UUID rootId = ctx.project.getRootNote().getId();
        Note child = ctx.noteService.createChildNote(
                rootId, "Child");
        ctx.noteService.createChildNote(
                child.getId(), "Grandchild A");
        ctx.noteService.createChildNote(
                child.getId(), "Grandchild B");

        ProjectFileManager.save(projectDir, ctx.project,
                ctx.noteService, ctx.linkService,
                ctx.stampService, ctx.registry);

        InMemoryNoteRepository loadRepo =
                new InMemoryNoteRepository();
        NoteService loadNoteService =
                new NoteServiceImpl(loadRepo);
        UUID loadedRoot = ProjectFileManager.load(projectDir,
                loadRepo, loadNoteService,
                new LinkServiceImpl(
                        new InMemoryLinkRepository()),
                new StampServiceImpl(
                        new InMemoryStampRepository(),
                        loadRepo),
                ctx.registry);

        // Child loaded
        assertEquals(1,
                loadNoteService.getChildren(loadedRoot)
                        .size(),
                "Root should have 1 child");
        Note loadedChild =
                loadNoteService.getChildren(loadedRoot)
                        .get(0);
        assertEquals("Child", loadedChild.getTitle());

        // Grandchildren loaded with correct container
        var grandchildren = loadNoteService.getChildren(
                loadedChild.getId());
        assertEquals(2, grandchildren.size(),
                "Child should have 2 grandchildren");
        // Verify container attribute is set
        for (Note gc : grandchildren) {
            var container = gc.getAttribute(
                    com.embervault.domain.Attributes
                            .CONTAINER);
            assertTrue(container.isPresent(),
                    "Grandchild should have $Container");
            assertEquals(loadedChild.getId().toString(),
                    ((com.embervault.domain.AttributeValue
                            .StringValue) container.get())
                            .value(),
                    "Grandchild $Container should be "
                            + "child's ID");
        }
    }

    @Test
    @DisplayName("saved grandchild files contain $Container")
    void save_grandchildFileContainsContainer(
            @TempDir Path tmp) throws Exception {
        Path projectDir = tmp.resolve("ContainerTest");
        var ctx = createContext();
        ctx.noteRepo.save(ctx.project.getRootNote());
        UUID rootId = ctx.project.getRootNote().getId();
        Note child = ctx.noteService.createChildNote(
                rootId, "Child");
        Note gc = ctx.noteService.createChildNote(
                child.getId(), "GC");

        ProjectFileManager.save(projectDir, ctx.project,
                ctx.noteService, ctx.linkService,
                ctx.stampService, ctx.registry);

        // Read the grandchild file and verify $Container
        String shard = gc.getId().toString().substring(0, 8);
        Path gcFile = projectDir.resolve("notes")
                .resolve(shard)
                .resolve(gc.getId() + ".md");
        assertTrue(Files.exists(gcFile),
                "Grandchild file should exist: " + gcFile);
        String content = Files.readString(gcFile);
        assertTrue(content.contains("$Container"),
                "File should contain $Container:\n"
                        + content);
        assertTrue(content.contains(
                child.getId().toString()),
                "File $Container should reference child ID");
    }

    @Test
    @DisplayName("loaded notes have correct $Container chain")
    void load_notesHaveContainerChain(@TempDir Path tmp) {
        Path projectDir = tmp.resolve("Chain");
        var ctx = createContext();
        ctx.noteRepo.save(ctx.project.getRootNote());
        UUID rootId = ctx.project.getRootNote().getId();
        Note child = ctx.noteService.createChildNote(
                rootId, "Child");
        Note gc = ctx.noteService.createChildNote(
                child.getId(), "GC");

        ProjectFileManager.save(projectDir, ctx.project,
                ctx.noteService, ctx.linkService,
                ctx.stampService, ctx.registry);

        // Load
        InMemoryNoteRepository loadRepo =
                new InMemoryNoteRepository();
        NoteService loadSvc = new NoteServiceImpl(loadRepo);
        UUID loadedRoot = ProjectFileManager.load(projectDir,
                loadRepo, loadSvc,
                new LinkServiceImpl(
                        new InMemoryLinkRepository()),
                new StampServiceImpl(
                        new InMemoryStampRepository(),
                        loadRepo),
                ctx.registry);

        // Verify all notes exist
        assertEquals(3, loadRepo.findAll().size(),
                "Should have root + child + grandchild");

        // Verify chain: root → child → gc
        var children = loadSvc.getChildren(loadedRoot);
        assertEquals(1, children.size(),
                "Root should have 1 child");
        assertEquals("Child", children.get(0).getTitle());

        var gcs = loadSvc.getChildren(
                children.get(0).getId());
        assertEquals(1, gcs.size(),
                "Child should have 1 grandchild");
        assertEquals("GC", gcs.get(0).getTitle());
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
