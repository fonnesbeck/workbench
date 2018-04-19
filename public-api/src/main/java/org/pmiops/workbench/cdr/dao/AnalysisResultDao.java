package org.pmiops.workbench.cdr.dao;
import org.pmiops.workbench.cdr.model.AnalysisResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnalysisResultDao extends CrudRepository<AnalysisResult, Long> {

    List<AnalysisResult> findAnalysisResultsByAnalysisId(Long analysisId);
    List<AnalysisResult> findAnalysisResultsByAnalysisIdAndStratum1(Long analysisId, String stratum1);
    List<AnalysisResult> findAnalysisResultsByAnalysisIdAndStratum1AndStratum2(Long analysisId, String stratum1, String stratum2);
    AnalysisResult findAnalysisResultByAnalysisId(Long analysisId);

    @Query(nativeQuery=true, value="SELECT ar.stratum_3,ar.stratum_4,ar.stratum_5,ar.count_value from achilles_results ar where (ar.stratum_1=?1 and ar.stratum_2=?2) and ar.analysis_id=?3")
    List<Object[]> findSurveyAnswers(String surveyConceptId,String questionId,String stratum5);

    @Query(nativeQuery=true, value="SELECT distinct ar.stratum_2,c.concept_name from achilles_results ar join concept c on (ar.stratum_2=cast(c.concept_id as char)) where ar.stratum_1=?1")
    List<Object[]> findSurveyQuestions(String surveyConceptId);


}
