package de.uhh.lt.xpertfinder.controller;

import com.google.gson.Gson;
import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.finder.ExpertQuery;
import de.uhh.lt.xpertfinder.methods.DefaultRequest;
import de.uhh.lt.xpertfinder.methods.ExpertFindingMethod;
import de.uhh.lt.xpertfinder.finder.ExpertTopic;
import de.uhh.lt.xpertfinder.methods.InfiniteRandomWeightedMethod;
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

    private Gson gson = new Gson();

    @PostMapping("/postExpertQuery")
    public RedirectView postExpertQuery(@RequestParam("redirectTo") String redirectTo, @ModelAttribute("expertQuery") ExpertQuery expertQuery, @ModelAttribute("expertTopic") ExpertTopic expertTopic, RedirectAttributes attributes) {

        // handle the special case, that the topic exactly matches one author name
        List<Long> ids = aanDao.checkExactAuthorMatch(expertTopic.getTopic());
        if(ids != null && ids.size() == 1) {
            return new RedirectView("/profile/"+ids.get(0), true);
        }

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

        ExpertFindingMethod method = methodService.getExpertFindingMethodById(expertQuery.getMethod()[0]);
        if(method != null) {
            expertTopic = new ExpertTopic(elasticSearch, restService, aanDao);
            // TODO: THIS IS NOT NICE; FIX IT!
            if(method instanceof InfiniteRandomWeightedMethod) {
                InfiniteRandomWeightedMethod.InfiniteRandomWeightedRequest request = gson.fromJson(expertQuery.getMethodParamMap().get(0).get(expertQuery.getMethod()[0]), InfiniteRandomWeightedMethod.InfiniteRandomWeightedRequest.class);
                expertTopic.setup(expertQuery.getTopic(), request.getDocuments(), method.needsPublications(), method.needsCollaborations(), method.needsCitations(), request.getOptions());
            } else {
                DefaultRequest request = gson.fromJson(expertQuery.getMethodParamMap().get(0).get(expertQuery.getMethod()[0]), DefaultRequest.class);
                expertTopic.setup(expertQuery.getTopic(), request.getDocuments(), method.needsPublications(), method.needsCollaborations(), method.needsCitations(), expertQuery.getOptions());
            }
            logger.debug("Finished creating TOPIC after " + (System.nanoTime() - time) + " nanoseconds");
        } else {
            logger.debug("FAILED creating TOPIC: Method is unknown!");
        }

        return expertTopic;
    }
}
