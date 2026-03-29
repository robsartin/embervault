package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the TextContrastUtil auto-contrast utility.
 */
class TextContrastUtilTest {

    @Test
    @DisplayName("white background returns black text")
    void whiteBackground_returnsBlack() {
        assertEquals("#000000",
                TextContrastUtil.contrastTextColor("#FFFFFF"));
    }

    @Test
    @DisplayName("black background returns white text")
    void blackBackground_returnsWhite() {
        assertEquals("#FFFFFF",
                TextContrastUtil.contrastTextColor("#000000"));
    }

    @Test
    @DisplayName("bright yellow returns black text")
    void brightYellow_returnsBlack() {
        assertEquals("#000000",
                TextContrastUtil.contrastTextColor("#FFFF00"));
    }

    @Test
    @DisplayName("dark blue returns white text")
    void darkBlue_returnsWhite() {
        assertEquals("#FFFFFF",
                TextContrastUtil.contrastTextColor("#000080"));
    }

    @Test
    @DisplayName("pure red returns black text (luminance above threshold)")
    void red_returnsBlack() {
        // Pure red #FF0000 has relative luminance ~0.2126
        assertEquals("#000000",
                TextContrastUtil.contrastTextColor("#FF0000"));
    }

    @Test
    @DisplayName("dark red returns white text")
    void darkRed_returnsWhite() {
        assertEquals("#FFFFFF",
                TextContrastUtil.contrastTextColor("#800000"));
    }

    @Test
    @DisplayName("light gray returns black text")
    void lightGray_returnsBlack() {
        assertEquals("#000000",
                TextContrastUtil.contrastTextColor("#C0C0C0"));
    }

    @Test
    @DisplayName("dark gray returns white text")
    void darkGray_returnsWhite() {
        assertEquals("#FFFFFF",
                TextContrastUtil.contrastTextColor("#404040"));
    }

    @Test
    @DisplayName("handles lowercase hex")
    void handlesLowercaseHex() {
        assertEquals("#000000",
                TextContrastUtil.contrastTextColor("#ffffff"));
    }

    @Test
    @DisplayName("rejects null input")
    void rejectsNull() {
        assertThrows(NullPointerException.class,
                () -> TextContrastUtil.contrastTextColor(null));
    }

    @Test
    @DisplayName("rejects invalid hex format")
    void rejectsInvalidHex() {
        assertThrows(IllegalArgumentException.class,
                () -> TextContrastUtil.contrastTextColor("not-hex"));
    }

    @Test
    @DisplayName("green returns black text (high luminance)")
    void green_returnsBlack() {
        // Pure green #00FF00 has high relative luminance
        assertEquals("#000000",
                TextContrastUtil.contrastTextColor("#00FF00"));
    }
}
