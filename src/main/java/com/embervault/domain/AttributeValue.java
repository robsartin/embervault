package com.embervault.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A sealed interface representing a typed attribute value in the Tinderbox model.
 *
 * <p>Uses Java sealed types with record variants for compile-time type safety
 * and exhaustive pattern matching in switch expressions.</p>
 */
public sealed interface AttributeValue
        permits AttributeValue.StringValue,
        AttributeValue.NumberValue,
        AttributeValue.BooleanValue,
        AttributeValue.ColorValue,
        AttributeValue.DateValue,
        AttributeValue.IntervalValue,
        AttributeValue.FileValue,
        AttributeValue.UrlValue,
        AttributeValue.ListValue,
        AttributeValue.SetValue,
        AttributeValue.ActionValue {

    /**
     * Infers the appropriate {@code AttributeValue} variant from a Java object.
     *
     * @param value the Java object to wrap
     * @return the corresponding AttributeValue
     * @throws IllegalArgumentException if the type cannot be mapped
     */
    static AttributeValue of(Object value) {
        Objects.requireNonNull(value, "value must not be null");
        return switch (value) {
            case String s -> new StringValue(s);
            case Double d -> new NumberValue(d);
            case Integer i -> new NumberValue(i.doubleValue());
            case Boolean b -> new BooleanValue(b);
            case TbxColor c -> new ColorValue(c);
            case Instant inst -> new DateValue(inst);
            case Duration dur -> new IntervalValue(dur);
            default -> throw new IllegalArgumentException(
                    "Cannot infer AttributeValue from: " + value.getClass().getName());
        };
    }

    /**
     * A string attribute value.
     *
     * @param value the string value
     */
    record StringValue(String value) implements AttributeValue {}

    /**
     * A numeric attribute value.
     *
     * @param value the double value
     */
    record NumberValue(double value) implements AttributeValue {}

    /**
     * A boolean attribute value.
     *
     * @param value the boolean value
     */
    record BooleanValue(boolean value) implements AttributeValue {}

    /**
     * A color attribute value.
     *
     * @param value the TbxColor value
     */
    record ColorValue(TbxColor value) implements AttributeValue {}

    /**
     * A date/time attribute value.
     *
     * @param value the Instant value
     */
    record DateValue(Instant value) implements AttributeValue {}

    /**
     * A time interval attribute value.
     *
     * @param value the Duration value
     */
    record IntervalValue(Duration value) implements AttributeValue {}

    /**
     * A file path attribute value.
     *
     * @param path the file path
     */
    record FileValue(String path) implements AttributeValue {}

    /**
     * A URL attribute value.
     *
     * @param url the URL string
     */
    record UrlValue(String url) implements AttributeValue {}

    /**
     * An ordered list of string values.
     *
     * @param values the list of strings (defensively copied)
     */
    record ListValue(List<String> values) implements AttributeValue {
        /**
         * Creates a ListValue with a defensive copy of the provided list.
         *
         * @param values the list of strings
         */
        public ListValue {
            values = List.copyOf(values);
        }
    }

    /**
     * An unordered set of unique string values.
     *
     * @param values the set of strings (defensively copied)
     */
    record SetValue(Set<String> values) implements AttributeValue {
        /**
         * Creates a SetValue with a defensive copy of the provided set.
         *
         * @param values the set of strings
         */
        public SetValue {
            values = Set.copyOf(values);
        }
    }

    /**
     * An action expression attribute value.
     *
     * @param expression the action expression string
     */
    record ActionValue(String expression) implements AttributeValue {}
}
