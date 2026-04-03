package com.embervault.adapter.in.ui.viewmodel;

import com.embervault.ViewType;

/**
 * Event published when a view is refreshed.
 *
 * <p>Allows other components to react to view refresh events without
 * direct coupling to the view that was refreshed.</p>
 *
 * @param viewType the type of the refreshed view
 */
public record ViewRefreshedEvent(ViewType viewType) {
}
