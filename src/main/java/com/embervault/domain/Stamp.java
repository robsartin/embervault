package com.embervault.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * A named action that can be applied to notes to quickly change their attributes.
 *
 * <p>Stamps are reusable actions in the Tinderbox model. A stamp has a name
 * (e.g., "Color:red") and an action string (e.g., "$Color=red") that describes
 * which attribute to set and to what value.</p>
 *
 * @param id     the unique identifier
 * @param name   the stamp name (e.g., "Color:red", "Mark Done")
 * @param action the action expression (e.g., "$Color=red", "$Checked=true")
 */
public record Stamp(UUID id, String name, String action) {

    /**
     * Creates a Stamp with validation.
     *
     * @param id     the unique identifier
     * @param name   the stamp name
     * @param action the action expression
     */
    public Stamp {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(action, "action must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (action.isBlank()) {
            throw new IllegalArgumentException("action must not be blank");
        }
    }

    /**
     * Factory method that creates a new Stamp with a random UUID.
     *
     * @param name   the stamp name
     * @param action the action expression
     * @return the new stamp
     */
    public static Stamp create(String name, String action) {
        return new Stamp(UuidGenerator.generate(), name, action);
    }
}
