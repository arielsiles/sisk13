package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.academics.AcademicSubjectGroupPK;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.HoraryBandContract;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Range;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for JobContract
 *
 * @author
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "JobContract.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "contratopuesto",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "JobContract.findJobContract", query = "select o from JobContract o where o.id=:id"),
                @NamedQuery(name = "JobContract.loadJobContracts", query = "select o from JobContract o " +
                        " left join fetch o.contract contract" +
                        " left join fetch contract.employee employee" +
                        " left join fetch o.job job" +
                        " left join fetch job.salary salary" +
                        " left join fetch salary.currency currency" +
                        " left join fetch job.jobCategory jobCategory" +
                        " left join fetch jobCategory.sector sector" +
                        " left join fetch job.organizationalUnit organizationalUnit" +
                        " left join fetch organizationalUnit.businessUnit businessUnit" +
                        " left join fetch businessUnit.organization organization" +
                        " left join fetch organizationalUnit.costCenter costCenter" +
                        " where o.id in (:idList)"),
                @NamedQuery(name = "JobContract.findAllJobContracts", query = "select o from JobContract o "),
                @NamedQuery(name = "JobContract.findJobContractByEmployee", query = "select o from JobContract o where o.contract.employee=:employee order by o.contract.id "),
                @NamedQuery(name = "JobContract.findLastJobContractByEmployee", query = "select max(o.id) from JobContract o left join o.contract contract left join contract.employee employee" +
                        " where employee=:employee"),
                @NamedQuery(name = "JobContract.findJobContractByEmployeeAndOrgUnit", query = "select distinct o from JobContract o where o.contract.employee=:employee and o.job.organizationalUnit=:organizationalUnit"),

                @NamedQuery(name = "JobContract.findJobContractByEmployeeAndOrgUnitAndDatesOfContract", query = "select distinct o from JobContract o " +
                        "where o.contract.employee=:employee " +
                        "and o.job.organizationalUnit.career=:career " +
                        "and o.contract.initDate=:initDate " +
                        "and o.contract.endDate=:endDate"),

                @NamedQuery(name = "JobContract.findEmployeesWithValidContractsByContractModeWithNullEndDateList",
                        query = "select o from JobContract o where o.contract.initDate<=:lastDayOfTheMonth and o.contract.endDate is null and o.contract.contractMode =:contractMode"),
                @NamedQuery(name = "JobContract.findEmployeesWithValidContractsByContractModeWithNotNullEndDateList",
                        query = "select o from JobContract o where o.contract.endDate>=:firstDayOfTheMonth and o.contract.contractMode =:contractMode"),
                @NamedQuery(name = "JobContract.findJobContractToGenerateTaxPayroll",
                        query = "select element from JobContract element join fetch element.contract contract join fetch contract.employee join fetch element.job job join fetch job.salary where element.contract.activeForTaxPayrollGeneration =:activeForTaxPayrollGeneration and element.job.organizationalUnit.businessUnit =:businessUnit and ((element.contract.initDate <= :startDate and element.contract.endDate >= :endDate) or (element.contract.initDate >=:startDate and element.contract.initDate <= :endDate) or (element.contract.endDate >= :startDate and element.contract.endDate <= :endDate))"),
                @NamedQuery(name = "JobContract.findJobContractByIdentifiers",
                        query = "select element from JobContract element join fetch element.contract contract join fetch contract.employee join fetch element.job job join fetch job.salary join fetch job.charge join fetch job.organizationalUnit organizationalUnit join fetch organizationalUnit.organizationalLevel where element.id in(:identifiers)"),
                @NamedQuery(name = "JobContract.findActiveByEmployeeAndDateRange",
                        query = "select jobContract from JobContract jobContract " +
                                "left join jobContract.contract contract " +
                                "left join contract.employee employee " +
                                "left join jobContract.job job " +
                                "left join job.jobCategory jobCategory " +
                                "where employee =:employee " +
                                "and contract.activeForPayrollGeneration=:activeForPayrollGeneration " +
                                "and ((contract.initDate <=:endDate and contract.endDate is null ) " +
                                "and jobCategory.payrollGenerationType=:payrollGenerationType " +
                                "or (contract.initDate<=:initDate and contract.endDate>=:endDate) " +
                                "or (contract.initDate>=:initDate and contract.initDate<=:endDate) " +
                                "or (contract.endDate>=:initDate and contract.endDate<=:endDate)) "),
                @NamedQuery(name = "JobContract.findActiveByEmployeeAndDateRangeAndGenerationType",
                        query = "select count(jobContract) from JobContract jobContract " +
                                "left join jobContract.contract contract " +
                                "left join contract.employee employee " +
                                "left join jobContract.job job " +
                                "left join job.jobCategory jobCategory " +
                                "where employee =:employee " +
                                "and contract.activeForPayrollGeneration=:activeForPayrollGeneration " +
                                "and ((contract.initDate <=:endDate and contract.endDate is null ) " +
                                "and jobCategory.payrollGenerationType=:payrollGenerationType " +
                                "or (contract.initDate<=:initDate and contract.endDate>=:endDate) " +
                                "or (contract.initDate>=:initDate and contract.initDate<=:endDate) " +
                                "or (contract.endDate>=:initDate and contract.endDate<=:endDate)) "),
                @NamedQuery(name = "JobContract.findJobContractPricePerPeriodByContractList", query = "select jobContract, " +
                        " (hb.duration/45*costPivotHoraryBandC.pricePerPeriod) " +
                        "from JobContract jobContract " +
                        "left join jobContract.costPivotHoraryBandContract costPivotHoraryBandC " +
                        "left join costPivotHoraryBandC.horaryBand hb " +
                        "where jobContract.contract.id in (:contractIdList) "),
                @NamedQuery(name = "JobContract.cleanJobContractByIdList",
                        query = "DELETE FROM JobContract jobContract " +
                                " WHERE jobContract.id in(:jobContractIdList) "),
                @NamedQuery(name = "JobContract.updateCostPivot",
                        query = "update JobContract jobContract " +
                                "set jobContract.costPivotHoraryBandContract= " +
                                "       (select horaryBandContract " +
                                "       from HoraryBandContract horaryBandContract " +
                                "       where horaryBandContract.id=" +
                                "           (select max(horaryBandContractI.id) " +
                                "           from HoraryBandContract horaryBandContractI" +
                                "           where horaryBandContractI.pricePerPeriod=" +
                                "               (select max(horaryBandContractP.pricePerPeriod) " +
                                "               from HoraryBandContract horaryBandContractP " +
                                "               where horaryBandContractP.jobContract=jobContract " +
                                "               and horaryBandContractP.active=:active" +
                                "               )" +
                                "           and horaryBandContractI.jobContract=jobContract " +
                                "           and horaryBandContractI.active=:active " +
                                "           )" +
                                "       and horaryBandContract.jobContract=jobContract " +
                                "       and horaryBandContract.active=:active " +
                                "        )" +
                                " where jobContract.contract.id in (:contractIdList)"),
                @NamedQuery(name = "JobContract.updateJobContractBuilding",
                        query = "UPDATE JobContract jobContract SET jobContract.building= " +
                                "   (select jContract.building" +
                                "   from JobContract jContract " +
                                "   where jContract= jobContract) " +
                                " WHERE jobContract.contractId in (:contractIdList)"),
                @NamedQuery(name = "JobContract.updateJobContractClassroom",
                        query = "UPDATE JobContract jobContract SET jobContract.classroom= " +
                                "   (select jContract.classroom " +
                                "   from JobContract jContract " +
                                "   where jContract= jobContract) " +
                                " WHERE jobContract.contractId in (:contractIdList)")

        }
)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "contratopuesto")
public class JobContract implements BaseModel {

