package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @version 3.4
 */

@NamedQueries({
        @NamedQuery(name = "VacationPlanning.load", query = "select vacationPlanning from VacationPlanning vacationPlanning" +
                " left join fetch vacationPlanning.jobContract jobContract " +
                " left join fetch jobContract.contract contract" +
                " left join fetch contract.employee employee" +
                " where vacationPlanning.id=:id")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "VacationPlanning.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "PLANVACACION",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "PLANVACACION", schema = Constants.KHIPUS_SCHEMA, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"CODIGO", "IDCOMPANIA"}),
        @UniqueConstraint(columnNames = {"IDPLANVACACION", "IDCOMPANIA"}),
        @UniqueConstraint(columnNames = {"IDCOMPANIA", "IDCONTRACTOPUESTO"})
})
public class VacationPlanning implements BaseModel {

    @Id
    @Column(name = "IDPLANVACACION", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "VacationPlanning.tableGenerator")
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    @NotNull
    private Long code;

    @Column(name = "ANIOSANTIGUEDAD", nullable = false)
    @NotNull
    private Integer seniorityYears;

    @Column(name = "DIASVACACION", nullable = false)
    @NotNull
    private Integer vacationDays;

    @Column(name = "DIASLIBRES", nullable = false)
    @NotNull
    private Integer daysOff;

    @Column(name = "DIASUSADOS", nullable = false)
    @NotNull
    private Integer daysUsed;

    @Column(name = "FECHAINICIO", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date initDate;

    @OneToMany(mappedBy = "vacationPlanning", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<VacationGestion> vacationGestionList = new ArrayList<VacationGestion>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCONTRACTOPUESTO", nullable = false)
    @NotNull
    private JobContract jobContract;

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

    public Integer getSeniorityYears() {
        return seniorityYears;
    }

    public void setSeniorityYears(Integer seniorityYears) {
        this.seniorityYears = seniorityYears;
    }

    public Integer getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(Integer vacationDays) {
        this.vacationDays = vacationDays;
    }

    public Integer getDaysOff() {
        return daysOff;
    }

    public void setDaysOff(Integer daysOff) {
        this.daysOff = daysOff;
    }

    public Integer getDaysUsed() {
        return daysUsed;
    }

    public void setDaysUsed(Integer daysUsed) {
        this.daysUsed = daysUsed;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public JobContract getJobContract() {
        return jobContract;
    }

    public void setJobContract(JobContract jobContract) {
        this.jobContract = jobContract;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<VacationGestion> getVacationGestionList() {
        return vacationGestionList;
    }

    public void setVacationGestionList(List<VacationGestion> vacationGestionList) {
        this.vacationGestionList = vacationGestionList;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getFullName() {
        return FormatUtils.toCodeName(getCode(), getJobContract().getContract().getEmployee().getFullName());
    }
}
