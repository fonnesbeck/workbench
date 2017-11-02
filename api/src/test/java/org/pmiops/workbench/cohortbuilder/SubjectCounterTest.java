package org.pmiops.workbench.cohortbuilder;

import com.google.cloud.bigquery.QueryJobConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pmiops.workbench.exceptions.BadRequestException;
import org.pmiops.workbench.model.SearchGroup;
import org.pmiops.workbench.model.SearchGroupItem;
import org.pmiops.workbench.model.SearchParameter;
import org.pmiops.workbench.model.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@Import({SubjectCounter.class, QueryBuilderFactory.class})
@ComponentScan(basePackages = "org.pmiops.workbench.cohortbuilder.*")
public class SubjectCounterTest {

    @Autowired
    SubjectCounter subjectCounter;

    @Test
    public void buildSubjectCounterQuery_BothIncludesAndExcludesEmpty() throws Exception {

        try {
            subjectCounter.buildSubjectCounterQuery(new SearchRequest());
        } catch (BadRequestException e) {
            assertEquals("Invalid SearchRequest: includes[] and excludes[] cannot both be empty", e.getMessage());
        }
    }

    @Test
    public void buildSubjectCounterQuery_ExcludesWithoutIncludes() throws Exception {

        String genderNamedParameter = "";
        SearchParameter parameter1 = new SearchParameter()
                .subtype("GEN")
                .conceptId(8507L);

        SearchGroupItem searchGroupItem1 = new SearchGroupItem()
                .type("DEMO")
                .addSearchParametersItem(parameter1);

        SearchGroup searchGroup1 = new SearchGroup()
                .addItemsItem(searchGroupItem1);

        SearchRequest request = new SearchRequest()
                .addExcludesItem(searchGroup1);

        QueryJobConfiguration actualRequest = subjectCounter.buildSubjectCounterQuery(request);

        for (String key : actualRequest.getNamedParameters().keySet()) {
            if (key.startsWith("gen")) {
                genderNamedParameter = key;
            }
        }

        final String expectedSql = "select count(distinct person_id) as count\n" +
                "from `${projectId}.${dataSetId}.person` person\n" +
                "where\n" +
                "person.person_id in (select distinct person_id\n" +
                "from `${projectId}.${dataSetId}.person` p\n" +
                "where p.gender_concept_id = @" + genderNamedParameter + "\n" +
                ")\n";

        assertEquals(expectedSql, actualRequest.getQuery());

        assertEquals("8507",
                actualRequest
                        .getNamedParameters()
                        .get(genderNamedParameter)
                        .getValue());
    }

    @Test
    public void buildSubjectCounterQuery() throws Exception {

        String genderNamedParameter = "";
        String conditionNamedParameter = "";
        String procedureNamedParameter = "";
        String cmICD9NamedParameter = "";
        String procICD9NamedParameter = "";
        String cmCPTNamedParameter = "";
        String procCPTNamedParameter = "";

        SearchParameter parameter1 = new SearchParameter()
                .domain("Condition")
                .value("001.1");
        SearchParameter parameter2 = new SearchParameter()
                .subtype("GEN")
                .conceptId(8507L);
        SearchParameter parameter3 = new SearchParameter()
                .domain("Procedure")
                .value("001.2");

        SearchGroupItem searchGroupItem1 = new SearchGroupItem()
                .type("ICD9")
                .addSearchParametersItem(parameter1);
        SearchGroupItem searchGroupItem2 = new SearchGroupItem()
                .type("DEMO")
                .addSearchParametersItem(parameter2);
        SearchGroupItem searchGroupItem3 = new SearchGroupItem()
                .type("CPT")
                .addSearchParametersItem(parameter3);

        SearchGroup searchGroup1 = new SearchGroup()
                .addItemsItem(searchGroupItem1);
        SearchGroup searchGroup2 = new SearchGroup()
                .addItemsItem(searchGroupItem2);
        SearchGroup searchGroup3 = new SearchGroup()
                .addItemsItem(searchGroupItem3);

        SearchRequest request = new SearchRequest()
                .addIncludesItem(searchGroup1)
                .addIncludesItem(searchGroup2)
                .addExcludesItem(searchGroup3);

        QueryJobConfiguration actualRequest = subjectCounter.buildSubjectCounterQuery(request);

        for (String key : actualRequest.getNamedParameters().keySet()) {
            if (key.startsWith("gen")) {
                genderNamedParameter = key;
            } else if (key.startsWith("Condition")){
                conditionNamedParameter = key;
            } else if (key.startsWith("Procedure")) {
                procedureNamedParameter = key;
            } else if (key.startsWith("cmICD9")) {
                cmICD9NamedParameter = key;
            } else if (key.startsWith("procICD9")) {
                procICD9NamedParameter = key;
            } else if (key.startsWith("cmCPT")) {
                cmCPTNamedParameter = key;
            } else if (key.startsWith("procCPT")) {
                procCPTNamedParameter = key;
            }
        }

        final String expectedSql = "select count(distinct person_id) as count\n" +
                "from `${projectId}.${dataSetId}.person` person\n" +
                "where\n" +
                "person.person_id in (select person_id\n" +
                "from `${projectId}.${dataSetId}.person` p\n" +
                "where person_id in (select distinct person_id\n" +
                "from `${projectId}.${dataSetId}.condition_occurrence` a, `${projectId}.${dataSetId}.concept` b\n" +
                "where a.condition_source_concept_id = b.concept_id\n" +
                "and b.vocabulary_id in (@" + cmICD9NamedParameter + ",@" + procICD9NamedParameter + ")\n" +
                "and b.concept_code in unnest(@" + conditionNamedParameter + ")\n" +
                ")\n" +
                ")\n" +
                "and person.person_id in (select distinct person_id\n" +
                "from `${projectId}.${dataSetId}.person` p\n" +
                "where p.gender_concept_id = @" + genderNamedParameter + "\n" +
                ")\n" +
                "and not exists\n" +
                "(select 'x' from\n" +
                "(select person_id\n" +
                "from `${projectId}.${dataSetId}.person` p\n" +
                "where person_id in (select distinct person_id\n" +
                "from `${projectId}.${dataSetId}.procedure_occurrence` a, `${projectId}.${dataSetId}.concept` b\n" +
                "where a.procedure_source_concept_id = b.concept_id\n" +
                "and b.vocabulary_id in (@" + cmCPTNamedParameter + ",@" + procCPTNamedParameter + ")\n" +
                "and b.concept_code in unnest(@" + procedureNamedParameter + ")\n" +
                ")\n" +
                ")\n" +
                "x where x.person_id = person.person_id)\n";

        assertEquals(expectedSql, actualRequest.getQuery());

        assertEquals("8507",
                actualRequest
                        .getNamedParameters()
                        .get(genderNamedParameter)
                        .getValue());

        assertEquals(parameter1.getValue(),
                actualRequest
                        .getNamedParameters()
                        .get(conditionNamedParameter)
                        .getArrayValues().get(0).getValue());
    }

}
