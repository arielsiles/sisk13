package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for Salary
 *
 * @author: Ariel Siles Encinas
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Contract.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "contrato",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "Contract.findAll", query = "select o from Contract o "),
                @NamedQuery(name = "Contract.findByEmployee", query = "select o from Contract o where o.employee=:employee "),
                @NamedQuery(name = "Contract.findEmployeesWithValidContractsByContractModeWithNullEndDateList",
                        query = "select o from Contract o where o.initDate<=:lastDayOfTheMonth and o.endDate is null and o.contractMode =:contractMode"),
                @NamedQuery(name = "Contract.findEmployeesWithValidContractsByContractModeWithNotNullEndDateList",
                        query = "select o from Contract o where o.endDate>=:firstDayOfTheMonth and o.contractMode =:contractMode"),

                @NamedQuery(name = "Contract.findValidContractByEmployeeByInitDateByNotNullEndDate", query = "select o from Contract o " +
                        "where o.employee=:employee and o.endDate>=:firstDayOfTheMonth"),
                @NamedQuery(name = "Contract.findValidContractByEmployeeByInitDateByNullEndDate", query = "select o from Contract o " +
                        "where o.employee=:employee and o.initDate<=:lastDayOfTheMonth and o.endDate is null"),
                @NamedQuery(name = "Contract.findByEmployeeAndOrgUnit", query = "select distinct o.contract from JobContract o " +
                        "where o.contract.employee=:employee and o.job.organizationalUnit=:organizationalUnit " +
                        "and o.contract.initDate<=:date and (o.contract.endDate>=:date or o.contract.endDate is null)"),
                @NamedQuery(name = "Contract.findByEmployeeInDateRange", query = "select contract from Contract contract " +
                        " where contract.activeForPayrollGeneration=:activeForPayrollGeneration and contract.employee=:employee and" +
                        " ((contract.initDate <=:endDate and contract.endDate is null ) or (contract.initDate<=:endDate and contract.endDate>=:initDate))" +
                        " order by contract.initDate"),
                @NamedQuery(name = "Contract.findContractsForPayrollGeneration", query = "select contract" +
                        " from Contract contract " +
                        " left join contract.jobContractList jobContract" +
                        " where contract.activeForPayrollGeneration=:activeForPayrollGeneration and contract.employee=:employee and" +
                        " ((contract.initDate <=:endDate and contract.endDate is null ) " +
                        "or (contract.initDate<=:initDate and contract.endDate>=:endDate) " +
                        "or (contract.initDate>=:initDate and contract.initDate<=:endDate) " +
                        "or (contract.endDate>=:initDate and contract.endDate<=:endDate)) and " +
                        " jobContract.job.organizationalUnit.businessUnit=:businessUnit and  jobContract.job.jobCategory=:jobCategory" +
                        " order by contract.initDate"),
                @NamedQuery(name = "Contract.findContractsForPayrollGenerationByLastDayOfMonth", query = "select contract" +
                        " from Contract contract " +
                        " left join contract.jobContractList jobContract" +
                        " where contract.activeForPayrollGeneration=:activeForPayrollGeneration and contract.employee=:employee and" +
                        " ((contract.initDate <=:endDate and contract.endDate is null ) " +
                        "or (contract.initDate<=:initDate and contract.endDate>=:endDate) " +
                        "or (contract.initDate>=:initDate and contract.initDate<=:endDate) " +
                        "or (contract.initDate<=:lastDayOfMonth and contract.initDate>=:endDate)" +
                        "or (contract.endDate>=:initDate and contract.endDate<=:endDate)) and " +
                        " jobContract.job.organizationalUnit.businessUnit=:businessUnit and  jobContract.job.jobCategory=:jobCategory" +
                        " order by contract.initDate"),
                @NamedQuery(name = "Contract.findInDateRange", query = "select contract from Contract contract " +
                        " where contract.activeForPayrollGeneration=:activeForPayrollGeneration and ((contract.initDate <=:endDate and contract.endDate is null ) or (contract.initDate<=:endDate and contract.endDate>=:initDate))" +
                        " order by contract.employee.id"),
                @NamedQuery(name = "Contract.findInDateRangeWithHoraryBandContract", query = "select contract,horaryBandContract" +
                        " from Contract contract " +
                        " left join contract.jobContractList jobContract" +
                        " left join jobContract.horaryBandContractList horaryBandContract" +
                        " where contract.activeForPayrollGeneration=:activeForPayrollGeneration and ((contract.initDate <=:endDate and contract.endDate is null ) or (contract.initDate<=:endDate and contract.endDate>=:initDate))" +
                        " and ((horaryBandContract.initDate <=:endDate and horaryBandContract.endDate is null ) or (horaryBandContract.initDate<=:endDate and horaryBandContract.endDate>=:initDate))" +
                        " order by contract.employee.id,contract.id"),
                @NamedQuery(name = "Contract.findByEmployeeInGestion", query = "SELECT contract " +
                        " FROM Contract contract " +
                        " WHERE contract.employee =:employee " +
                        " AND ((contract.initDate <=:endDate and contract.endDate is null ) " +
                        " OR (contract.initDate<=:initDate and contract.endDate>=:endDate) " +
                        " OR (contract.initDate>=:initDate and contract.initDate<=:endDate) " +
                        " OR (contract.endDate>=:initDate and contract.endDate<=:endDate)) "),
                @NamedQuery(name = "Contract.findMaxProfessorCareerAssigned", query = "SELECT ou.id, ou.name, count(ou.id) " +
                        " FROM Contract contract " +
                        " LEFT JOIN contract.jobContractList jobContrac " +
                        " LEFT JOIN jobContrac.horaryBandContractList horaryBandContract " +
                        " LEFT JOIN horaryBandContract.horary horary " +
                        " LEFT JOIN horary.organizationalUnit ou " +
                        " WHERE contract.employee =:employee " +
                        " AND ((contract.initDate <=:endDate and contract.endDate is null ) " +
                        " OR (contract.initDate<=:initDate and contract.endDate>=:endDate) " +
                        " OR (contract.initDate>=:initDate and contract.initDate<=:endDate) " +
                        " OR (contract.endDate>=:initDate and contract.endDate<=:endDate))" +
                        " GROUP BY ou.id, ou.name " +
                        " ORDER BY count(ou.id) desc, ou.name "),
                @NamedQuery(name = "Contract.sumOccupationalGlobalAmountByContract",
                        query = "UPDATE Contract contract SET contract.occupationalGlobalAmount= " +
                                "   (SELECT sum(jobContract.occupationalAmount) FROM JobContract jobContract " +
                                "       WHERE jobContract.contract.id=:contractId" +
                                "       AND jobContract.costPivotHoraryBandContract.active=:active)" +
                                "WHERE contract.id=:contractId "),
                @NamedQuery(name = "Contract.sumOccupationalBasicAmountByContract",
                        query = "UPDATE Contract contract SET contract.occupationalBasicAmount= " +
                                "   (SELECT sum(jobContract.job.salary.amount) FROM JobContract jobContract " +
                                "       WHERE jobContract.contract.id=:contractId " +
                                "       AND jobContract.costPivotHoraryBandContract.active=:active)" +
                                "WHERE contract.id=:contractId ")
        }
)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "contrato")
public class Contract implements BaseModel {

