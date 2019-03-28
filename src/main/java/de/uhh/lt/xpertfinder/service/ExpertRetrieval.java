package de.uhh.lt.xpertfinder.service;

import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.finder.ExpertRetrievalResult;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.model.graph.Authorship;
import de.uhh.lt.xpertfinder.model.graph.Citation;
import de.uhh.lt.xpertfinder.model.graph.Collaboration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Service
public class ExpertRetrieval {

    private static Logger logger = LoggerFactory.getLogger(ExpertRetrieval.class);

    @Autowired
    private ElasticSearchService elasticSearch;

    @Autowired
    private StatisticService statisticService;

    @Autowired
    AanDao aanDao;

    // graph
    private Graph graph;

    // statistics
    private Map<String, Double> documentRelevance;
    private double sumDocumentRelevance;

    // hindex
    private Map<String, Integer>  hindex;
    private int sumHindex;
    private Map<String, Integer>  globalhindex;

    private Map<String, Double> sumDocumentScores(Map<String, Double> documentScoreMap) {
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

    // model 2 document relevance
    private double pj(String doc) {
        return documentRelevance.get(doc);

//        return documentRelevance.get(doc) / sumDocumentRelevance;
    }

    private double pca2d(String author, String doc) {
        return 1.0d / graph.getAuthorDocumentNeighbors().get(author).size();
    }

    // recency
    private double pd2d(String doc1, String doc2) {
        for (Citation citation : graph.getDocumentDocumentOutNeighbors().get(doc1)) {
            if(citation.getDocument().equals(doc2)) {
                return citation.getWeight();
            }
        }

        System.out.println("LOL WHY?!");
        return 0;
    }

    // written top docs / top docs
    private double pjca(String author) {
        return graph.getAuthorDocumentNeighbors().containsKey(author) ? (double) graph.getAuthorDocumentNeighbors().get(author).size() / (double) graph.getDocs().size() : 0.0d;

//        int h = hindex.getOrDefault(author, 0);
//        int gh = globalhindex.getOrDefault(author, 0);
//        return (double) (h + gh) / (double) sumHindex;
    }

    private double pd2ca(String document, String author) {
//        return 1.0d / graph.getDocumentAuthorNeighbors().get(document).size();

        for (Authorship authorship : graph.getDocumentAuthorNeighbors().get(document)) {
            if(authorship.getAuthor().equals(author)) {
                return authorship.getWeight();
            }
        }

        System.out.println("LOL WHY?!");
        return 0;

//        int allWrittenDocuments = 0;
//        int writtenDocuments = graph.getAuthorDocumentNeighbors().get(author).size();
//
//        for (String coauthor : graph.getDocumentAuthorNeighbors().get(document)) {
//            int docs = graph.getAuthorDocumentNeighbors().get(coauthor).size();
//            allWrittenDocuments = allWrittenDocuments + docs;
//        }
//
//        return (double) writtenDocuments / (double) allWrittenDocuments;
    }

    // local & global collaboration count
    private double pca2ca(String author1, String author2) {
        for (Collaboration collaboration : graph.getAuthorAuthorNeighbors2().get(author1)) {
            if(collaboration.getAuthor().equals(author2)) {
                return collaboration.getWeight();
            }
        }

        System.out.println("LOL WHY?!");
        return 0;
    }

    private ExpertFindingResult infiniteRandomWalkFullWeightedGraph(double lambda, double epsilon, double md, double mca) {
        int maxIterations = 500;

        // create variables
        logger.debug("Init infinite random walk full weighted graph");
        Map<String, Double>[] pd = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pd[i] = new HashMap<>();
        }
        Map<String, Double>[] pca = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pca[i] = new HashMap<>();
        }

        // init variables t = 0
        // iteration 0
        int i = 0;
        pd[0] = documentRelevance;
        for(String author : graph.getAuthors()) {
            pca[0].put(author, 0d);
        }

