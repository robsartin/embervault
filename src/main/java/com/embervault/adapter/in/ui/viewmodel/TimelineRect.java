package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * A positioned rectangle on the timeline.
 *
 * @param id     the note id
 * @param x      the x-coordinate
 * @param y      the y-coordinate
 * @param width  the width (positive for date ranges, fixed for point-in-time)
 * @param height the height
 */
record TimelineRect(UUID id, double x, double y, double width, double height) {
}
