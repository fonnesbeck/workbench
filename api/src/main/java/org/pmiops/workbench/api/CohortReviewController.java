package org.pmiops.workbench.api;

import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.QueryResult;
import com.google.gson.Gson;
import org.pmiops.workbench.cohortbuilder.ParticipantCounter;
import org.pmiops.workbench.cohortreview.CohortReviewService;
import org.pmiops.workbench.db.model.Cohort;
import org.pmiops.workbench.db.model.CohortReview;
import org.pmiops.workbench.db.model.ParticipantCohortStatus;
import org.pmiops.workbench.db.model.ParticipantCohortStatusKey;
import org.pmiops.workbench.exceptions.BadRequestException;
import org.pmiops.workbench.exceptions.NotFoundException;
import org.pmiops.workbench.model.CohortStatus;
import org.pmiops.workbench.model.CohortSummaryListResponse;
import org.pmiops.workbench.model.CreateReviewRequest;
import org.pmiops.workbench.model.EmptyResponse;
import org.pmiops.workbench.model.ModifyCohortStatusRequest;
import org.pmiops.workbench.model.ModifyParticipantCohortAnnotationRequest;
import org.pmiops.workbench.model.ParticipantCohortAnnotation;
import org.pmiops.workbench.model.ParticipantCohortAnnotationListResponse;
import org.pmiops.workbench.model.ReviewStatus;
import org.pmiops.workbench.model.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
public class CohortReviewController implements CohortReviewApiDelegate {

    public static final String STATUS = "status";
    public static final String PARTICIPANT_ID = "participantKey.participantId";
    public static final Integer PAGE = 0;
    public static final Integer PAGE_SIZE = 25;
    public static final Integer MAX_REVIEW_SIZE = 10000;
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";

    private CohortReviewService cohortReviewService;
    private BigQueryService bigQueryService;
    private CodeDomainLookupService codeDomainLookupService;
    private ParticipantCounter participantCounter;

    private static final Logger log = Logger.getLogger(CohortReviewController.class.getName());

    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final Function<ParticipantCohortStatus, org.pmiops.workbench.model.ParticipantCohortStatus>
            TO_CLIENT_PARTICIPANT =
            new Function<ParticipantCohortStatus, org.pmiops.workbench.model.ParticipantCohortStatus>() {
                @Override
                public org.pmiops.workbench.model.ParticipantCohortStatus apply(ParticipantCohortStatus participant) {
                    return new org.pmiops.workbench.model.ParticipantCohortStatus()
                            .participantId(participant.getParticipantKey().getParticipantId())
                            .status(participant.getStatus())
                            .birthDatetime(participant.getBirthDate().getTime())
                            .ethnicityConceptId(participant.getEthnicityConceptId())
                            .genderConceptId(participant.getGenderConceptId())
                            .raceConceptId(participant.getRaceConceptId());
                }
            };

    /**
     * Converter function from backend representation (used with Hibernate) to
     * client representation (generated by Swagger).
     */
    private static final BiFunction<CohortReview, PageRequest, org.pmiops.workbench.model.CohortReview>
            TO_CLIENT_COHORTREVIEW =
            new BiFunction<CohortReview, PageRequest, org.pmiops.workbench.model.CohortReview>() {
                @Override
                public org.pmiops.workbench.model.CohortReview apply(CohortReview cohortReview, PageRequest pageRequest) {
                    final Sort.Order order = pageRequest.getSort().iterator().next();
                    return new org.pmiops.workbench.model.CohortReview()
                            .cohortReviewId(cohortReview.getCohortReviewId())
                            .cohortId(cohortReview.getCohortId())
                            .cdrVersionId(cohortReview.getCdrVersionId())
                            .creationTime(cohortReview.getCreationTime().toString())
                            .matchedParticipantCount(cohortReview.getMatchedParticipantCount())
                            .reviewedCount(cohortReview.getReviewedCount())
                            .reviewStatus(cohortReview.getReviewStatus())
                            .reviewSize(cohortReview.getReviewSize())
                            .page(pageRequest.getPageNumber())
                            .pageSize(pageRequest.getPageSize())
                            .sortOrder(order.getDirection().toString())
                            .sortColumn(order.getProperty());
                }
            };

    @Autowired
    CohortReviewController(CohortReviewService cohortReviewService,
                           BigQueryService bigQueryService,
                           CodeDomainLookupService codeDomainLookupService,
                           ParticipantCounter participantCounter) {
        this.cohortReviewService = cohortReviewService;
        this.bigQueryService = bigQueryService;
        this.codeDomainLookupService = codeDomainLookupService;
        this.participantCounter = participantCounter;
    }

