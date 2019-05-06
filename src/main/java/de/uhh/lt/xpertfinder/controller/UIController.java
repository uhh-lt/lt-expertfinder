package de.uhh.lt.xpertfinder.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uhh.lt.xpertfinder.dao.GoogleDao;
import de.uhh.lt.xpertfinder.dao.KeywordDao;
import de.uhh.lt.xpertfinder.finder.*;
import de.uhh.lt.xpertfinder.methods.DefaultRequest;
import de.uhh.lt.xpertfinder.methods.ExpertFindingMethod;
import de.uhh.lt.xpertfinder.model.graph.Graph;
import de.uhh.lt.xpertfinder.model.profiles.aan.Author;
import de.uhh.lt.xpertfinder.model.profiles.scholar.GoogleScholarAuthor;
import de.uhh.lt.xpertfinder.model.graph.Collaboration;
import de.uhh.lt.xpertfinder.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@SessionAttributes(value = {"expertTopic", "expertQuery"})
public class UIController extends SessionController {

    private static Logger logger = LoggerFactory.getLogger(UIController.class);

    @Autowired
    private ExpertRetrieval expertRetrieval;

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private KeywordDao keywordDao;

    @Autowired
    private GoogleDao googleDao;

    @Autowired
    private MethodService methodService;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @GetMapping("/ui")
    public String ui(@ModelAttribute("expertQuery") ExpertQuery expertQuery, @ModelAttribute("expertTopic") ExpertTopic expertTopic, BindingResult errors, Model model) {
        if(expertTopic.isInitialized()) {
            showUi(expertTopic, expertQuery, model);
        } else {
            System.out.println("not initialized!");
        }

        model.addAttribute("expertfindingmethods", methodService.getAllExpertFindingMethods());
        return "ui";
    }

    @GetMapping("/ui/reset")
    public ModelAndView resetUI(@ModelAttribute("expertQuery") ExpertQuery expertQuery, @ModelAttribute("expertTopic") ExpertTopic expertTopic, RedirectAttributes attributes) {
        expertQuery = expertQuery();
        expertTopic = expertTopic();

        attributes.addFlashAttribute("expertTopic", expertTopic);
        attributes.addFlashAttribute("expertQuery", expertQuery);

        return new ModelAndView("redirect:/ui");
    }

    private void showUi(ExpertTopic expertTopic, ExpertQuery expertQuery, Model model) {
        logger.debug("Start showing UI");
        long time = System.nanoTime();

        // find the experts with the selected method
        ExpertRetrievalResult expertRetrievalResult;
        ExpertFindingMethod method = methodService.getExpertFindingMethodById(expertQuery.getMethod()[0]);
        if(method == null) {
            logger.debug("ERROR: Method does not exist!");
            return;
        }
        DefaultRequest defaultRequest = gson.fromJson(expertQuery.getMethodParamMap().get(0).get(expertQuery.getMethod()[0]), method.getRequestObject().getClass());
        expertRetrievalResult = expertRetrieval.findExperts(expertTopic, expertQuery.getMethod()[0], defaultRequest);

        Graph graph = expertTopic.getGraph();

        // print some statistics
        System.out.println("Authors:" + graph.getAuthors().size());
        System.out.println("Documents:" + graph.getDocs().size());
        System.out.println("Expert Results:" + expertRetrievalResult.getExpertResultList().size());
        System.out.println("Document Results:" + expertRetrievalResult.getDocumentResultList().size());

        // feed results with additional information
        List<DocumentResult> documentResults = createDocumentResult(expertRetrievalResult.getDocumentResultList(), defaultRequest.getResults());
        List<ExpertResult> expertResults = createExpertResult(expertRetrievalResult.getExpertResultList(), expertRetrievalResult.getDocumentResultList(), defaultRequest.getResults(), graph);

        // add experts and documents to the view
        model.addAttribute("result", expertResults);
        model.addAttribute("documentResult", documentResults);

        // add statistics to the view
        ExpertStatistics statistics = new ExpertStatistics(expertTopic.getRelevantDocuments(), graph.getDocs().size(), graph.getAuthors().size(), graph.getNumAuthDoc(), graph.getNumDocDoc(), graph.getNumAuthAuth());
        model.addAttribute("statistics", statistics);

        logger.debug("Finished showing UI after " + (System.nanoTime() - time) + " nanoseconds");
    }

