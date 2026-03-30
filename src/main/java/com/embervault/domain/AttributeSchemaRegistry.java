package com.embervault.domain;

import static com.embervault.domain.Attributes.BADGE;
import static com.embervault.domain.Attributes.BORDER_COLOR;
import static com.embervault.domain.Attributes.CAPTION;
import static com.embervault.domain.Attributes.CHECKED;
import static com.embervault.domain.Attributes.COLOR;
import static com.embervault.domain.Attributes.CONTAINER;
import static com.embervault.domain.Attributes.CREATED;
import static com.embervault.domain.Attributes.DISPLAYED_ATTRIBUTES;
import static com.embervault.domain.Attributes.FLAGS;
import static com.embervault.domain.Attributes.HEIGHT;
import static com.embervault.domain.Attributes.IS_PROTOTYPE;
import static com.embervault.domain.Attributes.MODIFIED;
import static com.embervault.domain.Attributes.NAME;
import static com.embervault.domain.Attributes.OUTLINE_ORDER;
import static com.embervault.domain.Attributes.PROTOTYPE;
import static com.embervault.domain.Attributes.SHAPE;
import static com.embervault.domain.Attributes.STAMPS;
import static com.embervault.domain.Attributes.SUBTITLE;
import static com.embervault.domain.Attributes.TEXT;
import static com.embervault.domain.Attributes.URL;
import static com.embervault.domain.Attributes.WIDTH;
import static com.embervault.domain.Attributes.XPOS;
import static com.embervault.domain.Attributes.YPOS;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Registry of all known attribute definitions, pre-populated with system attributes.
 *
 * <p>Manages both system-defined and user-defined attributes. System attributes
 * follow the Tinderbox "$" naming convention.</p>
 */
public final class AttributeSchemaRegistry {

    private final Map<String, AttributeDefinition> definitions = new LinkedHashMap<>();

    /**
     * Creates a new registry pre-populated with the essential system attributes.
     */
    public AttributeSchemaRegistry() {
        registerSystemAttributes();
    }

    /**
     * Returns the definition for the given attribute name.
     *
     * @param name the attribute name
     * @return an optional containing the definition, or empty if unknown
     */
    public Optional<AttributeDefinition> get(String name) {
        return Optional.ofNullable(definitions.get(name));
    }

    /**
     * Returns all registered definitions.
     *
     * @return an unmodifiable list of all definitions
     */
    public List<AttributeDefinition> getAll() {
        return List.copyOf(definitions.values());
    }

    /**
     * Returns definitions filtered by the given attribute type.
     *
     * @param type the attribute type to filter by
     * @return an unmodifiable list of matching definitions
     */
    public List<AttributeDefinition> getByType(AttributeType type) {
        return definitions.values().stream()
                .filter(d -> d.type() == type)
                .toList();
    }

    /**
     * Returns whether the given attribute name is a system attribute.
     *
     * @param name the attribute name
     * @return true if it is a system attribute
     */
    public boolean isSystemAttribute(String name) {
        AttributeDefinition def = definitions.get(name);
        return def != null && def.system();
    }

    /**
     * Registers a user-defined attribute.
     *
     * @param definition the attribute definition to register
     * @throws IllegalArgumentException if an attribute with this name already exists
     */
    public void register(AttributeDefinition definition) {
        Objects.requireNonNull(definition, "definition must not be null");
        if (definitions.containsKey(definition.name())) {
            throw new IllegalArgumentException(
                    "Attribute already registered: " + definition.name());
        }
        definitions.put(definition.name(), definition);
    }

    private void registerSystemAttributes() {
        // Identity & Structure
        systemAttr(NAME, AttributeType.STRING, new AttributeValue.StringValue(""));
        systemAttr(IS_PROTOTYPE, AttributeType.BOOLEAN, new AttributeValue.BooleanValue(false));
        systemAttr(PROTOTYPE, AttributeType.STRING, new AttributeValue.StringValue(""));
        intrinsicAttr(CONTAINER, AttributeType.STRING,
                new AttributeValue.StringValue(""), false);
        intrinsicAttr(OUTLINE_ORDER, AttributeType.NUMBER,
                new AttributeValue.NumberValue(0), false);

        // Content
        systemAttr(TEXT, AttributeType.STRING, new AttributeValue.StringValue(""));
        systemAttr(SUBTITLE, AttributeType.STRING, new AttributeValue.StringValue(""));
        systemAttr(CAPTION, AttributeType.STRING, new AttributeValue.StringValue(""));

        // Appearance
        systemAttr(COLOR, AttributeType.COLOR,
                new AttributeValue.ColorValue(TbxColor.named("warm gray")));
        systemAttr(BORDER_COLOR, AttributeType.COLOR,
                new AttributeValue.ColorValue(TbxColor.hex("#000000")));
        systemAttr(SHAPE, AttributeType.STRING, new AttributeValue.StringValue("normal"));

        // Position & Size (intrinsic)
        intrinsicAttr(XPOS, AttributeType.NUMBER, new AttributeValue.NumberValue(0), false);
        intrinsicAttr(YPOS, AttributeType.NUMBER, new AttributeValue.NumberValue(0), false);
        intrinsicAttr(WIDTH, AttributeType.NUMBER, new AttributeValue.NumberValue(6), false);
        intrinsicAttr(HEIGHT, AttributeType.NUMBER, new AttributeValue.NumberValue(4), false);

        // Dates (intrinsic, read-only)
        intrinsicAttr(CREATED, AttributeType.DATE,
                new AttributeValue.DateValue(Instant.EPOCH), true);
        intrinsicAttr(MODIFIED, AttributeType.DATE,
                new AttributeValue.DateValue(Instant.EPOCH), true);

        // Flags & State
        systemAttr(CHECKED, AttributeType.BOOLEAN, new AttributeValue.BooleanValue(false));
        systemAttr(URL, AttributeType.URL, new AttributeValue.UrlValue(""));

        // Display
        systemAttr(DISPLAYED_ATTRIBUTES, AttributeType.SET,
                new AttributeValue.SetValue(Set.of()));
        systemAttr(FLAGS, AttributeType.SET, new AttributeValue.SetValue(Set.of()));
        systemAttr(BADGE, AttributeType.STRING, new AttributeValue.StringValue(""));

        // Project metadata
        systemAttr(STAMPS, AttributeType.LIST,
                new AttributeValue.ListValue(List.of()));
    }

    private void systemAttr(String name, AttributeType type, AttributeValue defaultValue) {
        definitions.put(name, AttributeDefinition.builder(name, type)
                .defaultValue(defaultValue)
                .system(true)
                .build());
    }

    private void intrinsicAttr(String name, AttributeType type,
            AttributeValue defaultValue, boolean readOnly) {
        definitions.put(name, AttributeDefinition.builder(name, type)
                .defaultValue(defaultValue)
                .intrinsic(true)
                .readOnly(readOnly)
                .system(true)
                .build());
    }
}
