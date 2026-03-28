package com.embervault.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.embervault.domain.Link;

/**
 * Outbound port for persisting and retrieving links between notes.
 */
public interface LinkRepository {

    /**
     * Saves the given link, creating or replacing it by id.
     *
     * @param link the link to save
     * @return the saved link
     */
    Link save(Link link);

    /**
     * Deletes the link with the given id.
     *
     * @param id the link id
     */
    void delete(UUID id);

    /**
     * Finds a link by its unique identifier.
     *
     * @param id the link id
     * @return an optional containing the link, or empty
     */
    Optional<Link> findById(UUID id);

    /**
     * Returns all links originating from the given source note.
     *
     * @param sourceId the source note id
     * @return the list of outbound links
     */
    List<Link> findLinksFrom(UUID sourceId);

    /**
     * Returns all links pointing to the given destination note.
     *
     * @param destId the destination note id
     * @return the list of inbound links
     */
    List<Link> findLinksTo(UUID destId);

    /**
     * Returns all links where the given note is either the source or destination.
     *
     * @param noteId the note id
     * @return the list of all connected links
     */
    List<Link> findAllLinksFor(UUID noteId);
}
