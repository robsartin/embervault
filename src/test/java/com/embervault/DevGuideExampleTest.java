package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.embervault.adapter.in.ui.viewmodel.AppState;
import com.embervault.adapter.in.ui.viewmodel.AppStateEventBridge;
import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;
import com.embervault.adapter.in.ui.viewmodel.EventBus;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteEditorViewModel;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;
import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.ProjectServiceImpl;
import com.embervault.application.StampServiceImpl;
import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Link;
import com.embervault.domain.Note;
import com.embervault.domain.Project;
import com.embervault.domain.Stamp;
import com.embervault.domain.TbxColor;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration test verifying the code snippets from doc/DEVELOPER_GUIDE.md.
 *
 * <p>Exercises domain, service, and viewmodel layers headlessly (no UI).
 * NOT tagged with {@code @Tag("ui")}.</p>
 */
class DevGuideExampleTest {

    private InMemoryNoteRepository noteRepo;
    private InMemoryLinkRepository linkRepo;
    private InMemoryStampRepository stampRepo;
    private NoteService noteService;
    private LinkService linkService;
    private StampService stampService;
    private Project project;
    private Note rootNote;
    private UUID rootId;

    @BeforeEach
    void setUp() {
        // Section 1: Creating an Empty Document
        noteRepo = new InMemoryNoteRepository();
        linkRepo = new InMemoryLinkRepository();
        stampRepo = new InMemoryStampRepository();

        noteService = new NoteServiceImpl(noteRepo);
        linkService = new LinkServiceImpl(linkRepo);
        stampService = new StampServiceImpl(stampRepo, noteRepo);

        project = new ProjectServiceImpl().createEmptyProject();
        rootNote = project.getRootNote();
        rootId = rootNote.getId();
        noteRepo.save(rootNote);
    }

    @Nested
    @DisplayName("Section 1: Creating an Empty Document")
    class CreateEmptyDocument {

        @Test
        @DisplayName("Project has a root note saved in repository")
        void projectHasRootNote() {
            assertNotNull(project);
            assertNotNull(rootNote);
            assertTrue(noteRepo.findById(rootId).isPresent());
        }
    }

    @Nested
    @DisplayName("Section 2: Creating Notes and Building a Hierarchy")
    class BuildHierarchy {

        @Test
        @DisplayName("Create child notes under root")
        void createChildNotes() {
            Note chapter1 = noteService.createChildNote(rootId, "Chapter 1");
            Note chapter2 = noteService.createChildNote(rootId, "Chapter 2");

            List<Note> children = noteService.getChildren(rootId);
            assertEquals(2, children.size());
            assertEquals("Chapter 1", children.get(0).getTitle());
            assertEquals("Chapter 2", children.get(1).getTitle());

            Note section1a = noteService.createChildNote(
                    chapter1.getId(), "Introduction");
            Note section1b = noteService.createChildNote(
                    chapter1.getId(), "Background");

            List<Note> ch1Kids = noteService.getChildren(chapter1.getId());
            assertEquals(2, ch1Kids.size());
            assertEquals("Introduction", ch1Kids.get(0).getTitle());
            assertEquals("Background", ch1Kids.get(1).getTitle());
        }

        @Test
        @DisplayName("Create sibling notes")
        void createSiblingNote() {
            Note ch1 = noteService.createChildNote(rootId, "Chapter 1");
            Note secA = noteService.createChildNote(ch1.getId(), "Intro");
            Note secB = noteService.createChildNote(ch1.getId(), "Background");

            Note secC = noteService.createSiblingNote(
                    secB.getId(), "Methods");

            List<Note> children = noteService.getChildren(ch1.getId());
            assertEquals(3, children.size());
            assertEquals("Methods", children.get(2).getTitle());
        }

        @Test
        @DisplayName("Indent and outdent notes")
        void indentOutdent() {
            Note ch1 = noteService.createChildNote(rootId, "Chapter 1");
            Note secA = noteService.createChildNote(
                    ch1.getId(), "Introduction");
            Note secB = noteService.createChildNote(
                    ch1.getId(), "Background");
            Note secC = noteService.createSiblingNote(
                    secB.getId(), "Methods");

            // Indent secC under secB
            noteService.indentNote(secC.getId());
            List<Note> secBchildren = noteService.getChildren(secB.getId());
            assertEquals(1, secBchildren.size());
            assertEquals("Methods", secBchildren.get(0).getTitle());

            // Outdent secC back to ch1
            noteService.outdentNote(secC.getId());
            List<Note> ch1Kids = noteService.getChildren(ch1.getId());
            assertTrue(ch1Kids.stream()
                    .anyMatch(n -> "Methods".equals(n.getTitle())));
        }

