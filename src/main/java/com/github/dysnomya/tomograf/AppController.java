package com.github.dysnomya.tomograf;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextField;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppController {
    @FXML
    private GridPane gridpane;

    @FXML
    private ComboBox<String> imageChoice;

    @FXML
    private CheckBox imageType;

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
    private TextField scansTextField;

    @FXML
    private Slider detectorsSlider;

    @FXML
    private TextField detectorsTextField;

    @FXML
    private Slider angleSlider;

    @FXML
    private TextField angleTextField;

    @FXML
    private Slider slider;

    @FXML
    private Slider filteredSlider;

    @FXML
    private ImageView sliderView;

    @FXML
    private ImageView filteredSliderView;

    private URL url;
//    private File file;
    private List<Image> frames = new ArrayList<>();
    private List<Image> filteredFrames = new ArrayList<>();

    @FXML
    private void initialize() {
        System.out.println("RMSE LOGS | scans | detectors | angle | unfiltered | filtered");

        setFileNames();

        initSliderAndText(scansSlider, scansTextField);
        initSliderAndText(detectorsSlider, detectorsTextField);
        initSliderAndText(angleSlider, angleTextField);

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

    private void initSliderAndText(Slider slider, TextField text) {
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            text.setText(String.valueOf(newVal.intValue()));
        });

        text.setOnAction(e -> {
            try {
                double val = Double.parseDouble(text.getText());
                if (val >= slider.getMin() && val <= slider.getMax()) {
                    slider.setValue(val);
                } else {
                    text.setText(String.format("%.0f", slider.getValue()));
                }
            } catch (NumberFormatException ex) {
                text.setText(String.format("%.0f", slider.getValue()));
            }
        });

    }

    @FXML
    private void setFileNames() {
        imageChoice.getItems().clear();
        String[] files = getFileNames();
        imageChoice.getItems().addAll(files);

//        generateImage();
    }

    private String[] getFileNames() {
        File folder;

        if (imageType.isSelected()) {
            folder = new File("src/main/resources/tomograf-dicom");
        } else {
            folder = new File("src/main/resources/tomograf-obrazy");
        }

        return folder.list();
    }

    @FXML
    private void generateImage() {
        imageBeforeName.setText(imageChoice.getValue());

        if (imageType.isSelected()) {
            this.url = getClass().getResource("/tomograf-dicom/" + imageChoice.getValue());
            try {
                imageBeforeView.setImage(SwingFXUtils.toFXImage(ImageIO.read(url), null));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }


        } else {
            this.url = getClass().getResource("/tomograf-obrazy/" + imageChoice.getValue());
            imageBeforeView.setImage(new Image(url.toExternalForm()));
        }
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

        System.gc();

        changeSliders();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    BufferedImage image = ImageIO.read(url); // JDeli.read(file)
                    Sinogram sinogram = new Sinogram(image, (int) scansSlider.getValue(), (int) detectorsSlider.getValue(), (int) angleSlider.getValue());
                    imageSinogram.setImage(sinogram.processSinogram());

                    imageResult.setImage(sinogram.recreateImage(frames));

                    BufferedImage bufferedImage = sinogram.getBufferedImage();

                    imageSinogramFiltered.setImage(sinogram.filterSinogram());
                    imageResultFiltered.setImage(sinogram.recreateImage(filteredFrames));

                    BufferedImage filteredBufferedImage = sinogram.getBufferedImage();

                    sliderView.setImage(frames.getFirst());
                    filteredSliderView.setImage(filteredFrames.getFirst());

                    // save images
                    saveImage(bufferedImage, filteredBufferedImage, imageBeforeName.getText());

                    // calculate RMSE
                    System.out.println(imageBeforeName.getText() + " " + scansSlider.getValue() + " " +
                            detectorsSlider.getValue() + " " + angleSlider.getValue() + " " +
                            RMSE.calculateRMSE(image, bufferedImage) + " " + RMSE.calculateRMSE(image, filteredBufferedImage));

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

    private void saveImage(BufferedImage image, BufferedImage imageFiltered, String name) {
        try {

            // save normal image
            File file = new File("src/main/resources/wyniki/" + name);
            File fileFiltered = new File("src/main/resources/wyniki/filtered/" + name);

            if (imageType.isSelected()) {
                File inputFile = new File("src/main/resources/tomograf-dicom/" + name);
                DicomUtil.saveDicomImage(inputFile, file, image);
                DicomUtil.saveDicomImage(inputFile, fileFiltered, imageFiltered);

            } else {
                ImageIO.write(image, "png", file);
                ImageIO.write(imageFiltered, "jpg", fileFiltered);
            }

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
        }
    }
}