package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.CreateNoteUseCase;
import com.embervault.application.port.in.DeleteNoteUseCase;
import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.application.port.in.GetOutlineNavigationQuery;
import com.embervault.application.port.in.MoveNoteUseCase;
import com.embervault.application.port.in.RenameNoteUseCase;
import com.embervault.domain.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Outline view tab.
 *
 * <p>Computes a tab title of the form "Outline: &lt;note title&gt;" that updates
 * reactively when the underlying note title changes. Manages a tree structure
 * of notes for hierarchical display and supports drill-down navigation.</p>
 */
public final class OutlineViewModel {

    private static final int MAX_TITLE_LENGTH = 20;

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<NoteDisplayItem> rootItems =
            FXCollections.observableArrayList();
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final NavigationStack navigationStack = new NavigationStack();
    private final GetNoteQuery getNoteQuery;
    private final CreateNoteUseCase createNoteUseCase;
    private final RenameNoteUseCase renameNoteUseCase;
    private final MoveNoteUseCase moveNoteUseCase;
    private final DeleteNoteUseCase deleteNoteUseCase;
    private final GetOutlineNavigationQuery outlineNavQuery;
    private final StringProperty rootNoteTitle;
    private final AppState appState;
    private final EventBus eventBus;

    /**
     * Constructs an OutlineViewModel that derives its tab title from
     * the given note title property.
     *
     * @param noteTitle        the observable note title
     * @param getNoteQuery     the query interface for reading notes
     * @param createNoteUseCase the use case for creating notes
     * @param renameNoteUseCase the use case for renaming notes
     * @param moveNoteUseCase  the use case for moving notes
     * @param deleteNoteUseCase the use case for deleting notes
     * @param outlineNavQuery  the query for outline navigation
     * @param appState         the shared application state for
     *                         data-change notification
     */
    public OutlineViewModel(StringProperty noteTitle,
            GetNoteQuery getNoteQuery,
            CreateNoteUseCase createNoteUseCase,
            RenameNoteUseCase renameNoteUseCase,
            MoveNoteUseCase moveNoteUseCase,
            DeleteNoteUseCase deleteNoteUseCase,
            GetOutlineNavigationQuery outlineNavQuery,
            AppState appState, EventBus eventBus) {
        Objects.requireNonNull(noteTitle, "noteTitle must not be null");
        this.getNoteQuery = Objects.requireNonNull(getNoteQuery,
                "getNoteQuery must not be null");
        this.createNoteUseCase = Objects.requireNonNull(
                createNoteUseCase,
                "createNoteUseCase must not be null");
        this.renameNoteUseCase = Objects.requireNonNull(
                renameNoteUseCase,
                "renameNoteUseCase must not be null");
        this.moveNoteUseCase = Objects.requireNonNull(
                moveNoteUseCase,
                "moveNoteUseCase must not be null");
        this.deleteNoteUseCase = Objects.requireNonNull(
                deleteNoteUseCase,
                "deleteNoteUseCase must not be null");
        this.outlineNavQuery = Objects.requireNonNull(
                outlineNavQuery,
                "outlineNavQuery must not be null");
        this.appState = Objects.requireNonNull(appState,
                "appState must not be null");
        this.eventBus = Objects.requireNonNull(eventBus,
                "eventBus must not be null");
        this.rootNoteTitle = noteTitle;
        updateTabTitle(noteTitle.get());
        // When the root note title changes and we're at the root level, update tab title
        noteTitle.addListener((obs, oldVal, newVal) -> {
            if (navigationStack.isAtRoot()) {
                updateTabTitle(newVal);
            }
        });
    }

    /** Returns the tab title property. */
    public ReadOnlyStringProperty tabTitleProperty() {
        return tabTitle.getReadOnlyProperty();
    }

    /** Returns the observable list of root-level note display items. */
    public ObservableList<NoteDisplayItem> getRootItems() {
        return rootItems;
    }

    /** Returns the selected note id property. */
    public ObjectProperty<UUID> selectedNoteIdProperty() {
        return selectedNoteId;
    }

