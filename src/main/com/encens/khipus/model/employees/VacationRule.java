package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * @author
 * @version 3.4
 */

@NamedQueries({
        @NamedQuery(name = "VacationRule.findAllOrderByCode",
                query = "select vacationRule from VacationRule vacationRule" +
                        " order by vacationRule.code desc "),
        @NamedQuery(name = "VacationRule.findBySeniorityYear",
                query = "select vacationRule from VacationRule vacationRule" +
                        " where (:seniorityYear >= vacationRule.fromYears and :seniorityYear <= vacationRule.toYears)" +
                        " or (:seniorityYear >= vacationRule.fromYears and vacationRule.toYears IS NULL)"),
        @NamedQuery(name = "VacationRule.findByRangeOverlap",
                query = "select vacationRule from VacationRule vacationRule" +
                        " where vacationRule.id <> :vacationRuleId " +
                        " and (vacationRule.toYears >= :fromYears or  vacationRule.toYears IS NULL)")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "VacationRule.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "REGLAVACACION",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "REGLAVACACION", schema = Constants.KHIPUS_SCHEMA, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"CODIGO", "IDCOMPANIA"}),
        @UniqueConstraint(columnNames = {"NOMBRE", "IDCOMPANIA"})})
public class VacationRule implements BaseModel {
    @Id
    @Column(name = "IDREGLAVACACION", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "VacationRule.tableGenerator")
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    @NotNull
    private Long code;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    @NotEmpty
    @Length(max = 100)
    private String name;

    @Column(name = "DIASVACACION", nullable = false)
    @NotNull
    private Integer vacationDays;

    @Column(name = "ANIOSINICIO", nullable = false)
    @NotNull
    private Integer fromYears;

    @Column(name = "ANIOSFIN", nullable = true)
    private Integer toYears;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(Integer vacationDays) {
        this.vacationDays = vacationDays;
    }

    public Integer getFromYears() {
        return fromYears;
    }

    public void setFromYears(Integer fromYears) {
        this.fromYears = fromYears;
    }

    public Integer getToYears() {
        return toYears;
    }

    public void setToYears(Integer toYears) {
        this.toYears = toYears;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getFullName() {
        return FormatUtils.toCodeName(getCode(), getName());
    }
}
