package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for Cash box transaction
 *
 * @author:
 */

@NamedQueries(
        {
                @NamedQuery(name = "CashBoxTransaction.findByCashBox", query = "select ct from CashBoxTransaction ct where ct.cashBox =:cashBox and ct.closingDate is null"),
                @NamedQuery(name = "CashBoxTransaction.findMaxClosingDate", query = "select max(ct.closingDate) from CashBoxTransaction ct where ct.cashBox =:cashBox"),
                @NamedQuery(name = "CashBoxTransaction.findByCashBoxUser", query = "select ct from CashBoxTransaction ct where ct.cashBoxUser =:user and ct.closingDate is null")}
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "CashBoxTransaction.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "transaccioncaja",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "transaccioncaja")
public class CashBoxTransaction implements BaseModel {

    @Id
    @Column(name = "idtransaccion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CashBoxTransaction.tableGenerator")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idcaja", referencedColumnName = "idcaja", nullable = false)
    private CashBox cashBox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuariocontrol", referencedColumnName = "idusuario")
    private User controlUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuariocaja", referencedColumnName = "idusuario", nullable = false)
    private User cashBoxUser;

    @Column(name = "fechaapertura", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date openingDate;

    @Column(name = "fechacierre")
    @Temporal(TemporalType.TIMESTAMP)
    private Date closingDate;

    @Column(name = "importetotal", precision = 13, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "importefalla", precision = 13, scale = 2)
    private BigDecimal faultAmount;

    @Column(name = "importemodificado", precision = 13, scale = 2)
    private BigDecimal modifiedAmount;

    @Column(name = "descripcionfalla")
    @Lob
    private String faultDescription;

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

    public CashBox getCashBox() {
        return cashBox;
    }

    public void setCashBox(CashBox cashBox) {
        this.cashBox = cashBox;
    }

    public User getControlUser() {
        return controlUser;
    }

    public void setControlUser(User controlUser) {
        this.controlUser = controlUser;
    }

    public User getCashBoxUser() {
        return cashBoxUser;
    }

    public void setCashBoxUser(User cashBoxUser) {
        this.cashBoxUser = cashBoxUser;
    }

    public Date getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(Date openingDate) {
        this.openingDate = openingDate;
    }

    public Date getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(Date closingDate) {
        this.closingDate = closingDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getFaultAmount() {
        return faultAmount;
    }

    public void setFaultAmount(BigDecimal faultAmount) {
        this.faultAmount = faultAmount;
    }

    public BigDecimal getModifiedAmount() {
        return modifiedAmount;
    }

    public void setModifiedAmount(BigDecimal modifiedAmount) {
        this.modifiedAmount = modifiedAmount;
    }

    public String getFaultDescription() {
        return faultDescription;
    }

    public void setFaultDescription(String faultDescription) {
        this.faultDescription = faultDescription;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
