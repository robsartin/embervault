package com.embervault.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.Note;

/**
 * Inbound port defining the note management use cases.
 */
public interface NoteService {

    /**
     * Creates a new note with the given title and content.
     */
    Note createNote(String title, String content);

    /**
     * Retrieves a note by its id.
     */
    Optional<Note> getNote(UUID id);

    /**
     * Retrieves all notes.
     */
    List<Note> getAllNotes();

    /**
     * Updates an existing note's title and content.
     */
    Note updateNote(UUID id, String title, String content);

    /**
     * Creates a new child note under the given parent.
     *
     * @param parentId the parent note id
     * @param title    the title for the new note
     * @return the newly created child note
     */
    Note createChildNote(UUID parentId, String title);

    /**
     * Returns the children of the note with the given id, ordered by $OutlineOrder.
     *
     * @param parentId the parent note id
     * @return the list of child notes
     */
    List<Note> getChildren(UUID parentId);

    /**
     * Returns whether the note with the given id has any children.
     *
     * @param noteId the note id
     * @return true if the note has children
     */
    boolean hasChildren(UUID noteId);

    /**
     * Moves a note to a new parent by changing its $Container attribute.
     *
     * @param noteId      the note to move
     * @param newParentId the new parent note id
     * @return the updated note
     */
    Note moveNote(UUID noteId, UUID newParentId);

    /**
     * Renames the note with the given id.
     *
     * @param noteId   the note id
     * @param newTitle the new title (must not be blank)
     * @return the updated note
     */
    Note renameNote(UUID noteId, String newTitle);

    /**
     * Creates a new sibling note directly after the given sibling in outline order.
     *
     * <p>The new note will have the same {@code $Container} as the sibling.
     * Subsequent siblings have their {@code $OutlineOrder} incremented to make room.</p>
     *
     * @param siblingId the id of the note to create a sibling after
     * @param title     the title for the new note (may be empty for rapid entry)
     * @return the newly created sibling note
     */
    Note createSiblingNote(UUID siblingId, String title);

    /**
     * Indents a note by making it a child of the note directly above it in outline order.
     *
     * <p>If the note is the first child (no note above), it is returned unchanged.</p>
     *
     * @param noteId the id of the note to indent
     * @return the updated note
     */
    Note indentNote(UUID noteId);

    /**
     * Outdents a note by moving it to be a sibling of its current parent.
     *
     * <p>The note is placed just after its old parent in the grandparent's children.
     * If the note has no parent or the parent has no grandparent, the note is returned
     * unchanged.</p>
     *
     * @param noteId the id of the note to outdent
     * @return the updated note
     */
    Note outdentNote(UUID noteId);

    /**
     * Deletes the note with the given id.
     */
    void deleteNote(UUID id);

    /**
     * Returns the previous note in outline order for the given note.
     *
     * <p>The previous note is the sibling with the next-lower {@code $OutlineOrder}
     * within the same {@code $Container}. If no previous sibling exists, the parent
     * note (the {@code $Container} note) is returned. If there is no parent,
     * {@code Optional.empty()} is returned.</p>
     *
     * @param noteId the note id
     * @return the previous note, or empty if none
     */
    Optional<Note> getPreviousInOutline(UUID noteId);

    /**
     * Deletes the note only if it is a leaf (has no children).
     *
     * @param noteId the note id
     * @return true if the note was deleted, false if it has children or does not exist
     */
    boolean deleteNoteIfLeaf(UUID noteId);

    /**
     * Searches all notes by case-insensitive substring matching on $Name and $Text.
     *
     * <p>Returns notes whose title or content contain the query string.
     * Title matches appear before text-only matches. Returns an empty list
     * if the query is null, empty, or blank.</p>
     *
     * @param query the search query
     * @return matching notes ordered by relevance (title matches first)
     */
    List<Note> searchNotes(String query);
}
