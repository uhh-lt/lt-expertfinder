package de.uhh.lt.xpertfinder.model.keyword;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@MappedSuperclass
public abstract class Keyword {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name="word",nullable = false)
    private String word;

    @Column(name="score",nullable = false)
    private Double score;

    @Column(name="file",nullable = false)
    private String file;

    public Keyword() {

    }

    public Keyword(String word, Double score, String file) {
        this.word = word;
        this.score = score;
        this.file = file;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
