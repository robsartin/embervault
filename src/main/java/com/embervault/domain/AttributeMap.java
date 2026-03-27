package com.embervault.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A sparse map holding a note's locally-set attribute values.
 *
 * <p>Only attributes that have been explicitly set on a note are stored here.
 * Attributes not present in the map should be resolved through the prototype
 * chain and then the document default.</p>
 */
public final class AttributeMap {

    private final Map<String, AttributeValue> values;

    /**
     * Creates an empty attribute map.
     */
    public AttributeMap() {
        this.values = new LinkedHashMap<>();
    }

    /**
     * Creates a copy of the given attribute map.
     *
     * @param other the map to copy
     */
    public AttributeMap(AttributeMap other) {
        this.values = new LinkedHashMap<>(other.values);
    }

    /**
     * Gets the locally-set value for the given attribute name.
     *
     * @param name the attribute name
     * @return an optional containing the value, or empty if not locally set
     */
    public Optional<AttributeValue> get(String name) {
        return Optional.ofNullable(values.get(name));
    }

    /**
     * Sets a local value for the given attribute name.
     *
     * @param name  the attribute name
     * @param value the value to set
     */
    public void set(String name, AttributeValue value) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(value, "value must not be null");
        values.put(name, value);
    }

    /**
     * Removes the local value for the given attribute name.
     *
     * @param name the attribute name
     */
    public void remove(String name) {
        values.remove(name);
    }

    /**
     * Returns whether the given attribute has a locally-set value.
     *
     * @param name the attribute name
     * @return true if the attribute is locally set
     */
    public boolean hasLocalValue(String name) {
        return values.containsKey(name);
    }

    /**
     * Returns an unmodifiable view of all locally-set entries.
     *
     * @return an unmodifiable map of attribute names to values
     */
    public Map<String, AttributeValue> localEntries() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Returns the number of locally-set attributes.
     *
     * @return the count
     */
    public int size() {
        return values.size();
    }
}
