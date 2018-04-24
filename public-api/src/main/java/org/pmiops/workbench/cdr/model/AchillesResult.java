package org.pmiops.workbench.cdr.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;


@Entity
//TODO need to add a way to dynamically switch between database versions
//this dynamic connection will eliminate the need for the catalog attribute
@Table(name = "achilles_results")
public class AchillesResult  {

    private Long id;
    private Long analysisId;
    private String stratum1;
    private String stratum2;
    private String stratum3;
    private String stratum4;
    private String stratum5;
    private Long countValue;

    @Id
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public AchillesResult id(Long val) {
        this.id = val;
        return this;
    }

    @Column(name="analysis_id")
    public Long getAnalysisId() {
        return analysisId;
    }
    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }
    public AchillesResult analysisId(Long val) {
        this.analysisId = val;
        return this;
    }

    @Column(name="stratum_1")
    public String getStratum1() {
        return stratum1;
    }
    public void setStratum1(String stratum1) {
        this.stratum1 = stratum1;
    }
    public AchillesResult stratum1(String val) {
        this.stratum1 = val;
        return this;
    }

    @Column(name="stratum_2")
    public String getStratum2() {
        return stratum2;
    }
    public void setStratum2(String stratum2) {
        this.stratum2 = stratum2;
    }
    public AchillesResult stratum2(String val) {
        this.stratum2 = val;
        return this;
    }

    @Column(name="stratum_3")
    public String getStratum3() {
        return stratum3;
    }
    public void setStratum3(String stratum3) {
        this.stratum3 = stratum3;
    }
    public AchillesResult stratum3(String val) {
        this.stratum3 = val;
        return this;
    }

    @Column(name="stratum_4")
    public String getStratum4() {
        return stratum4;
    }
    public void setStratum4(String stratum4) {
        this.stratum4 = stratum4;
    }
    public AchillesResult stratum4(String val) {
        this.stratum4 = val;
        return this;
    }

    @Column(name="stratum_5")
    public String getStratum5() {
        return stratum5;
    }
    public void setStratum5(String stratum5) {
        this.stratum5 = stratum5;
    }
    public AchillesResult stratum5(String val) {
        this.stratum5 = val;
        return this;
    }

    @Column(name="count_value")
    public Long getCountValue() {
        return countValue;
    }
    public void setCountValue(Long countValue) {
        this.countValue = countValue;
    }
    public AchillesResult countValue(Long val) {
        this.countValue = val;
        return this;
    }

}
