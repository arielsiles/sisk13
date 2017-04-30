package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for Salary History
 *
 * @author
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "SalaryHistory.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "historialsueldos",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "historialsueldos")
public class SalaryHistory implements BaseModel {

    @Id
    @Column(name = "idhistorialsueldos", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SalaryHistory.tableGenerator")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechainicio", nullable = false)
    private Date initDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fechafin")
    private Date endDate;

    @Column(name = "estado", length = 200)
    @Length(max = 200)
    private String state;

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

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /* public Salary getSalary() {
       return salary;
   }

   public void setSalary(Salary salary) {
       this.salary = salary;
   }

   public Job getJob() {
       return job;
   }

   public void setJob(Job job) {
       this.job = job;
   } */

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}