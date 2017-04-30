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
 * @version : EvaluationCriteriaValue, 21-10-2009 12:11:58 PM
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "EvaluationCriteria.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "valorcriterioevaluacion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)


@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "valorcriterioevaluacion", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcriterioevaluacion", "indice"}),
        @UniqueConstraint(columnNames = {"idcriterioevaluacion", "valor"}),
        @UniqueConstraint(columnNames = {"idcriterioevaluacion", "titulo"})
})
public class EvaluationCriteriaValue implements BaseModel {
    @Id
    @Column(name = "idvalorcriterioevaluacion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "EvaluationCriteria.tableGenerator")
    private Long id;

    @Column(name = "indice", nullable = false)
    private Integer sequence;

    @Column(name = "valor", nullable = false)
    private Integer value;

    @Column(name = "titulo", length = 250, nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "idcriterioevaluacion", nullable = false)
    private EvaluationCriteria evaluationCriteria;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EvaluationCriteria getEvaluationCriteria() {
        return evaluationCriteria;
    }

    public void setEvaluationCriteria(EvaluationCriteria evaluationCriteria) {
        this.evaluationCriteria = evaluationCriteria;
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

    public String getFullName() {
        return sequence + " " + title;
    }
}
