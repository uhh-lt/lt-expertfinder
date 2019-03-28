package de.uhh.lt.xpertfinder.controller;

import de.uhh.lt.xpertfinder.finder.DocumentResult;
import de.uhh.lt.xpertfinder.finder.ExpertQuery;
import de.uhh.lt.xpertfinder.finder.ExpertResult;
import de.uhh.lt.xpertfinder.finder.ExpertRetrievalResult;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.model.graph.Collaboration;
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

    @RequestMapping(value = "/table", method = RequestMethod.GET)
    public String table(@ModelAttribute("expertQuery") ExpertQuery expertQuery, @ModelAttribute("expertTopic") ExpertTopic lol, BindingResult errors, Model model) {

        if(expertQuery.getTopic() == null) {
            // nothing
        } else {
            List<List<ExpertResult>> results = evaluateExpertRetrieval(expertQuery);
            if(results != null) {
                tableToLatex(results);
                tableToCSV(results);
            }
            model.addAttribute("result", results);
        }

        return "table";
    }

    private List<List<ExpertResult>> evaluateExpertRetrieval(ExpertQuery eq) {
        List<List<ExpertResult>> results = new ArrayList<>();

        ExpertTopic expertTopic = new ExpertTopic(elasticSearch, restService, aanDao);
        expertTopic.setup(eq.getTopic(), eq.getTopDocCount(), true, true, true, eq.getOptions());

        if(!expertTopic.isInitialized()) {
            return null;
        }

        // results.add(expertRetrieval.findExpertsSimple(expertTopic, 7, eq.getResultCount()));                                                                     // local hindex
        // results.add(expertRetrieval.findExpertsSimple(expertTopic, 8, eq.getResultCount()));                                                                     // global hindex
        // results.add(expertRetrieval.findExpertsSimple(expertTopic, 9, eq.getResultCount()));                                                                     // local citations
        results.add(createExpertResult(expertRetrieval.findExpertsSimple(expertTopic, 10, eq.getResultCount()), eq.getResultCount(), expertTopic.getGraph()));                                                                     // global citations

        // results.add(expertRetrieval.findExpertsElastic(expertTopic, eq.getTopDocCount(), eq.getResultCount()));                                                          // elastic

        results.add(createExpertResult(expertRetrieval.findExperts(expertTopic, 3, eq.getResultCount(), eq.getK(), eq.getLambda(), eq.getEpsilon(), eq.getMd(), eq.getMca()), eq.getResultCount(), expertTopic.getGraph()));      // model2
        // results.add(expertRetrieval.findExperts(expertTopic, 0, eq.getResultCount(), eq.getK(), eq.getLambda(), eq.getEpsilon(), eq.getMd(), eq.getMca()));      // k-step
        // results.add(expertRetrieval.findExperts(expertTopic, 1, eq.getResultCount(), eq.getK(), eq.getLambda(), eq.getEpsilon(), eq.getMd(), eq.getMca()));      // infinite
        results.add(createExpertResult(expertRetrieval.findExperts(expertTopic, 2, eq.getResultCount(), eq.getK(), eq.getLambda(), eq.getEpsilon(), eq.getMd(), eq.getMca()), eq.getResultCount(), expertTopic.getGraph()));       // infinite2
        results.add(createExpertResult(expertRetrieval.findExperts(expertTopic, 4, eq.getResultCount(), eq.getK(), eq.getLambda(), eq.getEpsilon(), eq.getMd(), eq.getMca()), eq.getResultCount(), expertTopic.getGraph()));       // infinite3
        // results.add(expertRetrieval.findExperts(expertTopic, 5, eq.getResultCount(), eq.getK(), eq.getLambda(), eq.getEpsilon(), eq.getMd(), eq.getMca()));      // pagerank

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
            if(graph.getAuthorAuthorNeighbors2().containsKey(author)) {
                for(Collaboration coll : graph.getAuthorAuthorNeighbors2().get(author)) {
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

    private void tableToLatex(List<List<ExpertResult>> results) {

        for(int i = 0; i < results.get(0).size(); i++) {
            StringBuilder builder = new StringBuilder();
            builder.append((i+1) + " & ");

            int j = 0;
            for(List<ExpertResult> list : results) {
                builder.append(list.get(i).getName());

                if(j == results.size() - 1) {
                    builder.append(" \\\\");
                } else {
                    builder.append(" & ");
                }
                j++;
            }
            System.out.println(builder.toString());
        }
    }

    private void tableToCSV(List<List<ExpertResult>> results) {

        for(int i = 0; i < results.get(0).size(); i++) {
            StringBuilder builder = new StringBuilder();
            builder.append((i+1) + ";");

            int j = 0;
            for(List<ExpertResult> list : results) {
                builder.append(list.get(i).getName());

                if(j == results.size() - 1) {
                    //builder.append(";");
                } else {
                    builder.append(";");
                }
                j++;
            }
            System.out.println(builder.toString());
        }
    }
}