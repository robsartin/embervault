package com.embervault.domain;

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
        systemAttr("$Name", AttributeType.STRING, new AttributeValue.StringValue(""));
        systemAttr("$IsPrototype", AttributeType.BOOLEAN, new AttributeValue.BooleanValue(false));
        systemAttr("$Prototype", AttributeType.STRING, new AttributeValue.StringValue(""));
        intrinsicAttr("$Container", AttributeType.STRING,
                new AttributeValue.StringValue(""), false);
        intrinsicAttr("$OutlineOrder", AttributeType.NUMBER,
                new AttributeValue.NumberValue(0), false);

        // Content
        systemAttr("$Text", AttributeType.STRING, new AttributeValue.StringValue(""));
        systemAttr("$Subtitle", AttributeType.STRING, new AttributeValue.StringValue(""));
        systemAttr("$Caption", AttributeType.STRING, new AttributeValue.StringValue(""));

        // Appearance
        systemAttr("$Color", AttributeType.COLOR,
                new AttributeValue.ColorValue(TbxColor.named("warm gray")));
        systemAttr("$BorderColor", AttributeType.COLOR,
                new AttributeValue.ColorValue(TbxColor.hex("#000000")));
        systemAttr("$Shape", AttributeType.STRING, new AttributeValue.StringValue("normal"));

        // Position & Size (intrinsic)
        intrinsicAttr("$Xpos", AttributeType.NUMBER, new AttributeValue.NumberValue(0), false);
        intrinsicAttr("$Ypos", AttributeType.NUMBER, new AttributeValue.NumberValue(0), false);
        intrinsicAttr("$Width", AttributeType.NUMBER, new AttributeValue.NumberValue(6), false);
        intrinsicAttr("$Height", AttributeType.NUMBER, new AttributeValue.NumberValue(4), false);

        // Dates (intrinsic, read-only)
        intrinsicAttr("$Created", AttributeType.DATE,
                new AttributeValue.DateValue(Instant.EPOCH), true);
        intrinsicAttr("$Modified", AttributeType.DATE,
                new AttributeValue.DateValue(Instant.EPOCH), true);

        // Flags & State
        systemAttr("$Checked", AttributeType.BOOLEAN, new AttributeValue.BooleanValue(false));
        systemAttr("$URL", AttributeType.URL, new AttributeValue.UrlValue(""));

        // Display
        systemAttr("$DisplayedAttributes", AttributeType.SET,
                new AttributeValue.SetValue(Set.of()));
        systemAttr("$Flags", AttributeType.SET, new AttributeValue.SetValue(Set.of()));
        systemAttr("$Badge", AttributeType.STRING, new AttributeValue.StringValue(""));
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
