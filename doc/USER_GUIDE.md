# EmberVault User Guide

EmberVault is a note-taking application inspired by [Tinderbox](http://www.eastgate.com/Tinderbox/). Notes are flexible containers of typed attributes, organized in hierarchies, and visualized through multiple interactive views.

---

## Getting Started

Launch EmberVault:

```bash
JAVA_HOME=/Users/sartin/Library/Java/JavaVirtualMachines/openjdk-25.0.2/Contents/Home ./mvnw javafx:run
```

When EmberVault opens, you'll see a workspace with a view pane (initially a Map view), a text pane at the bottom for editing note content, and a menu bar.

---

## Views

EmberVault provides five ways to visualize your notes. Right-click on a view tab to switch between them.

### Map View

A spatial canvas where notes appear as colored rectangles. Drag notes to arrange them freely. This is the default view and works well for brainstorming, spatial organization, and visual thinking.

### Outline View

A hierarchical tree showing your notes ordered by outline position. Use this view to see and reorganize your note structure. You can:
- Click a note to select it
- Press **Enter** to rename a note inline
- Press **Tab** to indent (make a child of the note above)
- Press **Shift+Tab** to outdent (move up one level)
- Use **arrow keys** to navigate up and down

### Treemap View

Displays notes as area-proportional rectangles. Larger notes (or notes with more children) take up more space. Useful for seeing the relative size and structure of your document at a glance.

### Hyperbolic View

A graph visualization of links between notes. Place a focus note at the center and see its connections radiating outward. This view is most useful when you have created links between notes.

### Attribute Browser

Groups notes by the values of a chosen attribute. For example, browse by color to see all red notes together, all blue notes together, and so on. Useful for categorizing and filtering.

---

## Working with Notes

### Creating Notes

- **Cmd+N** — Create a new child note inside the currently selected note
- From the menu: **Note > New Note**

In the Outline view, new notes appear in the hierarchy. In the Map view, they appear as rectangles.

### Editing Notes

Select a note and use the **text pane** at the bottom of the window to edit its content. The text pane shows the selected note's `$Text` attribute.

To rename a note, double-click it (in Map or Treemap view) or press **Enter** in the Outline view.

### Organizing Notes

Notes are organized in a hierarchy (parent-child relationships):

- **Indent** a note to make it a child of the note above it
- **Outdent** a note to move it up one level in the hierarchy
- Drag notes in the Map view to reposition them spatially

### Deleting Notes

- **Cmd+D** — Delete the selected note
- From the menu: **Note > Delete Note**

---

## Attributes

Every note is a collection of typed attributes. Common attributes include:

| Attribute | Type | Description |
|-----------|------|-------------|
| `$Name` | String | The note's title |
| `$Text` | String | The note's body content |
| `$Color` | Color | The note's display color |
| `$Badge` | String | An icon/symbol displayed on the note |
| `$Checked` | Boolean | A checkbox state |
| `$Xpos` / `$Ypos` | Number | Position in the Map view |
| `$OutlineOrder` | Number | Position in the Outline view |

Attributes are typed — colors are stored as colors, numbers as numbers, and so on. This enables filtering, sorting, and searching by attribute values.

---

## Prototypes

A prototype is a special note that other notes can inherit from. When a note has a prototype, any attribute not explicitly set on the note is inherited from its prototype.

**Inheritance chain:** Note -> Prototype -> Prototype's Prototype -> Document Default

This lets you define templates. For example, create a "Meeting Notes" prototype with a green color and a star badge, then assign it to individual meeting notes — they'll all inherit the color and badge unless overridden.

---

## Stamps

Stamps are reusable named actions that set an attribute on a note. For example:

- "Color: red" sets `$Color` to red
- "Mark Done" sets `$Checked` to true

Create and manage stamps through the stamp editor. Apply a stamp to quickly tag or categorize notes without manually editing attributes.

---

## Links

Links are directed connections between two notes (source -> destination). Each link has a type (e.g., "untitled", "web", "prototype", "contains").

Links are the primary data for the Hyperbolic view. Create links between related notes to build a navigable web of connections.

---

## Search

Press **Cmd+F** to open the search bar. Type to search incrementally — results update as you type with a short debounce delay. Search matches against note titles and text content.

Press **Escape** to close the search bar.

---

## Export

EmberVault supports exporting your document in multiple formats:

- **JSON** — Full-fidelity export preserving all attributes and structure
- **Markdown** — Human-readable export with note hierarchy as headings
- **OPML** — Outline format compatible with other outlining tools

Access export from the **File** menu.

---

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| **Cmd+N** | New note |
| **Cmd+Shift+N** | New window |
| **Cmd+F** | Find (search) |
| **Cmd+D** | Delete note |
| **Cmd+K** | Command palette |
| **Cmd+S** | Save |
| **Cmd+O** | Open |
| **Enter** | Rename note (Outline view) |
| **Tab** | Indent note (Outline view) |
| **Shift+Tab** | Outdent note (Outline view) |
| **Arrow keys** | Navigate notes (Outline view) |
| **Escape** | Cancel edit / close search |

---

## Command Palette

Press **Cmd+K** to open the command palette. Type to filter available commands, then press **Enter** to execute. Use **arrow keys** to navigate the command list.

---

## Tips

- Use prototypes to create consistent note templates across your document
- The Map view is great for spatial brainstorming; switch to Outline for structured writing
- Use stamps to quickly apply common attribute changes
- Link related notes and explore them in the Hyperbolic view
- The Attribute Browser is useful for finding notes by color, badge, or any other attribute
