package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for accounts
 *
 * @author:
 */

@NamedQueries({})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "AccountType.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tipocuenta",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "tipocuenta")
public class AccountType implements BaseModel {

    @Id
    @Column(name = "idtipocuenta", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "AccountType.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    @Length(max = 100)
    private String name;

    @Column(name = "dias", nullable = true)
    private Integer days;

    @Column(name = "inta", precision = 4, scale = 2, nullable = false)
    private BigDecimal inta;

    @Column(name = "intb", precision = 4, scale = 2, nullable = false)
    private BigDecimal intb;

    /** Un tipo inactivo deja de ofrecerse al abrir o renovar cuentas, pero no se borra. */
    @Column(name = "activo")
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean active = Boolean.TRUE;

    @Column(name = "tipo", updatable = false)
    @Enumerated(EnumType.STRING)
    private SavingType savingType;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /*
     * Las cinco cuentas contables estaban mapeadas insertable=false/updatable=false, es
     * decir de solo lectura: se cargaban por SQL. Se abren para que el ABM de tipos de
     * cuenta pueda darlas de alta y corregirlas, que es de donde salen las cuentas del
     * asiento de provision (CTACF_*) y del de renovacion (CTAP_*).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CTAP_MN", referencedColumnName = "CUENTA")
    private CashAccount cashAccountMn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CTAP_ME", referencedColumnName = "CUENTA")
    private CashAccount cashAccountMe;

    /** Se deja EAGER como estaba: lo leen VoucherCreateAction y AccountServiceBean. */
    @ManyToOne
    @JoinColumn(name = "CTAP_MV", referencedColumnName = "CUENTA")
    private CashAccount cashAccountMv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CTACF_MN", referencedColumnName = "CUENTA")
    private CashAccount cashAccountChargeMn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CTACF_ME", referencedColumnName = "CUENTA")
    private CashAccount cashAccountChargeMe;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public CashAccount getCashAccountMn() {
        return cashAccountMn;
    }

    public void setCashAccountMn(CashAccount cashAccountMn) {
        this.cashAccountMn = cashAccountMn;
    }

    public CashAccount getCashAccountMe() {
        return cashAccountMe;
    }

    public void setCashAccountMe(CashAccount cashAccountMe) {
        this.cashAccountMe = cashAccountMe;
    }

    public CashAccount getCashAccountMv() {
        return cashAccountMv;
    }

    public void setCashAccountMv(CashAccount cashAccountMv) {
        this.cashAccountMv = cashAccountMv;
    }

    public BigDecimal getInta() {
        return inta;
    }

    public void setInta(BigDecimal inta) {
        this.inta = inta;
    }

    public BigDecimal getIntb() {
        return intb;
    }

    public void setIntb(BigDecimal intb) {
        this.intb = intb;
    }

    public SavingType getSavingType() {
        return savingType;
    }

    public void setSavingType(SavingType savingType) {
        this.savingType = savingType;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public CashAccount getCashAccountChargeMn() {
        return cashAccountChargeMn;
    }

    public void setCashAccountChargeMn(CashAccount cashAccountChargeMn) {
        this.cashAccountChargeMn = cashAccountChargeMn;
    }

    public CashAccount getCashAccountChargeMe() {
        return cashAccountChargeMe;
    }

    public void setCashAccountChargeMe(CashAccount cashAccountChargeMe) {
        this.cashAccountChargeMe = cashAccountChargeMe;
    }
}
