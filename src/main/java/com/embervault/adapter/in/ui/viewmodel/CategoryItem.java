package com.embervault.adapter.in.ui.viewmodel;

import java.util.List;
import java.util.Objects;

/**
 * A category grouping for the Attribute Browser view.
 *
 * <p>Each category represents a distinct value of the selected grouping
 * attribute, along with the notes that share that value.</p>
 *
 * @param value the attribute value for this category
 * @param notes the notes belonging to this category
 * @param count the number of notes in this category
 */
public record CategoryItem(
        String value,
        List<NoteDisplayItem> notes,
        int count
) {

    /**
     * Canonical constructor with validation and defensive copy.
     */
    public CategoryItem {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(notes, "notes must not be null");
        notes = List.copyOf(notes);
    }
}
