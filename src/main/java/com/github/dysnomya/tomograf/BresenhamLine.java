package com.github.dysnomya.tomograf;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BresenhamLine {
    private final List<Point> line;

    public BresenhamLine(double x0, double y0, double x1, double y1, int width, int height) {
        this.line = findLine(x0, y0, x1, y1, width, height);
    }

    private List<Point> findLine(double i0, double j0, double i1, double j1, int width, int height) {
        ArrayList<Point> line = new ArrayList<>();

        int x0 = (int) Math.round(i0);
        int y0 = (int) Math.round(j0);
        int x1 = (int) Math.round(i1);
        int y1 = (int) Math.round(j1);

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int e2;

        while (true) {
            line.add(new Point(x0, y0));

            if (x0 == x1 && y0 == y1)
                break;

            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }

        return line;
    }

    public List<Point> getLine() {
        return line;
    }
}
