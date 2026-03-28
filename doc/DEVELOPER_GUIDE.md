# EmberVault Developer Guide

A code-first walkthrough for programmatically creating documents, building
note hierarchies, and opening views. All snippets compile and run against
the current API. See also [ARCHITECTURE.md](ARCHITECTURE.md) and the
[ADRs](adr/) for design rationale.

> **Manual wiring** is used throughout so you can see exactly which objects
> are created and how they connect. A future `DocumentFactory` will
> automate this.

---

## 1. Creating an Empty Document

Wire the repositories, services, and project by hand:

```java
import com.embervault.adapter.out.persistence.InMemoryNoteRepository;
import com.embervault.adapter.out.persistence.InMemoryLinkRepository;
import com.embervault.adapter.out.persistence.InMemoryStampRepository;
import com.embervault.application.NoteServiceImpl;
import com.embervault.application.LinkServiceImpl;
import com.embervault.application.StampServiceImpl;
import com.embervault.application.ProjectServiceImpl;
import com.embervault.application.port.in.*;
import com.embervault.domain.*;

// Repositories (outbound adapters)
var noteRepo  = new InMemoryNoteRepository();
var linkRepo  = new InMemoryLinkRepository();
var stampRepo = new InMemoryStampRepository();

// Services (application layer)
NoteService  noteService  = new NoteServiceImpl(noteRepo);
LinkService  linkService  = new LinkServiceImpl(linkRepo);
StampService stampService = new StampServiceImpl(stampRepo, noteRepo);

// Project with a root note
Project project = new ProjectServiceImpl().createEmptyProject();
Note rootNote   = project.getRootNote();
UUID rootId     = rootNote.getId();

// The root note must be saved into the repository so children can find it
noteRepo.save(rootNote);
```

The root note is the top of the hierarchy. Every other note will be a
descendant of it.

---

## 2. Creating Notes and Building a Hierarchy

### Child notes

```java
Note chapter1  = noteService.createChildNote(rootId, "Chapter 1");
Note chapter2  = noteService.createChildNote(rootId, "Chapter 2");
Note section1a = noteService.createChildNote(chapter1.getId(), "Introduction");
Note section1b = noteService.createChildNote(chapter1.getId(), "Background");
```

`createChildNote` sets the `$Container` and `$OutlineOrder` attributes
automatically.

### Sibling notes

```java
Note section1c = noteService.createSiblingNote(section1b.getId(), "Methods");
```

The new note is inserted directly after the given sibling.

### Indent / outdent

```java
// Indent: makes the note a child of the note above it
noteService.indentNote(section1c.getId());

// Outdent: moves the note up one level
noteService.outdentNote(section1c.getId());
```

### Setting attributes

```java
chapter1.setAttribute(Attributes.COLOR,
        new AttributeValue.ColorValue(TbxColor.named("blue")));
chapter1.setAttribute(Attributes.TEXT,
        new AttributeValue.StringValue("This chapter covers..."));
chapter1.setAttribute(Attributes.BADGE,
        new AttributeValue.StringValue("star"));
```

Attribute names follow the Tinderbox `$Name` convention. See
`com.embervault.domain.Attributes` for the full list.

### Prototypes

Prototypes let notes inherit default attribute values:

```java
Note proto = noteService.createNote("Book Chapter", "");
proto.setAttribute(Attributes.IS_PROTOTYPE,
        new AttributeValue.BooleanValue(true));
proto.setAttribute(Attributes.COLOR,
        new AttributeValue.ColorValue(TbxColor.named("green")));
noteRepo.save(proto);

chapter1.setPrototypeId(proto.getId());
```

---

## 3. Querying Notes

```java
// Children of a note (ordered by $OutlineOrder)
List<Note> chapters = noteService.getChildren(rootId);

// Check if a note has children
boolean hasKids = noteService.hasChildren(chapter1.getId());

// Batch check
Map<UUID, Boolean> batch = noteService.hasChildrenBatch(
        List.of(chapter1.getId(), chapter2.getId()));

// Navigate the hierarchy
Optional<Note> prev = noteService.getPreviousInOutline(section1b.getId());

// Search by title or text (title matches first)
List<Note> results = noteService.searchNotes("Introduction");
```

---

## 4. Opening a Map View

The Map view displays notes as rectangles positioned by `$Xpos` / `$Ypos`.

```java
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

StringProperty titleProp = new SimpleStringProperty(rootNote.getTitle());
MapViewModel mapVm = new MapViewModel(titleProp, noteService);
mapVm.setBaseNoteId(rootId);
mapVm.loadNotes();
```