    /**
     * Sets the base note (root container) whose children are displayed.
     *
     * @param noteId the base note id
     */
    public void setBaseNoteId(UUID noteId) {
        String name = getNoteQuery.getNote(noteId)
                .map(Note::getTitle).orElse("Unknown");
        navigationStack.setCurrentId(noteId, name);
    }

    /** Returns the base note id. */
    public UUID getBaseNoteId() {
        return navigationStack.getCurrentId();
    }

    /**
     * Selects a note by id.
     *
     * @param noteId the note id to select, or null to clear selection
     */
    public void selectNote(UUID noteId) {
        selectedNoteId.set(noteId);
    }

    /** Loads the children of the base note into the observable list. */
    public void loadNotes() {
        UUID baseNoteId = navigationStack.getCurrentId();
        if (baseNoteId == null) {
            rootItems.clear();
            return;
        }
        List<Note> children = getNoteQuery.getChildren(baseNoteId);
        Map<UUID, Boolean> hasChildrenMap = getNoteQuery.hasChildrenBatch(
                children.stream().map(Note::getId).toList());
        rootItems.setAll(
                children.stream()
                        .map(note -> toDisplayItem(note,
                                hasChildrenMap.getOrDefault(note.getId(), false)))
                        .toList());
    }

    /**
     * Creates a new child note under the given parent with the given title.
     *
     * @param parentId the parent note id
     * @param title    the title for the new note
     * @return the display item for the created note
     */
    public NoteDisplayItem createChildNote(UUID parentId, String title) {
        Note child = createNoteUseCase.createChildNote(parentId, title);
        NoteDisplayItem item = toDisplayItem(child);
        // Add to root items if parent is the base note
        if (parentId.equals(navigationStack.getCurrentId())) {
            rootItems.add(item);
        }
        eventBus.publish(new NoteCreatedEvent(child.getId()));
        return item;
    }

    /**
     * Creates a new sibling note directly after the given sibling and reloads the tree.
     *
     * @param siblingId the sibling note id
     * @param title     the title for the new note (may be empty)
     * @return the display item for the created note
     */
    public NoteDisplayItem createSiblingNote(UUID siblingId, String title) {
        Note sibling = createNoteUseCase.createSiblingNote(siblingId, title);
        NoteDisplayItem item = toDisplayItem(sibling);
        loadNotes();
        eventBus.publish(new NoteCreatedEvent(sibling.getId()));
        return item;
    }

    /**
     * Indents a note (makes it a child of the note above) and reloads the tree.
     *
     * @param noteId the note id to indent
     */
    public void indentNote(UUID noteId) {
        moveNoteUseCase.indentNote(noteId);
        loadNotes();
        eventBus.publish(new NoteMovedEvent(noteId));
    }

    /**
     * Outdents a note (moves it up one level) and reloads the tree.
     *
     * @param noteId the note id to outdent
     */
    public void outdentNote(UUID noteId) {
        moveNoteUseCase.outdentNote(noteId);
        loadNotes();
        eventBus.publish(new NoteMovedEvent(noteId));
    }

    /**
     * Moves a note to a position within a parent and reloads.
     *
     * @param noteId      the note to move
     * @param newParentId the target parent
     * @param position    zero-based position among siblings
     */
    public void moveNoteToPosition(UUID noteId,
            UUID newParentId, int position) {
        moveNoteUseCase.moveNoteToPosition(noteId, newParentId,
                position);
        loadNotes();
        eventBus.publish(new NoteMovedEvent(noteId));
    }

