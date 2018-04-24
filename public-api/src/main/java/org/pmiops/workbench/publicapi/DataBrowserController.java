package org.pmiops.workbench.publicapi;


import org.pmiops.workbench.cdr.dao.AchillesAnalysisDao;
import org.pmiops.workbench.cdr.dao.DbDomainDao;
import org.pmiops.workbench.cdr.model.AchillesAnalysis;
import org.pmiops.workbench.cdr.dao.ConceptDao;
import org.pmiops.workbench.cdr.model.Concept;
import org.pmiops.workbench.cdr.model.DbDomain;
import org.pmiops.workbench.cdr.dao.AnalysisResultDao;
import org.pmiops.workbench.cdr.model.AnalysisResult;
import org.pmiops.workbench.cdr.model.AchillesResult;
import org.pmiops.workbench.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
public class DataBrowserController implements DataBrowserApiDelegate {

    @Autowired
    private ConceptDao conceptDao;

    @Autowired
    private AnalysisResultDao analysisResultDao;

    @Autowired
    private AchillesAnalysisDao achillesAnalysisDao;

    @Autowired
    private DbDomainDao dbDomainDao;

    public static final long PARTICIPANT_COUNT_ANALYSIS_ID = 1;
    public static final long COUNT_ANALYSIS_ID = 3000;
    public static final long GENDER_ANALYSIS_ID = 3101;
    public static final long AGE_ANALYSIS_ID = 3102;


