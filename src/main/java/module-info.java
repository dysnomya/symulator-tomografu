module com.github.dysnomya.tomograf {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires commons.math3;
    requires dcm4che.core;
    requires dcm4che.imageio;


    opens com.github.dysnomya.tomograf to javafx.fxml;
    exports com.github.dysnomya.tomograf;
}