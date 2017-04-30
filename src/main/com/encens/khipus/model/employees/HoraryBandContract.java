package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.academics.Asignature;
import com.encens.khipus.model.academics.Horary;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.JobContract;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for HoraryBandContract
 *
 * @author
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "HoraryBandContract.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "bandahorariacontrato",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "HoraryBandContract.findById", query = "select o from HoraryBandContract o where o.id=:id"),
                @NamedQuery(name = "HoraryBandContract.findHoraryBandContractByAcademicSchedule", query = "select o from HoraryBandContract o " +
                        "where o.academicSchedule=:academicSchedule "),
                @NamedQuery(name = "HoraryBandContract.findValidHoraryBandContractByContract", query = "select o from HoraryBandContract o " +
                        " where o.jobContract.contract.activeForPayrollGeneration=:activeForPayrollGeneration and o.jobContract.contract=:contract and" +
                        " ((o.initDate <=:endDate and o.endDate is null )" +
                        " or (o.initDate<=:endDate and o.endDate>=:initDate)" +
                        " or (o.initDate>=:initDate and o.initDate<=:endDate)" +
                        " or (o.endDate>=:initDate and o.endDate<=:endDate))" +
                        " order by o.id "),
                @NamedQuery(name = "HoraryBandContract.findValidHoraryBandContractByEmployee", query = "select o from HoraryBandContract o " +
                        " where o.jobContract.contract.activeForPayrollGeneration=:activeForPayrollGeneration and o.jobContract.contract.employee=:employee and" +
                        " ((o.initDate <=:endDate and o.endDate is null )" +
                        " or (o.initDate<=:endDate and o.endDate>=:initDate)" +
                        " or (o.initDate>=:initDate and o.initDate<=:endDate)" +
                        " or (o.endDate>=:initDate and o.endDate<=:endDate))" +
                        " order by o.id "),
                @NamedQuery(name = "HoraryBandContract.getValidHoraryBandContractsByEmployeeAndBusinessUnitAndJobCategory",
                        query = "select horaryBandContract from HoraryBandContract horaryBandContract " +
                                " where horaryBandContract.jobContract.contract.activeForPayrollGeneration=:activeForPayrollGeneration " +
                                " and horaryBandContract.jobContract.contract.employee=:employee " +
                                " and horaryBandContract.jobContract.job.organizationalUnit.businessUnit=:businessUnit " +
                                " and horaryBandContract.jobContract.job.jobCategory=:jobCategory " +
                                " and ((horaryBandContract.initDate <=:endDate and horaryBandContract.endDate is null )" +
                                " or (horaryBandContract.initDate<=:endDate and horaryBandContract.endDate>=:initDate)" +
                                " or (horaryBandContract.initDate>=:initDate and horaryBandContract.initDate<=:endDate)" +
                                " or (horaryBandContract.endDate>=:initDate and horaryBandContract.endDate<=:endDate))" +
                                " order by horaryBandContract.id "),
                @NamedQuery(name = "HoraryBandContract.getJobContractWithValidHoraryBandContractByEmployeeBusinessUnitJobCategory",
                        query = "select jobContract from JobContract jobContract " +
                                " left join jobContract.costPivotHoraryBandContract pivotHoraryBandContract " +
                                " left join jobContract.contract contract " +
                                " left join contract.employee employee " +
                                " left join jobContract.job job " +
                                " left join job.organizationalUnit organizationalUnit " +
                                " left join organizationalUnit.businessUnit businessUnit " +
                                " left join job.jobCategory jobCategory " +
                                " where contract.activeForPayrollGeneration =:activeForPayrollGeneration " +
                                " and employee.id =:employeeId " +
                                " and businessUnit =:businessUnit " +
                                " and jobCategory =:jobCategory " +
                                " and ((pivotHoraryBandContract.initDate <=:endDate and pivotHoraryBandContract.endDate is null )" +
                                " or (pivotHoraryBandContract.initDate<=:endDate and pivotHoraryBandContract.endDate>=:initDate)" +
                                " or (pivotHoraryBandContract.initDate>=:initDate and pivotHoraryBandContract.initDate<=:endDate)" +
                                " or (pivotHoraryBandContract.endDate>=:initDate and pivotHoraryBandContract.endDate<=:endDate))"),
                @NamedQuery(name = "HoraryBandContract.findByIdNumberAndHourRange", query = "select distinct o from HoraryBandContract o " +
                        "where o.jobContract.contract.employee.idNumber=:idNumber and  o.id <>:id"
                        + " and ((o.initDate <=:initDate and o.endDate >=:initDate) or (o.initDate <=:endDate and o.endDate >=:endDate))"
                        + " and ((o.horaryBand.initHour <=:initHour and o.horaryBand.endHour >=:initHour )"
                        + " or (o.horaryBand.initHour <=:endHour and o.horaryBand.endHour >=:endHour ))" +
                        " and o.horaryBand.initDay =:initDay and o.horaryBand.endDay =:endDay"
                ),
                @NamedQuery(name = "HoraryBandContract.findByIdNumberAndHourRangeWithoutId", query = "select distinct o from HoraryBandContract o " +
                        "where o.jobContract.contract.employee.idNumber=:idNumber"
                        + " and ((o.initDate <=:initDate and o.endDate >=:initDate) or (o.initDate <=:endDate and o.endDate >=:endDate))"
                        + " and ((o.horaryBand.initHour <=:initHour and o.horaryBand.endHour >=:initHour )"
                        + " or (o.horaryBand.initHour <=:endHour and o.horaryBand.endHour >=:endHour ))" +
                        " and o.horaryBand.initDay =:initDay and o.horaryBand.endDay =:endDay"
                ),
                @NamedQuery(name = "HoraryBandContract.findByJobContractActive", query = "SELECT o FROM HoraryBandContract o " +
                        " WHERE o.jobContract =:jobContract and o.active =:isActive" +
                        " ORDER BY o.subjet ASC"),
                @NamedQuery(name = "HoraryBandContract.findValidHoraryBandContractForMarProcessorByEmployeeAndDateRangeAndCurrentDate",
                        query = "select horaryBandContract from HoraryBandContract horaryBandContract " +
                                "left join fetch horaryBandContract.jobContract jobContract " +
                                "left join fetch jobContract.contract contract " +
                                "left join fetch contract.employee employee " +
                                "left join fetch jobContract.job job " +
                                "left join fetch job.jobCategory jobCategory " +
                                "left join fetch horaryBandContract.horaryBand horaryBand " +
                                "where contract.activeForPayrollGeneration=:activeForPayrollGeneration " +
                                "and employee=:employee and " +
                                "((horaryBandContract.initDate <=:endDate and horaryBandContract.endDate is null )" +
                                "or (horaryBandContract.initDate<=:endDate and horaryBandContract.endDate>=:initDate)" +
                                "or (horaryBandContract.initDate>=:initDate and horaryBandContract.initDate<=:endDate)" +
                                "or (horaryBandContract.endDate>=:initDate and horaryBandContract.endDate<=:endDate)) " +
                                "and horaryBand.id not in ( select horaryBandState.horaryBand.id from HoraryBandState horaryBandState " +
                                "where horaryBandState.type<>:pendingHoraryBandStateType " +
                                "and horaryBandState.date=:currentDate )"
                )
        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "bandahorariacontrato")
