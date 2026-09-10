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
import java.math.BigDecimal;
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
        pkColumnValue = "planvacacion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "planvacacion", schema = Constants.KHIPUS_SCHEMA, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo", "idcompania"}),
        @UniqueConstraint(columnNames = {"idplanvacacion", "idcompania"}),
        @UniqueConstraint(columnNames = {"idcompania", "idcontractopuesto"})
})
public class VacationPlanning implements BaseModel {

    @Id
    @Column(name = "idplanvacacion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "VacationPlanning.tableGenerator")
    private Long id;

    @Column(name = "codigo", nullable = false)
    @NotNull
    private Long code;

    @Column(name = "aniosantiguedad", nullable = false)
    @NotNull
    private Integer seniorityYears;

    @Column(name = "diasvacacion", nullable = false, precision = 7, scale = 2)
    @NotNull
    private BigDecimal vacationDays;

    @Column(name = "diaslibres", nullable = false, precision = 7, scale = 2)
    @NotNull
    private BigDecimal daysOff;

    @Column(name = "diasusados", nullable = false, precision = 7, scale = 2)
    @NotNull
    private BigDecimal daysUsed;

    @Column(name = "fechainicio", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date initDate;

    @OneToMany(mappedBy = "vacationPlanning", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<VacationGestion> vacationGestionList = new ArrayList<VacationGestion>(0);

    /**
     * El dia en que este plan dejo de devengar, que es el ultimo dia de trabajo del contrato.
     * Nula mientras el periodo sigue abierto.
     * <p/>
     * Sin esta fecha el devengo cuenta anios contra el dia de hoy y no mira el contrato: el plan
     * de alguien que se fue en 2024 seguiria sumando anios en 2030, generando derecho a
     * vacaciones para una relacion laboral que ya no existe.
     */
    @Column(name = "fechacierre")
    @Temporal(TemporalType.DATE)
    private Date closeDate;

    /* Fecha del saldo inicial declarado. Cuando esta seteada el sistema NO devenga nada
       anterior: ese saldo ya contiene todo lo anterior. Nula = el sistema devenga desde el
       inicio del contrato, y el saldo sale de cargar las vacaciones consumidas. */
    @Column(name = "fechasaldoinicial")
    @Temporal(TemporalType.DATE)
    private Date openingDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontractopuesto", nullable = false)
    @NotNull
    private JobContract jobContract;

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

    public BigDecimal getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(BigDecimal vacationDays) {
        this.vacationDays = vacationDays;
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

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    /** Un plan cerrado corresponde a un periodo terminado: no devenga mas. */
    public boolean isClosed() {
        return null != closeDate;
    }

    /**
     * Hasta que fecha devenga este plan: hoy si sigue abierto, y el dia del cierre si termino.
     * Es el unico lugar donde se decide, para que la pantalla y el proceso no puedan discrepar.
     */
    public Date getAccrualLimit() {
        Date today = new Date();
        return (null != closeDate && closeDate.before(today)) ? closeDate : today;
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(Date openingDate) {
        this.openingDate = openingDate;
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
