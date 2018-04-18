package org.pmiops.workbench.cdr.dao;
import org.pmiops.workbench.cdr.model.DbDomain;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DbDomainDao extends CrudRepository<DbDomain, Long> {
    // TODO -- maybe add order by
    List<DbDomain> findAll();

    @Query(nativeQuery=true, value="SELECT * from db_domain where db_type='survey' and concept_id <> 0")
    List<DbDomain> findByConceptId();
}
