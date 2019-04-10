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
public class LocalCitationsMethod implements ExpertFindingMethod<DefaultRequest> {

    private static Logger logger = LoggerFactory.getLogger(LocalCitationsMethod.class);

    @Override
    public String getId() {
        return "localcitations";
    }

    @Override
    public String getName() {
        return "Local Citations";
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
        return new DefaultRequest();
    }

    @Override
    public ExpertFindingResult findExperts(DefaultRequest request, ExpertTopic expertTopic) {
        Graph graph = expertTopic.getGraph();

        Map<String, Double> authorRelevanceMap = new HashMap<>();
        Map<String, Double> documentRelevanceMap = new HashMap<>();

        logger.debug("Init experts by local citations");
        for(String author : graph.getAuthors()) {
            int citations = 0;
            if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                for(String document : graph.getAuthorDocumentNeighbors().get(author)) {

                    if(graph.getDocumentDocumentInNeighbors().containsKey(document)) {
                        citations += graph.getDocumentDocumentInNeighbors().get(document).size();
                    }
                }
            }
            authorRelevanceMap.put(author, (double) citations);
        }

        return new ExpertFindingResult(documentRelevanceMap, authorRelevanceMap);
    }
}
