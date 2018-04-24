package org.pmiops.workbench.cdr.model;


import org.pmiops.workbench.cdr.model.AchillesResult;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import java.util.List;


@Entity
//TODO need to add a way to dynamically switch between database versions
//this dynamic connection will eliminate the need for the catalog attribute
@Table(name = "concept")
public class QuestionConcept extends Concept {

    public QuestionConcept() {
        super();
        System.out.println("Question Concept Constructor called");

    }
    private List<AchillesResult> answers;


    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name="stratum_2")
    public List<AchillesResult> getAnswers() {
        return answers;
    }
    public void setAnswers(List<AchillesResult> answers) {
        this.answers = answers;
    }
    public QuestionConcept answers(List<AchillesResult> answers) {
        this.answers = answers;
        return this;
    }

}
