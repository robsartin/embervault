package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;

import com.embervault.domain.AttributeType;

/**
 * Represents an editable attribute name/value pair for display in the Note Editor.
 *
 * <p>Decouples the editor view from the domain {@code AttributeValue} sealed interface,
 * presenting all values as strings for simple text-based editing.</p>
 */
public final class AttributeEntry {

    private final String name;
    private final String value;
    private final AttributeType type;

    /**
     * Constructs an attribute entry.
     *
     * @param name  the attribute name (e.g., "$Color")
     * @param value the string representation of the attribute value
     * @param type  the attribute type
     */
    public AttributeEntry(String name, String value, AttributeType type) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.value = Objects.requireNonNull(value, "value must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    /** Returns the attribute name. */
    public String getName() {
        return name;
    }

    /** Returns the string representation of the attribute value. */
    public String getValue() {
        return value;
    }

    /** Returns the attribute type. */
    public AttributeType getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AttributeEntry other)) {
            return false;
        }
        return name.equals(other.name)
                && value.equals(other.value)
                && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, type);
    }

    @Override
    public String toString() {
        return name + "=" + value + " (" + type + ")";
    }
}
