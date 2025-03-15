package com.github.dysnomya.tomograf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    private int[][] sinogramTable;
    private BufferedImage sinogram;
    private int[][] resultTable;
    private int[][] hitCount;
    private BufferedImage resultImage;


    public Sinogram(BufferedImage image) {
        this.image = image;
        this.scans = 90;
        this.detectors = 180;
        this.angle = 270;
        this.sinogramTable = new int[detectors][scans];
        this.sinogram = new BufferedImage(detectors, scans, BufferedImage.TYPE_BYTE_GRAY);
        this.resultTable = new int[image.getWidth()][image.getHeight()];
        this.hitCount = new int[image.getWidth()][image.getHeight()];
        this.resultImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    }

    public Sinogram(BufferedImage image, int scans, int detectors) {
        this.image = image;
        this.scans = scans;
        this.detectors = detectors;
        this.angle = 180;
        this.sinogramTable = new int[detectors][scans];
        this.sinogram = new BufferedImage(detectors, scans, BufferedImage.TYPE_BYTE_GRAY);
        this.resultTable = new int[image.getWidth()][image.getHeight()];
        this.hitCount = new int[image.getWidth()][image.getHeight()];
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

                sinogramTable[i][j] = getColorFromImage(x1, y1, x2, y2);
            }
        }
        normalizeSinogram();
    }

    private int getColorFromImage(double x1, double y1, double x2, double y2) {
        List<Point> colorList = getLine(x1, y1, x2, y2);

        return colorList.stream()
                .mapToInt(point -> {
                    int x = (int) point.getX();
                    int y = (int) point.getY();
                    return new Color(image.getRGB(x, y)).getRed();
                })
                .sum();
    }

    private void normalizeSinogram() {
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;

        for (int i = 0; i < detectors; i++) {
            for (int j = 0; j < scans; j++) {
                int radon = sinogramTable[i][j];
                minValue = Math.min(minValue, radon);
                maxValue = Math.max(maxValue, radon);
            }
        }

        for (int i = 0; i < detectors; i++) {
            for (int j = 0; j < scans; j++) {
                int radon = sinogramTable[i][j];
//                int color = (int) Math.round(((radon) * 255.0) / (maxValue));
                int color = (int) Math.round(((radon - minValue) * 255.0) / (maxValue - minValue));
                sinogram.setRGB(i, j, new Color(color, color, color).getRGB());
            }
        }
    }

    private List<Point> getLine(double x1, double y1, double x2, double y2) {
        List<Point> line = new ArrayList<>();

        int i1 = (int) Math.round(x1);
        int j1 = (int) Math.round(y1);
        int i2 = (int) Math.round(x2);
        int j2 = (int) Math.round(y2);

        int dx = Math.abs(i2 - i1);
        int dy = Math.abs(j2 - j1);

        int sx = i1 < i2 ? 1 : -1;
        int sy = j1 < j2 ? 1 : -1;

        int e = dx - dy;
        int e2;

        while (true) {
            if (i1 >= 0 && i1 < image.getWidth() && j1 >= 0 && j1 < image.getHeight()) {
                line.add(new Point(i1, j1));
            }

            if (i1 == i2 && j1 == j2) {
                break;
            }

            e2 = 2 * e;
            if (e2 > -dy) {
                i1 += sx;
                e -= dy;
            }

            if (e2 < dx) {
                j1 += sy;
                e += dx;
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

                colorLine(x1, y1, x2, y2, sinogramTable[i][j]);


            }
        }

        normalizeResult();

    }

    private void normalizeResult() {
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        System.out.println("normalizing");

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                if (hitCount[i][j] != 0) {
                    resultTable[i][j] /= hitCount[i][j];
                }
                int radon = resultTable[i][j];
                minValue = Math.min(minValue, radon);
                maxValue = Math.max(maxValue, radon);
            }
        }

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int radon = resultTable[i][j];
//                int color = (int) Math.round(((radon) * 255.0) / (maxValue));
                int color = (int) Math.round(((radon - minValue) * 255.0) / (maxValue - minValue));
                resultImage.setRGB(i, j, new Color(color, color, color).getRGB());
            }
        }
    }

    private void colorLine(double x1, double y1, double x2, double y2, int rgb) {
        List<Point> pointList = getLine(x1, y1, x2, y2);

        for (Point point : pointList) {
            int x = (int) point.getX();
            int y = (int) point.getY();
            resultTable[x][y] += rgb;
            hitCount[x][y] += 1;
        }
    }
}