To display in a JavaFX scene (UI context required):

```java
FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/com/embervault/adapter/in/ui/view/MapView.fxml"));
Parent mapView = loader.load();
((MapViewController) loader.getController()).initViewModel(mapVm);
```

---

## 5. Opening an Outline View

The Outline view shows the hierarchy as a tree ordered by `$OutlineOrder`.

```java
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;

StringProperty outlineTitleProp = new SimpleStringProperty(rootNote.getTitle());
OutlineViewModel outlineVm = new OutlineViewModel(outlineTitleProp, noteService);
outlineVm.setBaseNoteId(rootId);
outlineVm.loadNotes();
```

---

## 6. Opening a Treemap View

The Treemap view renders notes as area-proportional rectangles.

```java
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;

StringProperty treemapTitleProp = new SimpleStringProperty(rootNote.getTitle());
TreemapViewModel treemapVm = new TreemapViewModel(treemapTitleProp, noteService);
treemapVm.setBaseNoteId(rootId);
treemapVm.loadNotes();
```

---

## 7. Opening a Hyperbolic View

The Hyperbolic view renders the link graph. It requires both `NoteService`
and `LinkService`.

```java
import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;

HyperbolicViewModel hyperVm = new HyperbolicViewModel(noteService, linkService);
hyperVm.setFocusNote(rootId);
```

See [ADR-0014](adr/0014-use-testfx-for-ui-testing.md) for UI testing
guidance.

---

## 8. Opening an Attribute Browser and Note Editor

The Attribute Browser groups notes by a chosen attribute. The Note Editor
edits the selected note's properties.

```java
import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;
import com.embervault.adapter.in.ui.viewmodel.NoteEditorViewModel;
import com.embervault.domain.AttributeSchemaRegistry;

AttributeSchemaRegistry schemaRegistry = new AttributeSchemaRegistry();

AttributeBrowserViewModel browserVm =
        new AttributeBrowserViewModel(noteService, schemaRegistry);
browserVm.setSelectedAttribute(Attributes.COLOR);
// browserVm.groupNotes() is called automatically by setSelectedAttribute

NoteEditorViewModel editorVm =
        new NoteEditorViewModel(noteService, schemaRegistry);
```

---

## 9. Switching Views at Runtime

`ViewPaneContext` manages a single pane and supports switching between view
types via the `ViewType` enum. In a full UI setup:

```java
import com.embervault.ViewPaneContext;
import com.embervault.ViewType;

// ViewPaneContext is constructed with an initial view, then deps are set:
// paneContext.setDeps(paneDeps);

// Switch to a different view:
// paneContext.switchView(ViewType.TREEMAP);
```

Available view types: `MAP`, `OUTLINE`, `TREEMAP`, `HYPERBOLIC`, `BROWSER`.

---

## 10. Syncing Multiple Views

When one view mutates data, all views should refresh. Wire a shared
`refreshAll` callback:

```java
Runnable refreshAll = () -> {
    mapVm.loadNotes();
    outlineVm.loadNotes();
    treemapVm.loadNotes();
    // hyperbolicVm re-focuses if needed
};

mapVm.setOnDataChanged(refreshAll);
outlineVm.setOnDataChanged(refreshAll);
treemapVm.setOnDataChanged(refreshAll);
```

This is the same pattern used in `App.java`. Each ViewModel's
`DataChangeSupport` invokes the callback after mutations.

---

## 11. Using Stamps

Stamps are reusable actions that set an attribute on a note. The action
string follows the pattern `$Attribute=value`.

```java
Stamp colorStamp = stampService.createStamp("Color:red", "$Color=red");
Stamp checkStamp = stampService.createStamp("Mark Done", "$Checked=true");

// Apply a stamp to a note
stampService.applyStamp(colorStamp.id(), chapter1.getId());
```

After applying, the note's `$Color` attribute is set to `red`.

---

## 12. Working with Links

Links are directed relationships between notes. They are the core primitive
for the Hyperbolic view.

```java
Link link1 = linkService.createLink(chapter1.getId(), chapter2.getId());
Link link2 = linkService.createLink(
        chapter1.getId(), section1a.getId(), "contains");

// Query links
List<Link> from = linkService.getLinksFrom(chapter1.getId());
List<Link> to   = linkService.getLinksTo(chapter2.getId());
List<Link> all  = linkService.getAllLinksFor(chapter1.getId());

// Delete a link
linkService.deleteLink(link1.id());
```

To visualize links, open the Hyperbolic view (Section 7) focused on a
note that has links.
