package com.embervault.adapter.in.ui.viewmodel;

/**
 * Event published when a view is refreshed.
 *
 * <p>Allows other components to react to view refresh events without
 * direct coupling to the view that was refreshed.</p>
 *
 * @param viewTypeName the display name of the refreshed view type
 */
public record ViewRefreshedEvent(String viewTypeName) {
}
