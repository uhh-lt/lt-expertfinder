package de.uhh.lt.xpertfinder.model.profiles.scholar;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

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

    @Column(name="i10")
    private int i10;

    @Column(name="link")
    private String link;

    public GoogleScholarAuthor() {}

    public GoogleScholarAuthor(String name, String description, String img, String fieldsofwork, int citations, int hindex, int i10, String link) {
        this.name = name;
        this.description = description;
        this.img = img;
        this.fieldsofwork = fieldsofwork;
        this.citations = citations;
        this.hindex = hindex;
        this.i10 = i10;
        this.link = link;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getI10() {
        return i10;
    }

    public void setI10(int i10) {
        this.i10 = i10;
    }
}
