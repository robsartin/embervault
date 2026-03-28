package com.embervault.adapter.in.ui.viewmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.StampService;
import com.embervault.domain.Stamp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ViewModel for the Stamp Editor dialog.
 *
 * <p>Decouples the stamp editor view from the domain {@link Stamp} type by
 * exposing stamp data as observable string lists and providing CRUD operations
 * through the {@link StampService}.</p>
 */
public final class StampEditorViewModel {

    private static final Logger LOG = LoggerFactory.getLogger(
            StampEditorViewModel.class);

    private final StampService stampService;
    private final ObservableList<String> stampNames =
            FXCollections.observableArrayList();
    private final Map<String, UUID> nameToId = new LinkedHashMap<>();
    private final Map<String, String> nameToAction = new LinkedHashMap<>();

    /**
     * Constructs a StampEditorViewModel with the given stamp service.
     *
     * @param stampService the stamp service
     */
    public StampEditorViewModel(StampService stampService) {
        this.stampService = Objects.requireNonNull(stampService,
                "stampService must not be null");
    }

    /**
     * Returns the observable list of stamp names for display in the list view.
     *
     * @return the stamp names
     */
    public ObservableList<String> getStampNames() {
        return stampNames;
    }

    /**
     * Refreshes the stamp names from the service.
     */
    public void refresh() {
        stampNames.clear();
        nameToId.clear();
        nameToAction.clear();
        for (Stamp stamp : stampService.getAllStamps()) {
            stampNames.add(stamp.name());
            nameToId.put(stamp.name(), stamp.id());
            nameToAction.put(stamp.name(), stamp.action());
        }
    }

    /**
     * Returns the stamp id for the given name.
     *
     * @param name the stamp name
     * @return the stamp id, or null if not found
     */
    public UUID getIdForName(String name) {
        return nameToId.get(name);
    }

    /**
     * Returns the action string for the given stamp name.
     *
     * @param name the stamp name
     * @return the action string, or null if not found
     */
    public String getActionForName(String name) {
        return nameToAction.get(name);
    }

    /**
     * Creates a new stamp with the given name and action.
     *
     * <p>Returns {@code false} without creating anything if name or action
     * is null or blank.</p>
     *
     * @param name   the stamp name
     * @param action the action expression
     * @return true if the stamp was created, false if validation failed
     */
    public boolean createStamp(String name, String action) {
        if (name == null || name.isBlank()
                || action == null || action.isBlank()) {
            LOG.warn("Cannot create stamp: name and action "
                    + "must not be blank");
            return false;
        }
        stampService.createStamp(name, action);
        refresh();
        return true;
    }

    /**
     * Deletes the stamp with the given id.
     *
     * @param id the stamp id
     */
    public void deleteStamp(UUID id) {
        stampService.deleteStamp(id);
        refresh();
    }

    /**
     * Updates a stamp by deleting the old one and creating a new one.
     *
     * <p>Returns {@code false} without modifying anything if name or action
     * is null or blank.</p>
     *
     * @param oldId  the id of the stamp to replace
     * @param name   the new name
     * @param action the new action
     * @return true if the stamp was updated, false if validation failed
     */
    public boolean updateStamp(UUID oldId, String name, String action) {
        if (name == null || name.isBlank()
                || action == null || action.isBlank()) {
            LOG.warn("Cannot save stamp: name and action "
                    + "must not be blank");
            return false;
        }
        stampService.deleteStamp(oldId);
        stampService.createStamp(name, action);
        refresh();
        return true;
    }
}
