package org.pmiops.workbench.cdr.dao;

import org.pmiops.workbench.cdr.model.AchillesResult;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface AchillesResultDao extends CrudRepository<AchillesResult, Long> {
    AchillesResult findAchillesResultByAnalysisId(Long analysisId);


    @Query(nativeQuery=true,value="select ar.* from achilles_results ar where ar.stratum_1 = ?1 and ar.stratum_2 = ?2 and ar.stratum_4 regexp ?3")
    List<AchillesResult> findAchillesResults(String surveyConceptId,String qid,String regex);
}
