package com.github.dysnomya.tomograf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class SinogramProcessor {
    private BufferedImage image;
    private int scans;
    private int detectors;
    private int angle;
    private double[][] sinogramTable;
    private Statistics statistics;
    private BufferedImage sinogram;


    public SinogramProcessor(BufferedImage image, int scans, int detectors, int angle) {
        this.image = image;
        this.scans = scans;
        this.detectors = detectors;
        this.angle = angle;
        this.sinogram = new BufferedImage(detectors, scans, BufferedImage.TYPE_BYTE_GRAY);
        this.sinogramTable = new double[detectors][scans];
        this.statistics = new Statistics();
    }

    public Image getSinogram() {
        return SwingFXUtils.toFXImage(sinogram, null);
    }

    public BufferedImage getSinogramBufferedImage() {
        return sinogram;
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
                value += (image.getRGB(x, y) >> 16) & 0xFF;
            }

        }

        value = value / line.size();

        statistics.add(value);

        return value;
    }

    public void drawSinogram() {
        for (int i = 0; i < detectors; i++) {
            for (int j = 0; j < scans; j++) {
                int color = getNormalizedColor(i, j);
                sinogram.getRaster().setSample(i, j, 0, color);
            }
        }
    }

    private int getNormalizedColor(int x, int y) {

        double min = statistics.getMin();
        double max = statistics.getMax();

        if (min == max) {
            return 0;
        } else {
            int value = (int) (((sinogramTable[x][y] - min) * 255.0) / (max - min));

            value = Math.max(0, value);
            value = Math.min(255, value);

            return value;
        }
    }

    public void filterSinogram() {
        double[] kernel = createKernel();

        statistics = new Statistics(); // TODO: do sth with that

//        for (int detector = 0; detector < detectors; detector++) {
//            sinogramTable[detector] = convolve(sinogramTable[detector], kernel);
//        }

        double[][] filteredSinogram = new double[detectors][scans];

        for (int scan = 0; scan < scans; scan++) {
            for (int detector = 0; detector < detectors; detector++) {
                double sum = 0;

                for (int i = 0; i < kernel.length; i++) {
                    int index = detector - kernel.length / 2 + i;

                    if (index >= 0 && index < detectors) {
                        sum += sinogramTable[index][scan] * kernel[i];
                    }
                }

                filteredSinogram[detector][scan] = sum;
                statistics.add(sum);
            }
        }

        sinogramTable = filteredSinogram;
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

            statistics.add(sum);
        }

        return output;
    }

}
