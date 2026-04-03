package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Event published when a link is created between two notes.
 *
 * @param sourceId      the source note ID
 * @param destinationId the destination note ID
 */
public record LinkCreatedEvent(UUID sourceId, UUID destinationId) {
}
