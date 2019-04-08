package de.uhh.lt.xpertfinder.utils;

import java.util.List;
import java.util.Map;

public class MathUtils {

    public static double softmax(double target, List<Double> all) {

        double esum = 0;
        for(double d : all) {
            esum += Math.exp(d);
        }

        return Math.exp(target) / esum;
    }

    public static boolean checkConvergence(Map<String, Double> map1, Map<String, Double> map2, double epsilon) {
        return Math.abs(calculateNorm2(map1) - calculateNorm2(map2)) < epsilon;
    }

    private static double calculateNorm2(Map<String, Double> map) {
        double squareSum = 0;
        for(Map.Entry<String, Double> entry : map.entrySet()) {
            squareSum = squareSum + Math.exp(Math.log(entry.getValue()) + Math.log(entry.getValue()));
        }

        return Math.sqrt(squareSum);
    }
}
