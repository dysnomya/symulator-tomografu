package com.github.dysnomya.tomograf;

import java.awt.image.BufferedImage;

public class RMSE {

    public static double calculateRMSE(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            throw new IllegalArgumentException("Obrazy muszą mieć ten sam rozmiar.");
        }

        double sumSquaredError = 0.0;

        int width = img1.getWidth();
        int height = img1.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int gray1 = (rgb1 >> 16) & 0xFF;
                int gray2 = (rgb2 >> 16) & 0xFF;

                int diff = gray1 - gray2;
                sumSquaredError += diff * diff;
            }
        }

        double mse = sumSquaredError / (width * height);
        return Math.sqrt(mse);
    }
}
