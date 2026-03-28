package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.BadgeRegistry;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Treemap view tab.
 *
 * <p>Computes a tab title of the form "Treemap: &lt;note title&gt;" that updates
 * reactively when the underlying note title changes. Manages note display
 * items for treemap rendering and supports drill-down navigation.</p>
 */
public final class TreemapViewModel {

    private static final int MAX_TITLE_LENGTH = 20;
    private static final String DEFAULT_COLOR_HEX = "#808080";

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<NoteDisplayItem> noteItems =
            FXCollections.observableArrayList();
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final BooleanProperty canNavigateBack =
            new SimpleBooleanProperty(false);
    private final NoteService noteService;
    private final StringProperty rootNoteTitle;
    private final Deque<UUID> navigationHistory = new ArrayDeque<>();
    private UUID baseNoteId;
    private Runnable onDataChanged;

    /**
     * Constructs a TreemapViewModel that derives its tab title from the given note title property.
     *
     * @param noteTitle   the observable note title
     * @param noteService the note service for creating and querying notes
     */
    public TreemapViewModel(StringProperty noteTitle, NoteService noteService) {
        Objects.requireNonNull(noteTitle, "noteTitle must not be null");
        this.noteService = Objects.requireNonNull(noteService,
                "noteService must not be null");
        this.rootNoteTitle = noteTitle;
        updateTabTitle(noteTitle.get());
        noteTitle.addListener((obs, oldVal, newVal) -> {
            if (navigationHistory.isEmpty()) {
                updateTabTitle(newVal);
            }
        });
    }

    /**
     * Sets a callback to be invoked after any mutation operation.
     *
     * @param callback the callback to invoke, or null to clear
     */
    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    /** Returns the tab title property. */
    public ReadOnlyStringProperty tabTitleProperty() {
        return tabTitle.getReadOnlyProperty();
    }

    /** Returns the observable list of note display items. */
    public ObservableList<NoteDisplayItem> getNoteItems() {
        return noteItems;
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
        this.baseNoteId = noteId;
    }

    /** Returns the base note id. */
    public UUID getBaseNoteId() {
        return baseNoteId;
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
        if (baseNoteId == null) {
            noteItems.clear();
            return;
        }
        noteItems.setAll(
                noteService.getChildren(baseNoteId).stream()
                        .map(this::toDisplayItem)
                        .toList());
    }

    /**
     * Creates a new child note under the base note with the given title.
     *
     * @param title the title for the new note
     * @return the display item for the created note
     */
    public NoteDisplayItem createChildNote(String title) {
        Objects.requireNonNull(baseNoteId,
                "baseNoteId must be set before creating children");
        Note child = noteService.createChildNote(baseNoteId, title);
        NoteDisplayItem item = toDisplayItem(child);
        noteItems.add(item);
        notifyDataChanged();
        return item;
    }

    /** Returns the canNavigateBack property. */
    public ReadOnlyBooleanProperty canNavigateBackProperty() {
        return canNavigateBack;
    }

    /**
     * Drills down into a child note, making it the new base note.
     *
     * @param noteId the note id to drill into
     */
    public void drillDown(UUID noteId) {
        navigationHistory.push(baseNoteId);
        canNavigateBack.set(true);
        baseNoteId = noteId;
        noteService.getNote(noteId).ifPresent(note ->
                updateTabTitle(note.getTitle()));
        loadNotes();
        notifyDataChanged();
    }

    /**
     * Navigates back to the previous base note.
     */
    public void navigateBack() {
        if (navigationHistory.isEmpty()) {
            return;
        }
        baseNoteId = navigationHistory.pop();
        canNavigateBack.set(!navigationHistory.isEmpty());
        if (navigationHistory.isEmpty()) {
            updateTabTitle(rootNoteTitle.get());
        } else {
            noteService.getNote(baseNoteId).ifPresent(note ->
                    updateTabTitle(note.getTitle()));
        }
        loadNotes();
        notifyDataChanged();
    }

    /**
     * Computes treemap rectangle positions for the current notes at the given dimensions.
     *
     * <p>Each note is given equal weight (1.0) by default.</p>
     *
     * @param width  the available width
     * @param height the available height
     * @return the positioned rectangles
     */
    public List<TreemapRect> getTreemapRects(double width, double height) {
        List<TreemapItem> items = noteItems.stream()
                .map(n -> new TreemapItem(n.getId(), 1.0))
                .toList();
        return TreemapLayout.layout(items, 0, 0, width, height);
    }

    private void updateTabTitle(String title) {
        tabTitle.set("Treemap: " + TextUtils.truncate(title, MAX_TITLE_LENGTH));
    }

    private NoteDisplayItem toDisplayItem(Note note) {
        String colorHex = note.getAttribute("$Color")
                .map(v -> ((AttributeValue.ColorValue) v).value())
                .map(TbxColor::toHex)
                .orElse(DEFAULT_COLOR_HEX);
        String badge = resolveBadge(note);

        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent(),
                0, 0, 0, 0, colorHex,
                noteService.hasChildren(note.getId()), badge);
    }

    private static String resolveBadge(Note note) {
        return note.getAttribute("$Badge")
                .map(v -> ((AttributeValue.StringValue) v).value())
                .flatMap(BadgeRegistry::getBadgeSymbol)
                .orElse("");
    }
}
