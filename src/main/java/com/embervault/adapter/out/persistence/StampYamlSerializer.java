package com.embervault.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.embervault.domain.Stamp;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * Serializes and deserializes {@link Stamp} lists to/from YAML.
 */
public final class StampYamlSerializer {

    private StampYamlSerializer() { }

    /**
     * Serializes a list of stamps to YAML.
     */
    public static String serialize(List<Stamp> stamps) {
        StringBuilder sb = new StringBuilder();
        sb.append("stamps:\n");
        for (Stamp stamp : stamps) {
            sb.append("  - id: \"").append(stamp.id())
                    .append("\"\n");
            sb.append("    name: \"")
                    .append(escapeYaml(stamp.name()))
                    .append("\"\n");
            sb.append("    action: \"")
                    .append(escapeYaml(stamp.action()))
                    .append("\"\n");
        }
        return sb.toString();
    }

    /**
     * Deserializes YAML to a list of stamps.
     */
    @SuppressWarnings("unchecked")
    public static List<Stamp> deserialize(String yaml) {
        Load load = new Load(LoadSettings.builder().build());
        Object parsed = load.loadFromString(yaml);
        if (!(parsed instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object stampsList = map.get("stamps");
        if (!(stampsList instanceof List<?> items)) {
            return List.of();
        }
        List<Stamp> result = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> entry) {
                result.add(new Stamp(
                        UUID.fromString(
                                String.valueOf(entry.get("id"))),
                        String.valueOf(entry.get("name")),
                        String.valueOf(entry.get("action"))));
            }
        }
        return result;
    }

    private static String escapeYaml(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
