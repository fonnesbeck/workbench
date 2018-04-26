package org.pmiops.workbench.cdr.dao;

import org.pmiops.workbench.cdr.model.ConceptCriteria;
import org.pmiops.workbench.cdr.model.CriteriaConcept;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CriteriaConceptDao extends CrudRepository<CriteriaConcept, Long> {

  @Query(value =
    "select concept_id, " +
      "       concept_name, " +
      "       1 as isGroup " +
      "from concept_ancestor a " +
      "join concept b on a.ancestor_concept_id = b.concept_id " +
      "where descendant_concept_id in " +
      "        (select concept_id " +
      "          from concept " +
      "         where standard_concept in ('S','C') " +
      "           and domain_id = :domainId " +
      "           and match(concept_name) against(:value in boolean mode)) " +
      "order by max_levels_of_separation desc limit 1", nativeQuery = true)
  List<ConceptCriteria> findConceptCriteriaParent(@Param("domainId") String domainId,
                                                  @Param("value") String value);

  @Query(value =
    "select concept_id, concept_name, 1 as isGroup " +
      "          from concept " +
      "         where standard_concept in ('S','C') " +
      "           and domain_id = :domainId " +
      "           and match(concept_name) against(:value in boolean mode)", nativeQuery = true)
  List<ConceptCriteria> findConceptCriteriaParent1(@Param("domainId") String domainId,
                                                   @Param("value") String value);

  @Query(value =
    "select b.concept_id, " +
      "       b.concept_name, " +
      "       case " +
      "           when c.concept_id_1 is null then 0 " +
      "           else 1 " +
      "       end as is_group " +
      "from concept_relationship a " +
      "left join concept b on a.concept_id_2 = b.concept_id " +
      "left join " +
      "  (select a.concept_id_1, " +
      "          count(*) " +
      "     from concept_relationship a " +
      "     join concept b on a.concept_id_2 = b.concept_id " +
      "     join concept_relationship c on c.concept_id_2 = a.concept_id_1" +
      "     where a.relationship_id = 'Subsumes' " +
      "     and c.concept_id_1 = :conceptId " +
      "     group by a.concept_id_1) c on a.concept_id_2 = c.concept_id_1 " +
      "where a.concept_id_1 = :conceptId " +
      "  and relationship_id = 'Subsumes' " +
      "  and concept_id_2 IN " +
      "    (select ancestor_concept_id " +
      "       from concept_ancestor a " +
      "       left join concept b on a.ancestor_concept_id = b.concept_id " +
      "       where descendant_concept_id in " +
      "           (select concept_id " +
      "              from concept " +
      "             where standard_concept IN ('S','C') " +
      "               and domain_Id = :domainId " +
      "               and concept_name regexp :value" +
      "               and concept_id is not null) )", nativeQuery = true)
  List<ConceptCriteria> findConceptCriteriaChildren(@Param("conceptId") Long conceptId,
                                                    @Param("domainId") String domainId,
                                                    @Param("value") String value);

  @Query(value =
    "select concept_id as conceptId, " +
      "       concept_name as conceptName, " +
      "       max_levels_of_separation as isGroup " +
      "from concept_ancestor a " +
      "join concept b on a.ancestor_concept_id = b.concept_id " +
      "where descendant_concept_id in " +
      "        (select concept_id " +
      "          from concept " +
      "         where standard_concept in ('S','C') " +
      "           and domain_id = :domainId " +
      "           and concept_id = :conceptId) " +
      "order by max_levels_of_separation desc", nativeQuery = true)
  List<ConceptCriteria> findConceptCriteriaChildren1(@Param("conceptId") Long conceptId,
                                                     @Param("domainId") String domainId);
}
