package de.uhh.lt.xpertfinder.service;

import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Component
public class ElasticSearchService {

    private static Logger logger = LoggerFactory.getLogger(ElasticSearchService.class);

    private RestHighLevelClient client;

    public String elastichostname;
    public int elasticport;

    public ElasticSearchService(@Value("${elastichostname}") String elastichostname, @Value("${elasticport}") int elasticport) {
        this.elastichostname = elastichostname;
        this.elasticport = elasticport;
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(elastichostname, elasticport, "http")));
    }

    public class MyResult {

        public List<String> documents;
        public Map<String, int[]> info; // mapping für document --> document info, document info beinhaltet von 0 - topics.length die term frequencies und bei topics.length + 1 die länge des dokuments

        public MyResult(List<String> documents, Map<String, int[]> info) {
            this.documents = documents;
            this.info = info;
        }
    }

    // model graph aan
    public MyResult getDocumentIdsByTopicAAN(String topic) {
        String[] topics = topic.split(" |-");

        StringBuilder newTopic = new StringBuilder();
        for(String t : topics) {
            newTopic.append(t + " ");
        }
        topic = newTopic.toString().trim();

        System.out.println("TOPIC SIZE: " + topics.length);

        List<String> result = new ArrayList<>();
        Map<String,int[]> info = new HashMap<>();
        Gson gson = new Gson();

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));

        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("text", topic);
        matchQueryBuilder.operator(Operator.AND);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(matchQueryBuilder);
        boolQueryBuilder.filter(QueryBuilders.matchPhraseQuery("text", topic));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(100);

        SearchRequest searchRequest = new SearchRequest("aan");
        searchRequest.scroll(scroll);
        searchRequest.source(searchSourceBuilder);
        searchSourceBuilder.explain(true);
        searchSourceBuilder.fetchSource(false);

        String scrollId = "";
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();

            while (searchHits != null && searchHits.length > 0) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = client.searchScroll(scrollRequest);
                scrollId = searchResponse.getScrollId();

                // process search hits
                for(SearchHit hit : searchHits) {

                    Explanation e = hit.getExplanation();
                    int[] data = new int[topics.length+1];

                    if(topics.length == 1) {
                        data[topics.length] = (int) e.getDetails()[0].getDetails()[1].getDetails()[4].getValue();
                    } else {
                        data[topics.length] = (int) e.getDetails()[0].getDetails()[0].getDetails()[0].getDetails()[1].getDetails()[4].getValue();
                    }

                    for(int i = 0; i < topics.length; i++) {
                        if(i >= e.getDetails().length)
                            data[i] = 0;
                        else {
                            if(topics.length == 1) {
                                data[i] = (int) e.getDetails()[i].getDetails()[1].getDetails()[0].getValue();
                            } else {
                                data[i] = (int) e.getDetails()[0].getDetails()[i].getDetails()[0].getDetails()[1].getDetails()[0].getValue();
                            }
                        }
                    }

                    info.put(hit.getId(), data);
                    result.add(hit.getId());
                }

                searchHits = searchResponse.getHits().getHits();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            try {
                client.clearScroll(clearScrollRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new MyResult(result, info);
    }

    public class ScoredDocumentResult {
        public List<String> documents;
        public Map<String, Double> scores;

        public ScoredDocumentResult(List<String> documents, Map<String, Double> scores) {
            this.documents = documents;
            this.scores = scores;
        }
    }

    // model elastic
    public ScoredDocumentResult getScoredDocumentsForTopic(String topic) {
        String[] topics = topic.split(" |-");

        StringBuilder newTopic = new StringBuilder();
        for(String t : topics) {
            newTopic.append(t + " ");
        }
        topic = newTopic.toString().trim();

        List<String> result = new ArrayList<>();
        Map<String, Double> scores = new HashMap<>();

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchPhraseQuery("text", topic));
        searchSourceBuilder.size(50);

        SearchRequest searchRequest = new SearchRequest("aan");
        searchRequest.scroll(scroll);
        searchRequest.source(searchSourceBuilder);
        searchSourceBuilder.explain(true);
        searchSourceBuilder.fetchSource(false);

        String scrollId = "";
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();

            while (searchHits != null && searchHits.length > 0) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = client.searchScroll(scrollRequest);
                scrollId = searchResponse.getScrollId();

                // process search hits
                for(SearchHit hit : searchHits) {
                    result.add(hit.getId());
                    scores.put(hit.getId(), (double) hit.getScore());
                }

                searchHits = searchResponse.getHits().getHits();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            try {
                client.clearScroll(clearScrollRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new ScoredDocumentResult(result, scores);
    }

    /**
     * @param id AAN ID of the document
     * @return document abstract, can return null!
     */
    public String findDocumentAbstractById(String id) {

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        boolQueryBuilder.filter(QueryBuilders.termQuery("file.keyword", id));

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        String[] includeFields = new String[] {"intro"};
        String[] excludeFields = new String[] {};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        SearchRequest searchRequest = new SearchRequest("newpaper");
        searchRequest.types("_doc");
        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = client.search(searchRequest);
            for (SearchHit hit : searchResponse.getHits()) {
//            // do something with the SearchHit
//            String index = hit.getIndex();
//            String type = hit.getType();
//            String id2 = hit.getId();
//            float score = hit.getScore();

                return (String) hit.getSourceAsMap().get("intro");
            }
        } catch (IOException e) {
            logger.error("Connection Error - Something is wrong with Elasticsearch");
            e.printStackTrace();
        }

        logger.error("NewPaper Document with id " + id + " doesn't exist!");
        return null;
    }

    /**
     * @param id AAN ID of the document
     * @return document text, can return null!
     */
    public String findDocumentTextById(String id) {
        // init get request
        GetRequest getRequest = new GetRequest(
                "aan",
                "_doc",
                id);

        // manage includes and excludes
//        String[] includes = new String[]{"text"};
//        String[] excludes = Strings.EMPTY_ARRAY;
//        FetchSourceContext fetchSourceContext =
//                new FetchSourceContext(true, includes, excludes);
//        getRequest.fetchSourceContext(fetchSourceContext);

        // perform the get request
        try {
            GetResponse getResponse = client.get(getRequest);

            String index = getResponse.getIndex();
            String type = getResponse.getType();
            String id2 = getResponse.getId();

            if (getResponse.isExists()) {
                long version = getResponse.getVersion();
                String sourceAsString = getResponse.getSourceAsString();
                Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                byte[] sourceAsBytes = getResponse.getSourceAsBytes();

                if(sourceAsMap != null) {
                    if(sourceAsMap.get("text") != null)
                        return sourceAsMap.get("text").toString();
                }
                logger.error("Field text is not available in document " + id);
                return null;
            } else {
                logger.error("Problem with the GetRequest to get AAN Document with id " + id);
            }

        } catch (IOException e) {
            logger.error("Connection Error - Something is wrong with Elasticsearch");
            e.printStackTrace();
        }

        logger.error("AAN Document with id " + id + " doesn't exist!");
        return null;
    }

    public long getHitsMatchPhraseQuery(String phrase) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchPhraseQuery("text", phrase));

        SearchRequest searchRequest = new SearchRequest("aan");
        searchRequest.source(searchSourceBuilder);
        searchSourceBuilder.fetchSource(false);

        try {
            SearchResponse searchResponse = client.search(searchRequest);
            return searchResponse.getHits().totalHits;
        } catch (IOException e) {
            logger.error("Connection Error - Something is wrong with Elasticsearch");
            e.printStackTrace();
        }

        return 0;
    }

    public RestHighLevelClient getClient() {
        return client;
    }
}
