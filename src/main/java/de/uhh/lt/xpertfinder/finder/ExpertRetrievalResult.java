package de.uhh.lt.xpertfinder.finder;

import java.util.List;
import java.util.Map;

public class ExpertRetrievalResult {

    private Map<String, Double> expertResultList;
    private Map<String, Double> documentResultMap;

    public ExpertRetrievalResult(Map<String, Double> expertResultList, Map<String, Double> documentResultMap) {
        this.expertResultList = expertResultList;
        this.documentResultMap = documentResultMap;
    }

    public Map<String, Double> getExpertResultList() {
        return expertResultList;
    }

    public void setExpertResultList(Map<String, Double> expertResultList) {
        this.expertResultList = expertResultList;
    }

    public Map<String, Double> getDocumentResultList() {
        return documentResultMap;
    }

    public void setDocumentResultList(Map<String, Double> documentResultMap) {
        this.documentResultMap = documentResultMap;
    }
}
