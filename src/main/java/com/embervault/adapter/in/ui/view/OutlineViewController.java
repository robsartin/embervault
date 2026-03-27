package com.embervault.adapter.in.ui.view;

import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * FXML controller for the Outline view.
 *
 * <p>Contains no business logic; all state is managed by the {@link OutlineViewModel}.</p>
 */
public class OutlineViewController {

    @FXML private Label outlineLabel;

    private OutlineViewModel viewModel;

    /**
     * Injects the ViewModel and binds UI controls to its properties.
     */
    public void initViewModel(OutlineViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /** Returns the associated ViewModel. */
    public OutlineViewModel getViewModel() {
        return viewModel;
    }
}
