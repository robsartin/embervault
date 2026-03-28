package com.embervault.application.port.in;

import java.util.List;
import java.util.UUID;

import com.embervault.domain.Link;

/**
 * Inbound port defining the link management use cases.
 */
public interface LinkService {

    /**
     * Creates a new link between two notes with default type "untitled".
     *
     * @param source the source note id
     * @param dest   the destination note id
     * @return the created link
     */
    Link createLink(UUID source, UUID dest);

    /**
     * Creates a new link between two notes with the specified type.
     *
     * @param source the source note id
     * @param dest   the destination note id
     * @param type   the link type
     * @return the created link
     */
    Link createLink(UUID source, UUID dest, String type);

    /**
     * Returns all links originating from the given note.
     *
     * @param noteId the source note id
     * @return outbound links
     */
    List<Link> getLinksFrom(UUID noteId);

    /**
     * Returns all links pointing to the given note.
     *
     * @param noteId the destination note id
     * @return inbound links
     */
    List<Link> getLinksTo(UUID noteId);

    /**
     * Returns all links where the note is either source or destination.
     *
     * @param noteId the note id
     * @return all connected links
     */
    List<Link> getAllLinksFor(UUID noteId);

    /**
     * Deletes the link with the given id.
     *
     * @param linkId the link id
     */
    void deleteLink(UUID linkId);
}
