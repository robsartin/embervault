package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.embervault.application.DocumentContext;
import com.embervault.domain.Note;
import com.embervault.domain.Stamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Edge-case tests for {@link DocumentFactory}.
 *
 * <p>Verifies that all services wired by the factory share
 * the same underlying repositories and that the root note is
 * correctly persisted.</p>
 */
class DocumentFactoryEdgeCaseTest {

    @Test
    @DisplayName("root note is retrievable from the repository")
    void rootNote_shouldExistInRepository() {
        DocumentContext ctx = DocumentFactory.createEmpty();
        UUID rootId = ctx.project().getRootNote().getId();

        Optional<Note> found =
                ctx.noteRepository().findById(rootId);

        assertTrue(found.isPresent(),
                "Root note must be persisted by factory");
        assertEquals(rootId, found.get().getId());
    }

    @Test
    @DisplayName("root note title matches project root note")
    void rootNote_titleMatchesProject() {
        DocumentContext ctx = DocumentFactory.createEmpty();
        Note projectRoot = ctx.project().getRootNote();

        Optional<Note> repoRoot =
                ctx.noteRepository().findById(
                        projectRoot.getId());

        assertTrue(repoRoot.isPresent());
        assertEquals(projectRoot.getTitle(),
                repoRoot.get().getTitle());
    }

    @Test
    @DisplayName("stampService and noteService share "
            + "the same note repository")
    void stampAndNoteService_shareRepository() {
        DocumentContext ctx = DocumentFactory.createEmpty();
        UUID rootId = ctx.project().getRootNote().getId();

        // Create a child via noteService
        Note child = ctx.noteService()
                .createChildNote(rootId, "StampTarget");

        // Create and apply a stamp via stampService
        Stamp stamp = ctx.stampService()
                .createStamp("Tag:important",
                        "$Tag=important");
        ctx.stampService().applyStamp(
                stamp.getId(), child.getId());

        // Verify the attribute was set on the note visible
        // through the shared repository
        Optional<Note> updated =
                ctx.noteRepository().findById(child.getId());
        assertTrue(updated.isPresent());
        assertEquals("important",
                updated.get().getAttributes().get("Tag"),
                "Stamp should have written attribute via "
                        + "shared repository");
    }

    @Test
    @DisplayName("children created via noteService are "
            + "visible in the repository")
    void noteService_childrenVisibleInRepo() {
        DocumentContext ctx = DocumentFactory.createEmpty();
        UUID rootId = ctx.project().getRootNote().getId();

        ctx.noteService().createChildNote(rootId, "A");
        ctx.noteService().createChildNote(rootId, "B");

        List<Note> children =
                ctx.noteService().getChildren(rootId);
        assertEquals(2, children.size());

        // Both should be findable in the repository
        for (Note child : children) {
            assertTrue(ctx.noteRepository()
                    .findById(child.getId()).isPresent());
        }
    }

    @Test
    @DisplayName("two createEmpty calls return independent "
            + "contexts")
    void twoContexts_shouldBeIndependent() {
        DocumentContext ctx1 = DocumentFactory.createEmpty();
        DocumentContext ctx2 = DocumentFactory.createEmpty();

        assertNotSame(ctx1.project(), ctx2.project());
        assertNotSame(ctx1.noteService(),
                ctx2.noteService());

        // Mutating one should not affect the other
        ctx1.noteService().createChildNote(
                ctx1.project().getRootNote().getId(), "Only1");
        List<Note> ctx2Children = ctx2.noteService()
                .getChildren(
                        ctx2.project().getRootNote().getId());
        assertTrue(ctx2Children.isEmpty(),
                "Second context should be unaffected");
    }

    @Test
    @DisplayName("root note id is a valid UUID")
    void rootNote_idIsValidUuid() {
        DocumentContext ctx = DocumentFactory.createEmpty();
        UUID rootId = ctx.project().getRootNote().getId();

        assertNotNull(rootId);
        // Round-trip through toString/fromString
        assertEquals(rootId,
                UUID.fromString(rootId.toString()));
    }
}
