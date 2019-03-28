package de.uhh.lt.xpertfinder.utils;

import java.util.List;

public class MathUtils {

    public static double softmax(double target, List<Double> all) {

        double esum = 0;
        for(double d : all) {
            esum += Math.exp(d);
        }

        return Math.exp(target) / esum;
    }
}
