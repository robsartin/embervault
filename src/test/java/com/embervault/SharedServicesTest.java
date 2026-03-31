package com.embervault;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.ProjectServiceImpl;
import com.embervault.application.StampServiceImpl;
import com.embervault.application.UndoRedoService;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SharedServices}.
 */
class SharedServicesTest {

    @Test
    @DisplayName("record holds all shared services")
    void sharedServices_shouldHoldAllServices() {
        InMemoryNoteRepository noteRepo = new InMemoryNoteRepository();
        NoteService noteService = new NoteServiceImpl(noteRepo);
        LinkService linkService =
                new LinkServiceImpl(new InMemoryLinkRepository());
        StampService stampService = new StampServiceImpl(
                new InMemoryStampRepository(), noteRepo);
        AttributeSchemaRegistry registry = new AttributeSchemaRegistry();
        Project project = new ProjectServiceImpl().createEmptyProject();

        UndoRedoService undoRedoService = new UndoRedoService();
        SharedServices services = new SharedServices(
                project, noteRepo, noteService, linkService,
                stampService, registry, undoRedoService);

        assertNotNull(services.project());
        assertSame(noteService, services.noteService());
        assertSame(linkService, services.linkService());
        assertSame(stampService, services.stampService());
        assertSame(registry, services.schemaRegistry());
    }
}
