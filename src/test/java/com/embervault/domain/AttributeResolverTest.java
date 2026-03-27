package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeResolverTest {

    private AttributeSchemaRegistry registry;
    private AttributeResolver resolver;
    private Map<UUID, Note> noteStore;
    private Function<UUID, Optional<Note>> noteLookup;

    @BeforeEach
    void setUp() {
        registry = new AttributeSchemaRegistry();
        resolver = new AttributeResolver(registry);
        noteStore = new HashMap<>();
        noteLookup = id -> Optional.ofNullable(noteStore.get(id));
    }

    private void store(Note note) {
        noteStore.put(note.getId(), note);
    }

    @Test
    @DisplayName("resolves locally-set attribute value")
    void resolves_localValue() {
        Note note = Note.create("Test", "");
        note.setAttribute("$Shape", new AttributeValue.StringValue("cloud"));
        store(note);

        AttributeValue result = resolver.resolve(note, "$Shape", noteLookup);
        assertEquals(new AttributeValue.StringValue("cloud"), result);
    }

    @Test
    @DisplayName("falls back to document default when no local value")
    void fallsBack_toDefault() {
        Note note = Note.create("Test", "");
        store(note);

        AttributeValue result = resolver.resolve(note, "$Shape", noteLookup);
        assertEquals(new AttributeValue.StringValue("normal"), result);
    }

    @Test
    @DisplayName("inherits value from prototype")
    void inherits_fromPrototype() {
        Note proto = Note.create("Prototype", "");
        proto.setAttribute("$Shape", new AttributeValue.StringValue("oval"));
        proto.setAttribute("$IsPrototype", new AttributeValue.BooleanValue(true));
        store(proto);

        Note child = Note.create("Child", "");
        child.setPrototypeId(proto.getId());
        store(child);

        AttributeValue result = resolver.resolve(child, "$Shape", noteLookup);
        assertEquals(new AttributeValue.StringValue("oval"), result);
    }

    @Test
    @DisplayName("local value overrides inherited value")
    void localOverridesInherited() {
        Note proto = Note.create("Prototype", "");
        proto.setAttribute("$Shape", new AttributeValue.StringValue("oval"));
        store(proto);

        Note child = Note.create("Child", "");
        child.setPrototypeId(proto.getId());
        child.setAttribute("$Shape", new AttributeValue.StringValue("square"));
        store(child);

        AttributeValue result = resolver.resolve(child, "$Shape", noteLookup);
        assertEquals(new AttributeValue.StringValue("square"), result);
    }

    @Test
    @DisplayName("intrinsic attributes skip prototype chain")
    void intrinsic_skipsPrototypeChain() {
        Note proto = Note.create("Prototype", "");
        proto.setAttribute("$Xpos", new AttributeValue.NumberValue(100));
        store(proto);

        Note child = Note.create("Child", "");
        child.setPrototypeId(proto.getId());
        store(child);

        // $Xpos is intrinsic, so child should get the document default (0), not proto's 100
        AttributeValue result = resolver.resolve(child, "$Xpos", noteLookup);
        assertEquals(new AttributeValue.NumberValue(0), result);
    }

    @Test
    @DisplayName("intrinsic attribute with local value returns local value")
    void intrinsic_localValueReturned() {
        Note note = Note.create("Test", "");
        note.setAttribute("$Xpos", new AttributeValue.NumberValue(42));
        store(note);

        AttributeValue result = resolver.resolve(note, "$Xpos", noteLookup);
        assertEquals(new AttributeValue.NumberValue(42), result);
    }

    @Test
    @DisplayName("multi-level prototype chain works")
    void multiLevelPrototypeChain() {
        Note grandProto = Note.create("GrandProto", "");
        grandProto.setAttribute("$Shape", new AttributeValue.StringValue("diamond"));
        store(grandProto);

        Note proto = Note.create("Proto", "");
        proto.setPrototypeId(grandProto.getId());
        store(proto);

        Note child = Note.create("Child", "");
        child.setPrototypeId(proto.getId());
        store(child);

        AttributeValue result = resolver.resolve(child, "$Shape", noteLookup);
        assertEquals(new AttributeValue.StringValue("diamond"), result);
    }

    @Test
    @DisplayName("removing local value reverts to inherited")
    void removingLocalRevertsToInherited() {
        Note proto = Note.create("Proto", "");
        proto.setAttribute("$Shape", new AttributeValue.StringValue("oval"));
        store(proto);

        Note child = Note.create("Child", "");
        child.setPrototypeId(proto.getId());
        child.setAttribute("$Shape", new AttributeValue.StringValue("square"));
        store(child);

        // Verify local value first
        assertEquals(new AttributeValue.StringValue("square"),
                resolver.resolve(child, "$Shape", noteLookup));

        // Remove local value
        child.removeAttribute("$Shape");

        // Should now inherit from prototype
        assertEquals(new AttributeValue.StringValue("oval"),
                resolver.resolve(child, "$Shape", noteLookup));
    }

    @Test
    @DisplayName("unknown attribute returns string empty default")
    void unknownAttribute_returnsStringDefault() {
        Note note = Note.create("Test", "");
        store(note);

        AttributeValue result = resolver.resolve(note, "$Unknown", noteLookup);
        assertEquals(new AttributeValue.StringValue(""), result);
    }
}
