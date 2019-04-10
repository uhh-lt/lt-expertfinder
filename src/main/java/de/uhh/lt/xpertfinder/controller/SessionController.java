package de.uhh.lt.xpertfinder.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.finder.ExpertQuery;
import de.uhh.lt.xpertfinder.methods.ExpertFindingMethod;
import de.uhh.lt.xpertfinder.service.ElasticSearchService;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import de.uhh.lt.xpertfinder.service.MethodService;
import de.uhh.lt.xpertfinder.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@SessionAttributes(value = {"expertTopic", "expertQuery"})
public class SessionController {

    @Autowired
    protected ElasticSearchService elasticSearch;

    @Autowired
    protected RestService restService;

    @Autowired
    protected AanDao aanDao;

    @Autowired
    MethodService methodService;

    @ModelAttribute("expertTopic")
    public ExpertTopic expertTopic() {
        return new ExpertTopic(elasticSearch, restService, aanDao);
    }

    @ModelAttribute("expertQuery")
    public ExpertQuery expertQuery() {
        String topic = "";
        int topDocCount = 1000;
        int resultCount = 25;
        int k = 10;
        double lambda = 0.1;
        double epsilon = 0.00000001;
        double md = 0.5;
        double mca = 0.25;
        String method = "4";

        ExpertQuery expertQuery = new ExpertQuery(topic, topDocCount, resultCount, k, lambda, epsilon, md, mca);
        expertQuery.setMethod(method);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, String> methodParamMap = new HashMap<>();
        Map<String, Integer> methodParamLengthMap = new HashMap<>();
        for(ExpertFindingMethod efmethod : methodService.getAllExpertFindingMethods()) {
            String params = gson.toJson(efmethod.getRequestObject());
            int length = countLines(params);
            methodParamMap.put(efmethod.getId(), params);
            methodParamLengthMap.put(efmethod.getId(), length);
        }
        expertQuery.setMethodParamMap(methodParamMap);
        expertQuery.setMethodParamLengthMap(methodParamLengthMap);

        return expertQuery;
    }

    private static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }
}
