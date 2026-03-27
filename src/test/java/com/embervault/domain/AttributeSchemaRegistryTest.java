package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeSchemaRegistryTest {

    private AttributeSchemaRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AttributeSchemaRegistry();
    }

    @Test
    @DisplayName("get() returns $Name definition")
    void get_returnsName() {
        Optional<AttributeDefinition> def = registry.get("$Name");
        assertTrue(def.isPresent());
        assertEquals(AttributeType.STRING, def.get().type());
    }

    @Test
    @DisplayName("get() returns $Text definition")
    void get_returnsText() {
        Optional<AttributeDefinition> def = registry.get("$Text");
        assertTrue(def.isPresent());
        assertEquals(AttributeType.STRING, def.get().type());
    }

    @Test
    @DisplayName("get() returns $Color definition")
    void get_returnsColor() {
        Optional<AttributeDefinition> def = registry.get("$Color");
        assertTrue(def.isPresent());
        assertEquals(AttributeType.COLOR, def.get().type());
    }

    @Test
    @DisplayName("get() returns $Created as intrinsic and read-only")
    void get_returnsCreatedAsIntrinsic() {
        Optional<AttributeDefinition> def = registry.get("$Created");
        assertTrue(def.isPresent());
        assertTrue(def.get().intrinsic());
        assertTrue(def.get().readOnly());
    }

    @Test
    @DisplayName("get() returns $Modified as intrinsic and read-only")
    void get_returnsModifiedAsIntrinsic() {
        Optional<AttributeDefinition> def = registry.get("$Modified");
        assertTrue(def.isPresent());
        assertTrue(def.get().intrinsic());
        assertTrue(def.get().readOnly());
    }

    @Test
    @DisplayName("get() returns $Xpos as intrinsic")
    void get_returnsXposAsIntrinsic() {
        Optional<AttributeDefinition> def = registry.get("$Xpos");
        assertTrue(def.isPresent());
        assertTrue(def.get().intrinsic());
        assertEquals(AttributeType.NUMBER, def.get().type());
    }

    @Test
    @DisplayName("get() returns empty for unknown attribute")
    void get_returnsEmptyForUnknown() {
        assertTrue(registry.get("$Nonexistent").isEmpty());
    }

    @Test
    @DisplayName("getAll() returns all registered definitions")
    void getAll_returnsAll() {
        List<AttributeDefinition> all = registry.getAll();
        assertFalse(all.isEmpty());
        assertTrue(all.size() >= 17);
    }

    @Test
    @DisplayName("getByType() filters by attribute type")
    void getByType_filters() {
        List<AttributeDefinition> booleans = registry.getByType(AttributeType.BOOLEAN);
        assertFalse(booleans.isEmpty());
        assertTrue(booleans.stream().allMatch(d -> d.type() == AttributeType.BOOLEAN));
    }

    @Test
    @DisplayName("isSystemAttribute() returns true for built-in attributes")
    void isSystemAttribute_trueForBuiltIn() {
        assertTrue(registry.isSystemAttribute("$Name"));
    }

    @Test
    @DisplayName("isSystemAttribute() returns false for unknown attributes")
    void isSystemAttribute_falseForUnknown() {
        assertFalse(registry.isSystemAttribute("$MyCustom"));
    }

    @Test
    @DisplayName("register() adds a user attribute")
    void register_addsUserAttribute() {
        AttributeDefinition userAttr = AttributeDefinition
                .builder("$MyAttr", AttributeType.STRING)
                .defaultValue(new AttributeValue.StringValue(""))
                .build();

        registry.register(userAttr);

        assertTrue(registry.get("$MyAttr").isPresent());
        assertFalse(registry.isSystemAttribute("$MyAttr"));
    }

    @Test
    @DisplayName("register() rejects duplicate names")
    void register_rejectsDuplicates() {
        AttributeDefinition userAttr = AttributeDefinition
                .builder("$Name", AttributeType.STRING)
                .defaultValue(new AttributeValue.StringValue(""))
                .build();

        assertThrows(IllegalArgumentException.class, () -> registry.register(userAttr));
    }

    @Test
    @DisplayName("$IsPrototype is a boolean system attribute")
    void isPrototype_isBooleanSystem() {
        Optional<AttributeDefinition> def = registry.get("$IsPrototype");
        assertTrue(def.isPresent());
        assertEquals(AttributeType.BOOLEAN, def.get().type());
        assertTrue(def.get().system());
    }

    @Test
    @DisplayName("$Prototype is a string system attribute")
    void prototype_isStringSystem() {
        Optional<AttributeDefinition> def = registry.get("$Prototype");
        assertTrue(def.isPresent());
        assertEquals(AttributeType.STRING, def.get().type());
    }

    @Test
    @DisplayName("$URL is a URL system attribute")
    void url_isUrlSystem() {
        Optional<AttributeDefinition> def = registry.get("$URL");
        assertTrue(def.isPresent());
        assertEquals(AttributeType.URL, def.get().type());
    }

    @Test
    @DisplayName("$DisplayedAttributes is a set system attribute")
    void displayedAttributes_isSetSystem() {
        Optional<AttributeDefinition> def = registry.get("$DisplayedAttributes");
        assertTrue(def.isPresent());
        assertEquals(AttributeType.SET, def.get().type());
    }

    @Test
    @DisplayName("$Flags is a set system attribute")
    void flags_isSetSystem() {
        Optional<AttributeDefinition> def = registry.get("$Flags");
        assertTrue(def.isPresent());
        assertEquals(AttributeType.SET, def.get().type());
    }
}
