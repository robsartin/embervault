package com.embervault.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.embervault.domain.Link;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LinkYamlSerializer}.
 */
class LinkYamlSerializerTest {

    @Test
    @DisplayName("serialize produces valid YAML")
    void serialize_linksToYaml() {
        Link link = Link.create(
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(), "web");
        String yaml = LinkYamlSerializer.serialize(List.of(link));

        assertTrue(yaml.startsWith("links:\n"));
        assertTrue(yaml.contains("sourceId:"));
        assertTrue(yaml.contains("type: \"web\""));
    }

    @Test
    @DisplayName("deserialize restores links")
    void deserialize_yamlToLinks() {
        Link original = Link.create(
                java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID(), "prototype");
        String yaml = LinkYamlSerializer.serialize(
                List.of(original));
        List<Link> restored =
                LinkYamlSerializer.deserialize(yaml);

        assertEquals(1, restored.size());
        assertEquals(original.id(), restored.get(0).id());
        assertEquals(original.sourceId(),
                restored.get(0).sourceId());
        assertEquals(original.type(),
                restored.get(0).type());
    }

    @Test
    @DisplayName("empty list serializes to valid YAML")
    void serialize_emptyList() {
        String yaml = LinkYamlSerializer.serialize(List.of());
        assertTrue(yaml.startsWith("links:\n"));
    }
}
