package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for ViewColorConfig.
 */
class ViewColorConfigTest {

    @Test
    @DisplayName("record constructor maps all fields")
    void constructor_mapsAllFields() {
        ViewColorConfig config = new ViewColorConfig(
                "#111111", "#222222", "#333333",
                "#444444", "#555555", "#666666",
                "#777777", "#888888");

        assertEquals("#111111", config.canvasBackground());
        assertEquals("#222222", config.panelBackground());
        assertEquals("#333333", config.textColor());
        assertEquals("#444444", config.secondaryTextColor());
        assertEquals("#555555", config.borderColor());
        assertEquals("#666666", config.selectionColor());
        assertEquals("#777777", config.toolbarBackground());
        assertEquals("#888888", config.accentColor());
    }

    @Test
    @DisplayName("contrastTextColor returns black for white background")
    void contrastTextColor_blackForWhite() {
        assertEquals("#000000",
                ViewColorConfig.contrastTextColor("#FFFFFF"));
    }

    @Test
    @DisplayName("contrastTextColor returns white for black background")
    void contrastTextColor_whiteForBlack() {
        assertEquals("#FFFFFF",
                ViewColorConfig.contrastTextColor("#000000"));
    }
}
