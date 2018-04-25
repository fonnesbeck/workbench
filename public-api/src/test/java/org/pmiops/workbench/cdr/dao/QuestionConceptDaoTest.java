package org.pmiops.workbench.cdr.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pmiops.workbench.cdr.model.AchillesResult;
import org.pmiops.workbench.cdr.model.QuestionConcept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
@Import(LiquibaseAutoConfiguration.class)
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class QuestionConceptDaoTest {

    @Autowired
    QuestionConceptDao qDao;

    @Autowired
    AchillesResultDao arDao;

    private QuestionConcept obj1;
    private AchillesResult r1;
    private AchillesResult r2;

    private QuestionConcept createObj(int conceptId, String conceptName) {
        return new QuestionConcept()
                .conceptId(conceptId)
                .conceptName(conceptName)
                .conceptCode("PPI Whatever")
                .domainId("Observation")
                ;

    }


    @Before
    public void setUp() {
        String conceptId = "1586134";
        obj1 = createObj(Integer.parseInt(conceptId), "Test Question 1");

        qDao.save(obj1);
        r1 = resultObject(conceptId, "Answer 1", 50);
        arDao.save(r1);
        r2 = resultObject(conceptId, "Answer 2", 150);
        arDao.save(r2);


    }

    @Test
    public void findAllAnalyses() throws Exception {
        /* Todo write more tests */
        final List<QuestionConcept> list = qDao.findSurveyQuestions5(1586134);
        assert(obj1.getConceptId() == 1586134);
        assert(list.get(0).getConceptId() == obj1.getConceptId());
        assert(obj1.getAnswers().get(0).getCountValue() == 50);
        System.out.println(obj1.getAnswers().get(0).toString());
    }

    private AchillesResult resultObject(String conceptId, String name, long countValue) {
        long aid = 3110;
        return new AchillesResult()
                .analysisId(aid)
                .stratum1("34343")
                .stratum2(conceptId)
                .stratum3("3333")
                .stratum4(name)
                .stratum5("")
                .countValue(countValue);
    }


}
