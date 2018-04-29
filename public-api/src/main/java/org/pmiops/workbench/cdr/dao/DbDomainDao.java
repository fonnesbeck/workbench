package org.pmiops.workbench.cdr.dao;
import org.pmiops.workbench.cdr.model.DbDomain;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DbDomainDao extends CrudRepository<DbDomain, Long> {
    // TODO -- maybe add order by
    List<DbDomain> findAll();
    DbDomain findByConceptId(long conceptId);

    @Query(nativeQuery=true, value="SELECT * from db_domain where db_type='survey' and concept_id <> 0 order by domain_desc")
    List<DbDomain> findSurveyList();

    @Query(nativeQuery = true,value="SELECT count_value from db_domain where concept_id=?1")
    Integer findSurveyParticpantCount(Long surveyConceptId);



}
