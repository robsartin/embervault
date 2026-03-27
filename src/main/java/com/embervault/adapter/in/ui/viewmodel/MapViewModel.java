package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Map view tab.
 *
 * <p>Computes a tab title of the form "Map: &lt;note title&gt;" that updates
 * reactively when the underlying note title changes. Manages note display
 * items for spatial rendering and supports creating child notes.</p>
 */
public final class MapViewModel {

    private static final double SCALE_X = 40.0;
    private static final double SCALE_Y = 40.0;
    private static final double DEFAULT_WIDTH = 6.0;
    private static final double DEFAULT_HEIGHT = 4.0;
    private static final String DEFAULT_COLOR_HEX = "#808080";

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<NoteDisplayItem> noteItems =
            FXCollections.observableArrayList();
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final NoteService noteService;
    private UUID baseNoteId;

    /**
     * Constructs a MapViewModel that derives its tab title from the given note title property.
     *
     * @param noteTitle   the observable note title
     * @param noteService the note service for creating and querying notes
     */
    public MapViewModel(StringProperty noteTitle, NoteService noteService) {
        Objects.requireNonNull(noteTitle, "noteTitle must not be null");
        this.noteService = Objects.requireNonNull(noteService,
                "noteService must not be null");
        tabTitle.bind(Bindings.concat("Map: ", noteTitle));
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
                        .map(MapViewModel::toDisplayItem)
                        .toList());
    }

    /**
     * Creates a new child note under the base note with the given title.
     *
     * @param title the title for the new note
     * @return the display item for the created note
     */
    public NoteDisplayItem createChildNote(String title) {
        Objects.requireNonNull(baseNoteId, "baseNoteId must be set before creating children");
        Note child = noteService.createChildNote(baseNoteId, title);
        NoteDisplayItem item = toDisplayItem(child);
        noteItems.add(item);
        return item;
    }

    /**
     * Creates a new child note at the specified canvas position.
     *
     * @param title the title for the new note
     * @param xpos  the x position on the canvas
     * @param ypos  the y position on the canvas
     * @return the display item for the created note
     */
    public NoteDisplayItem createChildNoteAt(String title, double xpos, double ypos) {
        Objects.requireNonNull(baseNoteId, "baseNoteId must be set before creating children");
        Note child = noteService.createChildNote(baseNoteId, title);
        child.setAttribute("$Xpos", new AttributeValue.NumberValue(xpos / SCALE_X));
        child.setAttribute("$Ypos", new AttributeValue.NumberValue(ypos / SCALE_Y));
        NoteDisplayItem item = toDisplayItem(child);
        noteItems.add(item);
        return item;
    }

    /**
     * Updates a note's position by setting its $Xpos and $Ypos attributes.
     *
     * @param noteId the note id
     * @param xpos   the new x position
     * @param ypos   the new y position
     */
    public void updateNotePosition(UUID noteId, double xpos, double ypos) {
        noteService.getNote(noteId).ifPresent(note -> {
            note.setAttribute("$Xpos", new AttributeValue.NumberValue(xpos / SCALE_X));
            note.setAttribute("$Ypos", new AttributeValue.NumberValue(ypos / SCALE_Y));
        });
        // Update the display item in place
        for (int i = 0; i < noteItems.size(); i++) {
            NoteDisplayItem item = noteItems.get(i);
            if (item.getId().equals(noteId)) {
                noteItems.set(i, new NoteDisplayItem(
                        item.getId(), item.getTitle(), item.getContent(),
                        xpos, ypos,
                        item.getWidth(), item.getHeight(),
                        item.getColorHex(), item.isHasChildren()));
                break;
            }
        }
    }

    private static NoteDisplayItem toDisplayItem(Note note) {
        double xpos = note.getAttribute("$Xpos")
                .map(v -> ((AttributeValue.NumberValue) v).value() * SCALE_X)
                .orElse(0.0);
        double ypos = note.getAttribute("$Ypos")
                .map(v -> ((AttributeValue.NumberValue) v).value() * SCALE_Y)
                .orElse(0.0);
        double width = note.getAttribute("$Width")
                .map(v -> ((AttributeValue.NumberValue) v).value() * SCALE_X)
                .orElse(DEFAULT_WIDTH * SCALE_X);
        double height = note.getAttribute("$Height")
                .map(v -> ((AttributeValue.NumberValue) v).value() * SCALE_Y)
                .orElse(DEFAULT_HEIGHT * SCALE_Y);
        String colorHex = note.getAttribute("$Color")
                .map(v -> ((AttributeValue.ColorValue) v).value())
                .map(TbxColor::toHex)
                .orElse(DEFAULT_COLOR_HEX);

        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent(),
                xpos, ypos, width, height, colorHex, note.hasChildren());
    }
}
