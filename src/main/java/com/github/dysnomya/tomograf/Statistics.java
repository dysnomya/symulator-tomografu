package com.github.dysnomya.tomograf;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.DoubleSummaryStatistics;

public class Statistics {
    private final DoubleSummaryStatistics summary;
    private final StandardDeviation sd;

    Statistics() {
        this.summary = new DoubleSummaryStatistics();
        this.sd = new StandardDeviation();
    }

    public void add(double number) {
        summary.accept(number);
        sd.increment(number);
    }

    public double getMin() {
        return summary.getMin();
    }

    public double getMax() {
        return summary.getMax();
    }

    public double getAverage() {
        return summary.getAverage();
    }

    public double getStandardDeviation() {
        return sd.getResult();
    }
}
