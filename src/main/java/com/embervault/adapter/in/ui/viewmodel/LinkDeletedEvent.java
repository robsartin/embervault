package com.embervault.adapter.in.ui.viewmodel;

import java.util.UUID;

/**
 * Event published when a link between two notes is deleted.
 *
 * @param sourceId      the source note ID
 * @param destinationId the destination note ID
 */
public record LinkDeletedEvent(UUID sourceId, UUID destinationId) {
}
