package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Digits;
import org.hibernate.validator.Range;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : AcademicManagement, 12-11-2009 09:54:04 AM
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Cycle.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "ciclo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "Cycle.findActiveByGestionAndPeriod", query = "select c from Cycle c where c.gestion=:gestion and c.cycleType=:cycleType and c.active=:active"),
                @NamedQuery(name = "Cycle.countActiveCycleBySector", query = "select count(c) from Cycle c join c.cycleType ct where ct.sector=:sector and c.active=:active"),
                @NamedQuery(name = "Cycle.findActiveCycleBySector", query = "select c from Cycle c join c.cycleType ct where ct.sector=:sector and c.active=:active"),
                @NamedQuery(name = "Cycle.findCycle", query = "select c from Cycle c where c.id=:id")

        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ciclo", uniqueConstraints = {@UniqueConstraint(columnNames = {"idcompania", "nombre"})})
public class Cycle implements BaseModel {
    @Id
    @Column(name = "idciclo", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Cycle.tableGenerator")
    private Long id;

    @Column(name = "nombre", length = 200, nullable = false)
    private String name;

    @Column(name = "fechainicio", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "fechafin", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @ManyToOne
    @JoinColumn(name = "idtipociclo", nullable = false)
    private CycleType cycleType;

    @ManyToOne
    @JoinColumn(name = "cicloraiz", nullable = true)
    private Cycle rootCycle;

    @OneToMany(mappedBy = "rootCycle", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<Cycle> cycleList = new ArrayList<Cycle>(0);

    @Column(name = "activo")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean active;

    @Column(name = "SEMANASLABORALES", precision = 13, scale = 2)
    @Digits(integerDigits = 13, fractionalDigits = 2)
    private BigDecimal laboralWeeks;

    @Column(name = "DIASLABORALES")
    @Range(min = 1)
    private Integer laboralDays;

    @Column(name = "TIPOCAMBIOME", precision = 13, scale = 2)
    @Digits(integerDigits = 13, fractionalDigits = 2)
    private BigDecimal exchangeRate;

    //Todo must be created the GUI for that relationship
    @OneToMany(mappedBy = "cycle", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<CycleMonthlyDistribution> cycleMonthlyDistributionList = new ArrayList<CycleMonthlyDistribution>(0);

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "idgestion", nullable = false)
    private Gestion gestion;

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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public CycleType getCycleType() {
        return cycleType;
    }

    public void setCycleType(CycleType cycleType) {
        this.cycleType = cycleType;
    }

    public Cycle getRootCycle() {
        return rootCycle;
    }

    public void setRootCycle(Cycle rootCycle) {
        this.rootCycle = rootCycle;
    }

    public List<Cycle> getCycleList() {
        return cycleList;
    }

    public void setCycleList(List<Cycle> cycleList) {
        this.cycleList = cycleList;
    }

    public BigDecimal getLaboralWeeks() {
        return laboralWeeks;
    }

    public void setLaboralWeeks(BigDecimal laboralWeeks) {
        this.laboralWeeks = laboralWeeks;
    }

    public Integer getLaboralDays() {
        return laboralDays;
    }

    public void setLaboralDays(Integer laboralDays) {
        this.laboralDays = laboralDays;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public List<CycleMonthlyDistribution> getCycleMonthlyDistributionList() {
        return cycleMonthlyDistributionList;
    }

    public void setCycleMonthlyDistributionList(List<CycleMonthlyDistribution> cycleMonthlyDistributionList) {
        this.cycleMonthlyDistributionList = cycleMonthlyDistributionList;
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

    public Gestion getGestion() {
        return gestion;
    }

    public void setGestion(Gestion gestion) {
        this.gestion = gestion;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
