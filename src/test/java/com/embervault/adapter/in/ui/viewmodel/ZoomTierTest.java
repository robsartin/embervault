package com.embervault.adapter.in.ui.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ZoomTier} enum and its tier boundary logic.
 */
class ZoomTierTest {

    @Test
    @DisplayName("fromZoomLevel returns OVERVIEW for zoom < 0.4")
    void fromZoomLevel_shouldReturnOverview_whenZoomBelowPoint4() {
        assertEquals(ZoomTier.OVERVIEW, ZoomTier.fromZoomLevel(0.1));
        assertEquals(ZoomTier.OVERVIEW, ZoomTier.fromZoomLevel(0.3));
        assertEquals(ZoomTier.OVERVIEW, ZoomTier.fromZoomLevel(0.39));
    }

    @Test
    @DisplayName("fromZoomLevel returns TITLES_ONLY for zoom 0.4 to < 0.8")
    void fromZoomLevel_shouldReturnTitlesOnly_whenZoomBetweenPoint4AndPoint8() {
        assertEquals(ZoomTier.TITLES_ONLY, ZoomTier.fromZoomLevel(0.4));
        assertEquals(ZoomTier.TITLES_ONLY, ZoomTier.fromZoomLevel(0.5));
        assertEquals(ZoomTier.TITLES_ONLY, ZoomTier.fromZoomLevel(0.79));
    }

    @Test
    @DisplayName("fromZoomLevel returns NORMAL for zoom 0.8 to < 1.5")
    void fromZoomLevel_shouldReturnNormal_whenZoomBetweenPoint8And1Point5() {
        assertEquals(ZoomTier.NORMAL, ZoomTier.fromZoomLevel(0.8));
        assertEquals(ZoomTier.NORMAL, ZoomTier.fromZoomLevel(1.0));
        assertEquals(ZoomTier.NORMAL, ZoomTier.fromZoomLevel(1.49));
    }

    @Test
    @DisplayName("fromZoomLevel returns DETAILED for zoom >= 1.5")
    void fromZoomLevel_shouldReturnDetailed_whenZoomAbove1Point5() {
        assertEquals(ZoomTier.DETAILED, ZoomTier.fromZoomLevel(1.5));
        assertEquals(ZoomTier.DETAILED, ZoomTier.fromZoomLevel(2.0));
        assertEquals(ZoomTier.DETAILED, ZoomTier.fromZoomLevel(5.0));
    }

    @Test
    @DisplayName("OVERVIEW tier does not show title, content, or badge")
    void overviewTier_shouldNotShowAnything() {
        assertFalse(ZoomTier.OVERVIEW.isShowTitle());
        assertFalse(ZoomTier.OVERVIEW.isShowContent());
        assertFalse(ZoomTier.OVERVIEW.isShowBadge());
        assertEquals(0, ZoomTier.OVERVIEW.getTitleFontSize());
    }

    @Test
    @DisplayName("TITLES_ONLY tier shows title and badge but not content")
    void titlesOnlyTier_shouldShowTitleOnly() {
        assertTrue(ZoomTier.TITLES_ONLY.isShowTitle());
        assertFalse(ZoomTier.TITLES_ONLY.isShowContent());
        assertTrue(ZoomTier.TITLES_ONLY.isShowBadge());
        assertEquals(10, ZoomTier.TITLES_ONLY.getTitleFontSize());
    }

    @Test
    @DisplayName("NORMAL tier shows title, content, and badge")
    void normalTier_shouldShowAll() {
        assertTrue(ZoomTier.NORMAL.isShowTitle());
        assertTrue(ZoomTier.NORMAL.isShowContent());
        assertTrue(ZoomTier.NORMAL.isShowBadge());
        assertEquals(14, ZoomTier.NORMAL.getTitleFontSize());
    }

    @Test
    @DisplayName("DETAILED tier shows title, content, and badge with larger font")
    void detailedTier_shouldShowAllWithLargerFont() {
        assertTrue(ZoomTier.DETAILED.isShowTitle());
        assertTrue(ZoomTier.DETAILED.isShowContent());
        assertTrue(ZoomTier.DETAILED.isShowBadge());
        assertEquals(18, ZoomTier.DETAILED.getTitleFontSize());
    }

    @Test
    @DisplayName("boundary value at exactly 0.4 is TITLES_ONLY")
    void boundary_atExactlyPoint4_shouldBeTitlesOnly() {
        assertEquals(ZoomTier.TITLES_ONLY, ZoomTier.fromZoomLevel(0.4));
    }

    @Test
    @DisplayName("boundary value at exactly 0.8 is NORMAL")
    void boundary_atExactlyPoint8_shouldBeNormal() {
        assertEquals(ZoomTier.NORMAL, ZoomTier.fromZoomLevel(0.8));
    }

    @Test
    @DisplayName("boundary value at exactly 1.5 is DETAILED")
    void boundary_atExactly1Point5_shouldBeDetailed() {
        assertEquals(ZoomTier.DETAILED, ZoomTier.fromZoomLevel(1.5));
    }
}
