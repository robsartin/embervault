package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Input item for the treemap layout algorithm.
 *
 * @param id     the unique identifier for this item
 * @param weight the weight/size value determining area proportion
 */
public record TreemapItem(UUID id, double weight) {
}
