package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
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
        pkColumnValue = "GESTIONVACACION",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "GESTIONVACACION", schema = Constants.KHIPUS_SCHEMA)
public class VacationGestion implements BaseModel {

    @Id
    @Column(name = "IDGESTIONVACACION", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "VacationGestion.tableGenerator")
    private Long id;

    @Column(name = "DIASLIBRES", nullable = false)
    @NotNull
    private Integer daysOff;

    @Column(name = "DIASUSADOS", nullable = false)
    @NotNull
    private Integer daysUsed;

    @Column(name = "DIASVACACION", nullable = false)
    @NotNull
    private Integer vacationDays;

    @Column(name = "GESTION", nullable = false)
    @NotNull
    private Integer gestion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPLANVACACION", nullable = false)
    @NotNull
    private VacationPlanning vacationPlanning;

    @OneToMany(mappedBy = "vacationGestion", fetch = FetchType.LAZY)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<Vacation> vacationList = new ArrayList<Vacation>(0);

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

    public Integer getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(Integer vacationDays) {
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
