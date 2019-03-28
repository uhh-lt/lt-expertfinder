package de.uhh.lt.xpertfinder.dao;

import de.uhh.lt.xpertfinder.model.keyword.Keyword;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StatisticDao extends CrudRepository<Keyword, Long> {

    @Query(value = "SELECT p.author, COUNT(p.author) as c FROM publications_aan as p GROUP BY p.author", nativeQuery = true)
    List<Object[]> getPublicationCountPerAuthor();

    @Query(value = "SELECT p.author, COUNT(p.author) as coun FROM publications_aan as p JOIN citations_aan as c ON p.document = c.incoming_file GROUP BY p.author", nativeQuery = true)
    List<Object[]> getCitationCountPerAuthor();
}
