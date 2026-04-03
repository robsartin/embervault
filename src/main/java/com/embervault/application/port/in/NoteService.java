package com.embervault.application.port.in;

/**
 * Composite inbound port aggregating all note-related use cases.
 *
 * <p>This interface exists as a convenience for consumers that need
 * access to multiple use cases. New code should prefer depending on
 * the narrowest use case interface (e.g., {@link CreateNoteUseCase},
 * {@link GetNoteQuery}) following the Interface Segregation Principle.</p>
 *
 * <p>Extends all individual use case and query interfaces so that
 * existing consumers that depend on {@code NoteService} continue
 * to work without changes.</p>
 */
public interface NoteService extends
        CreateNoteUseCase,
        GetNoteQuery,
        DeleteNoteUseCase,
        RenameNoteUseCase,
        MoveNoteUseCase,
        UpdateNoteUseCase,
        SearchNotesQuery,
        GetOutlineNavigationQuery {
}
