package com.embervault.adapter.in.ui.viewmodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.embervault.application.port.in.StampService;
import com.embervault.domain.Stamp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for the Stamp Editor dialog.
 *
 * <p>Decouples the stamp editor view from the domain {@link Stamp} type by
 * exposing stamp data as observable string lists and providing CRUD operations
 * through the {@link StampService}.</p>
 */
public final class StampEditorViewModel {

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
     * @param name   the stamp name
     * @param action the action expression
     */
    public void createStamp(String name, String action) {
        stampService.createStamp(name, action);
        refresh();
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
     * @param oldId  the id of the stamp to replace
     * @param name   the new name
     * @param action the new action
     */
    public void updateStamp(UUID oldId, String name, String action) {
        stampService.deleteStamp(oldId);
        stampService.createStamp(name, action);
        refresh();
    }
}