        @Test
        @DisplayName("Set note attributes")
        void setAttributes() {
            Note ch1 = noteService.createChildNote(rootId, "Chapter 1");

            ch1.setAttribute(Attributes.COLOR,
                    new AttributeValue.ColorValue(TbxColor.named("blue")));
            ch1.setAttribute(Attributes.TEXT,
                    new AttributeValue.StringValue("This chapter covers..."));
            ch1.setAttribute(Attributes.BADGE,
                    new AttributeValue.StringValue("star"));

            assertEquals(TbxColor.named("blue"),
                    ((AttributeValue.ColorValue) ch1.getAttribute(
                            Attributes.COLOR).orElseThrow()).value());
            assertEquals("This chapter covers...", ch1.getContent());
        }

        @Test
        @DisplayName("Set prototype on a note")
        void prototypes() {
            Note proto = noteService.createNote("Book Chapter", "");
            proto.setAttribute(Attributes.IS_PROTOTYPE,
                    new AttributeValue.BooleanValue(true));
            proto.setAttribute(Attributes.COLOR,
                    new AttributeValue.ColorValue(TbxColor.named("green")));
            noteRepo.save(proto);

            Note ch1 = noteService.createChildNote(rootId, "Chapter 1");
            ch1.setPrototypeId(proto.getId());

            assertTrue(ch1.getPrototypeId().isPresent());
            assertEquals(proto.getId(), ch1.getPrototypeId().get());
        }
    }

    @Nested
    @DisplayName("Section 3: Querying Notes")
    class QueryNotes {

