package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Cash box type
 *
 * @author:
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "CashBoxType.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tipocaja",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "tipocaja", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "nombre"}))
public class CashBoxType implements BaseModel {

    @Id
    @Column(name = "idtipocaja", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CashBoxType.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "cuentacaja")
    private String cashAccountBoxCode;

    @Column(name = "cuentaingreso")
    private String cashAccountIncomeCode;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cuentacaja", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount cashAccountBox;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cuentaingreso", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount cashAccountIncome;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "cuentaxcobrar", referencedColumnName = "cuenta", nullable = false, updatable = false, insertable = false)
    })
    private CashAccount cashAccountReceivable;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public CashAccount getCashAccountBox() {
        return cashAccountBox;
    }

    public void setCashAccountBox(CashAccount cashAccountBox) {
        this.cashAccountBox = cashAccountBox;
    }

    public CashAccount getCashAccountIncome() {
        return cashAccountIncome;
    }

    public void setCashAccountIncome(CashAccount cashAccountIncome) {
        this.cashAccountIncome = cashAccountIncome;
    }

    public String getCashAccountBoxCode() {
        return cashAccountBoxCode;
    }

    public void setCashAccountBoxCode(String cashAccountBoxCode) {
        this.cashAccountBoxCode = cashAccountBoxCode;
    }

    public String getCashAccountIncomeCode() {
        return cashAccountIncomeCode;
    }

    public void setCashAccountIncomeCode(String cashAccountIncomeCode) {
        this.cashAccountIncomeCode = cashAccountIncomeCode;
    }

    public CashAccount getCashAccountReceivable() {
        return cashAccountReceivable;
    }

    public void setCashAccountReceivable(CashAccount cashAccountReceivable) {
        this.cashAccountReceivable = cashAccountReceivable;
    }
}
