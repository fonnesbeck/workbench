package org.pmiops.workbench.cdr.dao;
import org.pmiops.workbench.cdr.model.AchillesAnalysis;
import org.pmiops.workbench.cdr.model.AchillesResult;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AchillesAnalysisDao extends CrudRepository<AchillesAnalysis, Long> {
    List<AchillesAnalysis> findAll();

    AchillesAnalysis findAchillesAnalysisByAnalysisId(long analysisId);

    //AchillesAnalysis findByAnalysisIdAndResultsStratum2(long analysisId, String stratum2);
}


