package com.embervault.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.embervault.TestFixture;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.ColorScheme;
import com.embervault.domain.ColorSchemeRegistry;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.Stamp;
import com.embervault.domain.TbxColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests covering cross-service scenarios identified as gaps
 * in issue #164.
 */
class IntegrationTest {

    private DocumentContext ctx;
    private NoteService noteService;
    private LinkService linkService;
    private StampService stampService;
    private UUID rootId;

    @BeforeEach
    void setUp() {
        ctx = TestFixture.createDocumentContext();
        noteService = ctx.noteService();
        linkService = ctx.linkService();
        stampService = ctx.stampService();
        rootId = ctx.project().getRootNote().getId();
    }

    // ------------------------------------------------------------------
    // Stamp apply + verify
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Stamp apply and verify")
    class StampApplyVerify {

        @Test
        @DisplayName("applying color stamp changes note color attribute")
        void applyColorStamp_shouldChangeNoteColor() {
            Note note = noteService.createChildNote(rootId, "Task");
            Stamp stamp = stampService.createStamp("Red", "$Color=red");

            stampService.applyStamp(stamp.id(), note.getId());

            Note updated = noteService.getNote(note.getId()).orElseThrow();
            AttributeValue color = updated.getAttribute(Attributes.COLOR)
                    .orElseThrow();
            assertEquals(TbxColor.named("red"),
                    ((AttributeValue.ColorValue) color).value());
        }

        @Test
        @DisplayName("applying boolean stamp sets checked attribute")
        void applyBooleanStamp_shouldSetCheckedAttribute() {
            Note note = noteService.createChildNote(rootId, "Task");
            Stamp stamp = stampService.createStamp(
                    "Done", "$Checked=true");

            stampService.applyStamp(stamp.id(), note.getId());

            Note updated = noteService.getNote(note.getId()).orElseThrow();
            AttributeValue checked = updated.getAttribute("$Checked")
                    .orElseThrow();
            assertEquals(true,
                    ((AttributeValue.BooleanValue) checked).value());
        }

        @Test
        @DisplayName("applying stamp twice overwrites previous value")
        void applyStampTwice_shouldOverwritePreviousValue() {
            Note note = noteService.createChildNote(rootId, "Task");
            Stamp red = stampService.createStamp("Red", "$Color=red");
            Stamp blue = stampService.createStamp("Blue", "$Color=blue");

            stampService.applyStamp(red.id(), note.getId());
            stampService.applyStamp(blue.id(), note.getId());

            Note updated = noteService.getNote(note.getId()).orElseThrow();
            AttributeValue color = updated.getAttribute(Attributes.COLOR)
                    .orElseThrow();
            assertEquals(TbxColor.named("blue"),
                    ((AttributeValue.ColorValue) color).value());
        }
    }

    // ------------------------------------------------------------------
    // Link query
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Link query integration")
    class LinkQuery {

        @Test
        @DisplayName("links created between notes are queryable from both ends")
        void linksBetweenNotes_shouldBeQueryableFromBothEnds() {
            Note a = noteService.createChildNote(rootId, "A");
            Note b = noteService.createChildNote(rootId, "B");

            Link link = linkService.createLink(
                    a.getId(), b.getId(), "reference");

            List<Link> fromA = linkService.getLinksFrom(a.getId());
            assertEquals(1, fromA.size());
            assertEquals("reference", fromA.get(0).type());

            List<Link> toB = linkService.getLinksTo(b.getId());
            assertEquals(1, toB.size());
            assertEquals(a.getId(), toB.get(0).sourceId());
        }

        @Test
        @DisplayName("deleting a link removes it from both query directions")
        void deletingLink_shouldRemoveFromBothDirections() {
            Note a = noteService.createChildNote(rootId, "A");
            Note b = noteService.createChildNote(rootId, "B");
            Link link = linkService.createLink(a.getId(), b.getId());

            linkService.deleteLink(link.id());

            assertTrue(linkService.getLinksFrom(a.getId()).isEmpty());
            assertTrue(linkService.getLinksTo(b.getId()).isEmpty());
        }

        @Test
        @DisplayName("getAllLinksFor returns links regardless of direction")
        void getAllLinksFor_shouldReturnBothDirections() {
            Note a = noteService.createChildNote(rootId, "A");
            Note b = noteService.createChildNote(rootId, "B");
            Note c = noteService.createChildNote(rootId, "C");

            linkService.createLink(a.getId(), b.getId());
            linkService.createLink(c.getId(), a.getId());

            List<Link> allForA = linkService.getAllLinksFor(a.getId());
            assertEquals(2, allForA.size());
        }
    }

    // ------------------------------------------------------------------
    // ColorScheme
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("ColorScheme registry coverage")
    class ColorSchemeTests {

