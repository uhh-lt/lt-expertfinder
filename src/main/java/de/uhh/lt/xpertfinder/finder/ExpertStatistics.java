package de.uhh.lt.xpertfinder.finder;

public class ExpertStatistics {

    private int relevantDocs;
    private int selectedDocs;
    private int authors;

    private int publications;
    private int citations;
    private int collaborations;

    public ExpertStatistics(int relevantDocs, int selectedDocs, int authors, int publications, int citations, int collaborations) {
        this.relevantDocs = relevantDocs;
        this.selectedDocs = selectedDocs;
        this.authors = authors;
        this.publications = publications;
        this.citations = citations;
        this.collaborations = collaborations;
    }

    public int getRelevantDocs() {
        return relevantDocs;
    }

    public void setRelevantDocs(int relevantDocs) {
        this.relevantDocs = relevantDocs;
    }

    public int getSelectedDocs() {
        return selectedDocs;
    }

    public void setSelectedDocs(int selectedDocs) {
        this.selectedDocs = selectedDocs;
    }

    public int getAuthors() {
        return authors;
    }

    public void setAuthors(int authors) {
        this.authors = authors;
    }

    public int getPublications() {
        return publications;
    }

    public void setPublications(int publications) {
        this.publications = publications;
    }

    public int getCitations() {
        return citations;
    }

    public void setCitations(int citations) {
        this.citations = citations;
    }

    public int getCollaborations() {
        return collaborations;
    }

    public void setCollaborations(int collaborations) {
        this.collaborations = collaborations;
    }
}
