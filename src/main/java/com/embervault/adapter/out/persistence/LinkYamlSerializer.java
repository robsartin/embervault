package com.embervault.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.embervault.domain.Link;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * Serializes and deserializes {@link Link} lists to/from YAML.
 */
public final class LinkYamlSerializer {

    private LinkYamlSerializer() { }

    /**
     * Serializes a list of links to YAML.
     */
    public static String serialize(List<Link> links) {
        StringBuilder sb = new StringBuilder();
        sb.append("links:\n");
        for (Link link : links) {
            sb.append("  - id: \"").append(link.id())
                    .append("\"\n");
            sb.append("    sourceId: \"")
                    .append(link.sourceId()).append("\"\n");
            sb.append("    destinationId: \"")
                    .append(link.destinationId()).append("\"\n");
            sb.append("    type: \"").append(link.type())
                    .append("\"\n");
        }
        return sb.toString();
    }

    /**
     * Deserializes YAML to a list of links.
     */
    @SuppressWarnings("unchecked")
    public static List<Link> deserialize(String yaml) {
        Load load = new Load(LoadSettings.builder().build());
        Object parsed = load.loadFromString(yaml);
        if (!(parsed instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object linksList = map.get("links");
        if (!(linksList instanceof List<?> items)) {
            return List.of();
        }
        List<Link> result = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> entry) {
                result.add(new Link(
                        UUID.fromString(
                                String.valueOf(entry.get("id"))),
                        UUID.fromString(String.valueOf(
                                entry.get("sourceId"))),
                        UUID.fromString(String.valueOf(
                                entry.get("destinationId"))),
                        String.valueOf(entry.get("type"))));
            }
        }
        return result;
    }
}