    private List<ExpertResult> createExpertResult(Map<String, Double> authorRelevanceMap, Map<String, Double> documentRelevanceMap, int resultCount, Graph graph) {
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

        // create map of author id <-> google scholar aanprofile
        List<GoogleScholarAuthor> googleList = googleDao.findAllByAuthorIdIn(authorIds);
        Map<Long, GoogleScholarAuthor> googleMap = new HashMap<>();
        for(GoogleScholarAuthor a : googleList) {
            googleMap.put(a.getAuthorId(), a);
        }

        // create map of author name <-> keyword list
        Map<String, List<String>> authorKeywordsMap = new HashMap<>();
        List<Object[]> authorKeywords = keywordDao.findKeywordsForAuthors(authorList);
        for(Object[] authorKeyword : authorKeywords) {
            String author = (String) authorKeyword[0];
            String word = (String) authorKeyword[1];
            List<String> keywords = authorKeywordsMap.getOrDefault(author, new ArrayList<>());
            keywords.add(word);
            authorKeywordsMap.put(author, keywords);
        }

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
                documents.add(new DocumentResult((String) obj[1], (String) obj[0], documentRelevanceMap.getOrDefault((String) obj[0], 0d), (String) obj[3], (int) obj[2]));
            }
//            Collections.sort(documents, (o1, o2) -> o2.getCitations() - o1.getCitations());
            er.setDocuments(documents);

            // set google aanprofile information: image + description
            GoogleScholarAuthor googleProfile = googleMap.get(id);
            er.setDescription(googleProfile != null ? googleProfile.getDescription() : " ");
            if(googleProfile != null && googleProfile.getImg() != null && !googleProfile.getImg().isEmpty()) {
                er.setImage(googleProfile.getImg());
            } else {
                er.setImage("avatar_scholar_128.png");
            }

            // set keywords
            List<String> keywords =  authorKeywordsMap.getOrDefault(author, new ArrayList<>());
            er.setKeywords(keywords.subList(0, Math.min(keywords.size(), 10)));

            // add to result list
            result.add(er);
        }

        return result;
    }

    private List<DocumentResult> createDocumentResult(Map<String, Double> documentRelevanceMap, int resultCount) {
        List<DocumentResult> result = new ArrayList<>();

        // create file list of file <-> relevance map
        // and reduce list size to result Count
        List<String> fileList = documentRelevanceMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()).subList(0, Math.min(documentRelevanceMap.size(), resultCount));

        if(fileList.size() == 0) {
            return result;
        }

        // get document information
        List<Object[]> documentInformationList = aanDao.findDocumentInformationByIds(fileList);
        Map<String, DocumentResult> documentInformationMap = new HashMap<>();
        for(Object[] obj : documentInformationList) {
            documentInformationMap.put((String) obj[0], new DocumentResult((String) obj[1], (String) obj[0], -1d, (String) obj[3], (int) obj[2]));
        }

        // get document citations
        List<Object[]> documentCitationList = aanDao.findCitationCountForDocuments(fileList);
        Map<String, Integer> documentCitationMap = new HashMap<>();
        for(Object[] obj : documentCitationList) {
            documentCitationMap.put((String) obj[0], ((BigInteger) obj[1]).intValue());
        }

        // get document authors
        List<Object[]> documentAuthorList = aanDao.findAuthorsForDocuments(fileList);
        Map<String, List<Author>> documentAuthorMap = new HashMap<>();
        for(Object[] obj : documentAuthorList) {
            String file = (String) obj[0];
            String author = (String) obj[1];
            long authorId = ((BigInteger) obj[2]).longValue();
            List<Author> authors = documentAuthorMap.getOrDefault(file, new ArrayList<>());
            authors.add(new Author(author, authorId));
            documentAuthorMap.put(file, authors);
        }

        // get document keywords
        // TODO: FIX DUPLICATE KEYWORDS; USE SET !!
        List<Object[]> documentKeywordList = keywordDao.findKeywordsForDocuments(fileList);
        Map<String, List<String>> documentKeywordMap = new HashMap<>();
        for(Object[] obj : documentKeywordList) {
            List<String> keywords = documentKeywordMap.getOrDefault(obj[2], new ArrayList<>());
            keywords.add((String) obj[0]);
            documentKeywordMap.put((String) obj[2], keywords);
        }

        // create result
        for(String file : fileList) {
            DocumentResult documentResult = documentInformationMap.getOrDefault(file, new DocumentResult());
            documentResult.setCitations(documentCitationMap.getOrDefault(file, 0));
            documentResult.setAuthors(documentAuthorMap.getOrDefault(file, null));
            documentResult.setRelevance(documentRelevanceMap.getOrDefault(file, 0d));
            List<String> keywords = documentKeywordMap.getOrDefault(file, new ArrayList<>());
            documentResult.setKeywords(keywords.subList(0, Math.min(10, keywords.size())));
            result.add(documentResult);
        }

        Collections.sort(result, (o1, o2) -> o2.getCitations() - o1.getCitations());
        return result;
    }
}