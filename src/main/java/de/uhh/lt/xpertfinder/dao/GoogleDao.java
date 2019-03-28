package de.uhh.lt.xpertfinder.dao;

import de.uhh.lt.xpertfinder.model.profiles.scholar.GoogleScholarAuthor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GoogleDao extends CrudRepository<GoogleScholarAuthor, Long> {

    GoogleScholarAuthor findOneByAuthorId(Long aLong);

    List<GoogleScholarAuthor> findAllByAuthorIdIn(List<Long> ids);
}

