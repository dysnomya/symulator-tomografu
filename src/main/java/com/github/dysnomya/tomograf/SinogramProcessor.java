package com.github.dysnomya.tomograf;

import javafx.embed.swing.SwingFXUtils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import org.jtransforms.fft.DoubleFFT_1D;

public class SinogramProcessor {
    private BufferedImage image;
    private int scans;
    private int detectors;
    private int angle;
    private double[][] sinogramTable;
    private double tableMin;
    private double tableMax;
    private BufferedImage sinogram;

    public SinogramProcessor(BufferedImage image, int scans, int detectors, int angle) {
        this.image = image;
        this.scans = scans;
        this.detectors = detectors;
        this.angle = angle;
        this.sinogram = new BufferedImage(detectors, scans, BufferedImage.TYPE_BYTE_GRAY);
        this.sinogramTable = new double[detectors][scans];
        this.tableMin = Double.MAX_VALUE;
        this.tableMax = Double.MIN_VALUE;
    }

    public Image getSinogram() {
        return SwingFXUtils.toFXImage(sinogram, null);
    }

    public double[][] getSinogramTable() {
        return this.sinogramTable;
    }

    public void fillSinogramTable(BresenhamLine[][] lines) {

        for (int i = 0; i < detectors; i++) {
            for (int j = 0; j < scans; j++) {
                sinogramTable[i][j] = getColorFromLine(lines[j][i].getLine());

            }
        }
    }

    public int getColorFromLine(List<Point> line) {
        int value = 0;

        for (Point point : line) {
            int x = (int) point.getX();
            int y = (int) point.getY();
            if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
                value += new Color(image.getRGB(x, y)).getRed();
            }

        }

        this.tableMin = Math.min(value, tableMin);
        this.tableMax = Math.max(value, tableMax);

        return value;
    }

    public void drawSinogram() {
        for (int i = 0; i < detectors; i++) {
            for (int j = 0; j < scans; j++) {
                int color = getNormalizedColor(i, j).getRGB();
                sinogram.setRGB(i, j, color);
            }
        }
    }

    private Color getNormalizedColor(int x, int y) {
        if (tableMin == tableMax) {
            return new Color(0, 0, 0);
        } else {
            int value = (int) (((sinogramTable[x][y] - tableMin) * 255.0) / (tableMax - tableMin));
//            System.out.println(value + " " + tableMin + " " + tableMax);
            return new Color(value, value, value);
        }
    }

    public void filterSinogram() {
        double[] kernel = createKernel();

        for (int detector = 0; detector < detectors; detector++) {
            sinogramTable[detector] = convolve(sinogramTable[detector], kernel);
        }
    }

    private double[] createKernel() {
        double[] kernel = new double[21];

        for (int i = 0; i < 21; i++) {
            int k = i - 10;

            if (k == 0) {
                kernel[i] = 1.0;
            } else if (k % 2 == 0) {
                kernel[i] = 0.0;
            } else {
                kernel[i] = (-4.0 / Math.pow(Math.PI, 2)) / (k * k);
            }
        }

        return kernel;
    }

    public double[] convolve(double[] input, double[] kernel) {
        int inputLength = input.length;
        int kernelLength = kernel.length;
        int outputLength = inputLength;
        double[] output = new double[outputLength];

        int pad = (kernelLength - 1) / 2;

        for (int i = 0; i < outputLength; i++) {
            double sum = 0;
            for (int j = 0; j < kernelLength; j++) {
                int inputIndex = i - pad + j;
                if (inputIndex >= 0 && inputIndex < inputLength) {
                    sum += input[inputIndex] * kernel[j];
                }
            }
            output[i] = sum;

            tableMin = Math.min(sum, tableMin);
            tableMax = Math.max(sum, tableMax);
        }

        return output;
    }

}
