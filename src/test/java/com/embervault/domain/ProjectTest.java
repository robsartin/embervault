package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    @DisplayName("createEmpty() produces a project with non-null id and name")
    void createEmpty_shouldPopulateIdAndName() {
        Project project = Project.createEmpty();

        assertNotNull(project.getId());
        assertEquals("Untitled", project.getName());
    }

    @Test
    @DisplayName("createEmpty() produces a project with a root note titled Untitled")
    void createEmpty_shouldHaveRootNoteTitledUntitled() {
        Project project = Project.createEmpty();

        assertNotNull(project.getRootNote());
        assertEquals("Untitled", project.getRootNote().getTitle());
    }

    @Test
    @DisplayName("Constructor rejects null id")
    void constructor_shouldRejectNullId() {
        Note rootNote = Note.create("Title", "");
        assertThrows(NullPointerException.class,
                () -> new Project(null, "Name", rootNote));
    }

    @Test
    @DisplayName("Constructor rejects null name")
    void constructor_shouldRejectNullName() {
        Note rootNote = Note.create("Title", "");
        assertThrows(NullPointerException.class,
                () -> new Project(UUID.randomUUID(), null, rootNote));
    }

    @Test
    @DisplayName("Constructor rejects blank name")
    void constructor_shouldRejectBlankName() {
        Note rootNote = Note.create("Title", "");
        assertThrows(IllegalArgumentException.class,
                () -> new Project(UUID.randomUUID(), "   ", rootNote));
    }

    @Test
    @DisplayName("Constructor rejects null rootNote")
    void constructor_shouldRejectNullRootNote() {
        assertThrows(NullPointerException.class,
                () -> new Project(UUID.randomUUID(), "Name", null));
    }

    @Test
    @DisplayName("equals() is based on id")
    void equals_shouldBeBasedOnId() {
        UUID id = UUID.randomUUID();
        Note noteA = Note.create("A", "a");
        Note noteB = Note.create("B", "b");
        Project projectA = new Project(id, "A", noteA);
        Project projectB = new Project(id, "B", noteB);

        assertEquals(projectA, projectB);
    }

    @Test
    @DisplayName("hashCode() is consistent with equals()")
    void hashCode_shouldBeConsistentWithEquals() {
        UUID id = UUID.randomUUID();
        Note noteA = Note.create("A", "a");
        Note noteB = Note.create("B", "b");
        Project projectA = new Project(id, "A", noteA);
        Project projectB = new Project(id, "B", noteB);

        assertEquals(projectA.hashCode(), projectB.hashCode());
    }

    @Test
    @DisplayName("toString() includes id and name")
    void toString_shouldIncludeIdAndName() {
        Project project = Project.createEmpty();
        String str = project.toString();

        assertNotNull(str);
        assertEquals(true, str.contains(project.getId().toString()));
        assertEquals(true, str.contains("Untitled"));
    }
}