        // calculate random walk
        logger.debug("Calculate infinite random walk full weighted graph");
        do {
            i++;

            for(String doc : graph.getDocs()) {
                double score = Math.exp( Math.log(lambda) + Math.log(pj(doc)));
                double score2 = 0;
                double score3 = 0;

                if(graph.getDocumentAuthorNeighbors().containsKey(doc)) {
                    for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(doc)) {
                        score2 = score2 + Math.exp(
                                Math.log(pca2d(authorship.getAuthor(), doc))
                                + Math.log(pca[i-1].get(authorship.getAuthor()))
                        );
                    }
                }

                if(graph.getDocumentDocumentInNeighbors().containsKey(doc)) {
                    for(String document : graph.getDocumentDocumentInNeighbors().get(doc)) {
                        score3 = score3 + Math.exp(
                                Math.log(pd2d(document, doc))
                                + Math.log(pd[i-1].get(document))
                        );
                    }
                }

                //score + (1 - lambda) * ((1 - mu) * score2 + mu * score3)
                score = score + Math.exp(
                        Math.log(1 - lambda) + Math.log(
                                Math.exp(
                                        Math.log(1 - md) + Math.log(score2))
                                        + Math.exp(
                                        Math.log(md) + Math.log(score3)))
                );

                pd[i].put(doc, score);
            }

            for(String author : graph.getAuthors()) {
                double score = Math.exp(Math.log(lambda) + Math.log(pjca(author)));
                double score2 = 0;
                double score3 = 0;

                if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                    for(String document : graph.getAuthorDocumentNeighbors().get(author)) {
                        score2 = score2 + Math.exp(
                                        Math.log(pd2ca(document, author))
                                        + Math.log(pd[i-1].get(document))
                        );
                    }
                }

                if(graph.getAuthorAuthorNeighbors2().containsKey(author)) {
                    for(Collaboration coll : graph.getAuthorAuthorNeighbors2().get(author)) {
                        score3 = score3 + Math.exp(
                                        Math.log(pca2ca(coll.getAuthor(), author))
//                                        Math.log(coll.getWeight())
                                        + Math.log(pca[i-1].get(coll.getAuthor()))
                        );
                    }
                }

                score = score + Math.exp(
                        Math.log(1 - lambda) + Math.log(
                                Math.exp(
                                        Math.log(1 - mca) + Math.log(score2))
                                        + Math.exp(
                                        Math.log(mca) + Math.log(score3))
                        )
                );

