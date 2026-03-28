package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryNoteRepositoryTest {

    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
    }

    @Test
    @DisplayName("save() stores a note that can be retrieved by id")
    void save_shouldStoreNote() {
        Note note = Note.create("Title", "Content");

        repository.save(note);

        Optional<Note> found = repository.findById(note.getId());
        assertTrue(found.isPresent());
        assertEquals(note, found.get());
    }

    @Test
    @DisplayName("findById() returns empty for unknown id")
    void findById_shouldReturnEmptyForUnknownId() {
        Optional<Note> found = repository.findById(UUID.randomUUID());

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("findAll() returns all saved notes")
    void findAll_shouldReturnAllNotes() {
        Note noteA = Note.create("A", "a");
        Note noteB = Note.create("B", "b");
        repository.save(noteA);
        repository.save(noteB);

        List<Note> all = repository.findAll();

        assertEquals(2, all.size());
        assertTrue(all.contains(noteA));
        assertTrue(all.contains(noteB));
    }

    @Test
    @DisplayName("findAll() returns empty list when no notes exist")
    void findAll_shouldReturnEmptyListWhenEmpty() {
        List<Note> all = repository.findAll();

        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("delete() removes a note by id")
    void delete_shouldRemoveNote() {
        Note note = Note.create("Title", "Content");
        repository.save(note);

        repository.delete(note.getId());

        assertFalse(repository.findById(note.getId()).isPresent());
    }

    @Test
    @DisplayName("delete() is safe for unknown ids")
    void delete_shouldBeSafeForUnknownId() {
        repository.delete(UUID.randomUUID());
        // no exception expected
    }

    @Test
    @DisplayName("save() replaces an existing note with the same id")
    void save_shouldReplaceExistingNote() {
        Note note = Note.create("Old", "Old content");
        repository.save(note);

        note.update("New", "New content");
        repository.save(note);

        Note found = repository.findById(note.getId()).orElseThrow();
        assertEquals("New", found.getTitle());
        assertEquals("New content", found.getContent());
    }

    @Test
    @DisplayName("findChildren() returns notes whose $Container matches parent id")
    void findChildren_shouldReturnByContainerAttribute() {
        Note parent = Note.create("Parent", "");
        Note child1 = Note.create("Child1", "");
        Note child2 = Note.create("Child2", "");

        child1.setAttribute("$Container",
                new AttributeValue.StringValue(parent.getId().toString()));
        child1.setAttribute("$OutlineOrder", new AttributeValue.NumberValue(0));
        child2.setAttribute("$Container",
                new AttributeValue.StringValue(parent.getId().toString()));
        child2.setAttribute("$OutlineOrder", new AttributeValue.NumberValue(1));

        repository.save(parent);
        repository.save(child1);
        repository.save(child2);

        List<Note> children = repository.findChildren(parent.getId());

        assertEquals(2, children.size());
        assertEquals(child1, children.get(0));
        assertEquals(child2, children.get(1));
    }

    @Test
    @DisplayName("findChildren() returns children sorted by $OutlineOrder")
    void findChildren_shouldSortByOutlineOrder() {
        Note parent = Note.create("Parent", "");
        Note childA = Note.create("A", "");
        Note childB = Note.create("B", "");

        childA.setAttribute("$Container",
                new AttributeValue.StringValue(parent.getId().toString()));
        childA.setAttribute("$OutlineOrder", new AttributeValue.NumberValue(2));
        childB.setAttribute("$Container",
                new AttributeValue.StringValue(parent.getId().toString()));
        childB.setAttribute("$OutlineOrder", new AttributeValue.NumberValue(1));

        repository.save(parent);
        repository.save(childA);
        repository.save(childB);

        List<Note> children = repository.findChildren(parent.getId());

        assertEquals(2, children.size());
        assertEquals(childB, children.get(0));
        assertEquals(childA, children.get(1));
    }

    @Test
    @DisplayName("findChildren() returns empty list for unknown parent")
    void findChildren_shouldReturnEmptyForUnknownParent() {
        List<Note> children = repository.findChildren(UUID.randomUUID());

        assertTrue(children.isEmpty());
    }

    @Test
    @DisplayName("findChildren() returns empty list for parent with no children")
    void findChildren_shouldReturnEmptyForParentWithNoChildren() {
        Note parent = Note.create("Parent", "");
        repository.save(parent);

        List<Note> children = repository.findChildren(parent.getId());

        assertTrue(children.isEmpty());
    }

    // --- findNoteIdsWithChildren tests ---

    @Test
    @DisplayName("findNoteIdsWithChildren() returns ids that have at least one child")
    void findNoteIdsWithChildren_shouldReturnIdsWithChildren() {
        Note parent = Note.create("Parent", "");
        Note childless = Note.create("Childless", "");
        Note child = Note.create("Child", "");
        child.setAttribute("$Container",
                new AttributeValue.StringValue(parent.getId().toString()));
        child.setAttribute("$OutlineOrder", new AttributeValue.NumberValue(0));

        repository.save(parent);
        repository.save(childless);
        repository.save(child);

        Set<UUID> result = repository.findNoteIdsWithChildren(
                List.of(parent.getId(), childless.getId()));

        assertEquals(1, result.size());
        assertTrue(result.contains(parent.getId()));
        assertFalse(result.contains(childless.getId()));
    }

    @Test
    @DisplayName("findNoteIdsWithChildren() returns empty set when no ids have children")
    void findNoteIdsWithChildren_shouldReturnEmptyWhenNoneHaveChildren() {
        Note noteA = Note.create("A", "");
        Note noteB = Note.create("B", "");
        repository.save(noteA);
        repository.save(noteB);

        Set<UUID> result = repository.findNoteIdsWithChildren(
                List.of(noteA.getId(), noteB.getId()));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findNoteIdsWithChildren() returns empty set for empty input")
    void findNoteIdsWithChildren_shouldReturnEmptyForEmptyInput() {
        Set<UUID> result = repository.findNoteIdsWithChildren(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findNoteIdsWithChildren() ignores children of notes not in the input set")
    void findNoteIdsWithChildren_shouldIgnoreNonRequestedParents() {
        Note parentA = Note.create("ParentA", "");
        Note parentB = Note.create("ParentB", "");
        Note childOfA = Note.create("ChildOfA", "");
        childOfA.setAttribute("$Container",
                new AttributeValue.StringValue(parentA.getId().toString()));
        childOfA.setAttribute("$OutlineOrder", new AttributeValue.NumberValue(0));

        repository.save(parentA);
        repository.save(parentB);
        repository.save(childOfA);

        // Only ask about parentB
        Set<UUID> result = repository.findNoteIdsWithChildren(
                List.of(parentB.getId()));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findNoteIdsWithChildren() handles multiple parents with children")
    void findNoteIdsWithChildren_shouldHandleMultipleParentsWithChildren() {
        Note parentA = Note.create("ParentA", "");
        Note parentB = Note.create("ParentB", "");
        Note childOfA = Note.create("ChildOfA", "");
        childOfA.setAttribute("$Container",
                new AttributeValue.StringValue(parentA.getId().toString()));
        childOfA.setAttribute("$OutlineOrder", new AttributeValue.NumberValue(0));
        Note childOfB = Note.create("ChildOfB", "");
        childOfB.setAttribute("$Container",
                new AttributeValue.StringValue(parentB.getId().toString()));
        childOfB.setAttribute("$OutlineOrder", new AttributeValue.NumberValue(0));

        repository.save(parentA);
        repository.save(parentB);
        repository.save(childOfA);
        repository.save(childOfB);

        Set<UUID> result = repository.findNoteIdsWithChildren(
                List.of(parentA.getId(), parentB.getId()));

        assertEquals(2, result.size());
        assertTrue(result.contains(parentA.getId()));
        assertTrue(result.contains(parentB.getId()));
    }
}
