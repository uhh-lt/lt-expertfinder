package de.uhh.lt.xpertfinder.service;

import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.utils.StatisticUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

public class HindexService {

    private static Logger logger = LoggerFactory.getLogger(HindexService.class);

    private AanDao aanDao;
    private Graph graph;

    private int sumHindex;
    private Map<String, Integer> localHindex;
    private Map<String, Integer> globalHindex;

    public HindexService(AanDao aanDao, Graph graph) {
        this.aanDao = aanDao;
        this.graph = graph;
        logger.debug("Calculate h-index");
        this.localHindex = calculateLocalHindex();
        this.globalHindex = calculateGlobalHindex();
        this.sumHindex = calculateSumHindex();
    }

    private Map<String, Integer> calculateLocalHindex() {
        Map<String, Integer> hindex = new HashMap<>();

        if(!(graph.isCitation() && graph.isPublication()))
            return hindex;

        for(String author : graph.getAuthors()) {
            int score = 0;

            if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                List<String> writtenDocuments = graph.getAuthorDocumentNeighbors().get(author);
                int[] citationsCount = new int[writtenDocuments.size()];
                for(int i = 0; i < writtenDocuments.size(); i++) {
                    String document = writtenDocuments.get(i);
                    int citationCount = 0;
                    if(graph.getDocumentDocumentInNeighbors().containsKey(document)) {
                        citationCount = graph.getDocumentDocumentInNeighbors().get(document).size();
                    }
                    citationsCount[i] = citationCount;
                }

                score = StatisticUtils.hIndex(citationsCount);
            }
            hindex.put(author, score);
        }

        return hindex;
    }

    private Map<String, Integer> calculateGlobalHindex() {
        // get document citations per author
        Set<String> authors = new HashSet<>();
        List<Object[]> queryResult = aanDao.findAllDocumentCitations();

        Map<String, List<Integer>> documentCitationsPerAuthor = new HashMap<>();
        for(Object[] info : queryResult) {
            String author = (String) info[0];
            int documentCitation = ((BigInteger) info[2]).intValue();

            authors.add(author);

            if(!documentCitationsPerAuthor.containsKey(author)) {
                List<Integer> documentCitations = new ArrayList<>();
                documentCitations.add(documentCitation);
                documentCitationsPerAuthor.put(author, documentCitations);
            } else {
                List<Integer> documentCitations = documentCitationsPerAuthor.get(author);
                documentCitations.add(documentCitation);
            }
        }

        Map<String, Integer> hindex = new HashMap<>();
        for(String author : authors) {
            List<Integer> citationsCount = documentCitationsPerAuthor.get(author);
            int[] citations = new int[citationsCount.size()];
            for(int i = 0; i < citationsCount.size(); i++)
                citations[i] = citationsCount.get(i);

            hindex.put(author, StatisticUtils.hIndex(citations));
        }

        return hindex;
    }

    private int calculateSumHindex() {
        int sumHindex = 0;
        int sumLocalHindex = 0;
        int sumGlobalHindex = 0;

        for(String author : graph.getAuthors()) {
            if(localHindex.containsKey(author))
                sumLocalHindex += localHindex.get(author);

            if(globalHindex.containsKey(author))
                sumGlobalHindex += globalHindex.get(author);
        }
        sumHindex = sumLocalHindex + sumGlobalHindex;
        logger.debug("sumGlobal" + sumGlobalHindex + " sumLocal" + sumLocalHindex + " sumBoth" + sumHindex);
        return sumHindex;
    }

    public int getSumHindex() {
        return sumHindex;
    }

    public Map<String, Integer> getLocalHindex() {
        return localHindex;
    }

    public Map<String, Integer> getGlobalHindex() {
        return globalHindex;
    }
}
