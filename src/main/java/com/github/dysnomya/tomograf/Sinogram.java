package com.github.dysnomya.tomograf;

import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Sinogram {
    private BufferedImage image;
    private int scans;
    private int detectors;
    private int angle;
    private BresenhamLine[][] lines;
    private SinogramProcessor sinogramProcessor;


    public Sinogram(BufferedImage image) {
        this.image = image;
        this.scans = 90;
        this.detectors = 180;
        this.angle = 180;
    }

    public Sinogram(BufferedImage image, int scans, int detectors, int angle) {
        this(image);
        this.scans = scans;
        this.detectors = detectors;
        this.angle = angle;
    }

    public Image processSinogram() {
        this.lines = createLines();
        sinogramProcessor = new SinogramProcessor(image, scans, detectors, angle);
        sinogramProcessor.fillSinogramTable(this.lines);

        sinogramProcessor.drawSinogram();
        return sinogramProcessor.getSinogram();
    }

    public Image filterSinogram() {
        sinogramProcessor.filterSinogram();
        sinogramProcessor.drawSinogram();
        return sinogramProcessor.getSinogram();
    }

    public Image recreateImage(List<Image> sliderViews) {
        sliderViews.clear();

        SinogramReconstructor sr = new SinogramReconstructor(image.getWidth(), image.getHeight());

        // set 0 frame
        sliderViews.add(sr.getReconstruction());

        for (int scan = 0; scan < scans; scan++) {
            sr.fillReconstructionTable(lines, sinogramProcessor.getSinogramTable(), scan, detectors);
            sr.drawReconstruction();
            sliderViews.add(sr.getReconstruction());
        }

        return sr.getReconstruction();
    }

    public BresenhamLine[][] createLines() {
        BresenhamLine[][] lines = new BresenhamLine[scans][detectors];

        double r = Math.sqrt(Math.pow((double) image.getHeight() / 2, 2) + Math.pow((double) image.getWidth() / 2, 2));
//        double r = (double) Math.min(image.getHeight(), image.getWidth()) / 2;

        double rx = (double) image.getWidth() / 2;
        double ry = (double) image.getHeight() / 2;

        double alfa = Math.toRadians(360.0 / scans);
        double phi = Math.toRadians(angle);
        double pi = Math.PI;

        for (int j = 0; j < scans; j++) {
            for (int i = 0; i < detectors; i++) {
                double alfangle = Math.toRadians(-90) + alfa * j;

                double x1 = rx + r * Math.cos(alfangle);
                double y1 = ry + r * Math.sin(alfangle);

                double x2 = rx + r * Math.cos(alfangle + pi - phi / 2 + (i * (phi / (detectors - 1))));
                double y2 = ry + r * Math.sin(alfangle + pi - phi / 2 + (i * (phi / (detectors - 1))));

                lines[j][i] = new BresenhamLine(x1, y1, x2, y2);
            }
        }

        return lines;
    }
}