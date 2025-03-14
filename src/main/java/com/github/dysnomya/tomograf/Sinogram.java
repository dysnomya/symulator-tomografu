package com.github.dysnomya.tomograf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import java.awt.image.BufferedImage;

public class Sinogram {
    private BufferedImage image;
    private int scans;
    private int detectors;

    public Sinogram(BufferedImage image) {
        this.image = image;
        this.scans = 90;
        this.detectors = 180;
    }

    public Sinogram(BufferedImage image, int scans, int detectors) {
        this.image = image;
        this.scans = scans;
        this.detectors = detectors;
    }

    public Image getImage() {
        return SwingFXUtils.toFXImage(image, null);
    }

    public void doSth() {
        System.out.println(image.getHeight());
    }
}
