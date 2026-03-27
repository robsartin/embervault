module com.embervault {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.embervault to javafx.fxml;
    exports com.embervault;

    // Hexagonal architecture packages (ADR-0009).
    // Exports will be added as classes are introduced in each package.
    // Package structure:
    //   com.embervault.domain                    - core domain model
    //   com.embervault.application.port.in       - inbound ports (use cases)
    //   com.embervault.application.port.out      - outbound ports (repositories)
    //   com.embervault.adapter.in.ui             - inbound adapters (JavaFX)
    //   com.embervault.adapter.out.persistence   - outbound adapters (storage)
}
