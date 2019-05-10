package de.uhh.lt.xpertfinder.controller;

import com.google.gson.Gson;
import de.uhh.lt.xpertfinder.finder.*;
import de.uhh.lt.xpertfinder.methods.DefaultRequest;
import de.uhh.lt.xpertfinder.methods.ExpertFindingMethod;
import de.uhh.lt.xpertfinder.model.graph.Collaboration;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@SessionAttributes(value = {"expertTopic", "expertQuery"})
public class TableController extends SessionController {

    @Autowired
    ExpertRetrieval expertRetrieval;

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private MethodService methodService;

    @RequestMapping(value = "/table", method = RequestMethod.GET)
    public String table(@ModelAttribute("expertQuery") ExpertQuery expertQuery, @ModelAttribute("expertTopic") ExpertTopic lol, BindingResult errors, Model model) {

        if(expertQuery.getTopic() == null || expertQuery.getTopic().isEmpty()) {
            // nothing
        } else {
            List<List<ExpertResult>> results = evaluateExpertRetrieval(expertQuery);
            model.addAttribute("method1name", methodService.getExpertFindingMethodById(expertQuery.getMethod()[0]).getName());
            model.addAttribute("method2name", methodService.getExpertFindingMethodById(expertQuery.getMethod()[1]).getName());
            model.addAttribute("method3name", methodService.getExpertFindingMethodById(expertQuery.getMethod()[2]).getName());
            model.addAttribute("method4name", methodService.getExpertFindingMethodById(expertQuery.getMethod()[3]).getName());
            model.addAttribute("result", results);
        }

        model.addAttribute("expertfindingmethods", methodService.getAllExpertFindingMethods());
        return "table";
    }

    private List<List<ExpertResult>> evaluateExpertRetrieval(ExpertQuery eq) {
        List<List<ExpertResult>> results = new ArrayList<>();

        Gson gson = new Gson();

        for(int i = 0; i < 4; i++) {
            ExpertFindingMethod method = methodService.getExpertFindingMethodById(eq.getMethod()[i]);
            DefaultRequest defaultRequest = gson.fromJson(eq.getMethodParamMap().get(i).get(eq.getMethod()[i]), method.getRequestObject().getClass());

            ExpertTopic expertTopic = new ExpertTopic(elasticSearch, restService, aanDao);
            expertTopic.setup(eq.getTopic(), defaultRequest.getDocuments(), method.needsPublications(), method.needsCollaborations(), method.needsCitations(), eq.getOptions());

            if(!expertTopic.isInitialized()) {
                results.add(null);
                continue;
            }
            results.add(createExpertResult(expertRetrieval.findExperts(expertTopic, eq.getMethod()[i], defaultRequest), defaultRequest.getResults(), expertTopic.getGraph()));
        }

        return results;
    }

    private List<ExpertResult> createExpertResult(ExpertRetrievalResult expertRetrieval, int resultCount, Graph graph) {
        Map<String, Double> authorRelevanceMap = expertRetrieval.getExpertResultList();
        Map<String, Double> documentRelevanceMap = expertRetrieval.getDocumentResultList();
        // create author list of author <-> relevance map
        // and reduce list size to result Count
        List<String> authorList = authorRelevanceMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()).subList(0, Math.min(authorRelevanceMap.size(), resultCount));

        // create list of author ids
        List<Long> authorIds = authorList.stream().map(result -> graph.getAuthorId(result)).collect(Collectors.toList());

        // create maps of aan informations
        Map<String, Integer> globalhindex = graph.getHindexService().getGlobalHindex();
        Map<String, Integer> localhindex = graph.getHindexService().getLocalHindex();
        Map<String, Integer> citations = statisticService.getAuthorCitations();
        Map<String, Integer> publications = statisticService.getAuthorPublications();

        // create result
        List<ExpertResult> result = new ArrayList<>();
        for(String author : authorList) {
            ExpertResult er = new ExpertResult();
            er.setName(author);

            // set id
            Long id = graph.getAuthorId(author);
            er.setId(id);

            // set local and global hindex
            er.setLocalHindex(localhindex.getOrDefault(author, 0));
            er.setHindex(globalhindex.getOrDefault(author, 0));

            // calculate local and global collaborations
            int localCollaborations = 0;
            int globalColalborations = 0;
            if(graph.getAuthorAuthorNeighbors().containsKey(author)) {
                for(Collaboration coll : graph.getAuthorAuthorNeighbors().get(author)) {
                    localCollaborations += coll.getLocalCount();
                    globalColalborations += coll.getGlobalCount();
                }
            }
            er.setCollaborations(globalColalborations);
            er.setLocalcollaborations(localCollaborations);

            // calculate local and global publications
            er.setLocalpublications(graph.getAuthorDocumentNeighbors().containsKey(author) ? graph.getAuthorDocumentNeighbors().get(author).size() : 0);
            er.setPublications(publications.getOrDefault(author, -1));

            // calculate local and global citations
            int citcount = 0;
            if(graph.getAuthorDocumentNeighbors().containsKey(author)) {
                for(String document : graph.getAuthorDocumentNeighbors().get(author)) {
                    if(graph.getDocumentDocumentInNeighbors().containsKey(document)) {
                        citcount += graph.getDocumentDocumentInNeighbors().get(document).size();
                    }
                }
            }
            er.setLocalcitations(citcount);
            er.setCitations(citations.getOrDefault(author, -1));

            // set documents
            List<String> fileList =  graph.getAuthorDocumentNeighbors().get(author);
            List<Object[]> documentInformationList = aanDao.findDocumentInformationByIds(fileList);
            List<DocumentResult> documents = new ArrayList<>();
            for(Object[] obj : documentInformationList) {
                documents.add(new DocumentResult((String) obj[1], (String) obj[0], documentRelevanceMap != null ? documentRelevanceMap.getOrDefault((String) obj[0], 0d) : 0, (String) obj[3], (int) obj[2]));
            }
            // TODO: sort documents
            er.setDocuments(documents);

            // add to result list
            result.add(er);
        }

        return result;
    }
}