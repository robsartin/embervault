package com.embervault.application;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.out.LinkRepository;
import com.embervault.domain.Link;

/**
 * Application service implementing link use cases.
 *
 * <p>Delegates persistence to the {@link LinkRepository} outbound port.</p>
 */
public final class LinkServiceImpl implements LinkService {

    private final LinkRepository repository;

    /**
     * Constructs a LinkServiceImpl backed by the given repository.
     *
     * @param repository the link repository
     */
    public LinkServiceImpl(LinkRepository repository) {
        this.repository = Objects.requireNonNull(repository,
                "repository must not be null");
    }

    @Override
    public Link createLink(UUID source, UUID dest) {
        Link link = Link.create(source, dest);
        return repository.save(link);
    }

    @Override
    public Link createLink(UUID source, UUID dest, String type) {
        Link link = Link.create(source, dest, type);
        return repository.save(link);
    }

    @Override
    public List<Link> getLinksFrom(UUID noteId) {
        return repository.findLinksFrom(noteId);
    }

    @Override
    public List<Link> getLinksTo(UUID noteId) {
        return repository.findLinksTo(noteId);
    }

    @Override
    public List<Link> getAllLinksFor(UUID noteId) {
        return repository.findAllLinksFor(noteId);
    }

    @Override
    public void deleteLink(UUID linkId) {
        repository.delete(linkId);
    }
}