    /**
     * Create a cohort review per the specified workspaceId, cohortId, cdrVersionId and size. If participant cohort status
     * data exists for a review or no cohort review exists for cohortReviewId then throw a
     * {@link BadRequestException}.
     *
     * @param workspaceNamespace
     * @param workspaceId
     * @param cohortId
     * @param cdrVersionId
     * @param request
     * @return
     */
    @Override
    public ResponseEntity<org.pmiops.workbench.model.CohortReview> createCohortReview(String workspaceNamespace,
                                                                                      String workspaceId,
                                                                                      Long cohortId,
                                                                                      Long cdrVersionId,
                                                                                      CreateReviewRequest request) {
        if (request.getSize() <= 0 || request.getSize() > MAX_REVIEW_SIZE) {
            throw new BadRequestException(
                    String.format("Invalid Request: Cohort Review size must be between %s and %s", 0, MAX_REVIEW_SIZE));
        }
        CohortReview cohortReview = null;
        try {
            cohortReview = cohortReviewService.findCohortReview(cohortId, cdrVersionId);
        } catch (NotFoundException nfe) {
            cohortReview = initializeAndSaveCohortReview(workspaceNamespace, workspaceId, cohortId, cdrVersionId);
        }
        if(cohortReview.getReviewSize() > 0) {
            throw new BadRequestException(
                    String.format("Invalid Request: Cohort Review already created for cohortId: %s, cdrVersionId: %s",
                            cohortId, cdrVersionId));
        }

        Cohort cohort = cohortReviewService.findCohort(cohortId);
        //this validates that the user is in the proper workspace
        cohortReviewService.validateMatchingWorkspace(workspaceNamespace, workspaceId, cohort.getWorkspaceId());

        SearchRequest searchRequest = new Gson().fromJson(getCohortDefinition(cohort), SearchRequest.class);

        /** TODO: this is temporary and will be removed when we figure out the conceptId mappings **/
        codeDomainLookupService.findCodesForEmptyDomains(searchRequest.getIncludes());
        codeDomainLookupService.findCodesForEmptyDomains(searchRequest.getExcludes());

        QueryResult result = bigQueryService.executeQuery(bigQueryService.filterBigQueryConfig(
                participantCounter.buildParticipantIdQuery(searchRequest, request.getSize(), 0L)));
        Map<String, Integer> rm = bigQueryService.getResultMapper(result);

        List<ParticipantCohortStatus> participantCohortStatuses =
                createParticipantCohortStatusesList(cohortReview.getCohortReviewId(), result, rm);

        cohortReview
                .reviewSize(participantCohortStatuses.size())
                .reviewedCount(0L)
                .reviewStatus(ReviewStatus.CREATED);

        //when saving ParticipantCohortStatuses to the database the long value of birthdate is mutated.
        cohortReviewService.saveFullCohortReview(cohortReview, participantCohortStatuses);

        List<ParticipantCohortStatus> paginatedPCS =
                cohortReviewService.findParticipantCohortStatuses(cohortReview.getCohortReviewId(), createPageRequest(PAGE, PAGE_SIZE, ASC, PARTICIPANT_ID)).getContent();

        org.pmiops.workbench.model.CohortReview responseReview = TO_CLIENT_COHORTREVIEW.apply(cohortReview, createPageRequest(PAGE, PAGE_SIZE, ASC, PARTICIPANT_ID));
        responseReview.setParticipantCohortStatuses(paginatedPCS.stream().map(TO_CLIENT_PARTICIPANT).collect(Collectors.toList()));

        return ResponseEntity.ok(responseReview);
    }

