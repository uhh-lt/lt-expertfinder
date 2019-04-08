package de.uhh.lt.xpertfinder.methods;

import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import de.uhh.lt.xpertfinder.service.StatisticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GlobalCitationsMethod implements ExpertFindingMethod {

    private static Logger logger = LoggerFactory.getLogger(GlobalCitationsMethod.class);

    @Autowired
    AanDao aanDao;

    @Autowired
    StatisticService statisticService;

    @Override
    public String getId() {
        return "globalcitations";
    }

    @Override
    public String getName() {
        return "Global Citations";
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

        Map<String, Double> authorRelevanceMap = new HashMap<>();
        Map<String, Double> documentRelevanceMap = new HashMap<>();

        logger.debug("Find experts by global citations");

        // Associate each expert from the graph with her global citation count
        // This is the expert ranking
        Map<String, Integer> citations = statisticService.getAuthorCitations();
        for(String author : graph.getAuthors()) {
            authorRelevanceMap.put(author, (double) citations.getOrDefault(author, -1));
        }

        // get document citations
        // this is needed to rank the documents
        List<Object[]> documentCitationList = aanDao.findCitationCountForDocuments(new ArrayList<>(graph.getDocs()));
        Map<String, Integer> documentCitationMap = new HashMap<>();
        for(Object[] obj : documentCitationList) {
            documentCitationMap.put((String) obj[0], ((BigInteger) obj[1]).intValue());
        }

        // Associate each document from the graph with its global citation count
        // This is the document ranking
        for(String doc : graph.getDocs()) {
            documentRelevanceMap.put(doc, (double) documentCitationMap.getOrDefault(doc, 0));
        }

        return new ExpertFindingResult(documentRelevanceMap, authorRelevanceMap);
    }
}
