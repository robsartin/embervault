module com.embervault {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.embervault to javafx.fxml;
    exports com.embervault;

    // Domain layer: entities, value objects, Result type, exception hierarchy
    // (ADR-0010, ADR-0016, ADR-0017).
    exports com.embervault.domain;

    // Hexagonal architecture packages (ADR-0009).
    exports com.embervault.application.port.in;
    exports com.embervault.application.port.out;
    exports com.embervault.application;
    exports com.embervault.adapter.out.persistence;
    exports com.embervault.adapter.in.ui.viewmodel;
    exports com.embervault.adapter.in.ui.view;

    // MVVM sub-packages within the UI adapter (ADR-0013).
    // Open to javafx.fxml so FXMLLoader can reflectively instantiate controllers.
    opens com.embervault.adapter.in.ui.view to javafx.fxml;
    opens com.embervault.domain to javafx.base;
}
