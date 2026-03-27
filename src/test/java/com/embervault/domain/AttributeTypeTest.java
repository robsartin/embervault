package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeTypeTest {

    @Test
    @DisplayName("STRING javaType returns String.class")
    void string_javaType() {
        assertEquals(String.class, AttributeType.STRING.javaType());
    }

    @Test
    @DisplayName("NUMBER javaType returns Double.class")
    void number_javaType() {
        assertEquals(Double.class, AttributeType.NUMBER.javaType());
    }

    @Test
    @DisplayName("BOOLEAN javaType returns Boolean.class")
    void boolean_javaType() {
        assertEquals(Boolean.class, AttributeType.BOOLEAN.javaType());
    }

    @Test
    @DisplayName("COLOR javaType returns TbxColor.class")
    void color_javaType() {
        assertEquals(TbxColor.class, AttributeType.COLOR.javaType());
    }

    @Test
    @DisplayName("DATE javaType returns Instant.class")
    void date_javaType() {
        assertEquals(Instant.class, AttributeType.DATE.javaType());
    }

    @Test
    @DisplayName("INTERVAL javaType returns Duration.class")
    void interval_javaType() {
        assertEquals(Duration.class, AttributeType.INTERVAL.javaType());
    }

    @Test
    @DisplayName("FILE javaType returns String.class")
    void file_javaType() {
        assertEquals(String.class, AttributeType.FILE.javaType());
    }

    @Test
    @DisplayName("URL javaType returns String.class")
    void url_javaType() {
        assertEquals(String.class, AttributeType.URL.javaType());
    }

    @Test
    @DisplayName("LIST javaType returns List.class")
    void list_javaType() {
        assertEquals(List.class, AttributeType.LIST.javaType());
    }

    @Test
    @DisplayName("SET javaType returns Set.class")
    void set_javaType() {
        assertEquals(Set.class, AttributeType.SET.javaType());
    }

    @Test
    @DisplayName("ACTION javaType returns String.class")
    void action_javaType() {
        assertEquals(String.class, AttributeType.ACTION.javaType());
    }

    @Test
    @DisplayName("All 11 attribute types are defined")
    void allTypesExist() {
        assertEquals(11, AttributeType.values().length);
    }

    @Test
    @DisplayName("Every type has a non-null javaType")
    void everyTypeHasJavaType() {
        for (AttributeType type : AttributeType.values()) {
            assertNotNull(type.javaType(), "javaType for " + type + " must not be null");
        }
    }
}
