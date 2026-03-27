package com.embervault.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TbxColorTest {

    @Test
    @DisplayName("hex() creates a color from a hex string")
    void hex_createsColor() {
        TbxColor color = TbxColor.hex("#A482BF");
        assertEquals("#A482BF", color.toHex());
    }

    @Test
    @DisplayName("hex() normalises to uppercase")
    void hex_normalisesToUppercase() {
        TbxColor color = TbxColor.hex("#a482bf");
        assertEquals("#A482BF", color.toHex());
    }

    @Test
    @DisplayName("hex() rejects invalid format")
    void hex_rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> TbxColor.hex("not-a-color"));
    }

    @Test
    @DisplayName("hex() rejects null")
    void hex_rejectsNull() {
        assertThrows(NullPointerException.class, () -> TbxColor.hex(null));
    }

    @Test
    @DisplayName("named() creates a color with a name")
    void named_createsColor() {
        TbxColor color = TbxColor.named("red");
        assertEquals(Optional.of("red"), color.getName());
    }

    @Test
    @DisplayName("named() maps known names to hex values")
    void named_mapsKnownColors() {
        TbxColor red = TbxColor.named("red");
        assertEquals("#FF0000", red.toHex());
    }

    @Test
    @DisplayName("named() rejects null")
    void named_rejectsNull() {
        assertThrows(NullPointerException.class, () -> TbxColor.named(null));
    }

    @Test
    @DisplayName("named() rejects blank")
    void named_rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> TbxColor.named("  "));
    }

    @Test
    @DisplayName("rgb() creates a color from RGB components")
    void rgb_createsColor() {
        TbxColor color = TbxColor.rgb(255, 0, 0);
        assertEquals("#FF0000", color.toHex());
    }

    @Test
    @DisplayName("rgb() rejects out-of-range values")
    void rgb_rejectsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> TbxColor.rgb(256, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> TbxColor.rgb(-1, 0, 0));
    }

    @Test
    @DisplayName("hsv() creates a color from HSV components")
    void hsv_createsColor() {
        TbxColor color = TbxColor.hsv(0, 100, 100);
        assertEquals("#FF0000", color.toHex());
    }

    @Test
    @DisplayName("hsv() produces correct blue")
    void hsv_producesBlue() {
        TbxColor color = TbxColor.hsv(240, 100, 100);
        assertEquals("#0000FF", color.toHex());
    }

    @Test
    @DisplayName("hsv() produces correct green")
    void hsv_producesGreen() {
        TbxColor color = TbxColor.hsv(120, 100, 100);
        assertEquals("#00FF00", color.toHex());
    }

    @Test
    @DisplayName("hsv() with zero saturation produces gray")
    void hsv_zeroSaturationProducesGray() {
        TbxColor color = TbxColor.hsv(0, 0, 50);
        assertEquals("#808080", color.toHex());
    }

    @Test
    @DisplayName("equals() is based on hex value")
    void equals_basedOnHex() {
        TbxColor a = TbxColor.hex("#FF0000");
        TbxColor b = TbxColor.rgb(255, 0, 0);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("equals() returns false for different colors")
    void equals_falseForDifferent() {
        TbxColor a = TbxColor.hex("#FF0000");
        TbxColor b = TbxColor.hex("#00FF00");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("hashCode() is consistent with equals()")
    void hashCode_consistentWithEquals() {
        TbxColor a = TbxColor.hex("#FF0000");
        TbxColor b = TbxColor.rgb(255, 0, 0);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("getName() returns empty for hex-created colors")
    void getName_emptyForHex() {
        TbxColor color = TbxColor.hex("#A482BF");
        assertTrue(color.getName().isEmpty());
    }

    @Test
    @DisplayName("named() with warm gray produces valid color")
    void named_warmGray() {
        TbxColor color = TbxColor.named("warm gray");
        assertEquals(Optional.of("warm gray"), color.getName());
        assertEquals("#808080", color.toHex());
    }

    @Test
    @DisplayName("toString() includes hex value")
    void toString_includesHex() {
        TbxColor color = TbxColor.hex("#A482BF");
        assertTrue(color.toString().contains("#A482BF"));
    }
}
