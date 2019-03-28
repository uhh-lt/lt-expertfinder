package de.uhh.lt.xpertfinder.model.graph;

public class Collaboration {

    private String author;
    private int globalCount;
    private int localCount;
    private double weight;
    private double score;

    public Collaboration(String author, int count) {
        this.author = author;
        this.globalCount = count;
    }

    public String getAuthor() {
        return author;
    }

    public int getGlobalCount() {
        return globalCount;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getLocalCount() {
        return localCount;
    }

    public void setLocalCount(int localCount) {
        this.localCount = localCount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
