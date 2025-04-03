package com.github.dysnomya.tomograf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SinogramReconstructor {
    private double tableMin;
    private double tableMax;
    private BufferedImage reconstruction;
    private double[][] reconstructionTable;

    public SinogramReconstructor(int width, int height) {
        this.reconstruction = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        this.reconstructionTable = new double[width][height];
        this.tableMin = Double.MAX_VALUE;
        this.tableMax = Double.MIN_VALUE;
    }

    public Image getReconstruction() {
        return SwingFXUtils.toFXImage(reconstruction, null);
    }

    public void fillReconstructionTable(BresenhamLine[][] lines, double[][] sinogramTable, int scans, int detectors) {
        for (int i = 0; i < detectors; i++) {
            for (int j = 0; j < scans; j++) {
                for (Point point: lines[j][i].getLine()) {
                    int x = (int) point.getX();
                    int y = (int) point.getY();

                    if (x >= 0 && x < reconstruction.getWidth() && y >= 0 && y < reconstruction.getHeight()) {
                        reconstructionTable[x][y] += sinogramTable[i][j];

                        this.tableMin = Math.min(reconstructionTable[x][y], tableMin);
                        this.tableMax = Math.max(reconstructionTable[x][y], tableMax);
                    }
                }
            }
        }
    }

    public void drawReconstruction() {
        for (int i = 0; i < reconstruction.getWidth(); i++) {
            for (int j = 0; j < reconstruction.getHeight(); j++) {
                int color = getNormalizedColor(i, j).getRGB();
                reconstruction.setRGB(i, j, color);
            }
        }
    }

    private Color getNormalizedColor(int x, int y) {
        if (tableMin == tableMax) {
            return new Color(0, 0, 0);
        } else {
            int value = (int) (((reconstructionTable[x][y] - tableMin) * 255.0) / (tableMax - tableMin));
            return new Color(value, value, value);
        }
    }
}