public class HoraryBandContract implements BaseModel, Comparable {

    @Id
    @Column(name = "idbandahorariacontrato", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "HoraryBandContract.tableGenerator")
    private Long id;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontratopuesto", nullable = false, updatable = false, insertable = true)
    private JobContract jobContract;

    @Column(name = "idcontratopuesto", updatable = false, insertable = false)
    private Long jobContractId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idbandahoraria", nullable = false, updatable = false, insertable = true)
    private HoraryBand horaryBand;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDTIPOBANDAHORARIA", nullable = true, updatable = false, insertable = true)
    private TypeHoraryBand typeHoraryBand;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idtolerancia", nullable = false, updatable = false, insertable = true)
    private Tolerance tolerance;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idlimite", nullable = false, updatable = false, insertable = true)
    private Limit limit;

    @OneToMany(mappedBy = "horaryBandContract", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<ControlReport> controlReportList = new ArrayList<ControlReport>(0);

    @OneToMany(mappedBy = "horaryBandContract", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<MarkReport> markReportList = new ArrayList<MarkReport>(0);

    @Temporal(TemporalType.DATE)
    @Column(name = "fechainicio", nullable = false)
    private Date initDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "fechafin")
    private Date endDate;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "tipohora", length = 200)
    @Length(max = 200)
    private String timeType;

    @Column(name = "horarioacademico", nullable = true)
    private Long academicSchedule;

    @Column(name = "GESTION", nullable = true)
    private Integer gestion;

