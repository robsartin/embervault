package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetNoteQueryTest {

    private GetNoteQuery query;
    private CreateNoteUseCase creator;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        NoteServiceImpl service = new NoteServiceImpl(repository);
        query = service;
        creator = service;
    }

    @Test
    @DisplayName("NoteServiceImpl implements GetNoteQuery")
    void noteServiceImpl_shouldImplementGetNoteQuery() {
        assertTrue(query instanceof GetNoteQuery);
    }

    @Test
    @DisplayName("getNote() returns the note when it exists")
    void getNote_shouldReturnExistingNote() {
        Note created = creator.createNote("Title", "Content");

        Optional<Note> found = query.getNote(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created, found.get());
    }

    @Test
    @DisplayName("getNote() returns empty when note does not exist")
    void getNote_shouldReturnEmptyForMissing() {
        assertTrue(query.getNote(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("getAllNotes() returns all created notes")
    void getAllNotes_shouldReturnAll() {
        creator.createNote("A", "a");
        creator.createNote("B", "b");

        List<Note> all = query.getAllNotes();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("getChildren() returns children of a parent note")
    void getChildren_shouldReturnChildren() {
        Note parent = creator.createNote("Parent", "");
        creator.createChildNote(parent.getId(), "Child1");
        creator.createChildNote(parent.getId(), "Child2");

        List<Note> children = query.getChildren(parent.getId());

        assertEquals(2, children.size());
    }

    @Test
    @DisplayName("hasChildren() returns true when note has children")
    void hasChildren_shouldReturnTrue() {
        Note parent = creator.createNote("Parent", "");
        creator.createChildNote(parent.getId(), "Child");

        assertTrue(query.hasChildren(parent.getId()));
    }

    @Test
    @DisplayName("hasChildren() returns false when note has no children")
    void hasChildren_shouldReturnFalse() {
        Note note = creator.createNote("Lonely", "");

        assertFalse(query.hasChildren(note.getId()));
    }

    @Test
    @DisplayName("hasChildrenBatch() returns correct map")
    void hasChildrenBatch_shouldReturnCorrectMap() {
        Note parent = creator.createNote("Parent", "");
        Note childless = creator.createNote("Childless", "");
        creator.createChildNote(parent.getId(), "Child");

        Map<UUID, Boolean> result = query.hasChildrenBatch(
                List.of(parent.getId(), childless.getId()));

        assertTrue(result.get(parent.getId()));
        assertFalse(result.get(childless.getId()));
    }
}
