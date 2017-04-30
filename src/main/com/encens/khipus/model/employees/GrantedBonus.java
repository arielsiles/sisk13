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
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "GrantedBonus.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "BonoConseguido",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({
        @NamedQuery(name = "GrantedBonus.findAll", query = "select o from GrantedBonus o "),
        @NamedQuery(name = "GrantedBonus.findByPayrollGenerationCycle", query = "select o from GrantedBonus o " +
                " where o.payrollGenerationCycle=:payrollGenerationCycle order by o.amount"),
        @NamedQuery(name = "GrantedBonus.findByPayrollGenerationCycleJobContracts",
                query = "select element.jobContractId from GrantedBonus element where element.payrollGenerationCycle =:payrollGenerationCycle"),
        @NamedQuery(name = "GrantedBonus.findByPayrollGenerationCycleAndJobCategory",
                query = "select grantedBonus.jobContractId, grantedBonus from GrantedBonus grantedBonus " +
                        "where grantedBonus.payrollGenerationCycle =:payrollGenerationCycle and grantedBonus.jobContract.job.jobCategory=:jobCategory"),
        @NamedQuery(name = "GrantedBonus.findByJobContractId",
                query = "select element from GrantedBonus element where element.payrollGenerationCycle =:payrollGenerationCycle and element.jobContractId =:jobContractId "),
        @NamedQuery(name = "GrantedBonus.countByPayrollGenerationCycle",
                query = "select count(element) from GrantedBonus element where element.payrollGenerationCycle=:payrollGenerationCycle")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "BONOCONSEGUIDO")
public class GrantedBonus implements BaseModel {
    @Id
    @Column(name = "IDBONOCONSEGUIDO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "GrantedBonus.tableGenerator")
    private Long id;

    @Column(name = "MONTO", nullable = false, precision = 13, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "IDBONO", nullable = false, updatable = false, insertable = true)
    private Bonus bonus;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public Bonus getBonus() {
        return bonus;
    }

    public void setBonus(Bonus bonus) {
        this.bonus = bonus;
    }

    public Long getJobContractId() {
        return jobContractId;
    }

    public void setJobContractId(Long jobContractId) {
        this.jobContractId = jobContractId;
    }
}
