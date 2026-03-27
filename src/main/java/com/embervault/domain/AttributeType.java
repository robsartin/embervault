package com.embervault.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Defines the 11 Tinderbox attribute types with their Java mappings.
 */
public enum AttributeType {

    /** A string attribute. */
    STRING(String.class),

    /** A numeric attribute stored as a double. */
    NUMBER(Double.class),

    /** A boolean attribute. */
    BOOLEAN(Boolean.class),

    /** A color attribute stored as a {@link TbxColor}. */
    COLOR(TbxColor.class),

    /** A date/time attribute stored as an {@link Instant}. */
    DATE(Instant.class),

    /** A time interval attribute stored as a {@link Duration}. */
    INTERVAL(Duration.class),

    /** A file path attribute stored as a string. */
    FILE(String.class),

    /** A URL attribute stored as a string. */
    URL(String.class),

    /** An ordered list of strings. */
    LIST(List.class),

    /** An unordered set of strings with no duplicates. */
    SET(Set.class),

    /** An action expression stored as a string. */
    ACTION(String.class);

    private final Class<?> javaType;

    AttributeType(Class<?> javaType) {
        this.javaType = javaType;
    }

    /**
     * Returns the Java class that corresponds to this attribute type.
     *
     * @return the Java type
     */
    public Class<?> javaType() {
        return javaType;
    }
}
