package de.uhh.lt.xpertfinder.finder;

import java.util.Map;

public class ExpertFindingResult {

    private Map<String, Double> documentRelevanceMap;
    private Map<String, Double> authorRelevanceMap;

    public ExpertFindingResult(Map<String, Double> authorRelevanceMap) {
        this.authorRelevanceMap = authorRelevanceMap;
    }

    public ExpertFindingResult(Map<String, Double> documentRelevanceMap, Map<String, Double> authorRelevanceMap) {
        this.documentRelevanceMap = documentRelevanceMap;
        this.authorRelevanceMap = authorRelevanceMap;
    }

    public Map<String, Double> getDocumentRelevanceMap() {
        return documentRelevanceMap;
    }

    public void setDocumentRelevanceMap(Map<String, Double> documentRelevanceMap) {
        this.documentRelevanceMap = documentRelevanceMap;
    }

    public Map<String, Double> getAuthorRelevanceMap() {
        return authorRelevanceMap;
    }

    public void setAuthorRelevanceMap(Map<String, Double> authorRelevanceMap) {
        this.authorRelevanceMap = authorRelevanceMap;
    }
}
