package de.uhh.lt.xpertfinder.controller;

import de.uhh.lt.xpertfinder.dao.AanDao;
import de.uhh.lt.xpertfinder.dao.GoogleDao;
import de.uhh.lt.xpertfinder.dao.KeywordDao;
import de.uhh.lt.xpertfinder.model.profiles.aan.Profile;
import de.uhh.lt.xpertfinder.model.profiles.wikidata.WikidataProfile;
import de.uhh.lt.xpertfinder.model.profiles.scholar.GoogleScholarAuthor;
import de.uhh.lt.xpertfinder.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    @Autowired
    KeywordDao keywordDao;

    @Autowired
    StatisticService statisticService;

    @Autowired
    GoogleDao googleDao;

    @Autowired
    AanDao aanDao;

    @GetMapping("/profile/{id}")
    public String ui1(Model model, @PathVariable Long id) {

        // get author name
        String name = aanDao.findAuthorNameById(id);

        addProfilesToModel(model,name,id);
        return "profile";
    }

    private void addProfilesToModel(Model model, String name, Long id) {
        // get publications
        List<Object[]> publications = aanDao.findPublicationsByAuthorId(id);

        // get publication citations
        List<String> files = publications.stream().map(o -> (String) o[0]).collect(Collectors.toList());
        List<Object[]> documentCitationList = aanDao.findCitationCountForDocuments(files);
        Map<String, Integer> documentCitationMap = new HashMap<>();
        for(Object[] obj : documentCitationList) {
            documentCitationMap.put((String) obj[0], ((BigInteger) obj[1]).intValue());
        }

        // get collaborations
        List<Object[]> collaborations = aanDao.findCollaborationsForAuthorByName(name);

        // get statistics
        int hindex = statisticService.getGlobalHindex().getOrDefault(name, -1);
        int publicationcount = statisticService.getAuthorPublications().getOrDefault(name, -1);
        int citationcount = statisticService.getAuthorCitations().getOrDefault(name, -1);
        List<Object[]> citationsPerYear = aanDao.findCitationsPerYearForAuthorId(id);

        // get keywords
        List<Object[]> keywords = keywordDao.findKeywordsForAuthor(name);

        // create aan aanprofile
        Profile profile = new Profile(name, publications, documentCitationMap, collaborations, hindex, publicationcount, citationcount, keywords, citationsPerYear);

        // get google author
        GoogleScholarAuthor author = googleDao.findOneByAuthorId(id);

        // get wikidata aanprofile
        List<WikidataProfile> wiki = new ArrayList<>();
        for(Object[] wikiprofile : aanDao.findWikidataProfileForAuthorId(id)) {
            wiki.add(new WikidataProfile(wikiprofile));
        }

        model.addAttribute("aanprofile", profile);
        model.addAttribute("googleprofile", author);
        model.addAttribute("wikiprofile", wiki);
    }

}
