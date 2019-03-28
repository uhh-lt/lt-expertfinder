package de.uhh.lt.xpertfinder.dao;

import de.uhh.lt.xpertfinder.model.keyword.Keyword;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface AanDao extends CrudRepository<Keyword, Long> {

    @Query(value = "SELECT p.author, p.document FROM publications_aan p WHERE p.document IN ?1", nativeQuery = true)
    List<Object[]> findAllPublicationsAAN(List<String> files);

    @Query(value = "SELECT c.outgoing_file, c.incoming_file FROM citations_aan AS c WHERE c.outgoing_file IN ?1 AND c.incoming_file IN ?1", nativeQuery = true)
    List<Object[]> findAllCitationsAAN(List<String> files);

    @Query(value = "SELECT c.author1, c.author2 FROM collaborations_aan as c WHERE c.author1 IN ?1 AND c.author2 IN ?1", nativeQuery = true)
    List<Object[]> findAllCollaborationsAAN(List<String> authors);

    @Query(value = "SELECT c.author1, c.author2, c.count FROM collaborations_aan2 as c WHERE c.author1 IN ?1 AND c.author2 IN ?1", nativeQuery = true)
    List<Object[]> findAllCollaborationsAAN2(List<String> authors);

    @Query(value = "SELECT a.name, a.id FROM authors_aan a WHERE a.name IN ?1", nativeQuery = true)
    List<Object[]> findAllAuthorIds(Set<String> authors);

    @Query(value= "SELECT p.author, sub.file, sub.count FROM (SELECT incoming_file as file, COUNT(incoming_file) as count FROM citations_aan GROUP BY incoming_file) as sub JOIN publications_aan as p ON sub.file = p.document", nativeQuery = true)
    List<Object[]> findAllDocumentCitations();

    @Query(value ="SELECT a1.name as name1, a2.name as name2, COUNT(*) as count FROM publications_aan as p1, authors_aan as a1, publications_aan as p2, authors_aan as a2 WHERE p1.author = a1.name AND p2.author = a2.name AND p1.document = p2.document AND a1.id != a2.id AND p1.document IN ?1 GROUP BY a1.name, a2.name", nativeQuery = true)
    List<Object[]> findLocalCollaborations(List<String> files);

    @Query(value ="SELECT d.file, d.year FROM documents_aan as d WHERE d.file IN ?1", nativeQuery = true)
    List<Object[]> findDocumentYear(List<String> files);

    @Query(value ="SELECT d.file, d.title, d.year, d.venue FROM documents_aan as d WHERE d.file IN ?1", nativeQuery = true)
    List<Object[]> findDocumentInformationByIds(List<String> esIds);

    @Query(value ="SELECT c.incoming_file, COUNT(*) FROM citations_aan AS c WHERE c.incoming_file IN ?1 GROUP BY c.incoming_file", nativeQuery = true)
    List<Object[]> findCitationCountForDocuments(List<String> files);

    @Query(value ="SELECT pa.document, pa.author, a.id FROM documents_aan as d JOIN publications_aan pa ON d.file = pa.document JOIN authors_aan a ON pa.author = a.name WHERE d.file IN ?1", nativeQuery = true)
    List<Object[]> findAuthorsForDocuments(List<String> files);

    @Query(value ="SELECT a.name FROM authors_aan a WHERE a.id = ?1", nativeQuery = true)
    String findAuthorNameById(long id);

    // TODO: VERY PROBLEMATIC!!! if publication has no citations it is not returned!!!
    /**
     * @param id aan authorid
     * @return 0: file (str), 1: title (str), 2: year (int), 3: venue (str)
     */
    @Query(value = "SELECT d.file, d.title, d.year, d.venue FROM authors_aan a JOIN publications_aan ON a.name = publications_aan.author JOIN documents_aan d ON publications_aan.document = d.file WHERE a.id = ?1 GROUP BY d.file, d.title, d.year, d.venue", nativeQuery = true)
    List<Object[]> findPublicationsByAuthorId(Long id);

    @Query(value = "SELECT d.year, COUNT(*) AS citations FROM authors_aan a JOIN publications_aan AS p ON a.name = p.author JOIN documents_aan d ON p.document = d.file JOIN citations_aan AS c ON d.file = c.incoming_file WHERE a.id = ?1 GROUP BY d.year ORDER BY year ASC;", nativeQuery = true)
    List<Object[]> findCitationsPerYearForAuthorId(Long id);


    /**
     * @param authorname
     * @return 0: author (str), 1: collaboration count (int)
     */
    @Query(value = "SELECT * FROM (SELECT c.author2 as author, c.count, a.id FROM collaborations_aan2 AS c JOIN authors_aan as a ON c.author2 = a.name WHERE c.author1 = ?1   UNION SELECT c.author1 as author, c.count, a.id FROM collaborations_aan2 as c JOIN authors_aan as a ON c.author1 = a.name WHERE c.author2 = ?1) as mytable ORDER BY mytable.count DESC", nativeQuery = true)
    List<Object[]> findCollaborationsForAuthorByName(String authorname);

    @Query(value = "SELECT * FROM wikidata_map m JOIN wikidata w ON w.item = m.wikidataid WHERE m.authorid = ?1", nativeQuery = true)
    List<Object[]> findWikidataProfileForAuthorId(Long id);

    @Query(value = "SELECT a.id FROM authors_aan a WHERE (a.name LIKE %?1% OR a.alt_name LIKE %?1%)", nativeQuery = true)
    List<Long> checkExactAuthorMatch(String topic);

}
