package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.util.DateUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Entity for Charge
 *
 * @author: Ariel Siles Encinas
 */

@NamedQueries(
        {
                @NamedQuery(name = "SpecialDate.findSpecialDateRangeByEmployee",
                        query = "select sp.initPeriod,sp.endPeriod from SpecialDate sp" +
                                " where sp.specialDateTarget=:specialDateTarget and sp.employee=:employee and sp.credit=:credit and " +
                                " ((sp.initPeriod<=:initDate and sp.endPeriod>=:endDate)" +
                                " or (sp.initPeriod>=:initDate and sp.initPeriod<=:endDate)" +
                                " or (sp.endPeriod>=:initDate and sp.endPeriod<=:endDate))" +
                                " and sp.allDay=:allDay"),
                @NamedQuery(name = "SpecialDate.findSpecialDateTimeRangeByEmployee",
                        query = "select sp.initPeriod,sp.endPeriod, sp.startTime, sp.endTime from SpecialDate sp" +
                                " where sp.specialDateTarget=:specialDateTarget and sp.employee=:employee and sp.credit=:credit and " +
                                " ((sp.initPeriod<=:initDate and sp.endPeriod>=:endDate)" +
                                " or (sp.initPeriod>=:initDate and sp.initPeriod<=:endDate)" +
                                " or (sp.endPeriod>=:initDate and sp.endPeriod<=:endDate))" +
                                " and sp.allDay=:allDay"),
                @NamedQuery(name = "SpecialDate.findSpecialDateRangeByOrganizationalUnit",
                        query = "select sp.initPeriod,sp.endPeriod from SpecialDate sp" +
                                " where sp.specialDateTarget=:specialDateTarget and sp.organizationalUnit=:organizationalUnit and sp.credit=:credit and " +
                                " ((sp.initPeriod<=:initDate and sp.endPeriod>=:endDate)" +
                                " or (sp.initPeriod>=:initDate and sp.initPeriod<=:endDate)" +
                                " or (sp.endPeriod>=:initDate and sp.endPeriod<=:endDate))" +
                                " and sp.allDay=:allDay"),
                @NamedQuery(name = "SpecialDate.findSpecialDateTimeRangeByOrganizationalUnit",
                        query = "select sp.initPeriod,sp.endPeriod, sp.startTime, sp.endTime from SpecialDate sp" +
                                " where sp.specialDateTarget=:specialDateTarget and sp.organizationalUnit=:organizationalUnit and sp.credit=:credit and " +
                                " ((sp.initPeriod<=:initDate and sp.endPeriod>=:endDate)" +
                                " or (sp.initPeriod>=:initDate and sp.initPeriod<=:endDate)" +
                                " or (sp.endPeriod>=:initDate and sp.endPeriod<=:endDate))" +
                                " and sp.allDay=:allDay"),
                @NamedQuery(name = "SpecialDate.findSpecialDateRangeByBusinessUnit",
                        query = "select sp.initPeriod,sp.endPeriod from SpecialDate sp" +
                                " where sp.specialDateTarget=:specialDateTarget and sp.businessUnit=:businessUnit and sp.credit=:credit and " +
                                " ((sp.initPeriod<=:initDate and sp.endPeriod>=:endDate)" +
                                " or (sp.initPeriod>=:initDate and sp.initPeriod<=:endDate)" +
                                " or (sp.endPeriod>=:initDate and sp.endPeriod<=:endDate))" +
                                " and sp.allDay=:allDay"),
                @NamedQuery(name = "SpecialDate.findSpecialDateTimeRangeByBusinessUnit",
                        query = "select sp.initPeriod,sp.endPeriod, sp.startTime, sp.endTime from SpecialDate sp" +
                                " where sp.specialDateTarget=:specialDateTarget and sp.businessUnit=:businessUnit and sp.credit=:credit and " +
                                " ((sp.initPeriod<=:initDate and sp.endPeriod>=:endDate)" +
                                " or (sp.initPeriod>=:initDate and sp.initPeriod<=:endDate)" +
                                " or (sp.endPeriod>=:initDate and sp.endPeriod<=:endDate))" +
                                " and sp.allDay=:allDay")

        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "SpecialDate.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "fechaespecial",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "fechaespecial")
public class SpecialDate implements BaseModel {

    @Id
    @Column(name = "idfechaespecial", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SpecialDate.tableGenerator")
    private Long id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String title;

    @Column(name = "dia", nullable = true)
    private Integer day;

    @Column(name = "mes", nullable = true)
    private Integer month;

    @Column(name = "moveralunes", nullable = true)
    private Integer movetomonday;

    @Column(name = "ocurrencia", nullable = true)
    private Integer ocurrence;

    @Column(name = "tiporol", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private SpecialDateRol rolType;

    @Column(name = "destino", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SpecialDateTarget specialDateTarget;

    @Temporal(TemporalType.DATE)
    @Column(name = "fechainicio", nullable = false)
    private Date initPeriod;

    @Temporal(TemporalType.DATE)
    @Column(name = "fechafin")
    private Date endPeriod;

    @Temporal(TemporalType.TIME)
    @Column(name = "horainicio")
    private Date startTime;

    @Temporal(TemporalType.TIME)
    @Column(name = "horafin")
    private Date endTime;

    @Column(name = "ALLDAY", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean allDay;

    @Column(name = "gocedehaber")
    @Enumerated(EnumType.STRING)
    private SpecialDateType credit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontrato", nullable = true)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleado", nullable = true)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadorganizacional", nullable = true)
    private OrganizationalUnit organizationalUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio", nullable = true)
    private BusinessUnit businessUnit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idvacacion")
    private Vacation vacation;

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

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getMovetomonday() {
        return movetomonday;
    }

    public void setMovetomonday(Integer movetomonday) {
        this.movetomonday = movetomonday;
    }

    public Integer getOcurrence() {
        return ocurrence;
    }

    public void setOcurrence(Integer ocurrence) {
        this.ocurrence = ocurrence;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public SpecialDateRol getRolType() {
        return rolType;
    }

    public void setRolType(SpecialDateRol rolType) {
        this.rolType = rolType;
    }

    public Date getInitPeriod() {
        return initPeriod;
    }

    public void setInitPeriod(Date initPeriod) {
        this.initPeriod = initPeriod;
    }

    public Date getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(Date endPeriod) {
        this.endPeriod = endPeriod;
    }

    public SpecialDateTarget getSpecialDateTarget() {
        return specialDateTarget;
    }

    public void setSpecialDateTarget(SpecialDateTarget specialDateTarget) {
        if (null != specialDateTarget) {
            if (SpecialDateTarget.EMPLOYEE.equals(specialDateTarget)) {
                organizationalUnit = null;
                businessUnit = null;
            }
            if (SpecialDateTarget.ORGANIZATIONALUNIT.equals(specialDateTarget) ||
                    SpecialDateTarget.BUSINESSUNIT.equals(specialDateTarget)) {
                employee = null;
            }
        }
        this.specialDateTarget = specialDateTarget;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public SpecialDateType getCredit() {
        return credit;
    }

    public void setCredit(SpecialDateType credit) {
        this.credit = credit;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        if (allDay) {
            Calendar calendar = Calendar.getInstance();
            DateUtils.toMaxHours(calendar);
            setEndTime(calendar.getTime());
            DateUtils.toMinHours(calendar);
            setStartTime(calendar.getTime());
        }
        this.allDay = allDay;
    }

    public Vacation getVacation() {
        return vacation;
    }

    public void setVacation(Vacation vacation) {
        this.vacation = vacation;
    }
}