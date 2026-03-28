package com.embervault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ViewTypeTest {

    @ParameterizedTest
    @EnumSource(ViewType.class)
    @DisplayName("Each view type has a non-null display name")
    void displayName_shouldNotBeNull(ViewType type) {
        assertNotNull(type.displayName());
    }

    @ParameterizedTest
    @EnumSource(ViewType.class)
    @DisplayName("Each view type has an FXML file ending in .fxml")
    void fxmlFile_shouldEndWithFxml(ViewType type) {
        assertNotNull(type.fxmlFile());
        assertEquals(true, type.fxmlFile().endsWith(".fxml"),
                "FXML file should end with .fxml: "
                        + type.fxmlFile());
    }

    @Test
    @DisplayName("MAP has display name 'Map'")
    void map_displayName() {
        assertEquals("Map", ViewType.MAP.displayName());
    }

    @Test
    @DisplayName("OUTLINE has display name 'Outline'")
    void outline_displayName() {
        assertEquals("Outline", ViewType.OUTLINE.displayName());
    }

    @Test
    @DisplayName("TREEMAP has display name 'Treemap'")
    void treemap_displayName() {
        assertEquals("Treemap", ViewType.TREEMAP.displayName());
    }

    @Test
    @DisplayName("HYPERBOLIC has display name 'Hyperbolic'")
    void hyperbolic_displayName() {
        assertEquals("Hyperbolic",
                ViewType.HYPERBOLIC.displayName());
    }

    @Test
    @DisplayName("BROWSER has display name 'Browser'")
    void browser_displayName() {
        assertEquals("Browser", ViewType.BROWSER.displayName());
    }

    @Test
    @DisplayName("There are exactly 5 view types")
    void values_shouldHaveFiveTypes() {
        assertEquals(5, ViewType.values().length);
    }

    @Test
    @DisplayName("MAP FXML file is MapView.fxml")
    void map_fxmlFile() {
        assertEquals("MapView.fxml", ViewType.MAP.fxmlFile());
    }

    @Test
    @DisplayName("OUTLINE FXML file is OutlineView.fxml")
    void outline_fxmlFile() {
        assertEquals("OutlineView.fxml",
                ViewType.OUTLINE.fxmlFile());
    }

    @Test
    @DisplayName("TREEMAP FXML file is TreemapView.fxml")
    void treemap_fxmlFile() {
        assertEquals("TreemapView.fxml",
                ViewType.TREEMAP.fxmlFile());
    }

    @Test
    @DisplayName("HYPERBOLIC FXML file is HyperbolicView.fxml")
    void hyperbolic_fxmlFile() {
        assertEquals("HyperbolicView.fxml",
                ViewType.HYPERBOLIC.fxmlFile());
    }

    @Test
    @DisplayName("BROWSER FXML file is AttributeBrowserView.fxml")
    void browser_fxmlFile() {
        assertEquals("AttributeBrowserView.fxml",
                ViewType.BROWSER.fxmlFile());
    }
}
