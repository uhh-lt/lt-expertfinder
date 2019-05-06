package de.uhh.lt.xpertfinder.model.profiles.wikidata;

import java.math.BigInteger;

public class WikidataProfile {

    private int authorid;
    private String wikidataid;
    private String lables;
    private String awards;
    private String birthday;
    private String countires;
    private String educations;
    private String employers;
    private String fieldofworks;
    private String google;
    private String img;
    private String occupations;
    private String twitter;
    private String website;
    private int size;

    public WikidataProfile(Object[] wikidata) {
        this.authorid = ((BigInteger) wikidata[0]).intValue();
        this.wikidataid = (String) wikidata[1];
        this.lables = (String) wikidata[2];
        this.awards = (String) wikidata[3];
        this.birthday = (String) wikidata[4];
        this.countires = (String) wikidata[5];
        this.educations = (String) wikidata[6];
        this.employers = (String) wikidata[7];
        this.fieldofworks = (String) wikidata[8];
        this.google = (String) wikidata[9];
        this.img = (String) wikidata[10];
        this.occupations = (String) wikidata[11];
        this.twitter = (String) wikidata[12];
        this.website = (String) wikidata[13];

        size = 0;
        for(int i = 3; i <=14; i++) {
            if(i == 5 || i == 6 || i == 10 || i == 11 || i == 14)
                continue;

            size = size + (((String)wikidata[i]).isEmpty() ? 0 : wikidata[i].equals(" ") ? 0 : 1);
        }
    }

    public long getAuthorid() {
        return authorid;
    }

    public void setAuthorid(int authorid) {
        this.authorid = authorid;
    }

    public String getWikidataid() {
        return wikidataid;
    }

    public void setWikidataid(String wikidataid) {
        this.wikidataid = wikidataid;
    }

    public String getLables() {
        return lables;
    }

    public void setLables(String lables) {
        this.lables = lables;
    }

    public String getAwards() {
        return awards;
    }

    public void setAwards(String awards) {
        this.awards = awards;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCountires() {
        return countires;
    }

    public void setCountires(String countires) {
        this.countires = countires;
    }

    public String getEducations() {
        return educations;
    }

    public void setEducations(String educations) {
        this.educations = educations;
    }

    public String getEmployers() {
        return employers;
    }

    public void setEmployers(String employers) {
        this.employers = employers;
    }

    public String getFieldofworks() {
        return fieldofworks;
    }

    public void setFieldofworks(String fieldofworks) {
        this.fieldofworks = fieldofworks;
    }

    public String getGoogle() {
        return google;
    }

    public void setGoogle(String google) {
        this.google = google;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getOccupations() {
        return occupations;
    }

    public void setOccupations(String occupations) {
        this.occupations = occupations;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
