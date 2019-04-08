package de.uhh.lt.xpertfinder.controller;

import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.finder.ExpertQuery;
import de.uhh.lt.xpertfinder.methods.ExpertFindingMethod;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import de.uhh.lt.xpertfinder.service.MethodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
@SessionAttributes(value = {"expertTopic", "expertQuery"})
public class SearchController extends SessionController {

    private static Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    AanDao aanDao;

    @Autowired
    MethodService methodService;

    @PostMapping("/postExpertQuery")
    public RedirectView postExpertQuery(@RequestParam("redirectTo") String redirectTo, @ModelAttribute("expertQuery") ExpertQuery expertQuery, @ModelAttribute("expertTopic") ExpertTopic expertTopic, RedirectAttributes attributes) {

        // handle the special case, that the topic exactly matches one author name
        List<Long> ids = aanDao.checkExactAuthorMatch(expertTopic.getTopic());
        if(ids != null && ids.size() == 1) {
            return new RedirectView("/aanprofile/"+ids.get(0), true);
        }

        if(!redirectTo.equals("table"))
            expertTopic = createExpertTopic(expertQuery, expertTopic);

        attributes.addFlashAttribute("expertTopic", expertTopic);
        attributes.addFlashAttribute("expertQuery", expertQuery);

        return new RedirectView(redirectTo);
    }

    @GetMapping("/searchTopic/{topic}")
    public ModelAndView searchTopic(@PathVariable("topic") String topic, @ModelAttribute("expertQuery") ExpertQuery expertQuery, @ModelAttribute("expertTopic") ExpertTopic expertTopic, RedirectAttributes attributes) {
        expertTopic.setTopic(topic);

        expertTopic = createExpertTopic(expertQuery, expertTopic);

        attributes.addFlashAttribute("expertTopic", expertTopic);
        attributes.addFlashAttribute("expertQuery", expertQuery);

        return new ModelAndView("redirect:/ui");
    }

    private ExpertTopic createExpertTopic(ExpertQuery expertQuery, ExpertTopic expertTopic) {
        logger.debug("Start creating TOPIC");
        long time = System.nanoTime();

        ExpertFindingMethod method = methodService.getExpertFindingMethodById(expertQuery.getMethod());
        if(method != null) {
            expertTopic = new ExpertTopic(elasticSearch, restService, aanDao);
            expertTopic.setup(expertQuery.getTopic(), expertQuery.getTopDocCount(), method.needsPublications(), method.needsCollaborations(), method.needsCitations(), expertQuery.getOptions());
        } else {
            switch (Integer.parseInt(expertQuery.getMethod())) {
                case 2:
                case 4:
                case 5:
                case 7:
                case 8:
                case 9:
                case 10:
                    expertTopic = new ExpertTopic(elasticSearch, restService, aanDao);
                    expertTopic.setup(expertQuery.getTopic(), expertQuery.getTopDocCount(), true, true, true, expertQuery.getOptions());
                    break;
                case 0:
                case 1:
                case 3:
                case 6:
                    expertTopic = new ExpertTopic(elasticSearch, restService, aanDao);
                    expertTopic.setup(expertQuery.getTopic(), expertQuery.getTopDocCount(), true, false, false, expertQuery.getOptions());
                    break;
            }
        }
        logger.debug("Finished creating TOPIC after " + (System.nanoTime() - time) + " nanoseconds");
        return expertTopic;
    }
}
