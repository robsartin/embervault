package com.embervault.domain;

import java.util.List;
import java.util.Objects;

/**
 * Defines the schema for a single attribute in the Tinderbox model.
 *
 * <p>An attribute definition specifies the name, type, default value, and
 * metadata flags such as whether the attribute is intrinsic, read-only, or
 * system-defined.</p>
 *
 * @param name            the attribute name, case-sensitive (e.g., "$Name")
 * @param type            the attribute type
 * @param defaultValue    the document-level default value
 * @param description     a human-readable description
 * @param suggestedValues optional suggested values for UI dropdowns
 * @param readOnly        whether the attribute is computed/read-only
 * @param intrinsic       whether the attribute is intrinsic (never inherited from prototypes)
 * @param system          whether this is a built-in system attribute
 */
public record AttributeDefinition(
        String name,
        AttributeType type,
        AttributeValue defaultValue,
        String description,
        List<String> suggestedValues,
        boolean readOnly,
        boolean intrinsic,
        boolean system
) {

    /**
     * Canonical constructor with validation.
     */
    public AttributeDefinition {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(defaultValue, "defaultValue must not be null");
        Objects.requireNonNull(description, "description must not be null");
        suggestedValues = List.copyOf(suggestedValues);
    }

    /**
     * Creates a new builder for an AttributeDefinition.
     *
     * @param name the attribute name
     * @param type the attribute type
     * @return a new builder
     */
    public static Builder builder(String name, AttributeType type) {
        return new Builder(name, type);
    }

    /**
     * Builder for {@link AttributeDefinition}.
     */
    public static final class Builder {

        private final String name;
        private final AttributeType type;
        private AttributeValue defaultValue;
        private String description = "";
        private List<String> suggestedValues = List.of();
        private boolean readOnly;
        private boolean intrinsic;
        private boolean system;

        private Builder(String name, AttributeType type) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.type = Objects.requireNonNull(type, "type must not be null");
        }

        /** Sets the default value. */
        public Builder defaultValue(AttributeValue value) {
            this.defaultValue = value;
            return this;
        }

        /** Sets the description. */
        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        /** Sets the suggested values. */
        public Builder suggestedValues(List<String> values) {
            this.suggestedValues = values;
            return this;
        }

        /** Sets the read-only flag. */
        public Builder readOnly(boolean flag) {
            this.readOnly = flag;
            return this;
        }

        /** Sets the intrinsic flag. */
        public Builder intrinsic(boolean flag) {
            this.intrinsic = flag;
            return this;
        }

        /** Sets the system flag. */
        public Builder system(boolean flag) {
            this.system = flag;
            return this;
        }

        /**
         * Builds the AttributeDefinition.
         *
         * @return the attribute definition
         */
        public AttributeDefinition build() {
            Objects.requireNonNull(defaultValue, "defaultValue must not be null");
            return new AttributeDefinition(
                    name, type, defaultValue, description,
                    suggestedValues, readOnly, intrinsic, system);
        }
    }
}
