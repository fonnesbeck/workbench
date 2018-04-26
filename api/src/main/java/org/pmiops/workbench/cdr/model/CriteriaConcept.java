package org.pmiops.workbench.cdr.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "concept")
public class CriteriaConcept {

  private long conceptId;
  private String conceptName;
  private boolean group;
  private String count;
  private String domainId;

  @Column(name = "concept_id")
  public long getConceptId() {
    return conceptId;
  }

  public void setConceptId(long conceptId) {
    this.conceptId = conceptId;
  }

  public CriteriaConcept conceptId(long conceptId) {
    this.conceptId = conceptId;
    return this;
  }

  @Column(name = "concept_name")
  public String getConceptName() {
    return conceptName;
  }

  public void setConceptName(String conceptName) {
    this.conceptName = conceptName;
  }

  public CriteriaConcept conceptName(String conceptName) {
    this.conceptName = conceptName;
    return this;
  }

  @Column(name = "isGroup")
  public boolean getGroup() {
    return group;
  }

  public void setGroup(boolean group) {
    this.group = group;
  }

  public CriteriaConcept group(boolean group) {
    this.group = group;
    return this;
  }

  @Column(name = "count")
  public boolean getGroup() {
    return group;
  }

  public void setGroup(boolean group) {
    this.group = group;
  }

  public CriteriaConcept group(boolean group) {
    this.group = group;
    return this;
  }

}