    @Id
    @Column(name = "idcontrato", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Contract.tableGenerator")
    private Long id;

    @Column(name = "idempleado", updatable = false, insertable = false)
    private Long employeeId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleado", nullable = false, updatable = false)
    private Employee employee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idestadocontrato", nullable = false)
    private ContractState contractState;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idmodalidadcontrato", nullable = false)
    private ContractMode contractMode;

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<SpecialDate> specialDates = new ArrayList<SpecialDate>(0);

    @OneToMany(mappedBy = "contract", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<JobContract> jobContractList = new ArrayList<JobContract>(0);

    @Column(name = "numerocontrato", nullable = true)
    private Integer numberOfContract;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechainicio", nullable = false)
    private Date initDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechafin")
    private Date endDate;

    @Column(name = "respaldo", nullable = true, length = 200)
    private String back;

    @Column(name = "controlasistencia", nullable = true)
    private Integer attendanceControl;

    @Column(name = "activogenplan", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean activeForPayrollGeneration = true;

    @Column(name = "activogenplanfis", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean activeForTaxPayrollGeneration = false;

    @Column(name = "activofonpension", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean activePensionFund = false;

    @Column(name = "codregfonpension", length = 250)
    @Length(max = 250)
    private String pensionFundRegistrationCode;

    @Column(name = "AUTOMODIFCONTRATO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private boolean contractModificationAuthorization;

    @Column(name = "CODMODIFCONTRATO", length = 6)
    @Length(min = 6, max = 6)
    private String modificationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idinstfonpension", referencedColumnName = "identidadbensocial")
    private SocialWelfareEntity pensionFundOrganization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idinstsegsocial", referencedColumnName = "identidadbensocial")
    private SocialWelfareEntity socialSecurityOrganization;

    @Column(name = "codregsegsocial", length = 250)
    @Length(max = 250)
    private String socialSecurityRegistrationCode;

    @ManyToOne
    @JoinColumn(name = "idciclo", nullable = false)
    private Cycle cycle;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "MONTOGLOBALLABORAL", precision = 16, scale = 6)
    private BigDecimal occupationalGlobalAmount;

    @Column(name = "HABERBASICOLABORAL", precision = 16, scale = 6)
    private BigDecimal occupationalBasicAmount;

    @Column(name = "ACADEMICO")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean academic = false;

    @Column(name = "ESPECIAL")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean special = false;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Contract() {
    }

    public Contract(Employee employee) {
        this.employee = employee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Integer getNumberOfContract() {
        return numberOfContract;
    }

    public void setNumberOfContract(Integer numberOfContract) {
        this.numberOfContract = numberOfContract;
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

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public Integer getAttendanceControl() {
        return attendanceControl;
    }

    public void setAttendanceControl(Integer attendanceControl) {
        this.attendanceControl = attendanceControl;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public ContractMode getContractMode() {
        return contractMode;
    }

    public void setContractMode(ContractMode contractMode) {
        this.contractMode = contractMode;
    }

    public ContractState getContractState() {
        return contractState;
    }

    public void setContractState(ContractState contractState) {
        this.contractState = contractState;
    }

    public List<SpecialDate> getSpecialDates() {
        return specialDates;
    }

    public void setSpecialDates(List<SpecialDate> specialDates) {
        this.specialDates = specialDates;
    }

    public List<JobContract> getJobContractList() {
        return jobContractList;
    }

    public void setJobContractList(List<JobContract> jobContractList) {
        this.jobContractList = jobContractList;
    }

    public Boolean getActiveForPayrollGeneration() {
        return activeForPayrollGeneration;
    }

    public void setActiveForPayrollGeneration(Boolean activeForPayrollGeneration) {
        this.activeForPayrollGeneration = activeForPayrollGeneration;
    }

    public Boolean getActiveForTaxPayrollGeneration() {
        return activeForTaxPayrollGeneration;
    }

    public void setActiveForTaxPayrollGeneration(Boolean activeForTaxPayrollGeneration) {
        this.activeForTaxPayrollGeneration = activeForTaxPayrollGeneration;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Cycle getCycle() {
        return cycle;
    }

    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    public String getModificationCode() {
        return modificationCode;
    }

    public void setModificationCode(String modificationCode) {
        this.modificationCode = modificationCode;
    }

    public Boolean getActivePensionFund() {
        return activePensionFund;
    }

    public void setActivePensionFund(Boolean activePensionFund) {
        this.activePensionFund = activePensionFund;
    }

    public String getPensionFundRegistrationCode() {
        return pensionFundRegistrationCode;
    }

    public void setPensionFundRegistrationCode(String pensionFundRegistrationCode) {
        this.pensionFundRegistrationCode = pensionFundRegistrationCode;
    }

    public SocialWelfareEntity getPensionFundOrganization() {
        return pensionFundOrganization;
    }

    public void setPensionFundOrganization(SocialWelfareEntity pensionFundOrganization) {
        this.pensionFundOrganization = pensionFundOrganization;
    }

    public SocialWelfareEntity getSocialSecurityOrganization() {
        return socialSecurityOrganization;
    }

    public void setSocialSecurityOrganization(SocialWelfareEntity socialSecurityOrganization) {
        this.socialSecurityOrganization = socialSecurityOrganization;
    }

    public String getSocialSecurityRegistrationCode() {
        return socialSecurityRegistrationCode;
    }

    public void setSocialSecurityRegistrationCode(String socialSecurityRegistrationCode) {
        this.socialSecurityRegistrationCode = socialSecurityRegistrationCode;
    }

    public boolean getContractModificationAuthorization() {
        return contractModificationAuthorization;
    }

    public void setContractModificationAuthorization(boolean contractModificationAuthorization) {
        this.contractModificationAuthorization = contractModificationAuthorization;
    }

    public BigDecimal getOccupationalGlobalAmount() {
        return occupationalGlobalAmount;
    }

    public void setOccupationalGlobalAmount(BigDecimal occupationalGlobalAmount) {
        this.occupationalGlobalAmount = occupationalGlobalAmount;
    }

    public Boolean getAcademic() {
        return academic;
    }

    public void setAcademic(Boolean academic) {
        this.academic = academic;
    }

    public BigDecimal getOccupationalBasicAmount() {
        return occupationalBasicAmount;
    }

    public void setOccupationalBasicAmount(BigDecimal occupationalBasicAmount) {
        this.occupationalBasicAmount = occupationalBasicAmount;
    }

    public Boolean getSpecial() {
        return special;
    }

    public void setSpecial(Boolean special) {
        this.special = special;
    }
}