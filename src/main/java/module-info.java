module com.embervault {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.embervault to javafx.fxml;
    exports com.embervault;
}