    private static final Logger log = Logger.getLogger(DataBrowserController.class.getName());

    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final Function<Concept, org.pmiops.workbench.model.Concept>
            TO_CLIENT_CONCEPT =
            new Function<Concept, org.pmiops.workbench.model.Concept>() {
                @Override
                public org.pmiops.workbench.model.Concept apply(Concept concept) {
                    return new org.pmiops.workbench.model.Concept()
                            .conceptId(concept.getConceptId())
                            .conceptName(concept.getConceptName())
                            .standardConcept(concept.getStandardConcept())
                            .conceptCode(concept.getConceptCode())
                            .conceptClassId(concept.getConceptClassId())
                            .vocabularyId(concept.getVocabularyId())
                            .domainId(concept.getDomainId())
                            .countValue(concept.getCountValue())
                            .prevalence(concept.getPrevalence());
                }
            };
    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final Function<QuestionConcept, org.pmiops.workbench.model.QuestionConcept>
            TO_CLIENT_QUESTION_CONCEPT =
            new Function<QuestionConcept, org.pmiops.workbench.model.QuestionConcept>() {
                @Override
                public org.pmiops.workbench.model.QuestionConcept apply(QuestionConcept concept) {
                    //org.pmiops.workbench.model.QuestionConcept ret   = new org.pmiops.workbench.model.QuestionConcept()
                    return new org.pmiops.workbench.model.QuestionConcept()

                            .conceptId(concept.getConceptId())
                            .conceptName(concept.getConceptName())
                            .standardConcept(concept.getStandardConcept())
                            .conceptCode(concept.getConceptCode())
                            .conceptClassId(concept.getConceptClassId())
                            .vocabularyId(concept.getVocabularyId())
                            .domainId(concept.getDomainId())
                            .countValue(concept.getCountValue())
                            .prevalence(concept.getPrevalence());

                    //List<org.pmiops.workbench.model.AchillesResult> answers = concept.getAnswers().stream().map(TO_CLIENT_ACHILLESRESULT).collect(Collectors.toList());
                    //ret.answers(answers);

                    //return ret;

                }
            };


    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final Function<AnalysisResult, org.pmiops.workbench.model.AnalysisResult>
            TO_CLIENT_ANALYSIS_RESULT =
            new Function<AnalysisResult, org.pmiops.workbench.model.AnalysisResult>() {
                @Override
                public org.pmiops.workbench.model.AnalysisResult apply(org.pmiops.workbench.cdr.model.AnalysisResult cdr) {
                    return new org.pmiops.workbench.model.AnalysisResult()
                            .id(cdr.getId())
                            .analysisId(cdr.getAnalysisId())
                            .stratum1(cdr.getStratum1())
                            .stratum2(cdr.getStratum2())
                            .stratum3(cdr.getStratum3())
                            .stratum4(cdr.getStratum4())
                            .countValue(cdr.getCountValue());

                }
            };

    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final Function<AchillesAnalysis, org.pmiops.workbench.model.Analysis>
            TO_CLIENT_ANALYSIS =
            new Function<AchillesAnalysis, org.pmiops.workbench.model.Analysis>() {
                @Override
                public org.pmiops.workbench.model.Analysis apply(org.pmiops.workbench.cdr.model.AchillesAnalysis cdr) {
                    return new org.pmiops.workbench.model.Analysis()
                            .analysisId(cdr.getAnalysisId())
                            .analysisName(cdr.getAnalysisName())
                            .stratum1Name(cdr.getStratum1Name())
                            .stratum2Name(cdr.getStratum2Name())
                            .stratum3Name(cdr.getStratum3Name())
                            .stratum4Name(cdr.getStratum4Name())
                            .stratum5Name(cdr.getStratum5Name())
                            .chartType(cdr.getChartType())
                            .dataType(cdr.getDataType());

                }
            };

    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final Function<DbDomain, org.pmiops.workbench.model.DbDomain>
            TO_CLIENT_DBDOMAIN =
            new Function<DbDomain, org.pmiops.workbench.model.DbDomain>() {
                @Override
                public org.pmiops.workbench.model.DbDomain apply(org.pmiops.workbench.cdr.model.DbDomain cdr) {
                    return new org.pmiops.workbench.model.DbDomain()
                            .domainId(cdr.getDomainId())
                            .domainDisplay(cdr.getDomainDisplay())
                            .domainDesc(cdr.getDomainDesc())
                            .dbType(cdr.getDbType())
                            .domainRoute(cdr.getDomainRoute())
                            .conceptId(cdr.getConceptId())
                            .numParticipants(cdr.getCountValue());

                }
            };


    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final Function<SurveyAnswer, org.pmiops.workbench.model.SurveyAnswer>
            TO_CLIENT_SURVEYANSWER =
            new Function<SurveyAnswer, SurveyAnswer>() {
                @Override
                public org.pmiops.workbench.model.SurveyAnswer apply(SurveyAnswer cdr) {
                    return new org.pmiops.workbench.model.SurveyAnswer()
                            .answerConceptId(cdr.getAnswerConceptId())
                            .answerValue(cdr.getAnswerValue())
                            .stratumCount(cdr.getStratumCount());

                }
            };

    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final Function<SurveyQuestion, org.pmiops.workbench.model.SurveyQuestion>
            TO_CLIENT_SURVEYQUESTION =
            new Function<SurveyQuestion, SurveyQuestion>() {
                @Override
                public org.pmiops.workbench.model.SurveyQuestion apply(SurveyQuestion cdr) {
                    return new org.pmiops.workbench.model.SurveyQuestion()
                            .questionConceptId(cdr.getQuestionConceptId())
                            .questionValue(cdr.getQuestionValue())
                            .answers(cdr.getAnswers());

                }
            };

    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final Function<AchillesResult, org.pmiops.workbench.model.AchillesResult>
            TO_CLIENT_ACHILLESRESULT =
            new Function<AchillesResult, org.pmiops.workbench.model.AchillesResult>() {
                @Override
                public org.pmiops.workbench.model.AchillesResult apply(AchillesResult o) {
                    return new org.pmiops.workbench.model.AchillesResult()
                            .id(o.getId())
                            .analysisId(o.getAnalysisId())
                            .stratum1(o.getStratum1())
                            .stratum2(o.getStratum2())
                            .stratum3(o.getStratum3())
                            .stratum4(o.getStratum4())
                            .stratum5(o.getStratum5())
                            .countValue(o.getCountValue());
                }
            };


    /**
     * This method searches concepts
     *
     * @param conceptName
     * @param standardConcept
     * @param concept_code
     * @param vocabulary_id
     * @param domain_id
     * @return
     */
    @Override
    public ResponseEntity<ConceptListResponse> getConceptsSearch(
            String conceptName,
            String standardConcept,
            String concept_code,
            String vocabulary_id,
            String domain_id) {


        List<Concept> conceptList;

        // If Concept name do search on name

        if (conceptName != null) {
            conceptList = conceptDao.findConceptLikeName(conceptName);
        } else {
            conceptList = conceptDao.findConceptsOrderedByCount();
        }

        ConceptListResponse resp = new ConceptListResponse();
        resp.setItems(conceptList.stream().map(TO_CLIENT_CONCEPT).collect(Collectors.toList()));

        return ResponseEntity.ok(resp);
    }

    /**
     * This method gets concepts with maps to relationship in concept relationship table
     *
     * @param conceptId
     * @return
     */
    @Override
    public ResponseEntity<ConceptListResponse> getChildConcepts(Long conceptId) {
        List<Concept> conceptList = conceptDao.findConceptsMapsToChildren(conceptId);
        ConceptListResponse resp = new ConceptListResponse();
        resp.setItems(conceptList.stream().map(TO_CLIENT_CONCEPT).collect(Collectors.toList()));
        return ResponseEntity.ok(resp);
    }

