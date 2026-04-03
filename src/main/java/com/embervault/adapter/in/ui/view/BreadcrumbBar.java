package com.embervault.adapter.in.ui.view;

import java.util.function.IntConsumer;

import com.embervault.adapter.in.ui.viewmodel.BreadcrumbEntry;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * A horizontal breadcrumb bar showing the drill-down path as clickable labels
 * separated by "&#x203a;" separators. The last (current) entry is displayed as
 * plain text; earlier entries are clickable and invoke the supplied callback.
 */
public final class BreadcrumbBar extends HBox {

    private static final String SEPARATOR = " \u203A ";
    private static final String LINK_STYLE =
            "-fx-text-fill: #4A90D9; -fx-underline: true; -fx-cursor: hand;";
    private static final String CURRENT_STYLE =
            "-fx-text-fill: #333333; -fx-font-weight: bold;";

    private final ObservableList<BreadcrumbEntry> breadcrumbs;
    private final IntConsumer onNavigate;

    /**
     * Creates a breadcrumb bar bound to the given breadcrumb list.
     *
     * @param breadcrumbs the observable breadcrumb entries
     * @param onNavigate  callback receiving the breadcrumb index when clicked
     */
    public BreadcrumbBar(ObservableList<BreadcrumbEntry> breadcrumbs,
            IntConsumer onNavigate) {
        this.breadcrumbs = breadcrumbs;
        this.onNavigate = onNavigate;
        setSpacing(0);
        rebuild();
        breadcrumbs.addListener(
                (ListChangeListener<BreadcrumbEntry>) change -> rebuild());
    }

    private void rebuild() {
        getChildren().clear();
        if (breadcrumbs.isEmpty()) {
            setVisible(false);
            setManaged(false);
            return;
        }
        setVisible(true);
        setManaged(true);
        for (int i = 0; i < breadcrumbs.size(); i++) {
            if (i > 0) {
                Label separator = new Label(SEPARATOR);
                separator.setStyle("-fx-text-fill: #999999;");
                getChildren().add(separator);
            }
            BreadcrumbEntry entry = breadcrumbs.get(i);
            Label label = new Label(entry.displayName());
            if (i < breadcrumbs.size() - 1) {
                // Clickable ancestor
                label.setStyle(LINK_STYLE);
                label.setCursor(Cursor.HAND);
                final int index = i;
                label.setOnMouseClicked(e -> onNavigate.accept(index));
            } else {
                // Current location — not clickable
                label.setStyle(CURRENT_STYLE);
            }
            getChildren().add(label);
        }
    }
}
