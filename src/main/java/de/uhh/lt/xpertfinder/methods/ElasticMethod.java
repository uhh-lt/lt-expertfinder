package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.model.graph.Authorship;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.service.ElasticSearchService;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ElasticMethod implements ExpertFindingMethod {

    @Autowired
    ElasticSearchService elasticSearch;

    @Override
    public String getId() {
        return "elastic";
    }

    @Override
    public String getName() {
        return "Elastic";
    }

    @Override
    public boolean needsCollaborations() {
        return false;
    }

    @Override
    public boolean needsCitations() {
        return false;
    }

    @Override
    public boolean needsPublications() {
        return true;
    }

    @Override
    public ExpertFindingResult findExperts(int k, double lambda, double epsilon, double md, double mca, ExpertTopic expertTopic) {
        Map<String, Double> authorRelevanceMap;
        Map<String, Double> documentRelevanceMap;

        // Get scores for documents from elasticsearch
        ElasticSearchService.ScoredDocumentResult result = elasticSearch.getScoredDocumentsForTopic(expertTopic.getTopic());

        // sum document scores for each author to get an author ranking
        authorRelevanceMap = sumDocumentScores(expertTopic.getGraph(), result.scores);

        // use the document scores for the document ranking
        documentRelevanceMap = result.scores;

        return new ExpertFindingResult(documentRelevanceMap, authorRelevanceMap);
    }

    private Map<String, Double> sumDocumentScores(Graph graph, Map<String, Double> documentScoreMap) {
        Map<String, Double> result = new HashMap<>();

        for(Map.Entry<String, Double> entry : documentScoreMap.entrySet()) {

            if(!graph.getDocumentAuthorNeighbors().containsKey(entry.getKey()))
                continue;

            for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(entry.getKey())) {
                String author = authorship.getAuthor();
                if(!result.containsKey(author)) {
                    result.put(author, entry.getValue());
                } else {
                    result.put(author, result.get(author) + entry.getValue());
                }
            }
        }

        return result;
    }

}