    /**
     * This method gets concepts with maps to relationship in concept relationship table
     *
     * @param conceptId
     * @return
     */
    @Override
    public ResponseEntity<ConceptListResponse> getParentConcepts(Long conceptId) {
        List<Concept> conceptList = conceptDao.findConceptsMapsToParents(conceptId);
        ConceptListResponse resp = new ConceptListResponse();
        resp.setItems(conceptList.stream().map(TO_CLIENT_CONCEPT).collect(Collectors.toList()));
        return ResponseEntity.ok(resp);
    }

    /**
     * This method searches concepts
     *
     * @param analysisId
     * @param stratum1
     * @param stratum2
     * @return
     */
    @Override
    public ResponseEntity<AnalysisResultListResponse> getAnalysisResults(
            Long analysisId,
            String stratum1,
            String stratum2
    ) {

        final List<AnalysisResult> resultList;

        if ((stratum1 != null && !stratum1.isEmpty()) && (stratum2 != null && !stratum2.isEmpty())) {
            resultList = analysisResultDao.findAnalysisResultsByAnalysisIdAndStratum1AndStratum2(analysisId, stratum1, stratum2);
        } else if ((stratum1 != null && !stratum1.isEmpty())) {
            resultList = analysisResultDao.findAnalysisResultsByAnalysisIdAndStratum1(analysisId, stratum1);
        } else {
            resultList = analysisResultDao.findAnalysisResultsByAnalysisId(analysisId);
        }

        AnalysisResultListResponse resp = new AnalysisResultListResponse();
        resp.setItems(resultList.stream().map(TO_CLIENT_ANALYSIS_RESULT).collect(Collectors.toList()));


        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<org.pmiops.workbench.model.AnalysisResult> getParticipantCount() {
        AnalysisResult result = analysisResultDao.findAnalysisResultByAnalysisId(PARTICIPANT_COUNT_ANALYSIS_ID);
        return ResponseEntity.ok(TO_CLIENT_ANALYSIS_RESULT.apply(result));
    }

    /* getConceptCount(conceptId)
     * Returns
     */
    @Override
    public ResponseEntity<AnalysisResultListResponse> getConceptCount(String conceptId) {
        return this.getAnalysisResults(COUNT_ANALYSIS_ID, conceptId, null);
    }

    @Override
    public ResponseEntity<AnalysisResultListResponse> getConceptCountByGender(String conceptId) {
        return this.getAnalysisResults(GENDER_ANALYSIS_ID, conceptId, null);
    }

    @Override
    public ResponseEntity<AnalysisResultListResponse> getConceptCountByAge(String conceptId) {
        return this.getAnalysisResults(AGE_ANALYSIS_ID, conceptId, null);
    }

    @Override
    public ResponseEntity<AnalysisListResponse> getAnalyses() {

        List<AchillesAnalysis> resultList = achillesAnalysisDao.findAll();
        AnalysisListResponse resp = new AnalysisListResponse();
        resp.setItems(resultList.stream().map(TO_CLIENT_ANALYSIS).collect(Collectors.toList()));
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<DbDomainListResponse> getDbDomains() {
        List<DbDomain> resultList = dbDomainDao.findAll();
        DbDomainListResponse resp = new DbDomainListResponse();
        resp.setItems(resultList.stream().map(TO_CLIENT_DBDOMAIN).collect(Collectors.toList()));
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<DbDomainListResponse> getSurveyList() {
        List<DbDomain> surveys = dbDomainDao.findSurveyList();
        DbDomainListResponse resp = new DbDomainListResponse();
        resp.setItems(surveys.stream().map(TO_CLIENT_DBDOMAIN).collect(Collectors.toList()));
        return ResponseEntity.ok(resp);
    }


    @Override
    public ResponseEntity<SurveyResult> getSurveyResults(Long surveyConceptId, Long analysisId) {

        SurveyResult resp = new SurveyResult();
        return ResponseEntity.ok(resp);
    }


    @Override
    public ResponseEntity<QuestionListResponse> getSurveyQuestions5() {
        // 1586134
        List<QuestionConcept> resultList = analysisResultDao.findSurveyQuestions5();
        QuestionListResponse resp=new QuestionListResponse();
        resp.setItems(resultList.stream().map(TO_CLIENT_QUESTION_CONCEPT).collect(Collectors.toList()));
        return ResponseEntity.ok(resp);

    }

}
