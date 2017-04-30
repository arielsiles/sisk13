package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EvaluationCriteria entity
 *
 * @author
 * @version : 1.0.18
 */
@NamedQueries({
        @NamedQuery(name = "EvaluationCriteria.findDistinctsOnQuestionsByPollForm",
                query = "select distinct new EvaluationCriteria(q.evaluationCriteria) from Question q where q.section.pollForm=:pollForm")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "EvaluationCriteria.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "criterioevaluacion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)


@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "criterioevaluacion", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nombre"}))
public class EvaluationCriteria implements BaseModel {

    @Id
    @Column(name = "idcriterioevaluacion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "EvaluationCriteria.tableGenerator")
    private Long id;

    @Column(name = "nombre", length = 250, nullable = false)
    private String name;

    @Column(name = "descripcion", nullable = false)
    @Lob
    private String description;

    @OneToMany(mappedBy = "evaluationCriteria", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("sequence asc")
    private List<EvaluationCriteriaValue> evaluationCriteriaValueList = new ArrayList<EvaluationCriteriaValue>(0);

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public EvaluationCriteria() {

    }

    public EvaluationCriteria(EvaluationCriteria evaluationCriteria) {
        setId(evaluationCriteria.getId());
        setName(evaluationCriteria.getName());
        setEvaluationCriteriaValueList(evaluationCriteria.getEvaluationCriteriaValueList());
        setVersion(evaluationCriteria.getVersion());
        setCompany(evaluationCriteria.getCompany());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<EvaluationCriteriaValue> getEvaluationCriteriaValueList() {
        return evaluationCriteriaValueList;
    }

    public void setEvaluationCriteriaValueList(List<EvaluationCriteriaValue> evaluationCriteriaValueList) {
        this.evaluationCriteriaValueList = evaluationCriteriaValueList;
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
