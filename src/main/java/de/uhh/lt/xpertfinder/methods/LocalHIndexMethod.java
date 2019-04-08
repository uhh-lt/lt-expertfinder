package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LocalHIndexMethod implements ExpertFindingMethod {

    private static Logger logger = LoggerFactory.getLogger(LocalHIndexMethod.class);

    @Override
    public String getId() {
        return "localhindex";
    }

    @Override
    public String getName() {
        return "Local H-Index";
    }

    @Override
    public boolean needsCollaborations() {
        return true;
    }

    @Override
    public boolean needsCitations() {
        return true;
    }

    @Override
    public boolean needsPublications() {
        return true;
    }

    @Override
    public ExpertFindingResult findExperts(int k, double lambda, double epsilon, double md, double mca, ExpertTopic expertTopic) {
        Graph graph = expertTopic.getGraph();
        Map<String, Integer> hindex = expertTopic.getHindex();

        Map<String, Double> authorRelevanceMap = new HashMap<>();
        Map<String, Double> documentRelevanceMap = new HashMap<>();

        logger.debug("Init experts by local hindex");
        for(String author : graph.getAuthors()) {
            authorRelevanceMap.put(author, (double) hindex.getOrDefault(author, -1));
        }

        return new ExpertFindingResult(documentRelevanceMap, authorRelevanceMap);
    }
}
