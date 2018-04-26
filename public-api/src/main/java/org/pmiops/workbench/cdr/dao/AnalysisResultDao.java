package org.pmiops.workbench.cdr.dao;
import org.pmiops.workbench.cdr.model.AnalysisResult;
import org.pmiops.workbench.cdr.model.AchillesResult;
import org.pmiops.workbench.cdr.model.QuestionConcept;
import org.pmiops.workbench.cdr.model.Concept;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnalysisResultDao extends CrudRepository<AnalysisResult, Long> {

    List<AnalysisResult> findAnalysisResultsByAnalysisId(Long analysisId);
    List<AnalysisResult> findAnalysisResultsByAnalysisIdAndStratum1(Long analysisId, String stratum1);
    List<AnalysisResult> findAnalysisResultsByAnalysisIdAndStratum1AndStratum2(Long analysisId, String stratum1, String stratum2);
    AnalysisResult findAnalysisResultByAnalysisId(Long analysisId);
   // List<AnalysisResult> findAnalysisResultsByAnalysisIdAndStratum1OrderByStratum2AscStratum3Asc(Long analysisId,String stratum1);

    @Query(nativeQuery = true,value="SELECT * from achilles_results ar where ar.stratum_1=?1 and ar.analysis_id=?2 order by ar.stratum_2,ar.stratum_3")
    List<AchillesResult> findAchillesResults(String stratum1,Long analysisId);

    @Query(nativeQuery=true, value="SELECT ar.stratum_3,ar.stratum_4,ar.stratum_5,ar.count_value from achilles_results ar where (ar.stratum_1=?1 and ar.stratum_2=?2) and ar.analysis_id=?3")
    List<Object[]> findSurveyAnswers(String surveyConceptId,String questionId,String stratum5);

    @Query(nativeQuery=true, value="SELECT distinct ar.stratum_2,c.concept_name from achilles_results ar join concept c on (ar.stratum_2=c.concept_id) where ar.stratum_1=?1")
    List<QuestionConcept> findSurveyQuestions(String surveyConceptId);

    @Query(nativeQuery=true, value="SELECT concept_id, concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, concept_code,valid_start_date, valid_end_date, invalid_reason, count_value, prevalence  from concept where concept_id=?1")
    List<Concept> findSurveyQuestions5(Integer concept_id);


}
