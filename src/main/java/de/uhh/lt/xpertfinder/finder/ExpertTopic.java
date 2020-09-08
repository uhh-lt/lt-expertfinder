package de.uhh.lt.xpertfinder.finder;

import com.google.gson.Gson;
import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.model.graph.GraphOptions;
import de.uhh.lt.xpertfinder.service.ElasticSearchService;
import de.uhh.lt.xpertfinder.service.HindexService;
import de.uhh.lt.xpertfinder.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpertTopic {

    private ElasticSearchService elasticSearch;
    private RestService restService;
    private AanDao aanDao;

    private static Logger logger = LoggerFactory.getLogger(ExpertTopic.class);

    private String topic;

    // graph
    private Graph graph;

    // statistics
    private CorpusStatistic corpusStatistic;
    private Map<String,int[]> documentStatistics;
    private Map<String, Double> documentRelevance;
    private double sumDocumentRelevance;

    private int relevantDocuments; // not important!

    // hindex
    private Map<String, Integer>  hindex;
    private Map<String, Integer>  globalhindex;
    private int sumHindex; // both: hindex + globalHindex

    private boolean initialized = false;
    private boolean foundResult = true;

    public ExpertTopic(ElasticSearchService elasticSearch, RestService restService, AanDao aanDao) {
        this.elasticSearch = elasticSearch;
        this.restService = restService;
        this.aanDao = aanDao;
    }

    public void setup(String topic, int yearFrom, int yearTo, boolean includeTitle, int count, boolean publication, boolean collaboration, boolean citation, GraphOptions options) {
        if(initialized) {
            logger.error("ALREADY INITIALIZED");
            return;
        }

        initialized = true;

        logger.debug("Setting up expert topic");

        // extract terms from search topic
        this.topic = topic.toLowerCase();
        String[] terms = this.topic.replaceAll("\\+", "").replaceAll("\\s+", " ").trim().split(" ");

        // get related documents and statistics
        logger.debug("Get relevant documents");
        ElasticSearchService.MyResult result = elasticSearch.getDocumentIdsByTopicAAN(topic);

        if(result.documents.isEmpty() && result.info.isEmpty()) {
            logger.error("NO DOCUMENTS");
            foundResult = false;
            return;
        }
        foundResult = true;

        // Filter by year, title, venue
        List<Object[]> documentInformation = aanDao.findDocumentInformationByIds(result.documents);
        Map<String, String> documentTitleMap = new HashMap<>();
        Map<String, Integer> documentYearMap = new HashMap<>();
        Map<String, String> documentVenueMap = new HashMap<>();
        for(Object[] docInfo : documentInformation) {
            documentTitleMap.put((String) docInfo[0], (String) docInfo[1]);
            documentYearMap.put((String) docInfo[0], (int) docInfo[2]);
            documentVenueMap.put((String) docInfo[0], (String) docInfo[3]);
        }

        System.out.println("BEFORE YEAR FILTER: " + result.documents.size());
        result.documents = result.documents.stream().filter(s -> documentYearMap.getOrDefault(s, 0) <= yearTo).collect(Collectors.toList());
        result.documents = result.documents.stream().filter(s -> documentYearMap.getOrDefault(s, 0) >= yearFrom).collect(Collectors.toList());
        System.out.println("AFTER YEAR FILTER: " + result.documents.size());


        if(includeTitle) {
            System.out.println("BEFORE TITLE FILTER: " + result.documents.size());
            result.documents = result.documents.stream().filter(s -> {
                String title = documentTitleMap.getOrDefault(s, "");
                return containsAllWords(title.toLowerCase(), terms);
            }).collect(Collectors.toList());
            System.out.println("AFTER TITLE FILTER: " + result.documents.size());
        }

        if(result.documents.isEmpty()) {
            logger.error("NO DOCUMENTS");
            initialized = false;
            return;
        }

        System.out.println(result.documents.size());
        relevantDocuments = result.documents.size();
        List<String> topDocs = result.documents.size() > count ? result.documents.subList(0, count) : result.documents;

        // get document statistics
        documentStatistics = result.info;

        // get corpus statistics
        corpusStatistic = getCorpusStatistic(result.documents.get(0), terms);

        // create graph
        graph = new Graph(aanDao, topDocs, publication, citation, collaboration, options);

        // calculate document relevance
        calculateDocumentRelevance(terms);
        calculateSumDocRelevance();

        // get hindex
        HindexService hindexService = graph.getHindexService();
        hindex = hindexService.getLocalHindex();
        globalhindex = hindexService.getGlobalHindex();
        sumHindex = hindexService.getSumHindex();
    }

    private void calculateDocumentRelevance(String[] terms) {
        documentRelevance = new HashMap<>();

        logger.debug("Calculate document relevance scores");
        for(String doc : graph.getDocs()) {
            if(doc == null)
                continue;

            // per document - get document statistics: length and term frequencies
            int[] docInfo = documentStatistics.get(doc);
            int docLen = docInfo[terms.length];

            // calculate parameter lambda
            double lambda = Math.log(corpusStatistic.getAvgDocLength()) - Math.log(corpusStatistic.getAvgDocLength() + docLen);

            // per term - calculate probabilities
            double pq_dca = 0;
            int i = 0;
            for(String term : terms) {
                int tf = docInfo[i];
                double pt = corpusStatistic.getPt(term);
                double pt_d = Math.log(tf) - Math.log(docLen);
                double dModel = Math.exp(Math.log(1 - Math.exp(lambda)) + pt_d) + Math.exp(lambda + pt);
                pq_dca = pq_dca + Math.log(dModel);
                i++;
            }
            documentRelevance.put(doc, Math.exp(pq_dca));
        }
    }

    private void calculateSumDocRelevance() {
        double sum = 0;

        for(Map.Entry<String, Double> entry : documentRelevance.entrySet()) {
            sum += entry.getValue();
        }

        sumDocumentRelevance = sum;
    }

    private CorpusStatistic getCorpusStatistic(String esId, String[] terms) {
        logger.debug("Get corpus statistics");

        String result = restService.sendPostRequest("http://"+elasticSearch.elastichostname+":"+elasticSearch.elasticport+"/aan/_doc/"+esId+"/_termvectors", "{ \"fields\": [ \"text\" ], \"term_statistics\": true, \"field_statistics\": true }");
        if(result == null)
            return null;

        Map<String,Object> text = ((Map<String,Object>) ((Map<String,Object>) new Gson().fromJson(result, Map.class).get("term_vectors")).get("text"));
        Map<String,Object> fieldStatistics = (Map<String,Object>) text.get("field_statistics");
        Map<String,Object> termStatistics = (Map<String,Object>) text.get("terms");

        double docCount = (Double) fieldStatistics.get("doc_count");
        double sumTtf = (Double) fieldStatistics.get("sum_ttf");
        double avgDocLength = sumTtf / docCount;

        Map<String,Integer> totalTermFrequencies = new HashMap<>();
        Map<String,Double> pts = new HashMap<>();
        for(String term : terms) {
            Map<String,Object> terminfo = (Map<String,Object>) termStatistics.get(term);
            if(terminfo == null) {
                System.out.println("Term doesn't exist in this document");
                return null;
            }

            int ttf = ((Double)  terminfo.get("ttf")).intValue();
            double pt = Math.log(ttf) - Math.log(sumTtf);

            totalTermFrequencies.put(term, ((Double)  terminfo.get("ttf")).intValue());
            pts.put(term, pt);
        }

        return new CorpusStatistic(docCount, sumTtf, avgDocLength, totalTermFrequencies, pts);
    }

    public Graph getGraph() {
        return graph;
    }

    public Map<String, Double> getDocumentRelevance() {
        return documentRelevance;
    }

    public double getSumDocumentRelevance() {
        return sumDocumentRelevance;
    }

    public Map<String, Integer> getHindex() {
        return hindex;
    }

    public Map<String, Integer> getGlobalhindex() {
        return globalhindex;
    }

    public int getSumHindex() {
        return sumHindex;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public int getRelevantDocuments() {
        return relevantDocuments;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isFoundResult() {
        return foundResult;
    }

    public void setFoundResult(boolean foundResult) {
        this.foundResult = foundResult;
    }

    public void setInitialized(boolean initialize) { this.initialized = initialize; }

    private static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
    }
}
