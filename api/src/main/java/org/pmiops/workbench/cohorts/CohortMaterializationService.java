package org.pmiops.workbench.cohorts;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Provider;
import org.pmiops.workbench.api.BigQueryService;
import org.pmiops.workbench.cohortbuilder.FieldSetQueryBuilder;
import org.pmiops.workbench.cohortbuilder.ParticipantCounter;
import org.pmiops.workbench.cohortbuilder.ParticipantCriteria;
import org.pmiops.workbench.cohortbuilder.TableQueryAndConfig;
import org.pmiops.workbench.cohortreview.AnnotationQueryBuilder;
import org.pmiops.workbench.config.CdrBigQuerySchemaConfig;
import org.pmiops.workbench.config.CdrBigQuerySchemaConfig.ColumnConfig;
import org.pmiops.workbench.config.CdrBigQuerySchemaConfig.TableConfig;
import org.pmiops.workbench.db.dao.ParticipantCohortStatusDao;
import org.pmiops.workbench.db.model.CohortReview;
import org.pmiops.workbench.db.model.ParticipantIdAndCohortStatus;
import org.pmiops.workbench.db.model.ParticipantIdAndCohortStatus.Key;
import org.pmiops.workbench.exceptions.BadRequestException;
import org.pmiops.workbench.model.CohortStatus;
import org.pmiops.workbench.model.FieldSet;
import org.pmiops.workbench.model.MaterializeCohortRequest;
import org.pmiops.workbench.model.MaterializeCohortResponse;
import org.pmiops.workbench.model.SearchRequest;
import org.pmiops.workbench.model.TableQuery;
import org.pmiops.workbench.utils.PaginationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CohortMaterializationService {

  @VisibleForTesting
  static final String PERSON_ID = "person_id";
  @VisibleForTesting
  static final String PERSON_TABLE = "person";

  private static final List<CohortStatus> NOT_EXCLUDED =
      Arrays.asList(CohortStatus.INCLUDED, CohortStatus.NEEDS_FURTHER_REVIEW,
          CohortStatus.NOT_REVIEWED);

  private final BigQueryService bigQueryService;
  private final ParticipantCounter participantCounter;
  private final FieldSetQueryBuilder fieldSetQueryBuilder;
  private final AnnotationQueryBuilder annotationQueryBuilder;
  private final ParticipantCohortStatusDao participantCohortStatusDao;
  private final Provider<CdrBigQuerySchemaConfig> cdrSchemaConfigProvider;

  @Autowired
  public CohortMaterializationService(BigQueryService bigQueryService,
      ParticipantCounter participantCounter,
      FieldSetQueryBuilder fieldSetQueryBuilder,
      AnnotationQueryBuilder annotationQueryBuilder,
      ParticipantCohortStatusDao participantCohortStatusDao,
      Provider<CdrBigQuerySchemaConfig> cdrSchemaConfigProvider) {
    this.bigQueryService = bigQueryService;
    this.participantCounter = participantCounter;
    this.fieldSetQueryBuilder = fieldSetQueryBuilder;
    this.annotationQueryBuilder = annotationQueryBuilder;
    this.participantCohortStatusDao = participantCohortStatusDao;
    this.cdrSchemaConfigProvider = cdrSchemaConfigProvider;
  }

  private Set<Long> getParticipantIdsWithStatus(@Nullable CohortReview cohortReview, List<CohortStatus> statusFilter) {
    if (cohortReview == null) {
      return ImmutableSet.of();
    }
    Set<Long> participantIds = participantCohortStatusDao.findByParticipantKey_CohortReviewIdAndStatusIn(
        cohortReview.getCohortReviewId(), statusFilter)
        .stream()
        .map(ParticipantIdAndCohortStatus::getParticipantKey)
        .map(Key::getParticipantId)
        .collect(Collectors.toSet());
    return participantIds;
  }

  private ColumnConfig findPrimaryKey(TableConfig tableConfig) {
    for (ColumnConfig columnConfig : tableConfig.columns) {
      if (columnConfig.primaryKey != null && columnConfig.primaryKey) {
        return columnConfig;
      }
    }
    throw new IllegalStateException("Table lacks primary key!");
  }

  private TableQueryAndConfig getTableQueryAndConfig(FieldSet fieldSet) {
    TableQuery tableQuery;
    if (fieldSet == null) {
      tableQuery = new TableQuery();
      tableQuery.setTableName(PERSON_TABLE);
      tableQuery.setColumns(ImmutableList.of(PERSON_ID));
    } else {
      tableQuery = fieldSet.getTableQuery();
      if (tableQuery == null) {
        // TODO: support other kinds of field sets besides tableQuery
        throw new BadRequestException("tableQuery must be specified in field sets");
      }
      String tableName = tableQuery.getTableName();
      if (Strings.isNullOrEmpty(tableName)) {
        throw new BadRequestException("Table name must be specified in field sets");
      }
    }
    CdrBigQuerySchemaConfig cdrSchemaConfig = cdrSchemaConfigProvider.get();
    TableConfig tableConfig = cdrSchemaConfig.cohortTables.get(tableQuery.getTableName());
    if (tableConfig == null) {
      throw new BadRequestException("Table " + tableQuery.getTableName() + " is not a valid "
          + "cohort table; valid tables are: " +
          cdrSchemaConfig.cohortTables.keySet().stream().sorted().collect(Collectors.joining(",")));
    }
    Map<String, ColumnConfig> columnMap = Maps.uniqueIndex(tableConfig.columns,
        columnConfig -> columnConfig.name);

    List<String> columnNames = tableQuery.getColumns();
    if (columnNames == null || columnNames.isEmpty()) {
      // By default, return all columns on the table in question in our configuration.
      tableQuery.setColumns(columnMap.keySet().stream().collect(Collectors.toList()));
    }
    List<String> orderBy = tableQuery.getOrderBy();
    if (orderBy == null || orderBy.isEmpty()) {
      ColumnConfig primaryKey = findPrimaryKey(tableConfig);
      if (PERSON_ID.equals(primaryKey.name)) {
        tableQuery.setOrderBy(ImmutableList.of(PERSON_ID));
      } else {
        // TODO: consider having per-table default sort order based on e.g. timestamp
        tableQuery.setOrderBy(ImmutableList.of(PERSON_ID, primaryKey.name));
      }
    }
    return new TableQueryAndConfig(tableQuery, cdrSchemaConfig);
  }


  private ParticipantCriteria getParticipantCriteria(List<CohortStatus> statusFilter,
      @Nullable CohortReview cohortReview, SearchRequest searchRequest) {
    if (statusFilter.contains(CohortStatus.NOT_REVIEWED)) {
      Set<Long> participantIdsToExclude;
      if (statusFilter.size() < CohortStatus.values().length) {
        // Find the participant IDs that have statuses which *aren't* in the filter.
        Set<CohortStatus> statusesToExclude =
            Sets.difference(ImmutableSet.copyOf(CohortStatus.values()), ImmutableSet.copyOf(statusFilter));
        participantIdsToExclude = getParticipantIdsWithStatus(cohortReview, ImmutableList.copyOf(statusesToExclude));
      } else {
        participantIdsToExclude = ImmutableSet.of();
      }
      return new ParticipantCriteria(searchRequest, participantIdsToExclude);
    } else {
      Set<Long> participantIds = getParticipantIdsWithStatus(cohortReview, statusFilter);
      return new ParticipantCriteria(participantIds);
    }
  }

  public MaterializeCohortResponse materializeCohort(@Nullable CohortReview cohortReview,
      SearchRequest searchRequest, MaterializeCohortRequest request) {
    long offset = 0L;
    FieldSet fieldSet = request.getFieldSet();
    List<CohortStatus> statusFilter = request.getStatusFilter();
    String paginationToken = request.getPageToken();
    int pageSize = request.getPageSize();
    // TODO: add CDR version ID here
    Object[] paginationParameters = new Object[] { searchRequest, statusFilter };
    if (paginationToken != null) {
      PaginationToken token = PaginationToken.fromBase64(paginationToken);
      if (token.matchesParameters(paginationParameters)) {
        offset = token.getOffset();
      } else {
        throw new BadRequestException(
            String.format("Use of pagination token %s with new parameter values", paginationToken));
      }
    }
    int limit = pageSize + 1;

    MaterializeCohortResponse response = new MaterializeCohortResponse();
    Iterable<Map<String, Object>> results;
    if (statusFilter == null) {
      statusFilter = NOT_EXCLUDED;
    }
    if (fieldSet == null || fieldSet.getTableQuery() != null) {
      ParticipantCriteria criteria = getParticipantCriteria(statusFilter, cohortReview,
          searchRequest);
      if (criteria.getParticipantIdsToInclude() != null
          && criteria.getParticipantIdsToInclude().isEmpty()) {
        // There is no cohort review, or no participants matching the status filter;
        // return an empty response.
        return response;
      }
      results = fieldSetQueryBuilder.materializeTableQuery(getTableQueryAndConfig(fieldSet),
          criteria, limit, offset);
    } else if (fieldSet.getAnnotationQuery() != null) {
      if (cohortReview == null) {
        // There is no cohort review, so there are no annotations; return empty results.
        return response;
      }
      results = annotationQueryBuilder.materializeAnnotationQuery(cohortReview, statusFilter,
          fieldSet.getAnnotationQuery(), limit, offset);
    } else {
      throw new BadRequestException("Must specify tableQuery or annotationQuery");
    }
    int numResults = 0;
    boolean hasMoreResults = false;
    ArrayList<Object> responseResults = new ArrayList<>();
    for (Map<String, Object> resultMap : results) {
      if (numResults == pageSize) {
        hasMoreResults = true;
        break;
      }
      responseResults.add(resultMap);
      numResults++;
    }
    response.setResults(responseResults);
    if (hasMoreResults) {
      // TODO: consider pagination based on cursor / values rather than offset
      PaginationToken token = PaginationToken.of(offset + pageSize, paginationParameters);
      response.setNextPageToken(token.toBase64());
    }
    return response;
  }
}
