package de.uhh.lt.xpertfinder.model.profiles.scholar;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table(name = "google_papers")
public class GoogleScholarPaper {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="title",nullable = false)
    private String title;

    @Column(name="citations",nullable = true)
    private int citations;

    @Column(name="year",nullable = true)
    private int year;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "google_author_id")
    private GoogleScholarAuthor googleScholarAuthor;

    public GoogleScholarPaper() {
    }

    public GoogleScholarPaper(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCitations() {
        return citations;
    }

    public void setCitations(int citations) {
        this.citations = citations;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GoogleScholarAuthor getGoogleScholarAuthor() {
        return googleScholarAuthor;
    }

    public void setGoogleScholarAuthor(GoogleScholarAuthor googleScholarAuthor) {
        this.googleScholarAuthor = googleScholarAuthor;
    }

}
