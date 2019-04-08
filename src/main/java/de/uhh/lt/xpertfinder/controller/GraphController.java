package de.uhh.lt.xpertfinder.controller;

import de.uhh.lt.xpertfinder.finder.DocumentResult;
import de.uhh.lt.xpertfinder.finder.ExpertQuery;
import de.uhh.lt.xpertfinder.finder.ExpertRetrievalResult;
import de.uhh.lt.xpertfinder.methods.ExpertFindingMethod;
import de.uhh.lt.xpertfinder.model.graph.Function2;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.model.graph.Collaboration;
import de.uhh.lt.xpertfinder.service.ExpertRetrieval;
import de.uhh.lt.xpertfinder.service.ExpertTopic;
import de.uhh.lt.xpertfinder.service.MethodService;
import de.uhh.lt.xpertfinder.service.NewExpertRetrieval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.thymeleaf.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@SessionAttributes(value = {"expertTopic", "expertQuery"})
public class GraphController extends SessionController {

    private static Logger logger = LoggerFactory.getLogger(GraphController.class);

    @Autowired
    private ExpertRetrieval expertRetrieval;

    @Autowired
    private NewExpertRetrieval newExpertRetrieval;

    @Autowired
    private MethodService methodService;

    @GetMapping(value = "/graph")
    public String testcontroller2(@ModelAttribute("expertQuery") ExpertQuery expertQuery, @ModelAttribute("expertTopic") ExpertTopic expertTopic, BindingResult errors, Model model) {

        if(expertTopic.isInitialized()) {
            showGraph(expertTopic, expertQuery, model);
        } else {
            System.out.println("not initialized!");
        }

        model.addAttribute("expertfindingmethods", methodService.getAllExpertFindingMethods());
        return "graph";
    }

    private void showGraph(ExpertTopic expertTopic, ExpertQuery expertQuery, Model model) {
        Graph graph = expertTopic.getGraph();
        if(graph.getDocs() == null || (graph.getDocs() != null && graph.getDocs().isEmpty())) {
            return;
        }

        logger.debug("Start building graph");
        long time = System.nanoTime();

        // find the experts with the selected method
        ExpertRetrievalResult expertRetrievalResult;
        ExpertFindingMethod method = methodService.getExpertFindingMethodById(expertQuery.getMethod());
        if(method != null) {
            expertRetrievalResult = newExpertRetrieval.findExperts(expertTopic, expertQuery.getMethod(), expertQuery.getK(),expertQuery.getLambda(), expertQuery.getEpsilon(), expertQuery.getMd(), expertQuery.getMca());
        } else if(Integer.parseInt(expertQuery.getMethod()) < 6) {
            expertRetrievalResult = expertRetrieval.findExperts(expertTopic, Integer.parseInt(expertQuery.getMethod()), expertQuery.getResultCount(), expertQuery.getK(),expertQuery.getLambda(), expertQuery.getEpsilon(), expertQuery.getMd(), expertQuery.getMca());
        } else if (Integer.parseInt(expertQuery.getMethod()) == 6) {
            expertRetrievalResult = expertRetrieval.findExpertsElastic(expertTopic, Integer.parseInt(expertQuery.getMethod()), expertQuery.getResultCount());
        } else {
            expertRetrievalResult = expertRetrieval.findExpertsSimple(expertTopic, Integer.parseInt(expertQuery.getMethod()), expertQuery.getResultCount());
        }

        Map<String, Double> expertRelevanceMap = expertRetrievalResult.getExpertResultList();
        Map<String, Double> documentRelevanceMap = expertRetrievalResult.getDocumentResultList();

        OptionalDouble mer = expertRelevanceMap.entrySet().stream().mapToDouble(Map.Entry::getValue).max();
        OptionalDouble mdr = documentRelevanceMap.entrySet().stream().mapToDouble(Map.Entry::getValue).max();
        double maxExpertRelevance = mer.getAsDouble();
        double maxDocumentRelevance = mdr.getAsDouble();

        List<String> fileList = documentRelevanceMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        // get document information
        List<Object[]> documentInformationList = aanDao.findDocumentInformationByIds(fileList);
        Map<String, DocumentResult> documentInformationMap = new HashMap<>();
        for(Object[] obj : documentInformationList) {
            documentInformationMap.put((String) obj[0], new DocumentResult((String) obj[1], (String) obj[0], -1d, (String) obj[3], (int) obj[2]));
        }

        Map<String, Integer>  hindex = expertTopic.getHindex();
        Map<String, Integer>  globalhindex = expertTopic.getGlobalhindex();

        Map<String, Double> documentRelevance = new HashMap<>();
        // if publications are enabled, the probabilities depend on the model2's document relevance
        if(graph.isPublication()) {
            documentRelevance.putAll(expertTopic.getDocumentRelevance());
        }

        // probabilities depend on the global collaboration count an author has with another author
        class CollaborationResult {
            public int collaborations;
            public double probability;
        }

        Function2<String, String, CollaborationResult> calcCollaborationSize = new Function2<String, String, CollaborationResult>() {
            @Override
            public CollaborationResult apply(String author1, String author2) {
                int collaborationSum = 0;
                int collaborations = 0;


                for(Collaboration coll: graph.getAuthorAuthorNeighbors2().get(author1)) {
                    collaborationSum = collaborationSum + coll.getGlobalCount();

                    if(coll.getAuthor().equals(author2))
                        collaborations = coll.getGlobalCount();
                }

                CollaborationResult result = new CollaborationResult();
                result.collaborations = collaborations;
                result.probability = (double) collaborations / (double) collaborationSum;

                return result;
            }
        };

        // draw graph
        model.addAttribute("graph", graph.visualizeGraph(
                /*Aut Desc */ s -> StringUtils.capitalizeWords(s),
                /*Doc Desc */ s -> {
                    if(documentInformationMap.containsKey(s)) {
                        return documentInformationMap.get(s).getTitle();
                    } else {
                        return "no title available!";
                    }
                },
                /*Pub Desc */ (author, doc) -> "pub",
                /*Cit Desc */ (doc1, doc2) -> "cit",
                /*Col Desc */ (author1, author2) -> "col",
                /*Aut Desc */ (doc, author) -> "aut",
                /*Aut Size */ author -> 5.0 + 10d * (expertRelevanceMap.getOrDefault(author, 0d) / maxExpertRelevance),
                /*Doc Size */ document -> 5.0 + 10d * (documentRelevanceMap.getOrDefault(document, 0d) / maxDocumentRelevance),
                /*Pub Size */ (author, doc) -> 2.0,
                /*Cit Size */ (doc1, doc2) -> 2.0,
                /*Col Size */ (author1, author2) -> 2.0,
                /*Aut Size */ (doc, author) -> 2.0));


        logger.debug("Finished building graph: " + (System.nanoTime() - time));
    }

}
