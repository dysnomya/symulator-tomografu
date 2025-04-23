package com.github.dysnomya.tomograf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.DoubleSummaryStatistics;

public class SinogramReconstructor {
    private BufferedImage reconstruction;
    private double[][] reconstructionTable;
    private DoubleSummaryStatistics statistics;

    public SinogramReconstructor(int width, int height) {
        this.reconstruction = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        this.reconstructionTable = new double[width][height];
        this.statistics = new DoubleSummaryStatistics();
    }

    public Image getReconstruction() {
        return SwingFXUtils.toFXImage(reconstruction, null);
    }

    public BufferedImage getReconstructionBufferedImage() {
        return reconstruction;
    }

    public void fillReconstructionTable(BresenhamLine[][] lines, double[][] sinogramTable, int scan, int detectors) {
        for (int detector = 0; detector < detectors; detector++) {
            double projectionValue = sinogramTable[detector][scan];

            for (Point point : lines[scan][detector].getLine()) {
                int x = (int) point.getX();
                int y = (int) point.getY();

                if (x >= 0 && x < reconstruction.getWidth() && y >= 0 && y < reconstruction.getHeight()) {
                    reconstructionTable[x][y] += projectionValue;

                    statistics.accept(reconstructionTable[x][y]);
                }
            }
        }
    }

    public void drawReconstruction() {
        for (int i = 0; i < reconstruction.getWidth(); i++) {
            for (int j = 0; j < reconstruction.getHeight(); j++) {
                int color = getNormalizedColor(i, j);
                reconstruction.getRaster().setSample(i, j, 0, color);
            }
        }
    }


    private int getNormalizedColor(int x, int y) {

        double min = statistics.getMin();
        double max = statistics.getMax();

        if (min == max) {
            return 0;
        } else {
            int value = (int) (((reconstructionTable[x][y] - min) * 255.0) / (max - min));
            value = Math.max(value, 0);
            return value;
        }
    }
}
