module com.github.dysnomya.tomograf {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.github.dysnomya.tomograf to javafx.fxml;
    exports com.github.dysnomya.tomograf;
}