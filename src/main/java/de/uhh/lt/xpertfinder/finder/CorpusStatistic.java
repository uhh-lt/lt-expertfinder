package de.uhh.lt.xpertfinder.finder;

import java.util.Map;

public class CorpusStatistic {

    private double docCount;
    private double sumTtf;
    private double avgDocLength;
    private Map<String, Integer> totalTermFrequencies;
    private Map<String,Double> pts;

    public CorpusStatistic(double docCount, double sumTtf, double avgDocLength, Map<String, Integer> totalTermFrequencies, Map<String, Double> pts) {
        this.docCount = docCount;
        this.sumTtf = sumTtf;
        this.avgDocLength = avgDocLength;
        this.totalTermFrequencies = totalTermFrequencies;
        this.pts = pts;
    }

    public double getDocCount() {
        return docCount;
    }

    public double getSumTtf() {
        return sumTtf;
    }

    public double getAvgDocLength() {
        return avgDocLength;
    }

    public int getTotalTermFrequency(String term) {
        return totalTermFrequencies.get(term);
    }

    public double getPt(String term) {
        return pts.get(term);
    }
}
