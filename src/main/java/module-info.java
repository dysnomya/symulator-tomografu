module com.github.dysnomya.tomograf {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;


    opens com.github.dysnomya.tomograf to javafx.fxml;
    exports com.github.dysnomya.tomograf;
}