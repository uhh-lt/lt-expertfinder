package de.uhh.lt.xpertfinder.dao;

import de.uhh.lt.xpertfinder.model.keyword.Keyword;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

public interface KeywordDao extends CrudRepository<Keyword, Long> {

    @Query(value="SELECT kw.word, kw.rank FROM keywords_best as kw WHERE name = ?1 ORDER BY kw.rank DESC LIMIT 10", nativeQuery = true)
    List<Object[]> findKeywordsForAuthor(String author);

    @Query(value="SELECT kw.name, kw.word, kw.rank FROM keywords_best AS kw WHERE kw.name IN ?1 ORDER BY rank DESC;", nativeQuery = true)
    List<Object[]> findKeywordsForAuthors(List<String> author);

    @Query(value="SELECT k.word, k.rank, k.file FROM keywords_all AS k WHERE k.file IN ?1 AND (k.word like '% %' OR k.word like '%-%') ORDER BY k.rank ASC", nativeQuery = true)
    List<Object[]> findKeywordsForDocuments(List<String> files);

}

