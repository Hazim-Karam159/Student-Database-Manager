module gov.iti.jets {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens gov.iti.jets to javafx.fxml;
    exports gov.iti.jets;
}