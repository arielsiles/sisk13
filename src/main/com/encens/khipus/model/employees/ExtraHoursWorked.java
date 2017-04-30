package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.JobContract;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ExtraHoursWorked.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "HorasExtra",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "ExtraHoursWorked.findByPayrollGenerationCycleAndJobCategoryAndEmployee",
                query = "select element from ExtraHoursWorked element where element.payrollGenerationCycle=:payrollGenerationCycle " +
                        "and element.jobContract.job.jobCategory=:jobCategory and element.jobContract.contract.employee=:employee "),
        @NamedQuery(name = "ExtraHoursWorked.findByPayrollGenerationCycle_II",
                query = "select element from ExtraHoursWorked element join fetch element.jobContract where element.payrollGenerationCycle=:payrollGenerationCycle"),
        @NamedQuery(name = "ExtraHoursWorked.findByPayrollGenerationCycleAndJobCategory",
                query = "select extraHoursWorked.jobContractId, extraHoursWorked from ExtraHoursWorked extraHoursWorked " +
                        "where extraHoursWorked.payrollGenerationCycle=:payrollGenerationCycle and jobContract.job.jobCategory=:jobCategory"),
        @NamedQuery(name = "ExtraHoursWorked.countByPayrollGenerationCycle",
                query = "select count(element) from ExtraHoursWorked element where element.payrollGenerationCycle=:payrollGenerationCycle")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "HORASEXTRA")
public class ExtraHoursWorked implements BaseModel {
    @Id
    @Column(name = "IDHORASEXTRA", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ExtraHoursWorked.tableGenerator")
    private Long id;

    @Column(name = "HORASEXTRA", nullable = false, precision = 13, scale = 2)
    private BigDecimal extraHours;

    @Column(name = "TOTALPAGADO", nullable = false, precision = 13, scale = 2)
    private BigDecimal totalPaid;


    @Column(name = "IDCONTRATOPUESTO", nullable = false, updatable = false, insertable = false)
    private Long jobContractId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCONTRATOPUESTO", nullable = false, updatable = false, insertable = true)
    private JobContract jobContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCICLOGENERACIONPLANILLA", nullable = false, updatable = false, insertable = true)
    private PayrollGenerationCycle payrollGenerationCycle;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getExtraHours() {
        return extraHours;
    }

    public void setExtraHours(BigDecimal extraHours) {
        this.extraHours = extraHours;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public JobContract getJobContract() {
        return jobContract;
    }

    public void setJobContract(JobContract jobContract) {
        this.jobContract = jobContract;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
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

    public Long getJobContractId() {
        return jobContractId;
    }

    public void setJobContractId(Long jobContractId) {
        this.jobContractId = jobContractId;
    }
}
