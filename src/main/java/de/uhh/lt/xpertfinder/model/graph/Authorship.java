package de.uhh.lt.xpertfinder.model.graph;

public class Authorship {

    private String author;
    private int globalHindex;
    private int localHindex;
    private double weight;
    private double score;

    public Authorship(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getGlobalHindex() {
        return globalHindex;
    }

    public void setGlobalHindex(int globalHindex) {
        this.globalHindex = globalHindex;
    }

    public int getLocalHindex() {
        return localHindex;
    }

    public void setLocalHindex(int localHindex) {
        this.localHindex = localHindex;
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
}
