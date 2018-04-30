package org.pmiops.workbench.cdr.dao;
import org.pmiops.workbench.cdr.model.AnalysisResult;
import org.pmiops.workbench.cdr.model.AchillesResult;
import org.pmiops.workbench.cdr.model.QuestionConcept;
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

}
