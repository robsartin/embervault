package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.MapViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * FXML controller for the Map view.
 *
 * <p>Contains no business logic; all state is managed by the {@link MapViewModel}.</p>
 */
public class MapViewController {

    @FXML private Label mapLabel;

    private MapViewModel viewModel;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     */
    public void initViewModel(MapViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /** Returns the associated ViewModel. */
    public MapViewModel getViewModel() {
        return viewModel;
    }
}
