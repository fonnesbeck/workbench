package org.pmiops.workbench.cohorts;

import com.google.common.collect.ImmutableMap;
import org.bitbucket.radistao.test.runner.BeforeAfterSpringTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pmiops.workbench.api.BigQueryBaseTest;
import org.pmiops.workbench.api.BigQueryService;
import org.pmiops.workbench.api.DomainLookupService;
import org.pmiops.workbench.cdr.CdrVersionContext;
import org.pmiops.workbench.cdr.dao.CriteriaDao;
import org.pmiops.workbench.cdr.model.Criteria;
import org.pmiops.workbench.cohortbuilder.ParticipantCounter;
import org.pmiops.workbench.cohortbuilder.QueryBuilderFactory;
import org.pmiops.workbench.cohortbuilder.querybuilder.DemoQueryBuilder;
import org.pmiops.workbench.db.model.CdrVersion;
import org.pmiops.workbench.exceptions.BadRequestException;
import org.pmiops.workbench.model.MaterializeCohortResponse;
import org.pmiops.workbench.test.SearchRequests;
import org.pmiops.workbench.testconfig.TestJpaConfig;
import org.pmiops.workbench.testconfig.TestWorkbenchConfig;
import org.pmiops.workbench.utils.PaginationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

@RunWith(BeforeAfterSpringTestRunner.class)
@Import({DemoQueryBuilder.class, QueryBuilderFactory.class, CohortMaterializationService.class,
        BigQueryService.class, ParticipantCounter.class, DomainLookupService.class,
        QueryBuilderFactory.class, TestJpaConfig.class})
@ComponentScan(basePackages = "org.pmiops.workbench.cohortbuilder.*")
public class CohortMaterializationServiceTest extends BigQueryBaseTest {

  @Autowired
  private CohortMaterializationService cohortMaterializationService;

  private CdrVersion cdrVersion = new CdrVersion();

  @Autowired
  private TestWorkbenchConfig testWorkbenchConfig;

  @Autowired
  private CriteriaDao criteriaDao;

  @Before
  public void setUp() {
    cdrVersion = new CdrVersion();
    cdrVersion.setBigqueryDataset(testWorkbenchConfig.bigquery.dataSetId);
    cdrVersion.setBigqueryProject(testWorkbenchConfig.bigquery.projectId);
    CdrVersionContext.setCdrVersion(cdrVersion);

    Criteria icd9CriteriaGroup =
            new Criteria().group(true)
                    .name("group")
                    .selectable(true)
                    .code(SearchRequests.ICD9_GROUP_CODE)
                    .type(SearchRequests.ICD9_TYPE)
                    .parentId(0);
    criteriaDao.save(icd9CriteriaGroup);
    Criteria icd9CriteriaChild =
            new Criteria().group(false)
                    .name("child")
                    .selectable(true)
                    .code(SearchRequests.ICD9_GROUP_CODE + ".1")
                    .type(SearchRequests.ICD9_TYPE)
                    .domainId("Condition")
                    .parentId(icd9CriteriaGroup.getId());
    criteriaDao.save(icd9CriteriaChild);
  }

  @Override
  public List<String> getTableNames() {
    return Arrays.asList(
            "person",
            "concept",
            "condition_occurrence");
  }

  @Test
  public void testMaterializeCohortOneMale() {
    MaterializeCohortResponse response = cohortMaterializationService.materializeCohort(cdrVersion,
        SearchRequests.males(),null, 1000, null);
    assertPersonIds(response, 1L);
    assertThat(response.getNextPageToken()).isNull();
  }

  @Test
  public void testMaterializeCohortICD9Group() {
    MaterializeCohortResponse response = cohortMaterializationService.materializeCohort(cdrVersion,
            SearchRequests.icd9Codes(),null, 1000, null);
    assertPersonIds(response, 1L);
    assertThat(response.getNextPageToken()).isNull();
  }

  @Test
  public void testMaterializeCohortPaging() {
    MaterializeCohortResponse response = cohortMaterializationService.materializeCohort(cdrVersion,
        SearchRequests.allGenders(),null, 2, null);
    assertPersonIds(response, 1L, 2L);
    assertThat(response.getNextPageToken()).isNotNull();
    MaterializeCohortResponse response2 = cohortMaterializationService.materializeCohort(cdrVersion,
        SearchRequests.allGenders(),null, 2, response.getNextPageToken());
    assertPersonIds(response2, 102246L);
    assertThat(response2.getNextPageToken()).isNull();

    try {
      // Pagination token doesn't match, this should fail.
      cohortMaterializationService.materializeCohort(cdrVersion, SearchRequests.males(),
          null, 2, response.getNextPageToken());
      fail("Exception expected");
    } catch (BadRequestException e) {
      // expected
    }

    PaginationToken token = PaginationToken.fromBase64(response.getNextPageToken());
    PaginationToken invalidToken = new PaginationToken(-1L, token.getParameterHash());
    try {
      // Pagination token doesn't match, this should fail.
      cohortMaterializationService.materializeCohort(cdrVersion, SearchRequests.males(),
          null, 2, invalidToken.toBase64());
      fail("Exception expected");
    } catch (BadRequestException e) {
      // expected
    }
  }

  private void assertPersonIds(MaterializeCohortResponse response, long... personIds) {
    List<Object> expectedResults = new ArrayList<>();
    for (long personId : personIds) {
      expectedResults.add(ImmutableMap.of(CohortMaterializationService.PERSON_ID, personId));
    }
    assertThat(response.getResults()).isEqualTo(expectedResults);
  }
}
