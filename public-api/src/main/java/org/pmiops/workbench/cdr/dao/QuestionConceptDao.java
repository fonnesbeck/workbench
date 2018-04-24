package org.pmiops.workbench.cdr.dao;
import org.pmiops.workbench.cdr.model.QuestionConcept;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionConceptDao extends CrudRepository<QuestionConcept, Long> {



    @Query(nativeQuery=true, value="SELECT concept_id, concept_name, domain_id, vocabulary_id, concept_class_id, standard_concept, concept_code,valid_start_date, valid_end_date, invalid_reason, count_value, prevalence  from concept where concept_id=?1")
    List<QuestionConcept> findSurveyQuestions5(Integer concept_id);
}
