package de.uhh.lt.xpertfinder.finder;

import java.util.List;

public class ExpertResult implements Comparable {

    private long id;

    private String name;
    private Double score;
    private List<DocumentResult> documents;
    private List<String> keywords;

    private int localHindex;
    private int hindex;
    private int localpublications;
    private int publications;
    private int localcitations;
    private int citations;
    private int localcollaborations;
    private int collaborations;

    private String image;
    private String description;

    public ExpertResult() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public int getHindex() {
        return hindex;
    }

    public void setHindex(int hindex) {
        this.hindex = hindex;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLocalcollaborations() {
        return localcollaborations;
    }

    public void setLocalcollaborations(int localcollaborations) {
        this.localcollaborations = localcollaborations;
    }

    public long getId() {
        return id;
    }

    public int getLocalHindex() {
        return localHindex;
    }

    public void setLocalHindex(int localHindex) {
        this.localHindex = localHindex;
    }

    public int getCollaborations() {
        return collaborations;
    }

    public int getLocalpublications() {
        return localpublications;
    }

    public void setLocalpublications(int localpublications) {
        this.localpublications = localpublications;
    }

    public int getLocalcitations() {
        return localcitations;
    }

    public void setLocalcitations(int localcitations) {
        this.localcitations = localcitations;
    }

    public void setCollaborations(int collaborations) {
        this.collaborations = collaborations;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DocumentResult> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentResult> documents) {
        this.documents = documents;
    }

    @Override
    public int compareTo(Object o) {
        ExpertResult other = (ExpertResult) o;
        return this.getScore().compareTo(other.getScore());
    }
}
