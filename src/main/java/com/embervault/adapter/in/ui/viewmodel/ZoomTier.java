package com.embervault.adapter.in.ui.viewmodel;

import com.embervault.adapter.in.ui.view.ZoomTierRenderer;

/**
 * Defines semantic zoom tiers for the Map view.
 *
 * <p>Each tier specifies what level of detail to render for notes at a
 * given zoom level. The tiers progress from a high-level overview
 * (colored rectangles only) to a detailed view with larger fonts.</p>
 */
public enum ZoomTier {

    /** Zoom &lt; 0.4: colored rectangles only, no text. */
    OVERVIEW(false, false, false, 0),

    /** 0.4 &le; zoom &lt; 0.8: title text only, compact font. */
    TITLES_ONLY(true, false, true, 10),

    /** 0.8 &le; zoom &lt; 1.5: title (bold) + truncated content. */
    NORMAL(true, true, true, 14),

    /** Zoom &ge; 1.5: larger fonts, more content visible. */
    DETAILED(true, true, true, 18);

    private static final double OVERVIEW_UPPER = 0.4;
    private static final double TITLES_ONLY_UPPER = 0.8;
    private static final double NORMAL_UPPER = 1.5;

    private final boolean showTitle;
    private final boolean showContent;
    private final boolean showBadge;
    private final int titleFontSize;

    ZoomTier(boolean showTitle, boolean showContent, boolean showBadge,
            int titleFontSize) {
        this.showTitle = showTitle;
        this.showContent = showContent;
        this.showBadge = showBadge;
        this.titleFontSize = titleFontSize;
    }

    /** Returns whether this tier renders the note title. */
    public boolean isShowTitle() {
        return showTitle;
    }

    /** Returns whether this tier renders the note content. */
    public boolean isShowContent() {
        return showContent;
    }

    /** Returns whether this tier renders the badge. */
    public boolean isShowBadge() {
        return showBadge;
    }

    /** Returns the title font size for this tier, or 0 if no title is shown. */
    public int getTitleFontSize() {
        return titleFontSize;
    }

    /**
     * Creates the appropriate renderer for this zoom tier.
     *
     * @return a new ZoomTierRenderer for this tier
     */
    public ZoomTierRenderer createRenderer() {
        return switch (this) {
            case OVERVIEW -> new ZoomTierRenderer.OverviewRenderer();
            case TITLES_ONLY ->
                    new ZoomTierRenderer.TitlesOnlyRenderer();
            case NORMAL -> new ZoomTierRenderer.NormalRenderer();
            case DETAILED ->
                    new ZoomTierRenderer.DetailedRenderer();
        };
    }

    /**
     * Determines the zoom tier for the given zoom level.
     *
     * @param zoomLevel the current zoom level
     * @return the appropriate zoom tier
     */
    public static ZoomTier fromZoomLevel(double zoomLevel) {
        if (zoomLevel < OVERVIEW_UPPER) {
            return OVERVIEW;
        } else if (zoomLevel < TITLES_ONLY_UPPER) {
            return TITLES_ONLY;
        } else if (zoomLevel < NORMAL_UPPER) {
            return NORMAL;
        } else {
            return DETAILED;
        }
    }
}
