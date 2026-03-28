package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;

import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Edge-case tests for {@link NoteServiceImpl} — boundary conditions for
 * createSiblingNote, indentNote, outdentNote, and deleteNoteIfLeaf.
 */
class NoteServiceEdgeCaseTest {

    private NoteService service;
    private InMemoryNoteRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryNoteRepository();
        service = new NoteServiceImpl(repository, new Random(99L));
    }

    @Nested
    @DisplayName("createSiblingNote edge cases")
    class CreateSiblingNote {

        @Test
        @DisplayName("creates sibling of only child — order is 1")
        void onlyChild_siblingGetsOrder1() {
            Note parent = service.createNote("Parent", "");
            Note onlyChild = service.createChildNote(parent.getId(), "Only");

            Note sibling = service.createSiblingNote(onlyChild.getId(), "Sibling");

            double order = ((AttributeValue.NumberValue)
                    sibling.getAttribute("$OutlineOrder").orElseThrow()).value();
            assertEquals(1.0, order);
            List<Note> children = service.getChildren(parent.getId());
            assertEquals(2, children.size());
        }

        @Test
        @DisplayName("creates sibling between first and second child")
        void betweenFirstAndSecond() {
            Note parent = service.createNote("Parent", "");
            Note first = service.createChildNote(parent.getId(), "First");
            service.createChildNote(parent.getId(), "Second");
            service.createChildNote(parent.getId(), "Third");

            Note sibling = service.createSiblingNote(first.getId(), "After First");

            List<Note> children = service.getChildren(parent.getId());
            assertEquals(4, children.size());
            assertEquals("First", children.get(0).getTitle());
            assertEquals("After First", children.get(1).getTitle());
            assertEquals("Second", children.get(2).getTitle());
            assertEquals("Third", children.get(3).getTitle());
        }

        @Test
        @DisplayName("creates sibling with null title")
        void nullTitle_createsNoteWithEmptyTitle() {
            Note parent = service.createNote("Parent", "");
            Note child = service.createChildNote(parent.getId(), "Child");

            Note sibling = service.createSiblingNote(child.getId(), null);

            assertNotNull(sibling);
            assertEquals("", sibling.getTitle());
        }

        @Test
        @DisplayName("throws when sibling has no $Container")
        void noContainer_throws() {
            Note topLevel = service.createNote("TopLevel", "");

            assertThrows(NoSuchElementException.class,
                    () -> service.createSiblingNote(topLevel.getId(), "Sibling"));
        }
    }

    @Nested
    @DisplayName("indentNote edge cases")
    class IndentNote {

        @Test
        @DisplayName("indenting note with no $Container returns unchanged")
        void noContainer_returnsUnchanged() {
            Note topLevel = service.createNote("TopLevel", "");

            Note result = service.indentNote(topLevel.getId());

            assertEquals(topLevel.getId(), result.getId());
            assertTrue(result.getAttribute("$Container").isEmpty());
        }

        @Test
        @DisplayName("indenting third child moves it under second child")
        void thirdChild_movesUnderSecond() {
            Note root = service.createNote("Root", "");
            service.createChildNote(root.getId(), "A");
            Note childB = service.createChildNote(root.getId(), "B");
            Note childC = service.createChildNote(root.getId(), "C");

            service.indentNote(childC.getId());

            List<Note> rootChildren = service.getChildren(root.getId());
            assertEquals(2, rootChildren.size());
            assertEquals("A", rootChildren.get(0).getTitle());
            assertEquals("B", rootChildren.get(1).getTitle());

            List<Note> childrenOfB = service.getChildren(childB.getId());
            assertEquals(1, childrenOfB.size());
            assertEquals("C", childrenOfB.get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("outdentNote edge cases")
    class OutdentNote {

        @Test
        @DisplayName("outdenting when parent doesn't exist returns unchanged")
        void parentMissing_returnsUnchanged() {
            // Create a note with a $Container pointing to a non-existent parent
            Note orphan = Note.create("Orphan", "");
            orphan.setAttribute("$Container",
                    new AttributeValue.StringValue(UUID.randomUUID().toString()));
            orphan.setAttribute("$OutlineOrder",
                    new AttributeValue.NumberValue(0));
            repository.save(orphan);

            Note result = service.outdentNote(orphan.getId());

            assertEquals(orphan.getId(), result.getId());
        }

        @Test
        @DisplayName("outdenting bumps orders of subsequent grandparent children")
        void bumpsSubsequentOrders() {
            Note root = service.createNote("Root", "");
            Note parentA = service.createChildNote(root.getId(), "ParentA");
            Note parentB = service.createChildNote(root.getId(), "ParentB");
            Note child = service.createChildNote(parentA.getId(), "Child");

            service.outdentNote(child.getId());

            List<Note> rootChildren = service.getChildren(root.getId());
            assertEquals(3, rootChildren.size());
            assertEquals("ParentA", rootChildren.get(0).getTitle());
            assertEquals("Child", rootChildren.get(1).getTitle());
            assertEquals("ParentB", rootChildren.get(2).getTitle());
        }
    }

    @Nested
    @DisplayName("deleteNoteIfLeaf edge cases")
    class DeleteNoteIfLeaf {

        @Test
        @DisplayName("returns false when note has children")
        void noteWithChildren_returnsFalse() {
            Note parent = service.createNote("Parent", "");
            Note child = service.createChildNote(parent.getId(), "Child");
            service.createChildNote(child.getId(), "Grandchild");

            boolean deleted = service.deleteNoteIfLeaf(child.getId());

            assertFalse(deleted);
            assertTrue(service.getNote(child.getId()).isPresent());
        }

        @Test
        @DisplayName("deletes a top-level leaf note (no $Container)")
        void topLevelLeaf_deleted() {
            Note leaf = service.createNote("Leaf", "");

            boolean deleted = service.deleteNoteIfLeaf(leaf.getId());

            assertTrue(deleted);
            assertTrue(service.getNote(leaf.getId()).isEmpty());
        }

        @Test
        @DisplayName("returns false for non-existent note id")
        void nonExistentId_returnsFalse() {
            boolean deleted = service.deleteNoteIfLeaf(UUID.randomUUID());

            assertFalse(deleted);
        }
    }

    @Nested
    @DisplayName("renameNote edge cases")
    class RenameNote {

        @Test
        @DisplayName("rejects null title")
        void nullTitle_throws() {
            Note note = service.createNote("Title", "");

            assertThrows(NullPointerException.class,
                    () -> service.renameNote(note.getId(), null));
        }
    }

    @Nested
    @DisplayName("searchNotes edge cases")
    class SearchNotes {

        @Test
        @DisplayName("returns empty for null query")
        void nullQuery_returnsEmpty() {
            service.createNote("Test", "content");

            List<Note> results = service.searchNotes(null);

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("returns empty for blank query")
        void blankQuery_returnsEmpty() {
            service.createNote("Test", "content");

            List<Note> results = service.searchNotes("   ");

            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("title matches appear before text-only matches")
        void titleMatchesFirst() {
            Note textMatch = service.createNote("Other", "has the keyword apple");
            Note titleMatch = service.createNote("Apple Note", "no match here");

            List<Note> results = service.searchNotes("apple");

            assertEquals(2, results.size());
            assertEquals(titleMatch.getId(), results.get(0).getId());
            assertEquals(textMatch.getId(), results.get(1).getId());
        }
    }
}
