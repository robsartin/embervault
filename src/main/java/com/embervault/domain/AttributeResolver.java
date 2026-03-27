package com.embervault.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Domain service that resolves attribute values through the inheritance chain.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>Check the note's own AttributeMap</li>
 *   <li>If intrinsic, return the document default (skip prototype chain)</li>
 *   <li>Walk the prototype chain looking for a value</li>
 *   <li>Fall back to the AttributeDefinition's default value</li>
 * </ol>
 *
 * <p>Takes a {@code Function<UUID, Optional<Note>>} for note lookups to avoid
 * a domain dependency on the application layer's NoteRepository port.</p>
 */
public final class AttributeResolver {

    private static final AttributeValue UNKNOWN_DEFAULT = new AttributeValue.StringValue("");

    private final AttributeSchemaRegistry schemaRegistry;

    /**
     * Constructs an AttributeResolver backed by the given schema registry.
     *
     * @param schemaRegistry the schema registry
     */
    public AttributeResolver(AttributeSchemaRegistry schemaRegistry) {
        this.schemaRegistry = Objects.requireNonNull(schemaRegistry,
                "schemaRegistry must not be null");
    }

    /**
     * Resolves the value of the named attribute for the given note.
     *
     * @param note          the note to resolve the attribute for
     * @param attributeName the attribute name
     * @param noteLookup    a function that looks up notes by id
     * @return the resolved attribute value
     */
    public AttributeValue resolve(Note note, String attributeName,
            Function<UUID, Optional<Note>> noteLookup) {
        // 1. Local value
        if (note.getAttributes().hasLocalValue(attributeName)) {
            return note.getAttributes().get(attributeName).orElseThrow();
        }

        // 2. Look up definition
        Optional<AttributeDefinition> optDef = schemaRegistry.get(attributeName);
        if (optDef.isEmpty()) {
            return UNKNOWN_DEFAULT;
        }
        AttributeDefinition def = optDef.get();

        // 3. If intrinsic, skip prototype chain
        if (def.intrinsic()) {
            return def.defaultValue();
        }

        // 4. Walk prototype chain
        Optional<UUID> protoId = note.getPrototypeId();
        while (protoId.isPresent()) {
            Optional<Note> protoOpt = noteLookup.apply(protoId.get());
            if (protoOpt.isEmpty()) {
                break;
            }
            Note proto = protoOpt.get();
            if (proto.getAttributes().hasLocalValue(attributeName)) {
                return proto.getAttributes().get(attributeName).orElseThrow();
            }
            protoId = proto.getPrototypeId();
        }

        // 5. Document default
        return def.defaultValue();
    }
}
