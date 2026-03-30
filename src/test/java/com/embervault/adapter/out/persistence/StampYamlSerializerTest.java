package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.embervault.domain.Stamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StampYamlSerializer}.
 */
class StampYamlSerializerTest {

    @Test
    @DisplayName("serialize produces valid YAML")
    void serialize_stampsToYaml() {
        Stamp stamp = Stamp.create("Color:red", "$Color=red");
        String yaml = StampYamlSerializer.serialize(
                List.of(stamp));

        assertTrue(yaml.startsWith("stamps:\n"));
        assertTrue(yaml.contains("name: \"Color:red\""));
        assertTrue(yaml.contains("action: \"$Color=red\""));
    }

    @Test
    @DisplayName("deserialize restores stamps")
    void deserialize_yamlToStamps() {
        Stamp original = Stamp.create("Mark Done",
                "$Checked=true");
        String yaml = StampYamlSerializer.serialize(
                List.of(original));
        List<Stamp> restored =
                StampYamlSerializer.deserialize(yaml);

        assertEquals(1, restored.size());
        assertEquals(original.id(), restored.get(0).id());
        assertEquals("Mark Done", restored.get(0).name());
        assertEquals("$Checked=true",
                restored.get(0).action());
    }

    @Test
    @DisplayName("empty list serializes to valid YAML")
    void serialize_emptyList() {
        String yaml = StampYamlSerializer.serialize(List.of());
        assertTrue(yaml.startsWith("stamps:\n"));
    }
}
