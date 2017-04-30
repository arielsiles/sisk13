package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;

/**
 * FinalEvaluationPunctuationRange
 *
 * @author
 * @version 2.7
 */

@NamedQueries({
        @NamedQuery(name = "FinalEvaluationPunctuationRange.countBetween",
                query = "select count(fepr) " +
                        " from FinalEvaluationPunctuationRange fepr " +
                        " where fepr.finalEvaluationForm=:finalEvaluationForm and " +
                        " fepr.startRange<=:rangeValue and fepr.endRange>=:rangeValue"),
        @NamedQuery(name = "FinalEvaluationPunctuationRange.countByPunctuationRangeBetween",
                query = "select count(fepr) " +
                        " from FinalEvaluationPunctuationRange fepr " +
                        " where fepr<>:punctuationRange and " +
                        " fepr.finalEvaluationForm=:finalEvaluationForm and " +
                        " fepr.startRange<=:rangeValue and fepr.endRange>=:rangeValue"),
        @NamedQuery(name = "FinalEvaluationPunctuationRange.countName",
                query = "select count(fepr) " +
                        " from FinalEvaluationPunctuationRange fepr " +
                        " where fepr.finalEvaluationForm=:finalEvaluationForm and " +
                        " fepr.name=:name"),
        @NamedQuery(name = "FinalEvaluationPunctuationRange.countByPunctuationRangeName",
                query = "select count(fepr) " +
                        " from FinalEvaluationPunctuationRange fepr " +
                        " where fepr<>:punctuationRange and " +
                        " fepr.finalEvaluationForm=:finalEvaluationForm and " +
                        " fepr.name=:name"),
        @NamedQuery(name = "FinalEvaluationPunctuationRange.countInterpretation",
                query = "select count(fepr) " +
                        " from FinalEvaluationPunctuationRange fepr " +
                        " where fepr.finalEvaluationForm=:finalEvaluationForm and " +
                        " fepr.interpretation=:interpretation"),
        @NamedQuery(name = "FinalEvaluationPunctuationRange.countByPunctuationRangeInterpretation",
                query = "select count(fepr) " +
                        " from FinalEvaluationPunctuationRange fepr " +
                        " where fepr<>:punctuationRange and " +
                        " fepr.finalEvaluationForm=:finalEvaluationForm and " +
                        " fepr.interpretation=:interpretation")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "FinalEvaluationPunctuationRange.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "rangopuntua",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "rangopuntua", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idformevalfinal", "nombre"}),
        @UniqueConstraint(columnNames = {"idformevalfinal", "interpretacion"})
})
public class FinalEvaluationPunctuationRange implements BaseModel {
    @Id
    @Column(name = "idrangopuntua", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FinalEvaluationPunctuationRange.tableGenerator")
    private Long id;

    @Column(name = "nombre", length = 250, nullable = false)
    @Length(max = 250)
    private String name;

    @Column(name = "interpretacion", length = 250, nullable = false)
    @Length(max = 250)
    private String interpretation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idformevalfinal", nullable = false)
    private FinalEvaluationForm finalEvaluationForm;

    @Column(name = "posicion", nullable = false)
    private Integer position;

    @Column(name = "iniciorango", nullable = false)
    private Integer startRange;

    @Column(name = "finrango", nullable = false)
    private Integer endRange;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    public FinalEvaluationForm getFinalEvaluationForm() {
        return finalEvaluationForm;
    }

    public void setFinalEvaluationForm(FinalEvaluationForm finalEvaluationForm) {
        this.finalEvaluationForm = finalEvaluationForm;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getStartRange() {
        return startRange;
    }

    public void setStartRange(Integer startRange) {
        this.startRange = startRange;
    }

    public Integer getEndRange() {
        return endRange;
    }

    public void setEndRange(Integer endRange) {
        this.endRange = endRange;
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

    @Override
    public String toString() {
        return "FinalEvaluationPunctuationRange{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", interpretation='" + interpretation + '\'' +
                ", position=" + position +
                ", startRange=" + startRange +
                ", endRange=" + endRange +
                ", version=" + version +
                ", company=" + company +
                '}';
    }
}
