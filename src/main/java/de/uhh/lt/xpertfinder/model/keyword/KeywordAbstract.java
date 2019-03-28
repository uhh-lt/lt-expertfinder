package de.uhh.lt.xpertfinder.model.keyword;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "keywords_abstract")
public class KeywordAbstract extends Keyword {
    public KeywordAbstract(String word, Double score, String file) {
        super(word, score, file);
    }
}