    @Column(name = "PERIODO", nullable = true)
    private Integer period;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "horarioacademico", referencedColumnName = "HORARIO", nullable = true, insertable = false, updatable = false),
            @JoinColumn(name = "GESTION", referencedColumnName = "GESTION", nullable = true, insertable = false, updatable = false),
            @JoinColumn(name = "PERIODO", referencedColumnName = "PERIODO", nullable = true, insertable = false, updatable = false)
    })
    private Horary horary;

    @Column(name = "activo", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean active;

    @Column(name = "compartido", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean shared;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "asignatura", nullable = true, updatable = false, insertable = false)
    private Asignature asignature;

    @Column(name = "PIVOTECOSTO", nullable = true)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean costPivot;

    @Column(name = "EDIFICIO")
    private String building;

    @Column(name = "AMBIENTE")
    private String classroom;

    @Column(name = "asignatura", nullable = true)
    private String subjet;

    @Column(name = "grupoasignatura", length = 200)
    @Length(max = 200)
    private String groupSubject;

    @Column(name = "nombreasignatura", length = 200)
    @Length(max = 200)
    private String nameSubject;

    @Column(name = "PRECIOPERIODO", precision = 16, scale = 6)
    private BigDecimal pricePerPeriod;

    public HoraryBandContract() {

    }

    public HoraryBandContract(HoraryBandContract instance) {
        setInitDate(instance.getInitDate());
        setEndDate(instance.getEndDate());
        setTimeType(instance.getTimeType());
        setTolerance(instance.getTolerance());
        setLimit(instance.getLimit());
        setHoraryBand(new HoraryBand(instance.getHoraryBand()));
        setJobContract(instance.getJobContract());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobContract getJobContract() {
        return jobContract;
    }

    public void setJobContract(JobContract jobContract) {
        this.jobContract = jobContract;
    }

    public Long getJobContractId() {
        return jobContractId;
    }

    public void setJobContractId(Long jobContractId) {
        this.jobContractId = jobContractId;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<ControlReport> getControlReportList() {
        return controlReportList;
    }

    public void setControlReportList(List<ControlReport> controlReportList) {
        this.controlReportList = controlReportList;
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

    public List<MarkReport> getMarkReportList() {
        return markReportList;
    }

    public void setMarkReportList(List<MarkReport> markReportList) {
        this.markReportList = markReportList;
    }

    public String getTimeType() {
        return timeType;
    }

    public void setTimeType(String timeType) {
        this.timeType = timeType;
    }

    public HoraryBand getHoraryBand() {
        return horaryBand;
    }

    public void setHoraryBand(HoraryBand horaryBand) {
        this.horaryBand = horaryBand;
    }

    public Tolerance getTolerance() {
        return tolerance;
    }

    public void setTolerance(Tolerance tolerance) {
        this.tolerance = tolerance;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    // makes this object comparable in order to sort any list of this kind
    // only order by HoraryBand day, init hour, duration and end hour

    public int compareTo(Object o) {
        HoraryBandContract horaryBandContract = (HoraryBandContract) o;
        HoraryBand horaryBand = horaryBandContract.getHoraryBand();

        if (getHoraryBand().getInitHour().compareTo(horaryBand.getInitHour()) == 0) {
            // intentionally inverted in order to order desc. endHour.
            return horaryBand.getEndHour().compareTo(getHoraryBand().getEndHour());
        } else {
            return getHoraryBand().getInitHour().compareTo(horaryBand.getInitHour());
        }
    }

    public Long getAcademicSchedule() {
        return academicSchedule;
    }

    public void setAcademicSchedule(Long academicSchedule) {
        this.academicSchedule = academicSchedule;
    }

    public Integer getGestion() {
        return gestion;
    }

    public void setGestion(Integer gestion) {
        this.gestion = gestion;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Horary getHorary() {
        return horary;
    }

    public void setHorary(Horary horary) {
        this.horary = horary;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public String getSubjet() {
        return subjet;
    }

    public void setSubjet(String subjet) {
        this.subjet = subjet;
    }

    public String getGroupSubject() {
        return groupSubject;
    }

    public void setGroupSubject(String groupSubject) {
        this.groupSubject = groupSubject;
    }

    public String getNameSubject() {
        return nameSubject;
    }

    public void setNameSubject(String nameSubject) {
        this.nameSubject = nameSubject;
    }

    public Asignature getAsignature() {
        return asignature;
    }

    public void setAsignature(Asignature asignature) {
        this.asignature = asignature;
    }

    public BigDecimal getPricePerPeriod() {
        return pricePerPeriod;
    }

    public void setPricePerPeriod(BigDecimal pricePerPeriod) {
        this.pricePerPeriod = pricePerPeriod;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public Boolean getCostPivot() {
        return costPivot;
    }

    public void setCostPivot(Boolean costPivot) {
        this.costPivot = costPivot;
    }

    public TypeHoraryBand getTypeHoraryBand() {
        return typeHoraryBand;
    }

    public void setTypeHoraryBand(TypeHoraryBand typeHoraryBand) {
        this.typeHoraryBand = typeHoraryBand;
    }
}