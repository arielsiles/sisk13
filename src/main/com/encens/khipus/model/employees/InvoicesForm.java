package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.JobContract;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * @author
 * @version 2.26
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "InvoicesForm.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "formfactura",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "InvoicesForm.findByPayrollGenerationCycle",
                query = "select element from InvoicesForm element where element.payrollGenerationCycle = :payrollGenerationCycle and element.jobContract in(:jobContracts)"),
        @NamedQuery(name = "InvoicesForm.findInvoicesFormMapByPayrollGenerationCycleAndEmployeeList",
                query = "select invoicesForm.jobContract.contract.employeeId, invoicesForm from InvoicesForm invoicesForm " +
                        "where invoicesForm.payrollGenerationCycle = :payrollGenerationCycle and invoicesForm.employee.id in(:employeeIdList)"),
        @NamedQuery(name = "InvoicesForm.countByPayrollGenerationCycle",
                query = "select count(element) from InvoicesForm element where element.payrollGenerationCycle=:payrollGenerationCycle")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "formfactura")
public class InvoicesForm implements BaseModel {
    @Id
    @Column(name = "idformfactura", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "InvoicesForm.tableGenerator")
    private Long id;

    @Column(name = "fechapresentacion", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date presentationDate;

    @Column(name = "creditofiscal", nullable = false)
    private Integer fiscalCredit;

    @Column(name = "idcontratopuesto", nullable = false, updatable = false, insertable = false)
    private Long jobContractId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontratopuesto", nullable = false, updatable = false, insertable = true)
    private JobContract jobContract;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleado", referencedColumnName = "idempleado", nullable = false)
    @NotNull
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idciclogeneracionplanilla", nullable = false, updatable = false, insertable = true)
    private PayrollGenerationCycle payrollGenerationCycle;

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

    public Date getPresentationDate() {
        return presentationDate;
    }

    public void setPresentationDate(Date presentationDate) {
        this.presentationDate = presentationDate;
    }

    public Integer getFiscalCredit() {
        return fiscalCredit;
    }

    public void setFiscalCredit(Integer fiscalCredit) {
        this.fiscalCredit = fiscalCredit;
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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
