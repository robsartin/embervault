package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeDefinitionTest {

    @Test
    @DisplayName("Builder creates definition with all fields")
    void builder_createsDefinition() {
        AttributeDefinition def = AttributeDefinition.builder("$Name", AttributeType.STRING)
                .defaultValue(new AttributeValue.StringValue(""))
                .description("The name of the note")
                .suggestedValues(List.of("Untitled", "New Note"))
                .readOnly(false)
                .intrinsic(false)
                .system(true)
                .build();

        assertEquals("$Name", def.name());
        assertEquals(AttributeType.STRING, def.type());
        assertEquals(new AttributeValue.StringValue(""), def.defaultValue());
        assertEquals("The name of the note", def.description());
        assertEquals(List.of("Untitled", "New Note"), def.suggestedValues());
        assertFalse(def.readOnly());
        assertFalse(def.intrinsic());
        assertTrue(def.system());
    }

    @Test
    @DisplayName("Builder provides sensible defaults")
    void builder_sensibleDefaults() {
        AttributeDefinition def = AttributeDefinition.builder("$Test", AttributeType.STRING)
                .defaultValue(new AttributeValue.StringValue(""))
                .build();

        assertEquals("", def.description());
        assertTrue(def.suggestedValues().isEmpty());
        assertFalse(def.readOnly());
        assertFalse(def.intrinsic());
        assertFalse(def.system());
    }

    @Test
    @DisplayName("Builder rejects null name")
    void builder_rejectsNullName() {
        assertThrows(NullPointerException.class,
                () -> AttributeDefinition.builder(null, AttributeType.STRING));
    }

    @Test
    @DisplayName("Builder rejects null type")
    void builder_rejectsNullType() {
        assertThrows(NullPointerException.class,
                () -> AttributeDefinition.builder("$Name", null));
    }

    @Test
    @DisplayName("Builder rejects null defaultValue")
    void builder_rejectsNullDefaultValue() {
        assertThrows(NullPointerException.class,
                () -> AttributeDefinition.builder("$Name", AttributeType.STRING)
                        .defaultValue(null)
                        .build());
    }

    @Test
    @DisplayName("suggestedValues list is immutable")
    void suggestedValues_isImmutable() {
        AttributeDefinition def = AttributeDefinition.builder("$Test", AttributeType.STRING)
                .defaultValue(new AttributeValue.StringValue(""))
                .suggestedValues(List.of("a"))
                .build();

        assertThrows(UnsupportedOperationException.class,
                () -> def.suggestedValues().add("b"));
    }
}
