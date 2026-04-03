package com.embervault.adapter.in.ui.viewmodel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.embervault.application.port.in.GetNoteQuery;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeType;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Note;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Attribute Browser view tab.
 *
 * <p>Groups all notes by the values of a chosen attribute, allowing the user
 * to browse notes categorized by that attribute. Follows the Tinderbox Attribute
 * Browser model.</p>
 */
public final class AttributeBrowserViewModel {

    private static final int MAX_TITLE_LENGTH = 20;
    private static final String NONE_CATEGORY = "(none)";
    private static final Set<AttributeType> GROUPABLE_TYPES = EnumSet.of(
            AttributeType.STRING,
            AttributeType.BOOLEAN,
            AttributeType.COLOR,
            AttributeType.SET,
            AttributeType.LIST
    );

    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ObservableList<CategoryItem> categories =
            FXCollections.observableArrayList();
    private final ObservableList<String> availableAttributes =
            FXCollections.observableArrayList();
    private final ObjectProperty<UUID> selectedNoteId =
            new SimpleObjectProperty<>();
    private final GetNoteQuery getNoteQuery;
    private final AttributeSchemaRegistry schemaRegistry;
    private String selectedAttribute;
    private final AppState appState;

    /**
     * Constructs an AttributeBrowserViewModel.
     *
     * @param getNoteQuery   the query interface for reading notes
     * @param schemaRegistry the attribute schema registry
     * @param appState       the shared application state for
     *                       data-change notification
     */
    public AttributeBrowserViewModel(GetNoteQuery getNoteQuery,
            AttributeSchemaRegistry schemaRegistry,
            AppState appState) {
        this.getNoteQuery = Objects.requireNonNull(getNoteQuery,
                "getNoteQuery must not be null");
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry,
                "schemaRegistry must not be null");
        this.appState = Objects.requireNonNull(appState,
                "appState must not be null");
        updateTabTitle(null);
        loadAvailableAttributes();
    }

    /** Returns the tab title property. */
    public ReadOnlyStringProperty tabTitleProperty() {
        return tabTitle.getReadOnlyProperty();
    }

    /** Returns the observable list of category items. */
    public ObservableList<CategoryItem> getCategories() {
        return categories;
    }

    /** Returns the observable list of available attribute names. */
    public ObservableList<String> getAvailableAttributes() {
        return availableAttributes;
    }

    /** Returns the selected note id property. */
    public ObjectProperty<UUID> selectedNoteIdProperty() {
        return selectedNoteId;
    }

    /** Returns the currently selected grouping attribute name. */
    public String getSelectedAttribute() {
        return selectedAttribute;
    }

    /**
     * Sets the attribute to group notes by and regroups.
     *
     * @param attributeName the attribute name to group by (e.g., "$Color")
     */
    public void setSelectedAttribute(String attributeName) {
        this.selectedAttribute = attributeName;
        updateTabTitle(attributeName);
        groupNotes();
    }

    /**
     * Selects a note by id.
     *
     * @param noteId the note id to select, or null to clear selection
     */
    public void selectNote(UUID noteId) {
        selectedNoteId.set(noteId);
    }

    /**
     * Groups all notes by the currently selected attribute value.
     *
     * <p>Notes with no value for the selected attribute are placed in a
     * "(none)" category. For Set and List attributes, a note may appear
     * in multiple categories.</p>
     */
    public void groupNotes() {
        categories.clear();
        if (selectedAttribute == null) {
            return;
        }

        List<Note> allNotes = getNoteQuery.getAllNotes();
        Map<String, List<NoteDisplayItem>> grouped = new LinkedHashMap<>();

        for (Note note : allNotes) {
            List<String> values = extractAttributeValues(note, selectedAttribute);
            if (values.isEmpty()) {
                grouped.computeIfAbsent(NONE_CATEGORY, k -> new ArrayList<>())
                        .add(toDisplayItem(note));
            } else {
                for (String val : values) {
                    grouped.computeIfAbsent(val, k -> new ArrayList<>())
                            .add(toDisplayItem(note));
                }
            }
        }

        for (Map.Entry<String, List<NoteDisplayItem>> entry : grouped.entrySet()) {
            categories.add(new CategoryItem(
                    entry.getKey(), entry.getValue(), entry.getValue().size()));
        }
    }

    private List<String> extractAttributeValues(Note note, String attrName) {
        return note.getAttribute(attrName)
                .map(this::attributeValueToStrings)
                .orElse(List.of());
    }

    private List<String> attributeValueToStrings(AttributeValue value) {
        return switch (value) {
            case AttributeValue.StringValue sv -> {
                String s = sv.value();
                yield s.isEmpty() ? List.of() : List.of(s);
            }
            case AttributeValue.BooleanValue bv ->
                    List.of(String.valueOf(bv.value()));
            case AttributeValue.ColorValue cv ->
                    List.of(cv.value().toHex());
            case AttributeValue.SetValue setv ->
                    setv.values().isEmpty() ? List.of() : List.copyOf(setv.values());
            case AttributeValue.ListValue lv ->
                    lv.values().isEmpty() ? List.of() : List.copyOf(lv.values());
            default -> {
                String s = value.toString();
                yield s.isEmpty() ? List.of() : List.of(s);
            }
        };
    }

    private void loadAvailableAttributes() {
        availableAttributes.clear();
        schemaRegistry.getAll().stream()
                .filter(def -> GROUPABLE_TYPES.contains(def.type()))
                .filter(def -> !def.intrinsic())
                .map(def -> def.name())
                .forEach(availableAttributes::add);
    }

    private void updateTabTitle(String attributeName) {
        tabTitle.set(TextUtils.tabTitle("Browser", attributeName,
                MAX_TITLE_LENGTH));
    }

    private NoteDisplayItem toDisplayItem(Note note) {
        return new NoteDisplayItem(
                note.getId(), note.getTitle(), note.getContent(),
                0, 0, 0, 0,
                NoteDisplayHelper.resolveColorHex(note),
                getNoteQuery.hasChildren(note.getId()),
                NoteDisplayHelper.resolveBadge(note));
    }
}
