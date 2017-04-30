package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Encens Team
 *
 * @author
 * @version : PollPunctuation, 26-10-2009 09:08:21 PM
 */

@NamedQueries(
        {
                @NamedQuery(name = "PollPunctuation.deleteByPollCopy", query = "delete from PollPunctuation pp " +
                        "where pp.pollCopy=:pollCopy")}
)


@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PollPunctuation.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "puntuacion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "puntuacion")
public class PollPunctuation implements BaseModel {
    @Id
    @Column(name = "idpuntuacion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PollPunctuation.tableGenerator")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idejemplarencuesta", nullable = false)
    private PollCopy pollCopy;

    @ManyToOne
    @JoinColumn(name = "idpregunta", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "idvalorcriterioevaluacion", nullable = false)
    private EvaluationCriteriaValue evaluationCriteriaValue;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;


    public PollPunctuation() {
    }

    public PollPunctuation(PollCopy pollCopy, Question question, EvaluationCriteriaValue evaluationCriteriaValue) {
        this.pollCopy = pollCopy;
        this.question = question;
        this.evaluationCriteriaValue = evaluationCriteriaValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PollCopy getPollCopy() {
        return pollCopy;
    }

    public void setPollCopy(PollCopy pollCopy) {
        this.pollCopy = pollCopy;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public EvaluationCriteriaValue getEvaluationCriteriaValue() {
        return evaluationCriteriaValue;
    }

    public void setEvaluationCriteriaValue(EvaluationCriteriaValue evaluationCriteriaValue) {
        this.evaluationCriteriaValue = evaluationCriteriaValue;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