    @Id
    @Column(name = "idcontratopuesto", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "JobContract.tableGenerator")
    private Long id;

    @Column(name = "idpuesto", updatable = false, insertable = false)
    private Long jobId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idpuesto", nullable = false)
    private Job job;

    @Column(name = "idcontrato", updatable = false, insertable = false)
    private Long contractId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontrato", nullable = false, updatable = false, insertable = true)
    private Contract contract;

    @OneToMany(mappedBy = "jobContract", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @OrderBy("subjet ASC")
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<HoraryBandContract> horaryBandContractList = new ArrayList<HoraryBandContract>(0);

    @Column(name = "plan_estudio")
    private String curricula;

    @Column(name = "asignatura")
    private String asignature;

    @Column(name = "gestion")
    private Integer gestion;

    @Column(name = "periodo")
    private Integer period;

    @Column(name = "sistema")
    private Integer systemNumber;

    @Column(name = "grupo_asignatura")
    private String subjectGroup;

    @Column(name = "tipo_grupo")
    private String groupType;

    @Column(name = "EDIFICIO")
    private String building;

    @Column(name = "AMBIENTE")
    private String classroom;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "IDBANDAHORARIACONTRATO", referencedColumnName = "idbandahorariacontrato")
    private HoraryBandContract costPivotHoraryBandContract;

