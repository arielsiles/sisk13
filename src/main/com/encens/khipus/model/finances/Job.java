package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Charge;
import com.encens.khipus.model.employees.JobCategory;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity for Job
 *
 * @author: Ariel Siles Encinas
 */

@NamedQueries(
        {
                @NamedQuery(name = "Job.findAll", query = "select o from Job o "),
                @NamedQuery(name = "Job.findByOrganizationalUnit", query = "select j from Job j where " +
                        "j.organizationalUnit.id =:organizationalUnitId order by j.id "),
                @NamedQuery(name = "Job.findByOrgUnitCategorySectorAndSalary", query = "select j from Job j where " +
                        "j.organizationalUnit =:organizationalUnit and j.jobCategory.sector=:sector and j.jobCategory=:jobCategory " +
                        "and j.salary=:salary order by j.id "),
                @NamedQuery(name = "Job.findByGeneratedPayollAndEmployee", query =
                        "select distinct cr.horaryBandContract.jobContract.job" +
                                " from ControlReport cr" +
                                " where cr.generatedPayroll=:generatedPayroll and cr.horaryBandContract.jobContract.contract.employee=:employee"),
                @NamedQuery(name = "Job.findByGestionPayollAndEmployee", query =
                        "select max(job.id)" +
                                " from JobContract jobContract" +
                                " left join jobContract.contract contract" +
                                " left join contract.employee employee" +
                                " left join jobContract.job job" +
                                " left join job.jobCategory jobCategory" +
                                " left join job.organizationalUnit organizationalUnit" +
                                " left join organizationalUnit.businessUnit businessUnit" +
                                " where employee=:employee and contract.activeForPayrollGeneration=:activeForPayrollGeneration" +
                                " and ((contract.initDate <=:endDate and contract.endDate is null ) " +
                                "or (contract.initDate<=:initDate and contract.endDate>=:endDate) " +
                                "or (contract.initDate>=:initDate and contract.initDate<=:endDate) " +
                                "or (contract.endDate>=:initDate and contract.endDate<=:endDate))" +
                                " and jobCategory=:jobCategory" +
                                " and businessUnit=:businessUnit"),
                @NamedQuery(name = "Job.findByOrganizationalUnitCycleEmployeeCode", query =
                        "select distinct job from Job job " +
                                " left join job.organizationalUnit organizationalUnit " +
                                " left join job.jobContractList jobContract" +
                                " left join jobContract.contract contract" +
                                " left join contract.cycle cycle" +
                                " left join contract.employee employee" +
                                " where organizationalUnit =:organizationalUnit and cycle =:cycle and employee.employeeCode =:employeeCode"),
                @NamedQuery(name = "Job.cleanJobByIdList",
                        query = "DELETE FROM Job job " +
                                " WHERE job.id in(:jobIdList) ")


        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Job.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "puesto",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "puesto")
public class Job implements BaseModel {

    @Id
    @Column(name = "idpuesto", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Job.tableGenerator")
    private Long id;

    @Column(name = "idcargo", updatable = false, insertable = false)
    private Long chargeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcargo", nullable = true)
    private Charge charge;

    @Column(name = "idcategoriapuesto", updatable = false, insertable = false)
    private Long jobCategoryId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcategoriapuesto", nullable = false)
    private JobCategory jobCategory;

    @Column(name = "idsueldo", updatable = false, insertable = false)
    private Long salaryId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idsueldo", nullable = false)
    private Salary salary;

    @Column(name = "idunidadorganizacional", updatable = false, insertable = false)
    private Long organizationalUnitId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadorganizacional", nullable = false)
    private OrganizationalUnit organizationalUnit;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<JobContract> jobContractList = new ArrayList<JobContract>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Job() {
    }

    public Job(Salary salary) {
        this.salary = salary;
    }

    public Job(Job job) {
        setCharge(job.getCharge());
        setJobCategory(job.getJobCategory());
        setSalary(job.getSalary());
        setOrganizationalUnit(job.getOrganizationalUnit());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
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

    public JobCategory getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategory jobCategory) {
        this.jobCategory = jobCategory;
    }

    public Salary getSalary() {
        return salary;
    }

    public void setSalary(Salary salary) {
        this.salary = salary;
    }

    public OrganizationalUnit getOrganizationalUnit() {
        return organizationalUnit;
    }

    public void setOrganizationalUnit(OrganizationalUnit organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
    }

    public List<JobContract> getJobContractList() {
        return jobContractList;
    }

    public void setJobContractList(List<JobContract> jobContractList) {
        this.jobContractList = jobContractList;
    }

    public Long getChargeId() {
        return chargeId;
    }

    public void setChargeId(Long chargeId) {
        this.chargeId = chargeId;
    }

    public Long getJobCategoryId() {
        return jobCategoryId;
    }

    public void setJobCategoryId(Long jobCategoryId) {
        this.jobCategoryId = jobCategoryId;
    }

    public Long getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(Long salaryId) {
        this.salaryId = salaryId;
    }

    public Long getOrganizationalUnitId() {
        return organizationalUnitId;
    }

    public void setOrganizationalUnitId(Long organizationalUnitId) {
        this.organizationalUnitId = organizationalUnitId;
    }
}