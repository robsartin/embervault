package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.embervault.domain.ColorScheme;
import com.embervault.domain.ColorSchemeRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for ViewColorConfig.
 */
class ViewColorConfigTest {

    @Test
    @DisplayName("fromScheme maps all ColorScheme fields")
    void fromScheme_mapsAllFields() {
        ColorScheme scheme = new ColorScheme(
                "Test", "#111111", "#222222", "#333333",
                "#444444", "#555555", "#666666",
                "#777777", "#888888");

        ViewColorConfig config = ViewColorConfig.fromScheme(scheme);

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
    @DisplayName("fromScheme works with registry presets")
    void fromScheme_worksWithRegistryPresets() {
        ColorScheme standard = ColorSchemeRegistry.getDefault();
        ViewColorConfig config = ViewColorConfig.fromScheme(standard);

        assertEquals(standard.canvasBackground(),
                config.canvasBackground());
        assertEquals(standard.selectionColor(),
                config.selectionColor());
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
