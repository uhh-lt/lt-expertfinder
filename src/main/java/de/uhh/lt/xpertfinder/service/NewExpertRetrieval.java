package de.uhh.lt.xpertfinder.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uhh.lt.xpertfinder.finder.ExpertFindingResult;
import de.uhh.lt.xpertfinder.finder.ExpertRetrievalResult;
import de.uhh.lt.xpertfinder.methods.ExpertFindingMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NewExpertRetrieval {

    @Autowired
    MethodService methodService;

    private static Logger logger = LoggerFactory.getLogger(NewExpertRetrieval.class);

    public ExpertRetrievalResult findExperts(ExpertTopic expertTopic, String method, String params) {
        logger.debug("Start expert finding");
        long time = System.nanoTime();

        ExpertFindingMethod expertFindingMethod = methodService.getExpertFindingMethodById(method);
        if(expertFindingMethod == null) {
            logger.debug("No ExpertFindingMethod registered for ID " + method);
            return null;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(params);
        ExpertFindingResult experts = expertFindingMethod.findExperts(gson.fromJson(params, expertFindingMethod.getRequestObject().getClass()), expertTopic);
        if(experts == null) {
            logger.error("For some reason, the expert finding method " + expertFindingMethod.getName() + "was not able to find experts...");
            return null;
        }

        // sort results
        logger.debug("Sorting results");
        Map<String, Double> expertMap = sortByValue(experts.getAuthorRelevanceMap());

        Map<String, Double> documentMap = sortByValue(experts.getDocumentRelevanceMap());
        logger.debug("Finished after " + (System.nanoTime() - time) + " nanoseconds");
        return new ExpertRetrievalResult(expertMap, documentMap);
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

}