    @Column(name = "montolaboral", precision = 16, scale = 6)
    @Range(min = 0)
    private BigDecimal occupationalAmount;

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

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public List<HoraryBandContract> getHoraryBandContractList() {
        return horaryBandContractList;
    }

    public void setHoraryBandContractList(List<HoraryBandContract> horaryBandContractList) {
        this.horaryBandContractList = horaryBandContractList;
    }

    public Integer getExecutorUnitId() {
        return getJob().getOrganizationalUnit().getBusinessUnit().getExecutorUnitCodeAsInteger();
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public BigDecimal getOccupationalAmount() {
        return occupationalAmount;
    }

    public void setOccupationalAmount(BigDecimal occupationalAmount) {
        this.occupationalAmount = occupationalAmount;
    }

    public AcademicSubjectGroupPK getAcademicSubjectGroupPK() {
        AcademicSubjectGroupPK academicSubjectGroupPK = new AcademicSubjectGroupPK();
        academicSubjectGroupPK.setAsignature(getAsignature());
        academicSubjectGroupPK.setCurricula(getCurricula());
        academicSubjectGroupPK.setGestion(getGestion());
        academicSubjectGroupPK.setPeriod(getPeriod());
        academicSubjectGroupPK.setGroupType(getGroupType());
        academicSubjectGroupPK.setSubjectGroup(getSubjectGroup());
        academicSubjectGroupPK.setSystemNumber(getSystemNumber());
        return academicSubjectGroupPK;
    }

    public void setAcademicSubjectGroupPK(AcademicSubjectGroupPK academicSubjectGroup) {
        setAsignature(academicSubjectGroup != null && academicSubjectGroup.getAsignature() != null ? academicSubjectGroup.getAsignature() : null);
        setCurricula(academicSubjectGroup != null && academicSubjectGroup.getCurricula() != null ? academicSubjectGroup.getCurricula() : null);
        setGestion(academicSubjectGroup != null && academicSubjectGroup.getGestion() != null ? academicSubjectGroup.getGestion() : null);
        setPeriod(academicSubjectGroup != null && academicSubjectGroup.getPeriod() != null ? academicSubjectGroup.getPeriod() : null);
        setGroupType(academicSubjectGroup != null && academicSubjectGroup.getGroupType() != null ? academicSubjectGroup.getGroupType() : null);
        setSubjectGroup(academicSubjectGroup != null && academicSubjectGroup.getSubjectGroup() != null ? academicSubjectGroup.getSubjectGroup() : null);
        setSystemNumber(academicSubjectGroup != null && academicSubjectGroup.getSystemNumber() != null ? academicSubjectGroup.getSystemNumber() : null);
    }

    public String getCurricula() {
        return curricula;
    }

    public void setCurricula(String curricula) {
        this.curricula = curricula;
    }

    public String getAsignature() {
        return asignature;
    }

    public void setAsignature(String asignature) {
        this.asignature = asignature;
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

    public Integer getSystemNumber() {
        return systemNumber;
    }

    public void setSystemNumber(Integer systemNumber) {
        this.systemNumber = systemNumber;
    }

    public String getSubjectGroup() {
        return subjectGroup;
    }

    public void setSubjectGroup(String subjectGroup) {
        this.subjectGroup = subjectGroup;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
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

    public HoraryBandContract getCostPivotHoraryBandContract() {
        return costPivotHoraryBandContract;
    }

    public void setCostPivotHoraryBandContract(HoraryBandContract costPivotHoraryBandContract) {
        this.costPivotHoraryBandContract = costPivotHoraryBandContract;
    }
}