package com.embervault;

/**
 * Enumeration of the available view types that can be displayed
 * in a view pane within the split-pane layout.
 */
public enum ViewType {

    /** Spatial map view. */
    MAP("Map", "MapView.fxml"),

    /** Hierarchical outline view. */
    OUTLINE("Outline", "OutlineView.fxml"),

    /** Treemap visualization view. */
    TREEMAP("Treemap", "TreemapView.fxml"),

    /** Hyperbolic graph view. */
    HYPERBOLIC("Hyperbolic", "HyperbolicView.fxml"),

    /** Attribute browser view. */
    BROWSER("Browser", "AttributeBrowserView.fxml");

    private final String displayName;
    private final String fxmlFile;

    ViewType(String displayName, String fxmlFile) {
        this.displayName = displayName;
        this.fxmlFile = fxmlFile;
    }

    /** Returns the human-readable display name. */
    public String displayName() {
        return displayName;
    }

    /** Returns the FXML file name for this view type. */
    public String fxmlFile() {
        return fxmlFile;
    }
}