    /**
     * Renames a note, updating the display item in the root items list if present.
     *
     * @param noteId   the note id
     * @param newTitle the new title
     * @return true if the rename succeeded, false if the title was blank
     */
    public boolean renameNote(UUID noteId, String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            return false;
        }
        renameNoteUseCase.renameNote(noteId, newTitle);
        for (int i = 0; i < rootItems.size(); i++) {
            NoteDisplayItem item = rootItems.get(i);
            if (item.getId().equals(noteId)) {
                rootItems.set(i, new NoteDisplayItem(
                        item.getId(), newTitle, item.getContent(),
                        item.getXpos(), item.getYpos(),
                        item.getWidth(), item.getHeight(),
                        item.getColorHex(), item.isHasChildren(),
                        item.getBadge()));
                break;
            }
        }
        eventBus.publish(new NoteRenamedEvent(noteId, newTitle));
        return true;
    }

    /** Returns the canNavigateBack property. */
    public ReadOnlyBooleanProperty canNavigateBackProperty() {
        return navigationStack.canNavigateBackProperty();
    }

    /**
     * Returns the observable breadcrumb trail for the current drill-down path.
     *
     * @return the unmodifiable observable list of breadcrumb entries
     */
    public ObservableList<BreadcrumbEntry> getBreadcrumbs() {
        return navigationStack.getBreadcrumbs();
    }

    /**
     * Navigates to the breadcrumb at the given index, reloading notes
     * and updating the tab title.
     *
     * @param index the zero-based breadcrumb index to navigate to
     */
    public void navigateToBreadcrumb(int index) {
        navigationStack.navigateTo(index);
        if (navigationStack.isAtRoot()) {
            updateTabTitle(rootNoteTitle.get());
        } else {
            getNoteQuery.getNote(navigationStack.getCurrentId())
                    .ifPresent(note -> updateTabTitle(note.getTitle()));
        }
        loadNotes();
    }

    /**
     * Drills down into a child note, making it the new base note.
     *
     * @param noteId the note id to drill into
     */
    public void drillDown(UUID noteId) {
        String name = getNoteQuery.getNote(noteId)
                .map(Note::getTitle).orElse("Unknown");
        navigationStack.push(noteId, name);
        updateTabTitle(name);
        loadNotes();
        eventBus.publish(new NoteMovedEvent(noteId));
    }

    /**
     * Navigates back to the previous base note.
     */
    public void navigateBack() {
        UUID previous = navigationStack.pop();
        if (previous == null) {
            return;
        }
        if (navigationStack.isAtRoot()) {
            updateTabTitle(rootNoteTitle.get());
        } else {
            getNoteQuery.getNote(previous).ifPresent(note ->
                    updateTabTitle(note.getTitle()));
        }
        loadNotes();
        eventBus.publish(new NoteMovedEvent(previous));
    }

    /**
     * Returns whether the note with the given id has any children.
     *
     * @param noteId the note id
     * @return true if the note has children
     */
    public boolean hasChildren(UUID noteId) {
        return getNoteQuery.hasChildren(noteId);
    }

    /**
     * Returns the id of the previous note in outline order, or null if none.
     *
     * @param noteId the note id
     * @return the previous note id, or null
     */
    public UUID getPreviousNoteId(UUID noteId) {
        return outlineNavQuery.getPreviousInOutline(noteId)
                .map(Note::getId)
                .orElse(null);
    }

    /**
     * Deletes a leaf note and reloads the tree.
     *
     * @param noteId the note id to delete
     * @return true if the note was deleted, false if it has children or does not exist
     */
    public boolean deleteNote(UUID noteId) {
        boolean deleted = deleteNoteUseCase.deleteNoteIfLeaf(noteId);
        if (deleted) {
            loadNotes();
            eventBus.publish(new NoteDeletedEvent(noteId));
        }
        return deleted;
    }

    /**
     * Returns the children of the given note as display items.
     *
     * @param parentId the parent note id
     * @return the list of child display items
     */
    public ObservableList<NoteDisplayItem> getChildren(UUID parentId) {
        List<Note> children = getNoteQuery.getChildren(parentId);
        Map<UUID, Boolean> hasChildrenMap = getNoteQuery.hasChildrenBatch(
                children.stream().map(Note::getId).toList());
        return FXCollections.observableArrayList(
                children.stream()
                        .map(note -> toDisplayItem(note,
                                hasChildrenMap.getOrDefault(note.getId(), false)))
                        .toList());
    }

    private void updateTabTitle(String title) {
        tabTitle.set(TextUtils.tabTitle("Outline", title, MAX_TITLE_LENGTH));
    }

    private NoteDisplayItem toDisplayItem(Note note) {
        return toDisplayItem(note, getNoteQuery.hasChildren(note.getId()));
    }

    private NoteDisplayItem toDisplayItem(Note note, boolean hasChildren) {
        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent(),
                0, 0, 0, 0,
                NoteDisplayHelper.resolveColorHex(note),
                hasChildren,
                NoteDisplayHelper.resolveBadge(note));
    }
}
