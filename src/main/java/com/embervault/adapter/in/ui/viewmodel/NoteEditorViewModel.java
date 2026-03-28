package com.embervault.adapter.in.ui.viewmodel;

import static com.embervault.domain.Attributes.NAME;
import static com.embervault.domain.Attributes.TEXT;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.NoteService;
import com.embervault.domain.AttributeDefinition;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeType;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Note Editor pane.
 *
 * <p>Provides editable properties for a selected note's title, text content,
 * and attribute values. Changes are persisted via the NoteService.</p>
 */
public final class NoteEditorViewModel {

    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty text = new SimpleStringProperty("");
    private final ObservableList<AttributeEntry> editableAttributes =
            FXCollections.observableArrayList();
    private final NoteService noteService;
    private final AttributeSchemaRegistry schemaRegistry;
    private UUID currentNoteId;
    private Runnable onDataChanged;

    /**
     * Constructs a NoteEditorViewModel.
     *
     * @param noteService    the note service for querying and updating notes
     * @param schemaRegistry the attribute schema registry
     */
    public NoteEditorViewModel(NoteService noteService,
            AttributeSchemaRegistry schemaRegistry) {
        this.noteService = Objects.requireNonNull(noteService,
                "noteService must not be null");
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry,
                "schemaRegistry must not be null");
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

    /** Returns the title property. */
    public StringProperty titleProperty() {
        return title;
    }

    /** Returns the text property. */
    public StringProperty textProperty() {
        return text;
    }

    /** Returns the observable list of editable attribute entries. */
    public ObservableList<AttributeEntry> getEditableAttributes() {
        return editableAttributes;
    }

    /** Returns the currently loaded note id. */
    public UUID getCurrentNoteId() {
        return currentNoteId;
    }

    /**
     * Loads a note for editing.
     *
     * @param noteId the id of the note to edit, or null to clear the editor
     */
    public void setNote(UUID noteId) {
        this.currentNoteId = noteId;
        if (noteId == null) {
            title.set("");
            text.set("");
            editableAttributes.clear();
            return;
        }
        noteService.getNote(noteId).ifPresent(note -> {
            title.set(note.getTitle());
            text.set(note.getContent());
            loadAttributes(note);
        });
    }

    /**
     * Saves a new title for the current note.
     *
     * @param newTitle the new title
     */
    public void saveTitle(String newTitle) {
        if (currentNoteId == null || newTitle == null || newTitle.isBlank()) {
            return;
        }
        noteService.renameNote(currentNoteId, newTitle);
        title.set(newTitle);
        notifyDataChanged();
    }

    /**
     * Saves new text content for the current note.
     *
     * @param newText the new text content
     */
    public void saveText(String newText) {
        if (currentNoteId == null || newText == null) {
            return;
        }
        noteService.getNote(currentNoteId).ifPresent(note -> {
            note.setAttribute(TEXT, new AttributeValue.StringValue(newText));
        });
        text.set(newText);
        notifyDataChanged();
    }

    /**
     * Saves a new value for the named attribute on the current note.
     *
     * @param name  the attribute name
     * @param value the new string value
     */
    public void saveAttribute(String name, String value) {
        if (currentNoteId == null || name == null || value == null) {
            return;
        }
        noteService.getNote(currentNoteId).ifPresent(note -> {
            AttributeValue attrValue = parseAttributeValue(name, value);
            note.setAttribute(name, attrValue);
        });
        // Reload attributes to reflect the change
        noteService.getNote(currentNoteId).ifPresent(this::loadAttributes);
        notifyDataChanged();
    }

    private void loadAttributes(Note note) {
        editableAttributes.clear();
        Map<String, AttributeValue> entries = note.getAttributes().localEntries();
        for (Map.Entry<String, AttributeValue> entry : entries.entrySet()) {
            String attrName = entry.getKey();
            // Skip $Name and $Text since they have dedicated fields
            if (NAME.equals(attrName) || TEXT.equals(attrName)) {
                continue;
            }
            // Skip intrinsic/read-only attributes
            AttributeDefinition def = schemaRegistry.get(attrName).orElse(null);
            if (def != null && def.readOnly()) {
                continue;
            }
            AttributeType type = def != null ? def.type() : AttributeType.STRING;
            String valueStr = attributeValueToString(entry.getValue());
            editableAttributes.add(new AttributeEntry(attrName, valueStr, type));
        }
    }

    private String attributeValueToString(AttributeValue value) {
        return switch (value) {
            case AttributeValue.StringValue sv -> sv.value();
            case AttributeValue.NumberValue nv -> String.valueOf(nv.value());
            case AttributeValue.BooleanValue bv -> String.valueOf(bv.value());
            case AttributeValue.ColorValue cv -> cv.value().toHex();
            case AttributeValue.DateValue dv -> dv.value().toString();
            case AttributeValue.IntervalValue iv -> iv.value().toString();
            case AttributeValue.FileValue fv -> fv.path();
            case AttributeValue.UrlValue uv -> uv.url();
            case AttributeValue.ListValue lv -> String.join(";", lv.values());
            case AttributeValue.SetValue setv -> String.join(";", setv.values());
            case AttributeValue.ActionValue av -> av.expression();
        };
    }

    private AttributeValue parseAttributeValue(String name, String value) {
        AttributeDefinition def = schemaRegistry.get(name).orElse(null);
        if (def == null) {
            return new AttributeValue.StringValue(value);
        }
        return switch (def.type()) {
            case STRING, FILE, URL, ACTION ->
                    new AttributeValue.StringValue(value);
            case NUMBER -> {
                try {
                    yield new AttributeValue.NumberValue(Double.parseDouble(value));
                } catch (NumberFormatException e) {
                    yield new AttributeValue.NumberValue(0);
                }
            }
            case BOOLEAN ->
                    new AttributeValue.BooleanValue(Boolean.parseBoolean(value));
            default -> new AttributeValue.StringValue(value);
        };
    }
}
