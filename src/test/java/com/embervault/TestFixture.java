package com.embervault;

import java.util.UUID;

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
import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.Note;
import com.embervault.domain.Project;

/**
 * Shared test fixture for creating commonly needed service stacks.
 *
 * <p>Reduces boilerplate across tests that need a {@link NoteService},
 * {@link LinkService}, or {@link StampService} backed by in-memory
 * repositories.</p>
 */
public final class TestFixture {

    private TestFixture() {
        // utility class
    }

    /**
     * Creates a {@link NoteService} backed by a fresh in-memory repository.
     *
     * @return a ready-to-use NoteService
     */
    public static NoteService createNoteService() {
        return new NoteServiceImpl(new InMemoryNoteRepository());
    }

    /**
     * Creates a root note with the given child titles and returns the root
     * note's id.
     *
     * <p>This is a convenience for the common pattern of creating a parent
     * note and populating it with named children for testing hierarchy
     * operations.</p>
     *
     * @param svc    the note service to use
     * @param titles the titles for each child note
     * @return the id of the root note
     */
    public static UUID createRootWithChildren(NoteService svc,
            String... titles) {
        Note root = svc.createNote("Root", "");
        for (String title : titles) {
            svc.createChildNote(root.getId(), title);
        }
        return root.getId();
    }

    /**
     * Creates a fully wired {@link DocumentContext} identical to
     * {@link com.embervault.application.DocumentFactory#createEmpty()} but
     * returned here for convenience so tests only need one import.
     *
     * @return a fully wired document context
     */
    public static DocumentContext createDocumentContext() {
        NoteRepository noteRepository = new InMemoryNoteRepository();
        InMemoryLinkRepository linkRepository = new InMemoryLinkRepository();
        InMemoryStampRepository stampRepository = new InMemoryStampRepository();

        NoteService noteService = new NoteServiceImpl(noteRepository);
        LinkService linkService = new LinkServiceImpl(linkRepository);
        StampService stampService = new StampServiceImpl(
                stampRepository, noteRepository);

        Project project = new ProjectServiceImpl().createEmptyProject();
        noteRepository.save(project.getRootNote());

        return new DocumentContext(
                project, noteService, linkService,
                stampService, noteRepository);
    }
}
