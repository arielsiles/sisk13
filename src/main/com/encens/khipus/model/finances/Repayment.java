package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Employee;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for Exchange rate
 *
 * @author: Ariel Siles
 */


@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Repayment.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "reembolso",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "reembolso")
public class Repayment implements BaseModel {

    @Id
    @Column(name = "idreembolso", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Repayment.tableGenerator")
    private Long id;


    @Column(name = "fecha", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    @Column(name = "descripcion")
    @Lob
    private String description;

    @ManyToOne
    @JoinColumn(name = "idtiporeembolso", nullable = false)
    private RepaymentType repaymentType;

    @ManyToOne
    @JoinColumn(name = "idempleado", nullable = false)
    private Employee employee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RepaymentType getRepaymentType() {
        return repaymentType;
    }

    public void setRepaymentType(RepaymentType repaymentType) {
        this.repaymentType = repaymentType;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}