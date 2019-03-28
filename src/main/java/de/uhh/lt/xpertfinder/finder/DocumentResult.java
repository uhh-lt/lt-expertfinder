package de.uhh.lt.xpertfinder.finder;

import de.uhh.lt.xpertfinder.model.profiles.aan.Author;

import java.util.List;

public class DocumentResult implements Comparable {

    private String title;
    private String file;
    private Double relevance;
    private String venue;
    private int year;
    private int citations;
    private List<Author> authors;
    private List<String> keywords;

    public DocumentResult() {
    }

    public DocumentResult(String title, String file, double relevance, String venue, int year) {
        this.title = title;
        this.file = file;
        this.relevance = relevance;
        this.venue = venue;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Double getRelevance() {
        return relevance;
    }

    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getCitations() {
        return citations;
    }

    public void setCitations(int citations) {
        this.citations = citations;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public int compareTo(Object o) {
        DocumentResult other = (DocumentResult) o;
        return this.getRelevance().compareTo(other.getRelevance());
    }
}
