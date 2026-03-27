package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoteServiceImplTest {

    private NoteService service;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        service = new NoteServiceImpl(repository);
    }

    @Test
    @DisplayName("createNote() returns a persisted note")
    void createNote_shouldPersistAndReturn() {
        Note note = service.createNote("Title", "Content");

        assertNotNull(note);
        assertNotNull(note.getId());
        assertEquals("Title", note.getTitle());
        assertTrue(repository.findById(note.getId()).isPresent());
    }

    @Test
    @DisplayName("getNote() returns the note when it exists")
    void getNote_shouldReturnExistingNote() {
        Note created = service.createNote("Title", "Content");

        Optional<Note> found = service.getNote(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created, found.get());
    }

    @Test
    @DisplayName("getNote() returns empty when note does not exist")
    void getNote_shouldReturnEmptyForMissing() {
        Optional<Note> found = service.getNote(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("getAllNotes() returns all created notes")
    void getAllNotes_shouldReturnAll() {
        service.createNote("A", "a");
        service.createNote("B", "b");

        List<Note> all = service.getAllNotes();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("updateNote() updates title and content")
    void updateNote_shouldModifyNote() {
        Note created = service.createNote("Old", "Old");

        Note updated = service.updateNote(created.getId(), "New", "New");

        assertEquals("New", updated.getTitle());
        assertEquals("New", updated.getContent());
    }

    @Test
    @DisplayName("updateNote() throws when note does not exist")
    void updateNote_shouldThrowForMissing() {
        assertThrows(NoSuchElementException.class,
                () -> service.updateNote(UUID.randomUUID(), "T", "C"));
    }

    @Test
    @DisplayName("deleteNote() removes the note")
    void deleteNote_shouldRemoveNote() {
        Note created = service.createNote("Title", "Content");

        service.deleteNote(created.getId());

        assertTrue(service.getNote(created.getId()).isEmpty());
    }

    @Test
    @DisplayName("createChildNote() creates a child with $Container set to parent id")
    void createChildNote_shouldSetContainerToParentId() {
        Note parent = service.createNote("Parent", "");

        Note child = service.createChildNote(parent.getId(), "Child");

        assertNotNull(child);
        assertEquals("Child", child.getTitle());
        assertTrue(repository.findById(child.getId()).isPresent());

        // $Container on child should reference the parent
        String container = ((AttributeValue.StringValue)
                child.getAttribute("$Container").orElseThrow()).value();
        assertEquals(parent.getId().toString(), container);
    }

    @Test
    @DisplayName("createChildNote() sets $OutlineOrder sequentially")
    void createChildNote_shouldSetOutlineOrder() {
        Note parent = service.createNote("Parent", "");

        Note child1 = service.createChildNote(parent.getId(), "Child1");
        Note child2 = service.createChildNote(parent.getId(), "Child2");

        double order1 = ((AttributeValue.NumberValue)
                child1.getAttribute("$OutlineOrder").orElseThrow()).value();
        double order2 = ((AttributeValue.NumberValue)
                child2.getAttribute("$OutlineOrder").orElseThrow()).value();

        assertEquals(0.0, order1);
        assertEquals(1.0, order2);
    }

    @Test
    @DisplayName("createChildNote() does not modify parent note")
    void createChildNote_shouldNotModifyParent() {
        Note parent = service.createNote("Parent", "");
        int attrCountBefore = parent.getAttributes().size();

        service.createChildNote(parent.getId(), "Child");

        Note updatedParent = repository.findById(parent.getId()).orElseThrow();
        assertEquals(attrCountBefore, updatedParent.getAttributes().size());
    }

    @Test
    @DisplayName("createChildNote() sets random Xpos and Ypos")
    void createChildNote_shouldSetRandomPosition() {
        // Use a seeded random for deterministic test
        Random seeded = new Random(42L);
        NoteService seededService = new NoteServiceImpl(repository, seeded);

        Note parent = seededService.createNote("Parent", "");
        Note child = seededService.createChildNote(parent.getId(), "Child");

        Optional<AttributeValue> xpos = child.getAttribute("$Xpos");
        Optional<AttributeValue> ypos = child.getAttribute("$Ypos");

        assertTrue(xpos.isPresent());
        assertTrue(ypos.isPresent());

        double x = ((AttributeValue.NumberValue) xpos.get()).value();
        double y = ((AttributeValue.NumberValue) ypos.get()).value();
        assertTrue(x >= 0 && x <= 500, "Xpos should be 0-500, was " + x);
        assertTrue(y >= 0 && y <= 400, "Ypos should be 0-400, was " + y);
    }

    @Test
    @DisplayName("createChildNote() throws when parent does not exist")
    void createChildNote_shouldThrowForMissingParent() {
        assertThrows(NoSuchElementException.class,
                () -> service.createChildNote(UUID.randomUUID(), "Child"));
    }

    @Test
    @DisplayName("getChildren() returns children of a parent note")
    void getChildren_shouldReturnChildren() {
        Note parent = service.createNote("Parent", "");
        Note child1 = service.createChildNote(parent.getId(), "Child1");
        Note child2 = service.createChildNote(parent.getId(), "Child2");

        List<Note> children = service.getChildren(parent.getId());

        assertEquals(2, children.size());
        assertEquals(child1, children.get(0));
        assertEquals(child2, children.get(1));
    }

    @Test
    @DisplayName("getChildren() returns empty for note with no children")
    void getChildren_shouldReturnEmptyForNoChildren() {
        Note parent = service.createNote("Parent", "");

        List<Note> children = service.getChildren(parent.getId());

        assertTrue(children.isEmpty());
    }

    @Test
    @DisplayName("hasChildren() returns true when note has children")
    void hasChildren_shouldReturnTrueWhenHasChildren() {
        Note parent = service.createNote("Parent", "");
        service.createChildNote(parent.getId(), "Child");

        assertTrue(service.hasChildren(parent.getId()));
    }

    @Test
    @DisplayName("hasChildren() returns false when note has no children")
    void hasChildren_shouldReturnFalseWhenNoChildren() {
        Note note = service.createNote("Lonely", "");

        assertFalse(service.hasChildren(note.getId()));
    }

    @Test
    @DisplayName("moveNote() changes $Container to new parent")
    void moveNote_shouldChangeContainer() {
        Note parent1 = service.createNote("Parent1", "");
        Note parent2 = service.createNote("Parent2", "");
        Note child = service.createChildNote(parent1.getId(), "Child");

        Note moved = service.moveNote(child.getId(), parent2.getId());

        String container = ((AttributeValue.StringValue)
                moved.getAttribute("$Container").orElseThrow()).value();
        assertEquals(parent2.getId().toString(), container);

        // Old parent should have no children
        assertTrue(service.getChildren(parent1.getId()).isEmpty());
        // New parent should have the child
        assertEquals(1, service.getChildren(parent2.getId()).size());
    }

    @Test
    @DisplayName("moveNote() throws when note does not exist")
    void moveNote_shouldThrowForMissingNote() {
        Note parent = service.createNote("Parent", "");

        assertThrows(NoSuchElementException.class,
                () -> service.moveNote(UUID.randomUUID(), parent.getId()));
    }

    @Test
    @DisplayName("moveNote() throws when new parent does not exist")
    void moveNote_shouldThrowForMissingParent() {
        Note note = service.createNote("Note", "");

        assertThrows(NoSuchElementException.class,
                () -> service.moveNote(note.getId(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("renameNote() updates $Name attribute")
    void renameNote_shouldUpdateName() {
        Note note = service.createNote("Old Title", "Content");

        Note renamed = service.renameNote(note.getId(), "New Title");

        assertEquals("New Title", renamed.getTitle());
        assertEquals("Content", renamed.getContent());
    }

    @Test
    @DisplayName("renameNote() throws when note does not exist")
    void renameNote_shouldThrowForMissing() {
        assertThrows(NoSuchElementException.class,
                () -> service.renameNote(UUID.randomUUID(), "Title"));
    }

    @Test
    @DisplayName("renameNote() rejects empty title")
    void renameNote_shouldRejectEmptyTitle() {
        Note note = service.createNote("Title", "Content");

        assertThrows(IllegalArgumentException.class,
                () -> service.renameNote(note.getId(), ""));
    }

    @Test
    @DisplayName("renameNote() rejects blank title")
    void renameNote_shouldRejectBlankTitle() {
        Note note = service.createNote("Title", "Content");

        assertThrows(IllegalArgumentException.class,
                () -> service.renameNote(note.getId(), "   "));
    }
}
