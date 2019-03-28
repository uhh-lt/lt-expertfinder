package de.uhh.lt.xpertfinder.model.profiles.aan;

public class Publication {

    private String file;
    private String title;
    private int year;
    private String venue;
    private int citations;

    public Publication(Object[] publication, int citations) {
        this.file = (String) publication[0];
        this.title = (String) publication[1];
        this.year = (int) publication[2];
        this.venue = (String) publication[3];
        this.citations = citations;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public int getCitations() {
        return citations;
    }

    public void setCitations(int citations) {
        this.citations = citations;
    }
}
