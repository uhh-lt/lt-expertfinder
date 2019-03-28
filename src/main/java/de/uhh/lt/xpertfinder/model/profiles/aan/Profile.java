package de.uhh.lt.xpertfinder.model.profiles.aan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Profile {

    private String name;
    private List<Publication> publications;
    private List<AuthorCollaboration> collaborations;
    private List<Object[]> keywords;
    private List<Object[]> citationsPerYear;
    private int hindex;
    private int publicationcount;
    private int citationcount;

    public Profile(String name, List<Object[]> publications, Map<String, Integer> documentCitationMap, List<Object[]> collaborations, int hindex, int publicationcount, int citationcount, List<Object[]> keywords, List<Object[]> citationsPerYear) {
        initName(name);
        initPublications(publications, documentCitationMap);
        initCollaborations(collaborations);
        initKeywords(keywords);
        this.hindex = hindex;
        this.publicationcount = publicationcount;
        this.citationcount = citationcount;
        this.citationsPerYear = citationsPerYear;
        orderPublications();
    }

    private void orderPublications() {
        Collections.sort(publications, (o1, o2) -> o2.getCitations() - o1.getCitations());
    }

    private void initPublications(List<Object[]> publications, Map<String, Integer> documentCitationMap) {
        this.publications = new ArrayList<>();
        for(Object[] publication : publications) {
            this.publications.add(new Publication(publication, documentCitationMap.getOrDefault(publication[0], 0)));
        }
    }

    private void initCollaborations(List<Object[]> collaborations) {
        this.collaborations = new ArrayList<>();
        for(Object[] collaboration : collaborations) {
            this.collaborations.add(new AuthorCollaboration(collaboration));
        }
    }

    private void initKeywords(List<Object[]> keywords) {
        this.keywords = new ArrayList<>();
        int style = 5;
        boolean decrease = true;
        for(Object[] keyword : keywords) {
            String word = (String) keyword[0];
            Object[] mykeyword = new Object[2];
            mykeyword[0] = word;
            if(style == 0) {
                mykeyword[1] = "w3-theme";
            } else if (decrease) {
                mykeyword[1] = "w3-theme-d"+style;
            } else if (!decrease) {
                mykeyword[1] = "w3-theme-l"+style;
            }
            this.keywords.add(mykeyword);

            style = decrease ? style - 1 : style + 1;
            if(style == 0)
                decrease = false;
        }
    }

    private void initName(String name) {
//        this.name = StringUtils.capitalizeWords(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public List<AuthorCollaboration> getCollaborations() {
        return collaborations;
    }

    public void setCollaborations(List<AuthorCollaboration> collaborations) {
        this.collaborations = collaborations;
    }

    public int getHindex() {
        return hindex;
    }

    public void setHindex(int hindex) {
        this.hindex = hindex;
    }

    public int getPublicationcount() {
        return publicationcount;
    }

    public void setPublicationcount(int publicationcount) {
        this.publicationcount = publicationcount;
    }

    public int getCitationcount() {
        return citationcount;
    }

    public void setCitationcount(int citationcount) {
        this.citationcount = citationcount;
    }

    public List<Object[]> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Object[]> keywords) {
        this.keywords = keywords;
    }

    public List<Object[]> getCitationsPerYear() {
        return citationsPerYear;
    }

    public void setCitationsPerYear(List<Object[]> citationsPerYear) {
        this.citationsPerYear = citationsPerYear;
    }
}
