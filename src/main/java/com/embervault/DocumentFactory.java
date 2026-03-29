package com.embervault;

import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
import com.embervault.application.DocumentContext;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.ProjectServiceImpl;
import com.embervault.application.StampServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.application.port.out.LinkRepository;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.application.port.out.StampRepository;
import com.embervault.domain.Project;

/**
 * Convenience factory for creating a fully wired document.
 *
 * <p>Lives in the composition-root package ({@code com.embervault}) because it
 * wires concrete adapter implementations to application-layer ports. Placing it
 * here keeps the {@code application} package free of adapter dependencies,
 * respecting the Dependency Inversion Principle.</p>
 *
 * <p>Encapsulates the wiring of repositories, services, and the project so
 * that a developer can create a ready-to-use document in a single call:</p>
 *
 * <pre>{@code
 * DocumentContext ctx = DocumentFactory.createEmpty();
 * Project project = ctx.project();
 * NoteService noteService = ctx.noteService();
 *
 * // Create child notes, apply stamps, create links, etc.
 * noteService.createChildNote(project.getRootNote().getId(), "My Note");
 * }</pre>
 *
 * <p>All services share the same underlying repositories, so changes made
 * through one service are immediately visible to all others.</p>
 */
public final class DocumentFactory {

    private DocumentFactory() {
        // utility class
    }

    /**
     * Creates a new empty document with all services wired and the root note
     * persisted.
     *
     * <p>The returned {@link DocumentContext} contains:</p>
     * <ul>
     *   <li>A {@link Project} with a root note</li>
     *   <li>A {@link NoteService} backed by an in-memory repository</li>
     *   <li>A {@link LinkService} backed by an in-memory repository</li>
     *   <li>A {@link StampService} backed by in-memory repositories</li>
     *   <li>The shared {@link NoteRepository} for direct access if needed</li>
     * </ul>
     *
     * @return a fully wired document context
     */
    public static DocumentContext createEmpty() {
        // Create repositories
        NoteRepository noteRepository = new InMemoryNoteRepository();
        LinkRepository linkRepository = new InMemoryLinkRepository();
        StampRepository stampRepository = new InMemoryStampRepository();

        // Create services
        NoteService noteService = new NoteServiceImpl(noteRepository);
        LinkService linkService = new LinkServiceImpl(linkRepository);
        StampService stampService = new StampServiceImpl(
                stampRepository, noteRepository);

        // Create project and persist root note
        Project project = new ProjectServiceImpl().createEmptyProject();
        noteRepository.save(project.getRootNote());

        return new DocumentContext(
                project, noteService, linkService,
                stampService, noteRepository);
    }
}
