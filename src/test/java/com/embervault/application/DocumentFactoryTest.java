package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import com.embervault.domain.Note;
import com.embervault.domain.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DocumentFactoryTest {

    @Test
    @DisplayName("createEmpty() returns a DocumentContext with all services wired")
    void createEmpty_shouldReturnFullyWiredContext() {
        DocumentContext ctx = DocumentFactory.createEmpty();

        assertNotNull(ctx.project());
        assertNotNull(ctx.noteService());
        assertNotNull(ctx.linkService());
        assertNotNull(ctx.stampService());
        assertNotNull(ctx.noteRepository());
    }

    @Test
    @DisplayName("createEmpty() persists the root note in the repository")
    void createEmpty_shouldPersistRootNote() {
        DocumentContext ctx = DocumentFactory.createEmpty();

        Project project = ctx.project();
        Optional<Note> rootNote = ctx.noteRepository()
                .findById(project.getRootNote().getId());

        assertTrue(rootNote.isPresent(),
                "Root note should be saved in the repository");
        assertEquals(project.getRootNote().getId(), rootNote.get().getId());
    }

    @Test
    @DisplayName("createEmpty() services share the same repository")
    void createEmpty_servicesShouldShareRepository() {
        DocumentContext ctx = DocumentFactory.createEmpty();

        // Create a child via the service
        Note child = ctx.noteService().createChildNote(
                ctx.project().getRootNote().getId(), "Test Child");

        // Should be findable via the repository
        Optional<Note> found = ctx.noteRepository().findById(child.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Child", found.get().getTitle());
    }

    @Test
    @DisplayName("createEmpty() note service can create and retrieve children")
    void createEmpty_noteServiceShouldWork() {
        DocumentContext ctx = DocumentFactory.createEmpty();

        Note child = ctx.noteService().createChildNote(
                ctx.project().getRootNote().getId(), "Child Note");

        List<Note> children = ctx.noteService().getChildren(
                ctx.project().getRootNote().getId());
        assertFalse(children.isEmpty());
        assertEquals("Child Note", children.get(0).getTitle());
    }

    @Test
    @DisplayName("createEmpty() link service can create and retrieve links")
    void createEmpty_linkServiceShouldWork() {
        DocumentContext ctx = DocumentFactory.createEmpty();

        Note child1 = ctx.noteService().createChildNote(
                ctx.project().getRootNote().getId(), "A");
        Note child2 = ctx.noteService().createChildNote(
                ctx.project().getRootNote().getId(), "B");

        ctx.linkService().createLink(child1.getId(), child2.getId());

        assertFalse(ctx.linkService()
                .getLinksFrom(child1.getId()).isEmpty());
    }

    @Test
    @DisplayName("createEmpty() stamp service can create and apply stamps")
    void createEmpty_stampServiceShouldWork() {
        DocumentContext ctx = DocumentFactory.createEmpty();

        ctx.stampService().createStamp("Color:red", "$Color=red");

        assertFalse(ctx.stampService().getAllStamps().isEmpty());
    }

    @Test
    @DisplayName("createEmpty() project has a non-blank name")
    void createEmpty_projectShouldHaveName() {
        DocumentContext ctx = DocumentFactory.createEmpty();

        assertNotNull(ctx.project().getName());
        assertFalse(ctx.project().getName().isBlank());
    }
}
