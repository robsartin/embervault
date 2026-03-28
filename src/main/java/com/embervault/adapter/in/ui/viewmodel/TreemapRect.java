package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Output rectangle from the treemap layout algorithm.
 *
 * @param id     the unique identifier linking back to the input item
 * @param x      the x-coordinate of the top-left corner
 * @param y      the y-coordinate of the top-left corner
 * @param width  the width of the rectangle
 * @param height the height of the rectangle
 */
public record TreemapRect(UUID id, double x, double y, double width, double height) {
}
