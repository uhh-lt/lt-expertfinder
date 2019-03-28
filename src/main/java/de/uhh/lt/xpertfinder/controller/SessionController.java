package de.uhh.lt.xpertfinder.controller;

import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.finder.ExpertQuery;
import de.uhh.lt.xpertfinder.service.ElasticSearchService;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import de.uhh.lt.xpertfinder.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes(value = {"expertTopic", "expertQuery"})
public class SessionController {

    @Autowired
    protected ElasticSearchService elasticSearch;

    @Autowired
    protected RestService restService;

    @Autowired
    protected AanDao aanDao;

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
        return expertQuery;
    }
}
