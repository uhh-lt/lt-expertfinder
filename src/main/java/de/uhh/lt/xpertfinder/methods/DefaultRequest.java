package de.uhh.lt.xpertfinder.methods;

public class DefaultRequest {

    private int documents;
    private int results;

    public DefaultRequest() {
        this.documents = 1000;
        this.results = 25;
    }

    public DefaultRequest(int documents, int results) {
        this.documents = documents;
        this.results = results;
    }

    public int getDocuments() {
        return documents;
    }

    public void setDocuments(int documents) {
        this.documents = documents;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }
}
