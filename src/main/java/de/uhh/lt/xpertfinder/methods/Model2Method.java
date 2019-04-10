package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.model.graph.Authorship;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Model2Method implements ExpertFindingMethod<DefaultRequest> {
    @Override
    public String getId() {
        return "model2";
    }

    @Override
    public String getName() {
        return "Model2";
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
    public DefaultRequest getRequestObject() {
        return new DefaultRequest();
    }

    @Override
    public ExpertFindingResult findExperts(DefaultRequest request, ExpertTopic expertTopic) {
        Map<String, Double> authorRelevanceMap;
        Map<String, Double> documentRelevanceMap;

        documentRelevanceMap = expertTopic.getDocumentRelevance();
        authorRelevanceMap = sumDocumentScores(expertTopic.getGraph(), expertTopic.getDocumentRelevance());

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
