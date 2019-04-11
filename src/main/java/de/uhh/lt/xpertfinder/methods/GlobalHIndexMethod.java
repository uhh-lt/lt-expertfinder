package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.finder.ExpertTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GlobalHIndexMethod implements ExpertFindingMethod<DefaultRequest> {

    private static Logger logger = LoggerFactory.getLogger(GlobalHIndexMethod.class);

    @Override
    public String getId() {
        return "globalhindex";
    }

    @Override
    public String getName() {
        return "Global H-Index";
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
    public DefaultRequest getRequestObject() {
        return new DefaultRequest(1000, 25);
    }

    @Override
    public ExpertFindingResult findExperts(DefaultRequest request, ExpertTopic expertTopic) {
        Graph graph = expertTopic.getGraph();
        Map<String, Integer> globalhindex = expertTopic.getGlobalhindex();

        Map<String, Double> authorRelevanceMap = new HashMap<>();
        Map<String, Double> documentRelevanceMap = new HashMap<>();

        logger.debug("Rank experts by global hindex");
        for(String author : graph.getAuthors()) {
            authorRelevanceMap.put(author, (double) globalhindex.getOrDefault(author, -1));
        }

        return new ExpertFindingResult(documentRelevanceMap, authorRelevanceMap);
    }
}
