package com.github.dysnomya.tomograf;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import java.util.List;

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
    private Slider slider;

    @FXML
    private Slider filteredSlider;

    @FXML
    private ImageView sliderView;

    @FXML
    private ImageView filteredSliderView;

    private URL url;
    private List<Image> frames = new ArrayList<>();
    private List<Image> filteredFrames = new ArrayList<>();

    @FXML
    private void initialize() {
        String[] files = getFileNames();
        imageChoice.getItems().addAll(files);
        imageChoice.setValue(files[3]);
        generateImage();

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();

            if (index >= 0 && index < frames.size()) {
                sliderView.setImage(frames.get(index));
            }
        });

        filteredSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();

            if (index >= 0 && index < filteredFrames.size()) {
                filteredSliderView.setImage(filteredFrames.get(index));
            }
        });
    }

    private String[] getFileNames() {
        File folder = new File("src/main/resources/tomograf-obrazy");
        return folder.list();
    }

    @FXML
    private void generateImage() {
        imageBeforeName.setText(imageChoice.getValue());

        this.url = getClass().getResource("/tomograf-obrazy/" + imageChoice.getValue());
        imageBeforeView.setImage(new Image(url.toExternalForm()));

    }

    private void changeSliders() {
        slider.setMax(scansSlider.getValue());
        filteredSlider.setMax(scansSlider.getValue());
    }

    @FXML
    private void generateSinogram() {
        imageSinogram.setImage(null);
        imageResult.setImage(null);
        imageSinogramFiltered.setImage(null);
        imageResultFiltered.setImage(null);
        sliderView.setImage(null);
        filteredSliderView.setImage(null);


        changeSliders();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    BufferedImage image = ImageIO.read(url); // JDeli.read(file)
                    Sinogram sinogram = new Sinogram(image, (int) scansSlider.getValue(), (int) detectorsSlider.getValue(), (int) angleSlider.getValue());
                    imageSinogram.setImage(sinogram.processSinogram());
                    imageResult.setImage(sinogram.recreateImage(frames));
                    imageSinogramFiltered.setImage(sinogram.filterSinogram());
                    imageResultFiltered.setImage(sinogram.recreateImage(filteredFrames));

                    sliderView.setImage(frames.getFirst());
                    filteredSliderView.setImage(filteredFrames.getFirst());

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        System.out.println(e.getMessage());
                    });
                }

                return null;
            }
        };

        new Thread(task).start();
    }
}