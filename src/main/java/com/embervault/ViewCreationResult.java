package com.embervault;

import java.util.UUID;
import java.util.function.Consumer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;

/**
 * Result of creating a view via a {@link ViewFactory}.
 *
 * <p>Encapsulates all the pieces that {@link ViewPaneContext} needs
 * to wire up after a view switch: the tab title binding, the refresh
 * and initial-load runnables, the controller initializer, and an
 * optional selected-note property for selection wiring.</p>
 *
 * @param tabTitle              the tab title property to bind
 * @param viewRefresh           the runnable to refresh the view
 * @param initialLoad           the runnable to perform initial data load
 * @param controllerInitializer the consumer to initialize the FXML
 *                              controller
 * @param selectedNoteIdProperty the selected note property, or null
 *                               if the view does not support selection
 */
public record ViewCreationResult(
        ReadOnlyStringProperty tabTitle,
        Runnable viewRefresh,
        Runnable initialLoad,
        Consumer<Object> controllerInitializer,
        ObjectProperty<UUID> selectedNoteIdProperty) {
}
