package de.uhh.lt.xpertfinder.service;

import org.apache.http.HttpHost;
import org.apache.lucene.search.Explanation;
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
    public String elasticindex;

    public ElasticSearchService(@Value("${elastichostname}") String elastichostname, @Value("${elasticport}") int elasticport, @Value("${elasticindex}") String elasticindex) {
        this.elastichostname = elastichostname;
        this.elasticport = elasticport;
        this.elasticindex = elasticindex;
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
        // extract phrases & terms from search topic
        topic = topic.toLowerCase();
        topic = topic.replaceAll("\\s+", " ");
        String[] phrases = topic.split("\\+");
        for(int i = 0; i < phrases.length; i++) {
            phrases[i] = phrases[i].trim();
        }
        String[] topics = topic.replaceAll("\\+", "").replaceAll("\\s+", " ").trim().split(" ");
        StringBuilder newTopic = new StringBuilder();
        for(String t : topics) {
            newTopic.append(t + " ");
        }
        topic = newTopic.toString().trim();

        System.out.println("TOPIC SIZE: " + topics.length);

        List<String> result = new ArrayList<>();
        Map<String,int[]> info = new HashMap<>();

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("text", topic).operator(Operator.AND));
        for(String phrase : phrases) {
            boolQuery.filter(QueryBuilders.matchPhraseQuery("text", phrase));
        }

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery);
        searchSourceBuilder.size(100);

        SearchRequest searchRequest = new SearchRequest(elasticindex);
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
        String[] topics = topic.split(" ");

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

        SearchRequest searchRequest = new SearchRequest(elasticindex);
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
}
