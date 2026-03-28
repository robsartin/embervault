package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
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

    // --- createSiblingNote tests ---

    @Test
    @DisplayName("createSiblingNote() creates a note with same parent as sibling")
    void createSiblingNote_shouldCreateWithSameParent() {
        Note parent = service.createNote("Parent", "");
        Note sibling = service.createChildNote(parent.getId(), "Sibling");

        Note newNote = service.createSiblingNote(sibling.getId(), "New");

        assertNotNull(newNote);
        assertEquals("New", newNote.getTitle());
        String container = ((AttributeValue.StringValue)
                newNote.getAttribute("$Container").orElseThrow()).value();
        assertEquals(parent.getId().toString(), container);
    }

    @Test
    @DisplayName("createSiblingNote() places new note after sibling in outline order")
    void createSiblingNote_shouldPlaceAfterSibling() {
        Note parent = service.createNote("Parent", "");
        Note child1 = service.createChildNote(parent.getId(), "Child1");
        service.createChildNote(parent.getId(), "Child2");

        Note newNote = service.createSiblingNote(child1.getId(), "Between");

        double order = ((AttributeValue.NumberValue)
                newNote.getAttribute("$OutlineOrder").orElseThrow()).value();
        assertEquals(1.0, order);

        // Child2's order should have been bumped to 2
        List<Note> children = service.getChildren(parent.getId());
        assertEquals(3, children.size());
        assertEquals("Child1", children.get(0).getTitle());
        assertEquals("Between", children.get(1).getTitle());
        assertEquals("Child2", children.get(2).getTitle());
    }

    @Test
    @DisplayName("createSiblingNote() at end of list does not need to bump orders")
    void createSiblingNote_atEnd_shouldAppend() {
        Note parent = service.createNote("Parent", "");
        Note child1 = service.createChildNote(parent.getId(), "Child1");
        Note child2 = service.createChildNote(parent.getId(), "Child2");

        Note newNote = service.createSiblingNote(child2.getId(), "Last");

        List<Note> children = service.getChildren(parent.getId());
        assertEquals(3, children.size());
        assertEquals("Child1", children.get(0).getTitle());
        assertEquals("Child2", children.get(1).getTitle());
        assertEquals("Last", children.get(2).getTitle());
    }

    @Test
    @DisplayName("createSiblingNote() with empty title creates note with empty title")
    void createSiblingNote_emptyTitle_shouldCreateWithEmptyTitle() {
        Note parent = service.createNote("Parent", "");
        Note sibling = service.createChildNote(parent.getId(), "Sibling");

        Note newNote = service.createSiblingNote(sibling.getId(), "");

        assertEquals("", newNote.getTitle());
    }

    @Test
    @DisplayName("createSiblingNote() throws when sibling does not exist")
    void createSiblingNote_shouldThrowForMissingSibling() {
        assertThrows(NoSuchElementException.class,
                () -> service.createSiblingNote(UUID.randomUUID(), "Title"));
    }

    // --- indentNote tests ---

    @Test
    @DisplayName("indentNote() moves note to be child of note above")
    void indentNote_shouldMoveUnderNoteAbove() {
        Note parent = service.createNote("Parent", "");
        Note child1 = service.createChildNote(parent.getId(), "Child1");
        Note child2 = service.createChildNote(parent.getId(), "Child2");

        Note indented = service.indentNote(child2.getId());

        // child2 should now be a child of child1
        String container = ((AttributeValue.StringValue)
                indented.getAttribute("$Container").orElseThrow()).value();
        assertEquals(child1.getId().toString(), container);

        // parent should only have child1 now
        List<Note> parentChildren = service.getChildren(parent.getId());
        assertEquals(1, parentChildren.size());
        assertEquals("Child1", parentChildren.get(0).getTitle());

        // child1 should have child2 as its child
        List<Note> child1Children = service.getChildren(child1.getId());
        assertEquals(1, child1Children.size());
        assertEquals("Child2", child1Children.get(0).getTitle());
    }

    @Test
    @DisplayName("indentNote() returns unchanged when note is first child (no note above)")
    void indentNote_shouldReturnUnchangedWhenFirst() {
        Note parent = service.createNote("Parent", "");
        Note child1 = service.createChildNote(parent.getId(), "Child1");

        Note result = service.indentNote(child1.getId());

        // Should remain under parent
        String container = ((AttributeValue.StringValue)
                result.getAttribute("$Container").orElseThrow()).value();
        assertEquals(parent.getId().toString(), container);
    }

    @Test
    @DisplayName("indentNote() appends to end of target's existing children")
    void indentNote_shouldAppendToExistingChildren() {
        Note parent = service.createNote("Parent", "");
        Note child1 = service.createChildNote(parent.getId(), "Child1");
        service.createChildNote(child1.getId(), "ExistingGrandchild");
        Note child2 = service.createChildNote(parent.getId(), "Child2");

        service.indentNote(child2.getId());

        List<Note> child1Children = service.getChildren(child1.getId());
        assertEquals(2, child1Children.size());
        assertEquals("ExistingGrandchild", child1Children.get(0).getTitle());
        assertEquals("Child2", child1Children.get(1).getTitle());
    }

    @Test
    @DisplayName("indentNote() removes indented note from original parent's children (issue #118)")
    void indentNote_shouldRemoveFromOriginalParentChildren() {
        // Reproduce: root -> A, B, C; indent B under A
        // Then getChildren(root) should return only A and C, not B
        Note root = service.createNote("Root", "");
        Note childA = service.createChildNote(root.getId(), "A");
        Note childB = service.createChildNote(root.getId(), "B");
        Note childC = service.createChildNote(root.getId(), "C");

        // Before indent: root has 3 children
        List<Note> before = service.getChildren(root.getId());
        assertEquals(3, before.size());

        // Indent B (should go under A, the note above it)
        service.indentNote(childB.getId());

        // After indent: root should have only A and C
        List<Note> rootChildren = service.getChildren(root.getId());
        assertEquals(2, rootChildren.size(),
                "Root should have 2 children after indenting B under A");
        assertEquals("A", rootChildren.get(0).getTitle());
        assertEquals("C", rootChildren.get(1).getTitle());

        // A should now have B as its child
        List<Note> childrenOfA = service.getChildren(childA.getId());
        assertEquals(1, childrenOfA.size(),
                "A should have 1 child (B) after indent");
        assertEquals("B", childrenOfA.get(0).getTitle());
    }

    @Test
    @DisplayName("indentNote() throws when note does not exist")
    void indentNote_shouldThrowForMissingNote() {
        assertThrows(NoSuchElementException.class,
                () -> service.indentNote(UUID.randomUUID()));
    }

    // --- outdentNote tests ---

    @Test
    @DisplayName("outdentNote() moves note to be sibling of parent")
    void outdentNote_shouldMoveToGrandparent() {
        Note root = service.createNote("Root", "");
        Note parent = service.createChildNote(root.getId(), "Parent");
        Note child = service.createChildNote(parent.getId(), "Child");

        Note outdented = service.outdentNote(child.getId());

        // child should now be under root
        String container = ((AttributeValue.StringValue)
                outdented.getAttribute("$Container").orElseThrow()).value();
        assertEquals(root.getId().toString(), container);

        // parent should have no children
        assertTrue(service.getChildren(parent.getId()).isEmpty());

        // root should have parent and child
        List<Note> rootChildren = service.getChildren(root.getId());
        assertEquals(2, rootChildren.size());
    }

    @Test
    @DisplayName("outdentNote() places note just after old parent in grandparent's children")
    void outdentNote_shouldPlaceAfterOldParent() {
        Note root = service.createNote("Root", "");
        Note parent = service.createChildNote(root.getId(), "Parent");
        service.createChildNote(root.getId(), "Uncle");
        Note child = service.createChildNote(parent.getId(), "Child");

        service.outdentNote(child.getId());

        List<Note> rootChildren = service.getChildren(root.getId());
        assertEquals(3, rootChildren.size());
        assertEquals("Parent", rootChildren.get(0).getTitle());
        assertEquals("Child", rootChildren.get(1).getTitle());
        assertEquals("Uncle", rootChildren.get(2).getTitle());
    }

    @Test
    @DisplayName("outdentNote() returns unchanged when note has no parent ($Container)")
    void outdentNote_shouldReturnUnchangedWhenNoParent() {
        Note root = service.createNote("Root", "");

        Note result = service.outdentNote(root.getId());

        assertEquals(root.getId(), result.getId());
        // Should still have no $Container
        assertTrue(result.getAttribute("$Container").isEmpty());
    }

    @Test
    @DisplayName("outdentNote() returns unchanged when parent has no grandparent")
    void outdentNote_shouldReturnUnchangedWhenParentIsTopLevel() {
        Note topLevel = service.createNote("TopLevel", "");
        Note child = service.createChildNote(topLevel.getId(), "Child");

        // topLevel has no $Container, so child can't outdent further
        // Actually, topLevel has no $Container, but child's parent is topLevel.
        // The outdent should check if the parent (topLevel) has a $Container.
        // Since topLevel doesn't, child is already at the shallowest indentable level.
        Note result = service.outdentNote(child.getId());

        // Should remain under topLevel
        String container = ((AttributeValue.StringValue)
                result.getAttribute("$Container").orElseThrow()).value();
        assertEquals(topLevel.getId().toString(), container);
    }

    @Test
    @DisplayName("outdentNote() throws when note does not exist")
    void outdentNote_shouldThrowForMissingNote() {
        assertThrows(NoSuchElementException.class,
                () -> service.outdentNote(UUID.randomUUID()));
    }

    // --- getPreviousInOutline tests ---

    @Test
    @DisplayName("getPreviousInOutline() returns previous sibling")
    void getPreviousInOutline_shouldReturnPreviousSibling() {
        Note parent = service.createNote("Parent", "");
        Note child1 = service.createChildNote(parent.getId(), "Child1");
        Note child2 = service.createChildNote(parent.getId(), "Child2");

        Optional<Note> previous = service.getPreviousInOutline(child2.getId());

        assertTrue(previous.isPresent());
        assertEquals(child1.getId(), previous.get().getId());
    }

    @Test
    @DisplayName("getPreviousInOutline() returns parent when note is first child")
    void getPreviousInOutline_shouldReturnParentWhenFirstChild() {
        Note parent = service.createNote("Parent", "");
        Note child = service.createChildNote(parent.getId(), "Child");

        Optional<Note> previous = service.getPreviousInOutline(child.getId());

        assertTrue(previous.isPresent());
        assertEquals(parent.getId(), previous.get().getId());
    }

    @Test
    @DisplayName("getPreviousInOutline() returns empty when note has no container")
    void getPreviousInOutline_shouldReturnEmptyWhenNoContainer() {
        Note root = service.createNote("Root", "");

        Optional<Note> previous = service.getPreviousInOutline(root.getId());

        assertTrue(previous.isEmpty());
    }

    @Test
    @DisplayName("getPreviousInOutline() returns correct sibling among many")
    void getPreviousInOutline_shouldReturnCorrectSiblingAmongMany() {
        Note parent = service.createNote("Parent", "");
        Note child1 = service.createChildNote(parent.getId(), "Child1");
        Note child2 = service.createChildNote(parent.getId(), "Child2");
        Note child3 = service.createChildNote(parent.getId(), "Child3");

        Optional<Note> previous = service.getPreviousInOutline(child3.getId());

        assertTrue(previous.isPresent());
        assertEquals(child2.getId(), previous.get().getId());
    }

    @Test
    @DisplayName("getPreviousInOutline() throws when note does not exist")
    void getPreviousInOutline_shouldThrowForMissingNote() {
        assertThrows(NoSuchElementException.class,
                () -> service.getPreviousInOutline(UUID.randomUUID()));
    }

    // --- deleteNoteIfLeaf tests ---

    @Test
    @DisplayName("deleteNoteIfLeaf() deletes leaf note and returns true")
    void deleteNoteIfLeaf_shouldDeleteLeafNote() {
        Note parent = service.createNote("Parent", "");
        Note child = service.createChildNote(parent.getId(), "Child");

        boolean result = service.deleteNoteIfLeaf(child.getId());

        assertTrue(result);
        assertTrue(service.getNote(child.getId()).isEmpty());
    }

    @Test
    @DisplayName("deleteNoteIfLeaf() does not delete note with children and returns false")
    void deleteNoteIfLeaf_shouldNotDeleteNoteWithChildren() {
        Note parent = service.createNote("Parent", "");
        Note child = service.createChildNote(parent.getId(), "Child");
        service.createChildNote(child.getId(), "Grandchild");

        boolean result = service.deleteNoteIfLeaf(child.getId());

        assertFalse(result);
        assertTrue(service.getNote(child.getId()).isPresent());
    }

    @Test
    @DisplayName("deleteNoteIfLeaf() returns false when note does not exist")
    void deleteNoteIfLeaf_shouldReturnFalseForMissingNote() {
        boolean result = service.deleteNoteIfLeaf(UUID.randomUUID());

        assertFalse(result);
    }

    // --- hasChildrenBatch tests ---

    @Test
    @DisplayName("hasChildrenBatch() returns map with correct boolean values")
    void hasChildrenBatch_shouldReturnCorrectMap() {
        Note parent = service.createNote("Parent", "");
        Note childless = service.createNote("Childless", "");
        service.createChildNote(parent.getId(), "Child");

        Map<UUID, Boolean> result = service.hasChildrenBatch(
                List.of(parent.getId(), childless.getId()));

        assertTrue(result.get(parent.getId()));
        assertFalse(result.get(childless.getId()));
    }

    @Test
    @DisplayName("hasChildrenBatch() returns empty map for empty input")
    void hasChildrenBatch_shouldReturnEmptyMapForEmptyInput() {
        Map<UUID, Boolean> result = service.hasChildrenBatch(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("hasChildrenBatch() includes all requested ids in result")
    void hasChildrenBatch_shouldIncludeAllRequestedIds() {
        Note noteA = service.createNote("A", "");
        Note noteB = service.createNote("B", "");

        Map<UUID, Boolean> result = service.hasChildrenBatch(
                List.of(noteA.getId(), noteB.getId()));

        assertEquals(2, result.size());
        assertTrue(result.containsKey(noteA.getId()));
        assertTrue(result.containsKey(noteB.getId()));
    }
}
