package de.uhh.lt.xpertfinder.model.profiles.scholar;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "google_authors")
public class GoogleScholarAuthor {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="author_aan_id")
    private Long authorId;

    @Column(name="name")
    private String name;

    @Column(name="description")
    private String description;

    @Column(name="img")
    private String img;

    @Column(name="fieldsofwork")
    private String fieldsofwork;

    @Column(name="citations")
    private int citations;

    @Column(name="hindex")
    private int hindex;

    @Column(name="i20")
    private int i20;

    @OneToMany(mappedBy = "googleScholarAuthor", cascade = CascadeType.ALL)
    private List<GoogleScholarPaper> papers;

    public GoogleScholarAuthor() {}

    public GoogleScholarAuthor(String name, String description, String img, String fieldsofwork, int citations, int hindex, int i20) {
        this.name = name;
        this.description = description;
        this.img = img;
        this.fieldsofwork = fieldsofwork;
        this.citations = citations;
        this.hindex = hindex;
        this.i20 = i20;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFieldsofwork() {
        return fieldsofwork;
    }

    public void setFieldsofwork(String fieldsofwork) {
        this.fieldsofwork = fieldsofwork;
    }

    public int getCitations() {
        return citations;
    }

    public void setCitations(int citations) {
        this.citations = citations;
    }

    public int getHindex() {
        return hindex;
    }

    public void setHindex(int hindex) {
        this.hindex = hindex;
    }

    public int getI20() {
        return i20;
    }

    public void setI20(int i20) {
        this.i20 = i20;
    }

    public List<GoogleScholarPaper> getPapers() {
        return papers;
    }

    public void setPapers(List<GoogleScholarPaper> papers) {
        this.papers = papers;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
}
