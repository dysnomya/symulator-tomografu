package com.github.dysnomya.tomograf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Sinogram {
    private BufferedImage image;
    private int scans;
    private int detectors;
    private int angle;
    private BufferedImage sinogram;
    private BufferedImage resultImage;


    public Sinogram(BufferedImage image) {
        this.image = image;
        this.scans = 90;
        this.detectors = 180;
        this.angle = 270;
        this.sinogram = new BufferedImage(detectors, scans, BufferedImage.TYPE_BYTE_GRAY);
        this.resultImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    }

    public Sinogram(BufferedImage image, int scans, int detectors) {
        this.image = image;
        this.scans = scans;
        this.detectors = detectors;
        this.angle = 90;
        this.sinogram = new BufferedImage(detectors, scans, BufferedImage.TYPE_BYTE_GRAY);
        this.resultImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    }

    public Image getSinogram() {
        return SwingFXUtils.toFXImage(sinogram, null);
    }

    public Image getResultImage() {
        return SwingFXUtils.toFXImage(resultImage, null);
    }

    public void createSinogram() {
        double r = (double) Math.max(image.getHeight(), image.getWidth()) / 2;
        double alfa = Math.toRadians(360.0 / scans);
        double phi = Math.toRadians(angle);
        double pi = Math.PI;

        for (int j = 0; j < scans; j++) {
            for (int i = 0; i < detectors; i++) {
                double alfangle = Math.toRadians(90) + alfa * j;

                double x1 = r + r * Math.cos(alfangle);
                double y1 = r + r * Math.sin(alfangle);

                double x2 = r + r * Math.cos(alfangle + pi - phi / 2 + (i * (phi / (detectors - 1))));
                double y2 = r + r * Math.sin(alfangle + pi - phi / 2 + (i * (phi / (detectors - 1))));

                sinogram.setRGB(i, j, getColorFromImage(x1, y1, x2, y2));
            }
        }
//        normalizeSinogram();
    }

    private int getColorFromImage(double x1, double y1, double x2, double y2) {
        List<Integer> colorList = getLine(x1, y1, x2, y2);

//        int red = (int) Math.round(colorList.stream()
//                .mapToInt(c -> new Color(c).getRed())
//                .average()
//                .orElse(0));

        int red = Math.min(255, colorList.stream()
                .mapToInt(c -> new Color(c).getRed())
                .sum());

        return new Color(red, red, red).getRGB();
    }

    private void normalizeSinogram() {
        int minValue = 255;
        int maxValue = 0;

        for (int i = 0; i < sinogram.getWidth(); i++) {
            for (int j = 0; j < sinogram.getHeight(); j++) {
                int colorRGB = sinogram.getRGB(i, j);
                int red = new Color(colorRGB).getRed();
                minValue = Math.min(minValue, red);
                maxValue = Math.max(maxValue, red);
            }
        }

        for (int i = 0; i < sinogram.getWidth(); i++) {
            for (int j = 0; j < sinogram.getHeight(); j++) {
                int colorRGB = sinogram.getRGB(i, j);
                int red = new Color(colorRGB).getRed();
                red = (int) Math.round((red * 255.0) / maxValue);
                sinogram.setRGB(i, j, new Color(red, red, red).getRGB());
            }
        }
    }

    private List<Integer> getLine(double x1, double y1, double x2, double y2) {
        List<Integer> line = new ArrayList<>();

        int x1Int = (int) Math.round(x1);
        int y1Int = (int) Math.round(y1);
        int x2Int = (int) Math.round(x2);
        int y2Int = (int) Math.round(y2);

        int sx = x1Int < x2Int ? 1 : -1;
        int sy = y1Int < y2Int ? 1 : -1;

        int dx = Math.abs(x2Int - x1Int);
        int dy = Math.abs(y2Int - y1Int);

        int e = dx - dy;

        while (true) {
            if (x1Int >= 0 && x1Int < image.getWidth() && y1Int >= 0 && y1Int < image.getHeight()) {
                line.add(image.getRGB(x1Int, y1Int));
            }

            if (x1Int == x2Int && y1Int == y2Int) {
                break;
            }

            int e2 = e * 2;

            if (e2 > -dy) {
                e -= dy;
                x1Int += sx;
            }

            if (e2 < dx) {
                e += dx;
                y1Int += sy;
            }
        }

        return line;
    }

    public void recreateImage() {
        double r = (double) Math.max(image.getHeight(), image.getWidth()) / 2;
        double alfa = Math.toRadians(360.0 / scans);
        double phi = Math.toRadians(angle);
        double pi = Math.PI;

        for (int j = 0; j < scans; j++) {
            for (int i = 0; i < detectors; i++) {
                double alfangle = Math.toRadians(90) + alfa * j;

                double x1 = r + r * Math.cos(alfangle);
                double y1 = r + r * Math.sin(alfangle);

                double x2 = r + r * Math.cos(alfangle + pi - phi / 2 + (i * (phi / (detectors - 1))));
                double y2 = r + r * Math.sin(alfangle + pi - phi / 2 + (i * (phi / (detectors - 1))));

                colorLine(x1, y1, x2, y2, sinogram.getRGB(i, j));
            }
        }

    }

    private void colorLine(double x1, double y1, double x2, double y2, int rgb) {
        int x1Int = (int) Math.round(x1);
        int y1Int = (int) Math.round(y1);
        int x2Int = (int) Math.round(x2);
        int y2Int = (int) Math.round(y2);

        int sx = x1Int < x2Int ? 1 : -1;
        int sy = y1Int < y2Int ? 1 : -1;

        int dx = Math.abs(x2Int - x1Int);
        int dy = Math.abs(y2Int - y1Int);

        int e = dx - dy;

        while (true) {
            if (x1Int >= 0 && x1Int < image.getWidth() && y1Int >= 0 && y1Int < image.getHeight()) {
                if (rgb > resultImage.getRGB(x1Int, y1Int) ) {
                    resultImage.setRGB(x1Int, y1Int, rgb);
                }
            }

            if (x1Int == x2Int && y1Int == y2Int) {
                break;
            }

            int e2 = e * 2;

            if (e2 > -dy) {
                e -= dy;
                x1Int += sx;
            }

            if (e2 < dx) {
                e += dx;
                y1Int += sy;
            }
        }

    }
}