package com.embervault.adapter.out.persistence;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.AttributeDefinition;
import com.embervault.domain.AttributeMap;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.AttributeType;
import com.embervault.domain.AttributeValue;
import com.embervault.domain.Attributes;
import com.embervault.domain.Note;
import com.embervault.domain.TbxColor;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * Deserializes a Markdown string with YAML front matter into a
 * {@link Note}.
 *
 * <p>The YAML front matter contains attribute key-value pairs.
 * The markdown body after the closing {@code ---} becomes the
 * {@code $Text} attribute. The {@link AttributeSchemaRegistry}
 * provides type information for parsing typed attribute values.</p>
 */
public final class NoteFileDeserializer {

    private static final String DELIMITER = "---";
    private final AttributeSchemaRegistry registry;

    /**
     * Constructs a deserializer with the given schema registry.
     *
     * @param registry the attribute schema registry for type lookups
     */
    public NoteFileDeserializer(AttributeSchemaRegistry registry) {
        this.registry = Objects.requireNonNull(registry);
    }

    /**
     * Deserializes a markdown string into a Note.
     *
     * @param content the file content
     * @param noteId  the expected note UUID
     * @return the deserialized Note
     */
    @SuppressWarnings("unchecked")
    public Note deserialize(String content, UUID noteId) {
        String yaml;
        String body;
        int firstDelim = content.indexOf(DELIMITER);
        int secondDelim = content.indexOf(
                DELIMITER, firstDelim + DELIMITER.length());
        if (firstDelim >= 0 && secondDelim > firstDelim) {
            yaml = content.substring(
                    firstDelim + DELIMITER.length() + 1,
                    secondDelim);
            int bodyStart = secondDelim + DELIMITER.length();
            if (bodyStart < content.length()
                    && content.charAt(bodyStart) == '\n') {
                bodyStart++;
            }
            body = bodyStart < content.length()
                    ? content.substring(bodyStart) : "";
        } else {
            yaml = "";
            body = content;
        }

        // Parse YAML
        Load load = new Load(LoadSettings.builder().build());
        Object parsed = load.loadFromString(yaml);
        Map<String, Object> yamlMap = parsed instanceof Map
                ? (Map<String, Object>) parsed
                : Map.of();

        // Build Note
        AttributeMap attrs = new AttributeMap();
        Note note = new Note(noteId, attrs);

        // Process YAML entries
        UUID prototypeId = null;
        for (Map.Entry<String, Object> entry
                : yamlMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if ("id".equals(key)) {
                continue;
            }
            if ("prototypeId".equals(key)) {
                prototypeId = UUID.fromString(
                        String.valueOf(value));
                continue;
            }
            attrs.set(key, toAttributeValue(key, value));
        }

        if (prototypeId != null) {
            note.setPrototypeId(prototypeId);
        }

        // Body → $Text
        if (!body.isEmpty()) {
            String text = body.endsWith("\n")
                    ? body.substring(0, body.length() - 1)
                    : body;
            if (!text.isEmpty()) {
                attrs.set(Attributes.TEXT,
                        new AttributeValue.StringValue(text));
            }
        }

        return note;
    }

    @SuppressWarnings("unchecked")
    private AttributeValue toAttributeValue(
            String name, Object raw) {
        Optional<AttributeDefinition> defOpt = registry.get(name);
        if (defOpt.isEmpty()) {
            return new AttributeValue.StringValue(
                    String.valueOf(raw));
        }
        AttributeType type = defOpt.get().type();
        return switch (type) {
            case STRING -> new AttributeValue.StringValue(
                    String.valueOf(raw));
            case NUMBER -> new AttributeValue.NumberValue(
                    raw instanceof Number n
                            ? n.doubleValue()
                            : Double.parseDouble(
                                    String.valueOf(raw)));
            case BOOLEAN -> new AttributeValue.BooleanValue(
                    raw instanceof Boolean b
                            ? b
                            : Boolean.parseBoolean(
                                    String.valueOf(raw)));
            case COLOR -> new AttributeValue.ColorValue(
                    TbxColor.hex(String.valueOf(raw)));
            case DATE -> new AttributeValue.DateValue(
                    Instant.parse(String.valueOf(raw)));
            case INTERVAL -> new AttributeValue.IntervalValue(
                    Duration.parse(String.valueOf(raw)));
            case FILE -> new AttributeValue.FileValue(
                    String.valueOf(raw));
            case URL -> new AttributeValue.UrlValue(
                    String.valueOf(raw));
            case LIST -> {
                if (raw instanceof List<?> list) {
                    yield new AttributeValue.ListValue(
                            list.stream()
                                    .map(String::valueOf)
                                    .toList());
                }
                yield new AttributeValue.ListValue(
                        List.of(String.valueOf(raw)));
            }
            case SET -> {
                if (raw instanceof List<?> list) {
                    yield new AttributeValue.SetValue(
                            new HashSet<>(list.stream()
                                    .map(String::valueOf)
                                    .toList()));
                }
                yield new AttributeValue.SetValue(
                        java.util.Set.of(String.valueOf(raw)));
            }
            case ACTION -> new AttributeValue.ActionValue(
                    String.valueOf(raw));
        };
    }
}