    @Override
    public ResponseEntity<ParticipantCohortAnnotation> createParticipantCohortAnnotation(String workspaceNamespace,
                                                                                         String workspaceId,
                                                                                         Long cohortReviewId,
                                                                                         Long participantId,
                                                                                         ParticipantCohortAnnotation request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new ParticipantCohortAnnotation());
    }

    @Override
    public ResponseEntity<EmptyResponse> deleteParticipantCohortAnnotation(String workspaceNamespace,
                                                                           String workspaceId,
                                                                           Long cohortReviewId,
                                                                           Long participantId,
                                                                           Long annotationId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new EmptyResponse());
    }

    @Override
    public ResponseEntity<CohortSummaryListResponse> getCohortSummary(String workspaceNamespace,
                                                                      String workspaceId,
                                                                      Long cohortId,
                                                                      Long cdrVersionId,
                                                                      String domain) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new CohortSummaryListResponse());
    }

    @Override
    public ResponseEntity<ParticipantCohortAnnotationListResponse> getParticipantCohortAnnotations(String workspaceNamespace,
                                                                                                   String workspaceId,
                                                                                                   Long cohortReviewId,
                                                                                                   Long participantId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new ParticipantCohortAnnotationListResponse());
    }

    @Override
    public ResponseEntity<org.pmiops.workbench.model.ParticipantCohortStatus> getParticipantCohortStatus(String workspaceNamespace,
                                                                                                         String workspaceId,
                                                                                                         Long cohortId,
                                                                                                         Long cdrVersionId,
                                                                                                         Long participantId) {
        Cohort cohort = cohortReviewService.findCohort(cohortId);
        //this validates that the user is in the proper workspace
        cohortReviewService.validateMatchingWorkspace(workspaceNamespace, workspaceId, cohort.getWorkspaceId());
        CohortReview review = cohortReviewService.findCohortReview(cohortId, cdrVersionId);
        ParticipantCohortStatus status =
                cohortReviewService.findParticipantCohortStatus(review.getCohortReviewId(), participantId);
        return ResponseEntity.ok(TO_CLIENT_PARTICIPANT.apply(status));
    }

    @Override
    public ResponseEntity<org.pmiops.workbench.model.ParticipantCohortStatus> updateParticipantCohortStatus(String workspaceNamespace,
                                                                                                            String workspaceId,
                                                                                                            Long cohortId,
                                                                                                            Long cdrVersionId,
                                                                                                            Long participantId,
                                                                                                            ModifyCohortStatusRequest cohortStatusRequest) {

        Cohort cohort = cohortReviewService.findCohort(cohortId);
        //this validates that the user is in the proper workspace
        cohortReviewService.validateMatchingWorkspace(workspaceNamespace, workspaceId, cohort.getWorkspaceId());

        CohortReview cohortReview = cohortReviewService.findCohortReview(cohortId, cdrVersionId);

        ParticipantCohortStatus participantCohortStatus
                = cohortReviewService.findParticipantCohortStatus(cohortReview.getCohortReviewId(), participantId);

        participantCohortStatus.setStatus(cohortStatusRequest.getStatus());
        cohortReviewService.saveParticipantCohortStatus(participantCohortStatus);

        cohortReview.lastModifiedTime(new Timestamp(System.currentTimeMillis()));
        cohortReview.incrementReviewedCount();
        cohortReviewService.saveCohortReview(cohortReview);

        return ResponseEntity.ok(TO_CLIENT_PARTICIPANT.apply(participantCohortStatus));
    }

    /**
     * Get all participants for the specified cohortId and cdrVersionId. This endpoint does pagination
     * based on page, pageSize, sortOrder and sortColumn.
     *
     * @param cohortId
     * @param cdrVersionId
     * @param page
     * @param pageSize
     * @param sortOrder
     * @param sortColumn
     * @return
     */
    @Override
    public ResponseEntity<org.pmiops.workbench.model.CohortReview> getParticipantCohortStatuses(String workspaceNamespace,
                                                                                                String workspaceId,
                                                                                                Long cohortId,
                                                                                                Long cdrVersionId,
                                                                                                Integer page,
                                                                                                Integer pageSize,
                                                                                                String sortOrder,
                                                                                                String sortColumn,
                                                                                                List<String> filterColumns,
                                                                                                List<String> filterValues) {
        CohortReview cohortReview = null;
        try {
            cohortReview = cohortReviewService.findCohortReview(cohortId, cdrVersionId);
        } catch (NotFoundException nfe) {
            cohortReview = initializeAndSaveCohortReview(workspaceNamespace, workspaceId, cohortId, cdrVersionId);
        }

        PageRequest pageRequest = createPageRequest(page, pageSize, sortOrder, sortColumn);

        final List<ParticipantCohortStatus> participantCohortStatuses =
                cohortReviewService.findParticipantCohortStatuses(cohortReview.getCohortReviewId(), pageRequest).getContent();

        org.pmiops.workbench.model.CohortReview responseReview = TO_CLIENT_COHORTREVIEW.apply(cohortReview, pageRequest);
        responseReview.setParticipantCohortStatuses(
                participantCohortStatuses.stream().map(TO_CLIENT_PARTICIPANT).collect(Collectors.toList()));

        return ResponseEntity.ok(responseReview);
    }

    @Override
    public ResponseEntity<ParticipantCohortAnnotation>
    updateParticipantCohortAnnotation(String workspaceNamespace,
                                      String workspaceId,
                                      Long cohortReviewId,
                                      Long participantId,
                                      Long annotationId,
                                      ModifyParticipantCohortAnnotationRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new ParticipantCohortAnnotation());
    }

    /**
     * Helper method to create a new {@link CohortReview} and persist it to the workbench database.
     *
     * @param workspaceNamespace
     * @param workspaceId
     * @param cohortId
     * @param cdrVersionId
     * @return
     */
    private CohortReview initializeAndSaveCohortReview(String workspaceNamespace,
                                                       String workspaceId,
                                                       Long cohortId,
                                                       Long cdrVersionId) {
        Cohort cohort = cohortReviewService.findCohort(cohortId);
        //this validates that the user is in the proper workspace
        cohortReviewService.validateMatchingWorkspace(workspaceNamespace, workspaceId, cohort.getWorkspaceId());

        SearchRequest request = new Gson().fromJson(getCohortDefinition(cohort), SearchRequest.class);

        /** TODO: this is temporary and will be removed when we figure out the conceptId mappings **/
        codeDomainLookupService.findCodesForEmptyDomains(request.getIncludes());
        codeDomainLookupService.findCodesForEmptyDomains(request.getExcludes());

        QueryResult result = bigQueryService.executeQuery(
                bigQueryService.filterBigQueryConfig(participantCounter.buildParticipantCounterQuery(request)));
        Map<String, Integer> rm = bigQueryService.getResultMapper(result);
        List<FieldValue> row = result.iterateAll().iterator().next();
        long cohortCount = bigQueryService.getLong(row, rm.get("count"));

        CohortReview cohortReview = createNewCohortReview(cohortId, cdrVersionId, cohortCount);
        return cohortReviewService.saveCohortReview(cohortReview);
    }

    private List<ParticipantCohortStatus> createParticipantCohortStatusesList(Long cohortReviewId,
                                                                              QueryResult result,
                                                                              Map<String, Integer> rm) {
        List<ParticipantCohortStatus> participantCohortStatuses = new ArrayList<>();
        for (List<FieldValue> row : result.iterateAll()) {
            String birthDateTimeString = bigQueryService.getString(row, rm.get("birth_datetime"));
            if (birthDateTimeString == null) {
                throw new BigQueryException(500, "birth_datetime is null at position: " + rm.get("birth_datetime"));
            }
            Date birthDate = Date.from(Instant.ofEpochMilli(Double.valueOf(birthDateTimeString).longValue() * 1000));
            participantCohortStatuses.add(
                    new ParticipantCohortStatus()
                            .participantKey(
                                    new ParticipantCohortStatusKey(
                                            cohortReviewId,
                                            bigQueryService.getLong(row, rm.get("person_id"))))
                            .status(CohortStatus.NOT_REVIEWED)
                            .birthDate(new java.sql.Date(birthDate.getTime()))
                            .genderConceptId(bigQueryService.getLong(row, rm.get("gender_concept_id")))
                            .raceConceptId(bigQueryService.getLong(row, rm.get("race_concept_id")))
                            .ethnicityConceptId(bigQueryService.getLong(row, rm.get("ethnicity_concept_id"))));
        }
        return participantCohortStatuses;
    }

    private String getCohortDefinition(Cohort cohort) {
        String definition = cohort.getCriteria();
        if (definition == null) {
            throw new NotFoundException(
                    String.format("Not Found: No Cohort definition matching cohortId: %s", cohort.getCohortId()));
        }
        return definition;
    }

    private PageRequest createPageRequest(Integer page, Integer pageSize, String sortOrder, String sortColumn) {
        int pageParam = Optional.ofNullable(page).orElse(PAGE);
        int pageSizeParam = Optional.ofNullable(pageSize).orElse(PAGE_SIZE);
        Sort.Direction orderParam = getSortOrder(sortOrder);
        String columnParam = getSortColumn(sortColumn);

        final Sort sort = (columnParam.equals(PARTICIPANT_ID))
                ? new Sort(orderParam, columnParam)
                : new Sort(orderParam, columnParam, PARTICIPANT_ID);
        return new PageRequest(pageParam, pageSizeParam, sort);
    }

    private Sort.Direction getSortOrder(String sortOrder) {
        return Sort.Direction.fromString(Optional.ofNullable(sortOrder)
                .filter(o -> o.equalsIgnoreCase(DESC)).orElse(ASC));
    }

    private String getSortColumn(String sortColumn) {
        return Optional.ofNullable(sortColumn)
                .filter(o -> o.equalsIgnoreCase(STATUS)).orElse(PARTICIPANT_ID);
    }

    private CohortReview createNewCohortReview(Long cohortId, Long cdrVersionId, long cohortCount) {
        CohortReview cohortReview;
        cohortReview = new CohortReview();
        cohortReview.setCohortId(cohortId);
        cohortReview.setCdrVersionId(cdrVersionId);
        cohortReview.matchedParticipantCount(cohortCount);
        cohortReview.setCreationTime(new Timestamp(System.currentTimeMillis()));
        cohortReview.reviewedCount(0L);
        cohortReview.reviewStatus(ReviewStatus.NONE);
        return cohortReview;
    }

}
