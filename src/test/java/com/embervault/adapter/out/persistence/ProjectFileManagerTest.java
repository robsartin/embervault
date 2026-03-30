package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
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
}
