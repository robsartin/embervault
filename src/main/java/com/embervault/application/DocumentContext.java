package com.embervault.application;

import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.application.port.out.NoteRepository;
import com.embervault.domain.Project;

/**
 * Holds a fully wired document with all services and the project.
 *
 * <p>Returned by {@link DocumentFactory#createEmpty()} so that a developer
 * can get a ready-to-use document without understanding the internal wiring
 * of the application. All services share the same underlying repositories.</p>
 *
 * @param project        the project containing the root note
 * @param noteService    the note service for CRUD operations on notes
 * @param linkService    the link service for creating/querying links
 * @param stampService   the stamp service for creating/applying stamps
 * @param noteRepository the note repository (shared by noteService and stampService)
 */
public record DocumentContext(
        Project project,
        NoteService noteService,
        LinkService linkService,
        StampService stampService,
        NoteRepository noteRepository) {
}
