package com.embervault;

import java.util.Objects;

import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Project;

/**
 * Wraps the shared dependencies needed to set up a new window.
 *
 * <p>Provides convenient accessors that delegate to
 * {@link SharedServices} while also carrying the
 * {@link WindowManager} reference.</p>
 *
 * @param services      the shared services
 * @param windowManager the window manager
 */
public record WindowSetupContext(
        SharedServices services,
        WindowManager windowManager) {

    /** Compact canonical constructor with null checks. */
    public WindowSetupContext {
        Objects.requireNonNull(services,
                "services must not be null");
        Objects.requireNonNull(windowManager,
                "windowManager must not be null");
    }

    /** Returns the current project. */
    public Project project() {
        return services.project();
    }

    /** Returns the note service. */
    public NoteService noteService() {
        return services.noteService();
    }

    /** Returns the link service. */
    public LinkService linkService() {
        return services.linkService();
    }

    /** Returns the stamp service. */
    public StampService stampService() {
        return services.stampService();
    }

    /** Returns the attribute schema registry. */
    public AttributeSchemaRegistry schemaRegistry() {
        return services.schemaRegistry();
    }
}
