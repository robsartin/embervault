package com.embervault;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests for {@link ViewFactoryRegistry}.
 */
class ViewFactoryRegistryTest {

    private ViewFactoryRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ViewFactoryRegistry();
    }

    @ParameterizedTest
    @EnumSource(ViewType.class)
    @DisplayName("getFactory returns non-null factory for every ViewType")
    void getFactory_shouldReturnNonNullForEveryViewType(
            ViewType type) {
        ViewFactory factory = registry.getFactory(type);
        assertNotNull(factory,
                "Factory should not be null for " + type);
    }
}
