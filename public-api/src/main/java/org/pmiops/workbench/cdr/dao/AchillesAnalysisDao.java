package org.pmiops.workbench.cdr.dao;
import org.pmiops.workbench.cdr.model.AchillesAnalysis;
import org.pmiops.workbench.cdr.model.AchillesResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AchillesAnalysisDao extends CrudRepository<AchillesAnalysis, Long> {
    List<AchillesAnalysis> findAll();

    AchillesAnalysis findAchillesAnalysisByAnalysisId(long analysisId);
    AchillesAnalysis getByAnalysisId(long analysisId);


    List<AchillesAnalysis> findByResults_Stratum2(String stratum2);
    AchillesAnalysis findAchillesAnalysisByAnalysisIdAndResults_Stratum2(long analysisId, String stratum2);

    @Query(value = "select a.*, r.* " +
            "from achilles_analysis a left outer join achilles_results r on a.analysis_id = r.analysis_id " +
            "where a.analysis_id = :analysisId and r.stratum_2 = stratum2",
            nativeQuery = true)
    AchillesAnalysis findResultsByStratum2(@Param("analysisId") long analysisId, @Param("stratum2") String stratum2);

}


