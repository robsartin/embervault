package com.embervault.adapter.in.ui.viewmodel;

import java.util.Objects;
import java.util.UUID;

/**
 * Presentation-layer representation of a note for display in the View.
 *
 * <p>Decouples the View from the domain {@code Note} entity, satisfying
 * the ADR-0013 constraint that Views must not reference domain packages.</p>
 */
public final class NoteDisplayItem {

    private final UUID id;
    private final String title;
    private final String content;
    private final double xpos;
    private final double ypos;
    private final double width;
    private final double height;
    private final String colorHex;
    private final boolean hasChildren;
    private final String badge;

    /**
     * Constructs a display item with the given id, title, and content.
     */
    public NoteDisplayItem(UUID id, String title, String content) {
        this(id, title, content, 0, 0, 6, 4, "#808080", false, "");
    }

    /**
     * Constructs a display item with full map and outline display data.
     *
     * @param id          the note id
     * @param title       the note title
     * @param content     the note content
     * @param xpos        the x position
     * @param ypos        the y position
     * @param width       the width
     * @param height      the height
     * @param colorHex    the fill color as hex string
     * @param hasChildren whether this note has children
     */
    public NoteDisplayItem(UUID id, String title, String content,
            double xpos, double ypos, double width, double height,
            String colorHex, boolean hasChildren) {
        this(id, title, content, xpos, ypos, width, height, colorHex,
                hasChildren, "");
    }

    /**
     * Constructs a display item with full map, outline, and badge data.
     *
     * @param id          the note id
     * @param title       the note title
     * @param content     the note content
     * @param xpos        the x position
     * @param ypos        the y position
     * @param width       the width
     * @param height      the height
     * @param colorHex    the fill color as hex string
     * @param hasChildren whether this note has children
     * @param badge       the badge Unicode symbol, or empty string if none
     */
    public NoteDisplayItem(UUID id, String title, String content,
            double xpos, double ypos, double width, double height,
            String colorHex, boolean hasChildren, String badge) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.xpos = xpos;
        this.ypos = ypos;
        this.width = width;
        this.height = height;
        this.colorHex = Objects.requireNonNull(colorHex, "colorHex must not be null");
        this.hasChildren = hasChildren;
        this.badge = Objects.requireNonNull(badge, "badge must not be null");
    }

    /** Returns the note id. */
    public UUID getId() {
        return id;
    }

    /** Returns the title. */
    public String getTitle() {
        return title;
    }

    /** Returns the content. */
    public String getContent() {
        return content;
    }

    /** Returns the x position for map rendering. */
    public double getXpos() {
        return xpos;
    }

    /** Returns the y position for map rendering. */
    public double getYpos() {
        return ypos;
    }

    /** Returns the width for map rendering. */
    public double getWidth() {
        return width;
    }

    /** Returns the height for map rendering. */
    public double getHeight() {
        return height;
    }

    /** Returns the fill color as a hex string (e.g., "#808080"). */
    public String getColorHex() {
        return colorHex;
    }

    /** Returns whether this note has children. */
    public boolean isHasChildren() {
        return hasChildren;
    }

    /** Returns the badge Unicode symbol, or empty string if no badge. */
    public String getBadge() {
        return badge;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NoteDisplayItem other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
