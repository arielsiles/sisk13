package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @version 3.4
 */
@NamedQueries({
        @NamedQuery(name = "VacationGestion.findByVacationPlanningGestion",
                query = "select vacationGestion from VacationGestion vacationGestion" +
                        " where vacationGestion.vacationPlanning=:vacationPlanning and vacationGestion.gestion=:gestion"),
        @NamedQuery(name = "VacationGestion.findByVacationPlanningAvailableDaysOff",
                query = "select vacationGestion from VacationGestion vacationGestion" +
                        " where vacationGestion.vacationPlanning.id=:vacationPlanningId " +
                        " and vacationGestion.daysOff > 0 " +
                        " order by vacationGestion.gestion"),
        @NamedQuery(name = "VacationGestion.findByVacationPlanningOrdered",
                query = "select vacationGestion from VacationGestion vacationGestion" +
                        " where vacationGestion.vacationPlanning = :vacationPlanning" +
                        " order by vacationGestion.gestion asc"),
        @NamedQuery(name = "VacationGestion.sumVacationDaysByVacationPlanning",
                query = "select sum(vacationGestion.vacationDays) from VacationGestion vacationGestion" +
                        " where vacationGestion.vacationPlanning.id=:vacationPlanningId"),
        @NamedQuery(name = "VacationGestion.sumDaysUsedByVacationPlanning",
                query = "select sum(vacationGestion.daysUsed) from VacationGestion vacationGestion" +
                        " where vacationGestion.vacationPlanning.id=:vacationPlanningId"),
        @NamedQuery(name = "VacationGestion.sumDaysOffByVacationPlanning",
                query = "select sum(vacationGestion.daysOff) from VacationGestion vacationGestion" +
                        " where vacationGestion.vacationPlanning.id=:vacationPlanningId")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "VacationGestion.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "gestionvacacion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "gestionvacacion", schema = Constants.KHIPUS_SCHEMA)
public class VacationGestion implements BaseModel {

    @Id
    @Column(name = "idgestionvacacion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "VacationGestion.tableGenerator")
    private Long id;

    @Column(name = "diaslibres", nullable = false, precision = 7, scale = 2)
    @NotNull
    private BigDecimal daysOff;

    @Column(name = "diasusados", nullable = false, precision = 7, scale = 2)
    @NotNull
    private BigDecimal daysUsed;

    @Column(name = "diasvacacion", nullable = false, precision = 7, scale = 2)
    @NotNull
    private BigDecimal vacationDays;

    @Column(name = "gestion", nullable = false)
    @NotNull
    private Integer gestion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanvacacion", nullable = false)
    @NotNull
    private VacationPlanning vacationPlanning;

    @OneToMany(mappedBy = "vacationGestion", fetch = FetchType.LAZY)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<Vacation> vacationList = new ArrayList<Vacation>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getDaysOff() {
        return daysOff;
    }

    public void setDaysOff(BigDecimal daysOff) {
        this.daysOff = daysOff;
    }

    public BigDecimal getDaysUsed() {
        return daysUsed;
    }

    public void setDaysUsed(BigDecimal daysUsed) {
        this.daysUsed = daysUsed;
    }

    public BigDecimal getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(BigDecimal vacationDays) {
        this.vacationDays = vacationDays;
    }

    public Integer getGestion() {
        return gestion;
    }

    public void setGestion(Integer gestion) {
        this.gestion = gestion;
    }

    public VacationPlanning getVacationPlanning() {
        return vacationPlanning;
    }

    public void setVacationPlanning(VacationPlanning vacationPlanning) {
        this.vacationPlanning = vacationPlanning;
    }

    public List<Vacation> getVacationList() {
        return vacationList;
    }

    public void setVacationList(List<Vacation> vacationList) {
        this.vacationList = vacationList;
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
}
