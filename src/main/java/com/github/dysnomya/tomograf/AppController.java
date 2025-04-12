package com.github.dysnomya.tomograf;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

public class AppController {
    @FXML
    private GridPane gridpane;

    @FXML
    private ComboBox<String> imageChoice;

    @FXML
    private Label imageBeforeName;

    @FXML
    private ImageView imageBeforeView;

    @FXML
    private ImageView imageSinogram;

    @FXML
    private ImageView imageResult;

    @FXML
    private ImageView imageSinogramFiltered;

    @FXML
    private ImageView imageResultFiltered;

    @FXML
    private Slider scansSlider;

    @FXML
    private Slider detectorsSlider;

    @FXML
    private Slider angleSlider;


    @FXML
    private void initialize() {
        imageChoice.getItems().addAll(getFileNames());
    }

    private String[] getFileNames() {
        File folder = new File("src/main/resources/tomograf-obrazy");
        return folder.list();
    }

    @FXML
    private void generateImage() {
        imageBeforeName.setText(imageChoice.getValue());

        URL url = getClass().getResource("/tomograf-obrazy/" + imageChoice.getValue());
        imageBeforeView.setImage(new Image(url.toExternalForm()));

        generateSinogram(url);
    }

    private void generateSinogram(URL url) {
        try {
            BufferedImage image = ImageIO.read(url); // JDeli.read(file)
            Sinogram sinogram = new Sinogram(image, (int) scansSlider.getValue(), (int) detectorsSlider.getValue(), (int) angleSlider.getValue());
            imageSinogram.setImage(sinogram.processSinogram());
            sinogram.recreateImage();
            imageResult.setImage(sinogram.getResultImage());

            imageSinogramFiltered.setImage(sinogram.filterSinogram());

            imageResultFiltered.setImage(sinogram.recreateImage());

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}