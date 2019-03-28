package de.uhh.lt.xpertfinder.service;

import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.dao.StatisticDao;
import de.uhh.lt.xpertfinder.utils.StatisticUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.*;

@Service
public class StatisticService {

    @Autowired
    StatisticDao statisticDao;

    @Autowired
    AanDao aanDao;

    Map<String, Integer> authorPublications = new HashMap<>();
    Map<String, Integer> authorCitations = new HashMap<>();
    Map<String, Integer> globalHindex = new HashMap<>();

    @PostConstruct
    public void init() {
        calculateAuthorCitations();
        calculateAuthorPublications();
        calculateGlobalHindex();
    }

    private void calculateAuthorPublications() {
        List<Object[]> info = statisticDao.getPublicationCountPerAuthor();
        for(Object[] o : info) {
            String author = (String) o[0];
            int publications = ((BigInteger) o[1]).intValue();

            if(!authorPublications.containsKey(author))
                authorPublications.put(author, publications);
        }
    }

    private void calculateAuthorCitations() {
        List<Object[]> info = statisticDao.getCitationCountPerAuthor();
        for(Object[] o : info) {
            String author = (String) o[0];
            int citations = ((BigInteger) o[1]).intValue();

            if(!authorCitations.containsKey(author))
                authorCitations.put(author, citations);
        }
    }

    private void calculateGlobalHindex() {
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

        globalHindex = hindex;
    }

    public Map<String, Integer> getAuthorPublications() {
        return authorPublications;
    }

    public Map<String, Integer> getAuthorCitations() {
        return authorCitations;
    }

    public Map<String, Integer> getGlobalHindex() {
        return globalHindex;
    }
}
