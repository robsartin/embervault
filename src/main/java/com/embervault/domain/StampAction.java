package com.embervault.domain;

import java.util.Objects;

/**
 * Utility for parsing stamp action strings into attribute name and value pairs.
 *
 * <p>Supports the {@code $AttrName=value} format. Type inference converts:</p>
 * <ul>
 *   <li>"true" / "false" to {@link AttributeValue.BooleanValue}</li>
 *   <li>Numeric strings to {@link AttributeValue.NumberValue}</li>
 *   <li>Known color names and hex codes to {@link AttributeValue.ColorValue}</li>
 *   <li>Everything else to {@link AttributeValue.StringValue}</li>
 * </ul>
 */
public final class StampAction {

    private StampAction() {
        // utility class
    }

    /**
     * The result of parsing a stamp action string.
     *
     * @param attributeName the attribute name (including the $ prefix)
     * @param value         the inferred attribute value
     */
    public record ParsedAction(String attributeName, AttributeValue value) {

        /**
         * Creates a ParsedAction with validation.
         *
         * @param attributeName the attribute name
         * @param value         the attribute value
         */
        public ParsedAction {
            Objects.requireNonNull(attributeName, "attributeName must not be null");
            Objects.requireNonNull(value, "value must not be null");
        }
    }

    /**
     * Parses an action string of the form {@code $AttrName=value}.
     *
     * @param action the action string to parse
     * @return the parsed action
     * @throws IllegalArgumentException if the format is invalid
     */
    public static ParsedAction parse(String action) {
        Objects.requireNonNull(action, "action must not be null");
        if (!action.startsWith("$")) {
            throw new IllegalArgumentException(
                    "Action must start with '$': " + action);
        }
        int eqIndex = action.indexOf('=');
        if (eqIndex < 0) {
            throw new IllegalArgumentException(
                    "Action must contain '=': " + action);
        }
        String attrName = action.substring(0, eqIndex).trim();
        if (attrName.length() < 2) {
            throw new IllegalArgumentException(
                    "Attribute name must not be empty: " + action);
        }
        String rawValue = action.substring(eqIndex + 1).trim();
        AttributeValue value = inferValue(rawValue);
        return new ParsedAction(attrName, value);
    }

    /**
     * Infers the AttributeValue type from a raw string value.
     *
     * @param raw the raw value string
     * @return the inferred AttributeValue
     */
    static AttributeValue inferValue(String raw) {
        // Boolean
        if ("true".equalsIgnoreCase(raw)) {
            return new AttributeValue.BooleanValue(true);
        }
        if ("false".equalsIgnoreCase(raw)) {
            return new AttributeValue.BooleanValue(false);
        }

        // Number
        try {
            double num = Double.parseDouble(raw);
            return new AttributeValue.NumberValue(num);
        } catch (NumberFormatException ignored) {
            // not a number, continue
        }

        // Color — hex format
        if (raw.startsWith("#") && raw.length() == 7) {
            try {
                TbxColor color = TbxColor.hex(raw);
                return new AttributeValue.ColorValue(color);
            } catch (IllegalArgumentException ignored) {
                // not a valid hex color
            }
        }

        // Color — named
        try {
            TbxColor color = TbxColor.named(raw);
            return new AttributeValue.ColorValue(color);
        } catch (IllegalArgumentException ignored) {
            // not a named color
        }

        // Default: string
        return new AttributeValue.StringValue(raw);
    }
}