        @Test
        @DisplayName("Get children, hasChildren, search")
        void queryOperations() {
            Note ch1 = noteService.createChildNote(rootId, "Chapter 1");
            Note ch2 = noteService.createChildNote(rootId, "Chapter 2");
            noteService.createChildNote(ch1.getId(), "Introduction");
            Note secB = noteService.createChildNote(
                    ch1.getId(), "Background");

            // Children
            List<Note> chapters = noteService.getChildren(rootId);
            assertEquals(2, chapters.size());

            // hasChildren
            assertTrue(noteService.hasChildren(ch1.getId()));
            assertFalse(noteService.hasChildren(ch2.getId()));

            // Batch check
            Map<UUID, Boolean> batch = noteService.hasChildrenBatch(
                    List.of(ch1.getId(), ch2.getId()));
            assertTrue(batch.get(ch1.getId()));
            assertFalse(batch.get(ch2.getId()));

            // Previous in outline
            Optional<Note> prev =
                    noteService.getPreviousInOutline(secB.getId());
            assertTrue(prev.isPresent());
            assertEquals("Introduction", prev.get().getTitle());

            // Search
            List<Note> results = noteService.searchNotes("Introduction");
            assertFalse(results.isEmpty());
            assertEquals("Introduction", results.get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("Sections 4-8: Creating ViewModels headlessly")
    class ViewModels {

        @Test
        @DisplayName("Section 4: MapViewModel loads notes")
        void mapViewModel() {
            noteService.createChildNote(rootId, "Note A");
            StringProperty titleProp =
                    new SimpleStringProperty(rootNote.getTitle());

            AppState appState = new AppState();
            MapViewModel mapVm = new MapViewModel(
                    titleProp, noteService, appState);
            mapVm.setBaseNoteId(rootId);
            mapVm.loadNotes();

            assertNotNull(mapVm.tabTitleProperty().get());
            assertFalse(mapVm.tabTitleProperty().get().isEmpty());
        }

        @Test
        @DisplayName("Section 5: OutlineViewModel loads notes")
        void outlineViewModel() {
            noteService.createChildNote(rootId, "Note A");
            StringProperty titleProp =
                    new SimpleStringProperty(rootNote.getTitle());

            OutlineViewModel outlineVm =
                    new OutlineViewModel(titleProp, noteService,
                            new AppState());
            outlineVm.setBaseNoteId(rootId);
            outlineVm.loadNotes();

            assertNotNull(outlineVm.tabTitleProperty().get());
        }

        @Test
        @DisplayName("Section 6: TreemapViewModel loads notes")
        void treemapViewModel() {
            noteService.createChildNote(rootId, "Note A");
            StringProperty titleProp =
                    new SimpleStringProperty(rootNote.getTitle());

            TreemapViewModel treemapVm =
                    new TreemapViewModel(titleProp, noteService,
                            new AppState());
            treemapVm.setBaseNoteId(rootId);
            treemapVm.loadNotes();

            assertNotNull(treemapVm.tabTitleProperty().get());
        }

        @Test
        @DisplayName("Section 7: HyperbolicViewModel sets focus")
        void hyperbolicViewModel() {
            HyperbolicViewModel hyperVm =
                    new HyperbolicViewModel(noteService, linkService,
                            new AppState());
            hyperVm.setFocusNote(rootId);

            assertEquals(rootId, hyperVm.getFocusNoteId());
        }

        @Test
        @DisplayName("Section 8: AttributeBrowserViewModel and NoteEditorViewModel")
        void attributeBrowserAndEditor() {
            noteService.createChildNote(rootId, "Note A");
            AttributeSchemaRegistry schemaRegistry =
                    new AttributeSchemaRegistry();
            AppState appState = new AppState();

            AttributeBrowserViewModel browserVm =
                    new AttributeBrowserViewModel(
                            noteService, schemaRegistry, appState);
            browserVm.setSelectedAttribute(Attributes.COLOR);

            assertNotNull(browserVm.tabTitleProperty().get());

            NoteEditorViewModel editorVm =
                    new NoteEditorViewModel(noteService, schemaRegistry,
                            appState);
            assertNotNull(editorVm);
        }
    }

    @Nested
    @DisplayName("Section 10: Syncing Multiple Views")
    class SyncViews {

        @Test
        @DisplayName("AppState notifies observers on data change")
        void syncCallback() {
            StringProperty titleProp =
                    new SimpleStringProperty(rootNote.getTitle());
            AppState appState = new AppState();
            EventBus eventBus = new EventBus();
            new AppStateEventBridge(eventBus, appState);
            MapViewModel mapVm = new MapViewModel(
                    titleProp, noteService, appState, eventBus);
            mapVm.setBaseNoteId(rootId);

            int[] callCount = {0};
            appState.dataVersionProperty().addListener(
                    (obs, oldVal, newVal) -> callCount[0]++);

            // Creating a child note through the VM triggers the callback
            mapVm.createChildNote("Trigger");
            assertTrue(callCount[0] > 0,
                    "appState listener should have been called");
        }
    }

    @Nested
    @DisplayName("Section 11: Using Stamps")
    class Stamps {

        @Test
        @DisplayName("Create and apply a stamp")
        void createAndApply() {
            Note ch1 = noteService.createChildNote(rootId, "Chapter 1");

            Stamp colorStamp =
                    stampService.createStamp("Color:red", "$Color=red");
            Stamp checkStamp =
                    stampService.createStamp("Mark Done", "$Checked=true");

            stampService.applyStamp(colorStamp.id(), ch1.getId());

            Optional<AttributeValue> colorVal =
                    ch1.getAttribute(Attributes.COLOR);
            assertTrue(colorVal.isPresent());
        }
    }

    @Nested
    @DisplayName("Section 12: Working with Links")
    class Links {

        @Test
        @DisplayName("Create, query, and delete links")
        void linkOperations() {
            Note ch1 = noteService.createChildNote(rootId, "Chapter 1");
            Note ch2 = noteService.createChildNote(rootId, "Chapter 2");
            Note secA = noteService.createChildNote(
                    ch1.getId(), "Introduction");

            Link link1 = linkService.createLink(
                    ch1.getId(), ch2.getId());
            Link link2 = linkService.createLink(
                    ch1.getId(), secA.getId(), "contains");

            List<Link> from = linkService.getLinksFrom(ch1.getId());
            assertEquals(2, from.size());

            List<Link> to = linkService.getLinksTo(ch2.getId());
            assertEquals(1, to.size());

            List<Link> all = linkService.getAllLinksFor(ch1.getId());
            assertEquals(2, all.size());

            linkService.deleteLink(link1.id());
            assertEquals(1, linkService.getLinksFrom(ch1.getId()).size());
        }
    }
}
