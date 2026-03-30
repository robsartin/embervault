package com.embervault;

import com.embervault.application.port.in.LinkService;
import com.embervault.application.port.in.NoteService;
import com.embervault.application.port.in.StampService;
import com.embervault.domain.AttributeSchemaRegistry;
import com.embervault.domain.Project;

/**
 * Holds the shared singleton services that are common across all windows.
 *
 * <p>All windows in a multi-window setup share the same services and project,
 * ensuring they operate on the same underlying data.</p>
 *
 * @param project         the current project with root note
 * @param noteService     the note service
 * @param linkService     the link service
 * @param stampService    the stamp service
 * @param schemaRegistry  the attribute schema registry
 */
public record SharedServices(
        Project project,
        NoteService noteService,
        LinkService linkService,
        StampService stampService,
        AttributeSchemaRegistry schemaRegistry
) { }
