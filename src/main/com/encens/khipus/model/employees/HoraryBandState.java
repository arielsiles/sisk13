package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.OrganizationalUnit;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Holds information about HoraryBand status by date
 *
 * @author
 * @version 3.0
 */
@NamedQueries({
        @NamedQuery(name = "HoraryBandState.findByDateAndHoraryBand",
                query = "select horaryBandState from HoraryBandState horaryBandState " +
                        "where horaryBandState.date=:date and horaryBandState.horaryBand.id=:horaryBandId ")

})
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "HoraryBandState.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "estadobandahoraria")
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "estadobandahoraria")
public class HoraryBandState implements BaseModel {

    @Id
    @Column(name = "idestadobandahoraria", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "HoraryBandState.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idbandahoraria", referencedColumnName = "idbandahoraria", nullable = false)
    @NotNull
    private HoraryBand horaryBand;

    @Column(name = "fecha", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date date;

    @Temporal(TemporalType.TIME)
    @Column(name = "horainicio", nullable = false)
    @NotNull
    private Date initHour;

    @Temporal(TemporalType.TIME)
    @Column(name = "horafin", nullable = false)
    @NotNull
    private Date endHour;

    @Column(name = "duracion")
    private Integer duration;

    @Column(name = "antesinicio", nullable = false)
    private Integer beforeInit;

    @Column(name = "despuesinicio", nullable = false)
    private Integer afterInit;

    @Column(name = "antesfin", nullable = false)
    private Integer beforeEnd;

    @Column(name = "despuesfin", nullable = false)
    private Integer afterEnd;

    @Column(name = "estado", length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private HoraryBandStateType type;

    @Column(name = "mindescuento")
    private Integer minutesDiscount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleado", nullable = false, updatable = false)
    @NotNull
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio", referencedColumnName = "idunidadnegocio")
    private BusinessUnit businessUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "COD_CC", length = 6)
    @Length(max = 6)
    private String costCenterCode;


    @Column(name = "NO_CIA", length = 2)
    @Length(max = 2)
    private String companyNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idbandahorariac")
    private HoraryBandContract horaryBandContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadorganizacional")
    private OrganizationalUnit organizationalUnit;

    @OneToMany(mappedBy = "horaryBandState", fetch = FetchType.LAZY)
    private List<MarkStateHoraryBandState> markStateHoraryBandStateList = new ArrayList<MarkStateHoraryBandState>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
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

    public HoraryBand getHoraryBand() {
        return horaryBand;
    }

    public void setHoraryBand(HoraryBand horaryBand) {
        this.horaryBand = horaryBand;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public HoraryBandStateType getType() {
        return type;
    }

    public void setType(HoraryBandStateType type) {
        this.type = type;
    }

    public Integer getMinutesDiscount() {
        return minutesDiscount;
    }

    public void setMinutesDiscount(Integer minutesDiscount) {
        this.minutesDiscount = minutesDiscount;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public HoraryBandContract getHoraryBandContract() {
        return horaryBandContract;
    }

    public void setHoraryBandContract(HoraryBandContract horaryBandContract) {
        this.horaryBandContract = horaryBandContract;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public Date getInitHour() {
        return initHour;
    }

    public void setInitHour(Date initHour) {
        this.initHour = initHour;
    }

    public Date getEndHour() {
        return endHour;
    }

    public void setEndHour(Date endHour) {
        this.endHour = endHour;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getBeforeInit() {
        return beforeInit;
    }

    public void setBeforeInit(Integer beforeInit) {
        this.beforeInit = beforeInit;
    }

    public Integer getAfterInit() {
        return afterInit;
    }

    public void setAfterInit(Integer afterInit) {
        this.afterInit = afterInit;
    }

    public Integer getBeforeEnd() {
        return beforeEnd;
    }

    public void setBeforeEnd(Integer beforeEnd) {
        this.beforeEnd = beforeEnd;
    }

    public Integer getAfterEnd() {
        return afterEnd;
    }

    public void setAfterEnd(Integer afterEnd) {
        this.afterEnd = afterEnd;
    }

    public List<MarkStateHoraryBandState> getMarkStateHoraryBandStateList() {
        return markStateHoraryBandStateList;
    }

    public void setMarkStateHoraryBandStateList(List<MarkStateHoraryBandState> markStateHoraryBandStateList) {
        this.markStateHoraryBandStateList = markStateHoraryBandStateList;
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