package de.uhh.lt.xpertfinder.utils;

import java.util.Arrays;

public class StatisticUtils {

    public static int hIndex(int[] citations) {
        if (citations == null || citations.length == 0) {
            return 0;
        }
        Arrays.sort(citations);
        for (int h = citations.length; h > 0; h--) {
            int x = citations.length - h;
            if (citations[x] >= h) {
                return h;
            }
        }
        return 0;
    }

}
