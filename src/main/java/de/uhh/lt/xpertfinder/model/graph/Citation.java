package de.uhh.lt.xpertfinder.model.graph;

public class Citation {

    private String document;
    private int year;
    private int globalDist;
    private int localDist;
    private double weight;
    private double score;

    public Citation(String document) {
        this.document = document;
    }

    public String getDocument() {
        return document;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getGlobalDist() {
        return globalDist;
    }

    public void setGlobalDist(int globalDist) {
        this.globalDist = globalDist;
    }

    public int getLocalDist() {
        return localDist;
    }

    public void setLocalDist(int localDist) {
        this.localDist = localDist;
    }
}