                pca[i].put(author, score);
            }

            if(i == maxIterations - 1) {
                break;
            }

        } while(!checkConvergence(pca[i], pca[i-1], epsilon));
        System.out.println(i + " Iterations");

        return new ExpertFindingResult(pd[i], pca[i]);
    }

    private ExpertFindingResult infiniteRandomWalkFullGraph(double lambda, double epsilon, double md, double mca) {
        int maxIterations = 500;

        // create variables
        logger.debug("Init infinite random walk full graph");
        Map<String, Double>[] pd = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pd[i] = new HashMap<>();
        }
        Map<String, Double>[] pca = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pca[i] = new HashMap<>();
        }
        // init variables t = 0
        // iteration 0
        int i = 0;
        pd[0] = documentRelevance;
        for(String author : graph.getAuthors()) {
            pca[0].put(author, 0d);
        }

        // calculate random walk
        logger.debug("Calculate infinite random walk full graph");
        do {
            i++;

            for(String doc : graph.getDocs()) {
                double score = Math.exp( Math.log(lambda) + Math.log(documentRelevance.get(doc)));
                double score2 = 0;
                double score3 = 0;

                if(graph.getDocumentAuthorNeighbors().containsKey(doc)) {
                    for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(doc)) {
                        score2 = score2 + Math.exp(
                                Math.log(pca[i-1].get(authorship.getAuthor()))
                                + Math.log(1.0d / graph.getAuthorDocumentNeighbors().get(authorship.getAuthor()).size())
                        );
                    }
                }

                if(graph.getDocumentDocumentInNeighbors().containsKey(doc)) {
                    for(String document : graph.getDocumentDocumentInNeighbors().get(doc)) {
                        score3 = score3 + Math.exp(
                                Math.log(1.0d / graph.getDocumentDocumentOutNeighbors().get(document).size())
                                + Math.log(pd[i-1].get(document))
                        );
                    }
                }

                //score + (1 - lambda) * ((1 - mu) * score2 + mu * score3)
                score = score + Math.exp(
                            Math.log(1 - lambda) + Math.log(
                                    Math.exp(
                                            Math.log(1 - md) + Math.log(score2))
                                    + Math.exp(
                                            Math.log(md) + Math.log(score3)))
                        );

                pd[i].put(doc, score);
            }

            for(String author : graph.getAuthors()) {
                double score = Math.exp(Math.log(lambda) + Math.log( graph.getAuthorDocumentNeighbors().containsKey(author) ? (double) graph.getAuthorDocumentNeighbors().get(author).size() / (double) graph.getDocs().size() : 0.0d / (double) graph.getDocs().size()));
                double score2 = 0;
                double score3 = 0;

                if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                    for(String document : graph.getAuthorDocumentNeighbors().get(author)) {
                        score2 = score2 + Math.exp(
                                Math.log(pd[i-1].get(document))
                                + Math.log(1.0d / graph.getDocumentAuthorNeighbors().get(document).size())
                        );
                    }
                }

                if(graph.getAuthorAuthorNeighbors().containsKey(author)) {
                    for(String auth : graph.getAuthorAuthorNeighbors().get(author)) {
                        score3 = score3 + Math.exp(
                                Math.log(1.0d / graph.getAuthorAuthorNeighbors().get(auth).size())
                                + Math.log(pca[i-1].get(auth))
                        );
                    }
                }

                score = score + Math.exp(
                        Math.log(1 - lambda) + Math.log(
                                Math.exp(
                                        Math.log(1 - mca) + Math.log(score2))
                                + Math.exp(
                                        Math.log(mca) + Math.log(score3))
                        )
                );

                pca[i].put(author, score);
            }

            if(i == maxIterations - 1) {
                break;
            }

        } while(!checkConvergence(pca[i], pca[i-1], epsilon));
        System.out.println(i + " Iterations");

        return new ExpertFindingResult(pd[i], pca[i]);
    }

    private ExpertFindingResult infiniteRandomWalk(double lambda, double epsilon) {
        int maxIterations = 500;

        // create variables
        logger.debug("Init infinite random walk");
        Map<String, Double>[] pd = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pd[i] = new HashMap<>();
        }
        Map<String, Double>[] pca = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pca[i] = new HashMap<>();
        }
        // init variables t = 0
        pd[0] = documentRelevance;
        for(String author : graph.getAuthors()) {
            pca[0].put(author, 0d);
        }

        // calculate random walk
        logger.debug("Calculate infinite random walk");
        int i = 0;
        do {
            i++;

            for(String doc : graph.getDocs()) {
                double score = Math.exp( Math.log(lambda) + Math.log(documentRelevance.get(doc)));
                double score2 = 0;

                if(graph.getDocumentAuthorNeighbors().containsKey(doc)) {
                    for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(doc)) {
                        score2 = score2 + Math.exp(
                                Math.log(pca[i-1].get(authorship.getAuthor()))
                                        + Math.log(1.0d / graph.getAuthorDocumentNeighbors().get(authorship.getAuthor()).size()));
                    }

                    pd[i].put(doc, score + Math.exp(Math.log(1 - lambda) + Math.log(score2)));
                } else {
                    pd[i].put(doc, score);
                }
            }

            for(String author : graph.getAuthors()) {
                double score = Math.exp(Math.log(lambda) + Math.log( graph.getAuthorDocumentNeighbors().containsKey(author) ? (double) graph.getAuthorDocumentNeighbors().get(author).size() / (double) graph.getDocs().size() : 0.0d));
                double score2 = 0;

                if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                    for(String document : graph.getAuthorDocumentNeighbors().get(author)) {
                        score2 = score2 + Math.exp(
                                Math.log(pd[i-1].get(document))
                                        + Math.log(1.0d / graph.getDocumentAuthorNeighbors().get(document).size())
                        );
                    }
                    pca[i].put(author, score + Math.exp(Math.log(1 - lambda) + Math.log(score2)));
                } else {
                    pca[i].put(author, score);
                }
            }

            if(i == maxIterations - 1) {
                break;
            }

        } while(!checkConvergence(pca[i], pca[i-1], epsilon));
        System.out.println(i + " Iterations");

        return new ExpertFindingResult(pd[i], pca[i]);
    }

    private ExpertFindingResult kRandomWalk(int k) {
        logger.debug("Init " + k + "-step random walk");
        // create variables
        Map<String, Double>[] pd = new Map[k];
        for(int i = 0; i < pd.length ; i++) {
            pd[i] = new HashMap<>();
        }
        Map<String, Double>[] pca = new Map[k];
        for(int i = 0; i < pd.length ; i++) {
            pca[i] = new HashMap<>();
        }
        // init variables t = 0
        pd[0] = documentRelevance;
        for(String author : graph.getAuthors()) {
            pca[0].put(author, 0d);
        }

        // calculate random walk
        logger.debug("Calculate random walk");
        for(int i = 1; i < k; i++) {
            for(String doc : graph.getDocs()) {
                double score = Math.exp(Math.log(pd[i-1].get(doc)) + Math.log(documentRelevance.get(doc)));

                if(graph.getDocumentAuthorNeighbors().containsKey(doc)) {
                    for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(doc)) {
                        score = score + Math.exp(
                                Math.log(pca[i-1].get(authorship.getAuthor()))
                                        + Math.log(1.0d / graph.getAuthorDocumentNeighbors().get(authorship.getAuthor()).size()));
                    }
                }
                pd[i].put(doc, score);
            }

            for(String author : graph.getAuthors()) {
                double score = 0;

                if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                    for(String doc : graph.getAuthorDocumentNeighbors().get(author)) {
                        score = score + Math.exp(
                                Math.log(1 - documentRelevance.get(doc))
                                        + Math.log(1.0d / graph.getDocumentAuthorNeighbors().get(doc).size())
                                        + Math.log(pd[i-1].get(doc)));
                    }
                }
                pca[i].put(author, score);
            }
        }

        return new ExpertFindingResult(pd[k-1], pca[k-1]);
    }

    private ExpertFindingResult model2() {
        logger.debug("Calculate Model2");
        return new ExpertFindingResult(documentRelevance, sumDocumentScores(documentRelevance));
    }

    private ExpertFindingResult pageRank(double lambda, double epsilon) {
        int maxIterations = 500;

        // create variables
        logger.debug("Init page rank");

        Map<String, Double>[] pd = new Map[maxIterations];
        for(int i = 0; i < pd.length ; i++) {
            pd[i] = new HashMap<>();
        }
        Map<String, Double>[] pa = new Map[maxIterations];
        for(int i = 0; i < pa.length ; i++) {
            pa[i] = new HashMap<>();
        }

        Set<String> docs = graph.getDocs();
        Set<String> authors = graph.getAuthors();
        int n = docs.size() + authors.size();

        // iteration 0
        int i = 0;
        for(String doc : docs) {
            pd[i].put(doc, 1.0d / n);
        }
        for(String author : authors) {
            pa[i].put(author, 1.0d / n);
        }

        double d = lambda;

        do {
            i++;

            for(String doc : docs) {

                double score = (1 - d) / n;
                double sum1 = 0;
                double sum2 = 0;

                if(graph.getDocumentDocumentInNeighbors().containsKey(doc)) {
                    for(String inc_doc : graph.getDocumentDocumentInNeighbors().get(doc)) {
                        sum1 += pd[i-1].get(inc_doc) / graph.getOutDegDocument(inc_doc);
                    }
                }

                if(graph.getDocumentAuthorNeighbors().containsKey(doc)) {
                    for(Authorship authorship : graph.getDocumentAuthorNeighbors().get(doc)) {
                        String inc_author = authorship.getAuthor();
                        sum2 += pa[i-1].get(inc_author) / graph.getOutDegAuthor(inc_author);
                    }
                }

                pd[i].put(doc, score + d * (sum1 + sum2));
            }

            for(String author : authors) {

                double score = (1 - d) / n;
                double sum1 = 0;
                double sum2 = 0;

                if(graph.getAuthorAuthorNeighbors2().containsKey(author)) {
                    for(Collaboration collaboration : graph.getAuthorAuthorNeighbors2().get(author)) {
                        String inc_author = collaboration.getAuthor();
                        sum1 += pa[i-1].get(inc_author)  / graph.getOutDegAuthor(inc_author);
                    }
                }

                if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                    for(String inc_doc : graph.getAuthorDocumentNeighbors().get(author)) {
                        sum2 = pd[i-1].get(inc_doc) / graph.getOutDegDocument(inc_doc);
                    }
                }

                pa[i].put(author, score + d * (sum1 +sum2));
            }

            if(i == maxIterations - 1)
                break;

        } while(!checkConvergence(pa[i], pa[i-1], epsilon));

        System.out.println("Iterations: " + i);

        return new ExpertFindingResult(pd[i], pa[i]);
    }

    public ExpertRetrievalResult findExpertsSimple(ExpertTopic expertTopic, int method, int resultCount) {
        logger.debug("Start expert finding");
        long time = System.nanoTime();

        this.graph = expertTopic.getGraph();
        this.documentRelevance = expertTopic.getDocumentRelevance();
        this.sumDocumentRelevance = expertTopic.getSumDocumentRelevance();
        this.hindex = expertTopic.getHindex();
        this.globalhindex = expertTopic.getGlobalhindex();
        this.sumHindex = expertTopic.getSumHindex();

        if(graph == null) {
            logger.error("GRAPH IS NULL! - Please use setup() to set a topic");
            return null;
        }

        // choose method
        Map<String, Double> experts = null;
        switch (method) {
            case 7:
                logger.debug("Init experts by local hindex");
                Map<String, Double> expertScores = new HashMap<>();
                for(String author : graph.getAuthors()) {
                    expertScores.put(author, (double) hindex.getOrDefault(author, -1));
                }
                experts = expertScores;
                break;
            case 8:
                logger.debug("Init experts by global hindex");
                Map<String, Double> expertScores2 = new HashMap<>();
                for(String author : graph.getAuthors()) {
                    expertScores2.put(author, (double) globalhindex.getOrDefault(author, -1));
                }
                experts = expertScores2;
                break;
            case 9:
                logger.debug("Init experts by local citations");
                Map<String, Double> expertScores3 = new HashMap<>();
                for(String author : graph.getAuthors()) {
                    int citations = 0;
                    if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                        for(String document : graph.getAuthorDocumentNeighbors().get(author)) {

                            if(graph.getDocumentDocumentInNeighbors().containsKey(document)) {
                                citations += graph.getDocumentDocumentInNeighbors().get(document).size();
                            }
                        }
                    }
                    expertScores3.put(author, (double) citations);
                }
                experts = expertScores3;
                break;
            case 10:
                logger.debug("Init experts by global citations");
                Map<String, Integer> citations = statisticService.getAuthorCitations();
                Map<String, Double> expertScores4 = new HashMap<>();
                for(String author : graph.getAuthors()) {
                    expertScores4.put(author, (double) citations.getOrDefault(author, -1));
                }
                experts = expertScores4;
                break;
        }

        if(experts == null) {
            logger.error("METHOD DOESN'T EXIST! - Choose method 0 - 5");
            return null;
        }

        // get document citations
        List<Object[]> documentCitationList = aanDao.findCitationCountForDocuments(new ArrayList<>(graph.getDocs()));
        Map<String, Integer> documentCitationMap = new HashMap<>();
        for(Object[] obj : documentCitationList) {
            documentCitationMap.put((String) obj[0], ((BigInteger) obj[1]).intValue());
        }

        // create document weigths (citations)
        Map<String, Double> documentMap = new HashMap<>();
        for(String doc : graph.getDocs()) {
            documentMap.put(doc, (double) documentCitationMap.getOrDefault(doc, 0));
        }

        // sort results
        logger.debug("Sorting results");
        Map<String, Double> expertMap = sortByValue(experts);

        logger.debug("Finished after " + (System.nanoTime() - time) + " nanoseconds");
        return new ExpertRetrievalResult(expertMap, sortByValue(documentMap));
    }

    public ExpertRetrievalResult findExpertsElastic(ExpertTopic expertTopic, int topDocCount, int resultCount) {
        logger.debug("Start expert finding");
        long time = System.nanoTime();

        // extract expert topic infos
        this.graph = expertTopic.getGraph();
        this.documentRelevance = expertTopic.getDocumentRelevance();
        this.sumDocumentRelevance = expertTopic.getSumDocumentRelevance();
        this.hindex = expertTopic.getHindex();
        this.globalhindex = expertTopic.getGlobalhindex();
        this.sumHindex = expertTopic.getSumHindex();


        // get relevant documents
        logger.debug("Get relevant documents");
        ElasticSearchService.ScoredDocumentResult result = elasticSearch.getScoredDocumentsForTopic(expertTopic.getTopic());
        System.out.println("relevant documents: " + result.documents.size());

        Map<String, Double> experts = sumDocumentScores(result.scores);

        // sort results
        logger.debug("Sorting results");
        Map<String, Double> expertMap = sortByValue(experts);

        logger.debug("Finished after " + (System.nanoTime() - time) + " nanoseconds");
        return new ExpertRetrievalResult(expertMap, result.scores);
    }

    public ExpertRetrievalResult findExperts(ExpertTopic expertTopic, int method, int resultCount, int k, double lambda, double epsilon, double md, double mca) {
        logger.debug("Start expert finding");
        long time = System.nanoTime();

        this.graph = expertTopic.getGraph();
        this.documentRelevance = expertTopic.getDocumentRelevance();
        this.sumDocumentRelevance = expertTopic.getSumDocumentRelevance();
        this.hindex = expertTopic.getHindex();
        this.globalhindex = expertTopic.getGlobalhindex();
        this.sumHindex = expertTopic.getSumHindex();

        if(graph == null) {
            logger.error("GRAPH IS NULL! - Please use setup() to set a topic");
            return null;
        }

        switch (method) {
            case 0:
            case 1:
            case 3:
            case 5:
            case 6:
                if(!graph.isPublication()) {
                    logger.error("GRAPH IS NOT SUITABLE! - Missing publications");
                    return null;
                }
                break;
            case 2:
            case 4:
            case 7:
            case 8:
            case 9:
            case 10:
                if(!graph.isPublication() || !graph.isCitation() || !graph.isCollaboration()) {
                    logger.error("GRAPH IS NOT SUITABLE! - Missing publications");
                    return null;
                }
                break;
        }

        // choose method
        ExpertFindingResult experts = null;
        switch (method) {
            case 0:
                experts = kRandomWalk(k);
                break;
            case 1:
                experts = infiniteRandomWalk(lambda, epsilon);
                break;
            case 2:
                experts = infiniteRandomWalkFullGraph(lambda, epsilon, md, mca);
                break;
            case 3:
                experts = model2();
                break;
            case 4:
                experts = infiniteRandomWalkFullWeightedGraph(lambda, epsilon, md, mca);
                break;
            case 5:
                experts = pageRank(lambda, epsilon);
                break;
        }

        if(experts == null) {
            logger.error("METHOD DOESN'T EXIST! - Choose method 0 - 5");
            return null;
        }

        // sort results
        logger.debug("Sorting results");
        Map<String, Double> expertMap = sortByValue(experts.getAuthorRelevanceMap());

        Map<String, Double> documentMap = sortByValue(experts.getDocumentRelevanceMap());
        logger.debug("Finished after " + (System.nanoTime() - time) + " nanoseconds");
        return new ExpertRetrievalResult(expertMap, documentMap);
    }

    private static boolean checkConvergence(Map<String, Double> map1, Map<String, Double> map2, double epsilon) {
        return Math.abs(calculateNorm2(map1) - calculateNorm2(map2)) < epsilon;
    }

    private static double calculateNorm2(Map<String, Double> map) {
        double squareSum = 0;
        for(Map.Entry<String, Double> entry : map.entrySet()) {
            squareSum = squareSum + Math.exp(Math.log(entry.getValue()) + Math.log(entry.getValue()));
        }

        return Math.sqrt(squareSum);
    }

    private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private static <K, V> Map<K, V> sortByKey(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<K>) ((Map.Entry<K, V>) (o1)).getKey()).compareTo(((Map.Entry<K, V>) (o2)).getKey());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}