        @Test
        @DisplayName("all registry schemes have non-blank fields")
        void allSchemes_shouldHaveNonBlankFields() {
            List<ColorScheme> schemes = ColorSchemeRegistry.getAllSchemes();

            assertFalse(schemes.isEmpty());
            for (ColorScheme scheme : schemes) {
                assertNotNull(scheme.name());
                assertFalse(scheme.canvasBackground().isBlank());
                assertFalse(scheme.panelBackground().isBlank());
                assertFalse(scheme.textColor().isBlank());
                assertFalse(scheme.secondaryTextColor().isBlank());
                assertFalse(scheme.borderColor().isBlank());
                assertFalse(scheme.selectionColor().isBlank());
                assertFalse(scheme.toolbarBackground().isBlank());
                assertFalse(scheme.accentColor().isBlank());
            }
        }

        @Test
        @DisplayName("each preset is retrievable by name")
        void eachPreset_shouldBeRetrievableByName() {
            List<ColorScheme> all = ColorSchemeRegistry.getAllSchemes();
            for (ColorScheme scheme : all) {
                assertTrue(
                        ColorSchemeRegistry.getScheme(scheme.name())
                                .isPresent(),
                        "Should find scheme: " + scheme.name());
            }
        }
    }

    // ------------------------------------------------------------------
    // DocumentFactory wiring verification
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("DocumentFactory wiring")
    class DocumentFactoryWiring {

        @Test
        @DisplayName("all services are functional end-to-end")
        void allServices_shouldBeFunctionalEndToEnd() {
            // NoteService: create hierarchy
            Note child = noteService.createChildNote(rootId, "Child");
            assertNotNull(child);
            assertEquals(1, noteService.getChildren(rootId).size());

            // StampService: create and apply
            Stamp stamp = stampService.createStamp("Mark", "$Color=green");
            stampService.applyStamp(stamp.id(), child.getId());
            Note updated = noteService.getNote(child.getId()).orElseThrow();
            assertTrue(updated.getAttribute(Attributes.COLOR).isPresent());

            // LinkService: create link
            Note child2 = noteService.createChildNote(rootId, "Child2");
            Link link = linkService.createLink(
                    child.getId(), child2.getId());
            assertNotNull(link);
            assertEquals(1,
                    linkService.getLinksFrom(child.getId()).size());
        }
    }

    // ------------------------------------------------------------------
    // End-to-end: create document, add notes, indent, stamp, search
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("End-to-end workflow")
    class EndToEnd {

        @Test
        @DisplayName("full workflow: create, indent, stamp, search, verify")
        void fullWorkflow_shouldProduceConsistentState() {
            // 1. Create notes
            Note ch1 = noteService.createChildNote(rootId, "Chapter 1");
            Note ch2 = noteService.createChildNote(rootId, "Chapter 2");
            Note sec1 = noteService.createChildNote(
                    ch1.getId(), "Introduction");
            Note sec2 = noteService.createChildNote(
                    ch1.getId(), "Background");

            assertEquals(2, noteService.getChildren(rootId).size());
            assertEquals(2,
                    noteService.getChildren(ch1.getId()).size());

            // 2. Indent sec2 under sec1
            noteService.indentNote(sec2.getId());
            assertEquals(1,
                    noteService.getChildren(ch1.getId()).size());
            assertEquals(1,
                    noteService.getChildren(sec1.getId()).size());

            // 3. Outdent sec2 back
            noteService.outdentNote(sec2.getId());
            assertEquals(2,
                    noteService.getChildren(ch1.getId()).size());

            // 4. Apply stamps
            Stamp redStamp = stampService.createStamp(
                    "Red", "$Color=red");
            stampService.applyStamp(redStamp.id(), ch1.getId());

            Note ch1Updated = noteService.getNote(ch1.getId())
                    .orElseThrow();
            AttributeValue color = ch1Updated
                    .getAttribute(Attributes.COLOR).orElseThrow();
            assertEquals(TbxColor.named("red"),
                    ((AttributeValue.ColorValue) color).value());

            // 5. Create links
            linkService.createLink(
                    ch1.getId(), ch2.getId(), "reference");
            assertEquals(1,
                    linkService.getLinksFrom(ch1.getId()).size());
            assertEquals(1,
                    linkService.getLinksTo(ch2.getId()).size());

            // 6. Search
            List<Note> results = noteService.searchNotes("Chapter");
            assertEquals(2, results.size());

            List<Note> introResults = noteService.searchNotes(
                    "Introduction");
            assertEquals(1, introResults.size());
            assertEquals("Introduction",
                    introResults.get(0).getTitle());
        }

        @Test
        @DisplayName("TestFixture.createRootWithChildren produces correct hierarchy")
        void testFixtureHelper_shouldProduceCorrectHierarchy() {
            NoteService svc = TestFixture.createNoteService();
            UUID root = TestFixture.createRootWithChildren(
                    svc, "Alpha", "Beta", "Gamma");

            List<Note> children = svc.getChildren(root);
            assertEquals(3, children.size());
            assertEquals("Alpha", children.get(0).getTitle());
            assertEquals("Beta", children.get(1).getTitle());
            assertEquals("Gamma", children.get(2).getTitle());
        }
    }
}
