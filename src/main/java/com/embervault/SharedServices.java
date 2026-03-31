package com.embervault;

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
import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Project;

/**
 * Holds the shared singleton services that are common across all windows.
 *
 * <p>All windows in a multi-window setup share the same services and project,
 * ensuring they operate on the same underlying data.</p>
 *
 * @param project         the current project with root note
 * @param noteRepository  the note repository
 * @param noteService     the note service
 * @param linkService     the link service
 * @param stampService    the stamp service
 * @param schemaRegistry    the attribute schema registry
 * @param undoRedoService   the undo/redo service
 */
public record SharedServices(
        Project project,
        NoteRepository noteRepository,
        NoteService noteService,
        LinkService linkService,
        StampService stampService,
        AttributeSchemaRegistry schemaRegistry,
        UndoRedoService undoRedoService
) {

    /**
     * Creates a new set of shared services with in-memory repositories.
     *
     * @return a fully wired SharedServices instance
     */
    public static SharedServices create() {
        Project project =
                new ProjectServiceImpl().createEmptyProject();
        InMemoryNoteRepository noteRepo = new InMemoryNoteRepository();
        NoteService noteService = new NoteServiceImpl(noteRepo);
        LinkService linkService =
                new LinkServiceImpl(new InMemoryLinkRepository());
        StampService stampService = new StampServiceImpl(
                new InMemoryStampRepository(), noteRepo);
        noteRepo.save(project.getRootNote());
        AttributeSchemaRegistry schemaRegistry =
                new AttributeSchemaRegistry();
        UndoRedoService undoRedoService = new UndoRedoService();
        return new SharedServices(project, noteRepo,
                noteService, linkService,
                stampService, schemaRegistry,
                undoRedoService);
    }
}
