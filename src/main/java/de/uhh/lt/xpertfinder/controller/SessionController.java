package de.uhh.lt.xpertfinder.controller;

import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.finder.ExpertQuery;
import de.uhh.lt.xpertfinder.service.ElasticSearchService;
import de.uhh.lt.xpertfinder.finder.ExpertTopic;
import de.uhh.lt.xpertfinder.service.MethodService;
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

    @Autowired
    MethodService methodService;

    @ModelAttribute("expertTopic")
    public ExpertTopic expertTopic() {
        return new ExpertTopic(elasticSearch, restService, aanDao);
    }

    @ModelAttribute("expertQuery")
    public ExpertQuery expertQuery() {
        String topic = "";
        String method[] = {"inifiniterandomfull", "model2", "globalcitations", "globalhindex"};
        return new ExpertQuery(topic, method, methodService);
    }
}
