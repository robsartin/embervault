module com.embervault {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.embervault to javafx.fxml;
    exports com.embervault;

    // Domain exception hierarchy (ADR-0016) and hexagonal architecture packages (ADR-0009).
    exports com.embervault.domain;
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
