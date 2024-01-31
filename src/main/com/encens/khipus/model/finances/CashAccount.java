package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.customers.CreditState;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CashAccount
 *
 * @author
 * @version 2.0
 */

@NamedQueries({
        @NamedQuery(name = "CashAccount.findByAccountCode", query = "select ca from CashAccount ca where ca.accountCode=:accountCode"),
        @NamedQuery(name = "CashAccount.findCashAccountList", query = "select ca from CashAccount ca order by ca.accountCode"),
        @NamedQuery(name = "CashAccount.findCashAccountListByType", query = "select ca from CashAccount ca where ca.accountType=:accountType order by ca.accountCode"),
        @NamedQuery(name = "CashAccount.findAccountsUtil", query = "select ca from CashAccount ca where ca.util =:util"),
        @NamedQuery(name = "CashAccount.findByActiveAccount", query = "select ca from CashAccount ca where ca.accountCode=:accountCode and ca.active=:active")
})
@Entity
@EntityListeners({CompanyNumberListener.class})
@Table(name = "arcgms", schema = Constants.FINANCES_SCHEMA)
public class CashAccount implements BaseModel {

    @EmbeddedId
    private CashAccountPk id = new CashAccountPk();

    @Column(name = "no_cia", insertable = false, updatable = false)
    private String companyNumber;

    @Column(name = "cuenta", insertable = false, updatable = false)
    private String accountCode;

    @Column(name = "descri", length = 100)
    @Length(max = 100)
    private String description;

    @Column(name = "cta_raiz")
    private String rootAccountCode;

    @Column(name = "cta_niv3")
    private String accountLevel3Code;

    @Column(name = "est", length = 3)
    @Enumerated(EnumType.STRING)
    private CreditState state;

    /*@Column(name = "TIPO", length = 2, updatable = false)
    @Length(max = 2)
    private String accountType;*/

    @Column(name = "tipo")
    @Enumerated(EnumType.STRING)
    private CashAccountType accountType;


    @Column(name = "clase", length = 1)
    @Length(max = 1)
    private String accountClass;

    @Column(name = "ind_mov")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean movementAccount;

    @Column(name = "ind_presup")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean budgetAccount;

    @Column(name = "debitos", precision = 14, scale = 2)
    private BigDecimal debit;

    @Column(name = "creditos", precision = 14, scale = 2)
    private BigDecimal credit;

    @Column(name = "saldo_per_ant", precision = 15, scale = 2)
    private BigDecimal nationalBalancePreviousPeriod;

    @Column(name = "saldo_mes_ant", precision = 15, scale = 2)
    private BigDecimal nationalBalancePreviousMonth;

    @Column(name = "moneda")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", insertable = false, updatable = false),
            @JoinColumn(name = "cta_raiz", referencedColumnName = "cuenta", insertable = false, updatable = false)
    })
    private CashAccount rootCashAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", insertable = false, updatable = false),
            @JoinColumn(name = "cta_niv3", referencedColumnName = "cuenta", insertable = false, updatable = false)
    })
    private CashAccount cashAccountLeve3;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", insertable = false, updatable = false),
            @JoinColumn(name = "moneda", referencedColumnName = "cod_mon", insertable = false, updatable = false)
    })
    private FinancesCurrency financesCurrency;

    @Column(name = "activa", updatable = true)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean active;

    @Column(name = "util", updatable = true)
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean util;

    @Column(name = "nomutil", updatable = true)
    private String utilName;

    @Column(name = "f_inactiva")
    @Temporal(TemporalType.DATE)
    private Date inactiveDate;

    @Column(name = "permite_iva")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean allowIva;

    @Column(name = "saldo_per_ant_dol", precision = 20, scale = 6)
    private BigDecimal foreignBalancePreviousPeriod;

    @Column(name = "saldo_mes_ant_dol", precision = 20, scale = 6)
    private BigDecimal foreignBalancePreviousMonth;

    @Column(name = "debitos_dol", precision = 20, scale = 6)
    private BigDecimal foreignDebit;

    @Column(name = "creditos_dol", precision = 20, scale = 6)
    private BigDecimal foreignCredit;

    // contabilidad
    @Column(name = "permiso_con")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean hasAccountingPermission;

    //tessoreria
    @Column(name = "permiso_che")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean hasTreasuryPermission;

    //cuentas por pagar
    @Column(name = "permiso_cxp")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean hasPayableAccountsPermission;

    //activos fijos
    @Column(name = "permiso_afijo")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean hasFixedAssetsPermission;

    //inventarios
    @Column(name = "permiso_inv")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean hasWarehousePermission;

    //cuentas por cobrar
    @Column(name = "permiso_cxc")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean hasReceivableAccountsPermission;

    @Column(name = "exije_cc")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean hasCostCenter;

    @Column(name = "gru_cta", length = 6)
    @Length(max = 6)
    private String groupAccountCode;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<AccountingMovementDetail> accountingMovementDetailList = new ArrayList<AccountingMovementDetail>(0);

    public CashAccountPk getId() {
        return id;
    }

    public void setId(CashAccountPk id) {
        this.id = id;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccountClass() {
        return accountClass;
    }

    public void setAccountClass(String accountClass) {
        this.accountClass = accountClass;
    }

    public Boolean getMovementAccount() {
        return movementAccount;
    }

    public void setMovementAccount(Boolean movementAccount) {
        this.movementAccount = movementAccount;
    }

    public Boolean getBudgetAccount() {
        return budgetAccount;
    }

    public void setBudgetAccount(Boolean budgetAccount) {
        this.budgetAccount = budgetAccount;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }

    public BigDecimal getNationalBalancePreviousPeriod() {
        return nationalBalancePreviousPeriod;
    }

    public void setNationalBalancePreviousPeriod(BigDecimal nationalBalancePreviousPeriod) {
        this.nationalBalancePreviousPeriod = nationalBalancePreviousPeriod;
    }

    public BigDecimal getNationalBalancePreviousMonth() {
        return nationalBalancePreviousMonth;
    }

    public void setNationalBalancePreviousMonth(BigDecimal nationalBalancePreviousMonth) {
        this.nationalBalancePreviousMonth = nationalBalancePreviousMonth;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public FinancesCurrency getFinancesCurrency() {
        return financesCurrency;
    }

    public void setFinancesCurrency(FinancesCurrency financesCurrency) {
        this.financesCurrency = financesCurrency;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getInactiveDate() {
        return inactiveDate;
    }

    public void setInactiveDate(Date inactiveDate) {
        this.inactiveDate = inactiveDate;
    }

    public Boolean getAllowIva() {
        return allowIva;
    }

    public void setAllowIva(Boolean allowIva) {
        this.allowIva = allowIva;
    }

    public BigDecimal getForeignBalancePreviousPeriod() {
        return foreignBalancePreviousPeriod;
    }

    public void setForeignBalancePreviousPeriod(BigDecimal foreignBalancePreviousPeriod) {
        this.foreignBalancePreviousPeriod = foreignBalancePreviousPeriod;
    }

    public BigDecimal getForeignBalancePreviousMonth() {
        return foreignBalancePreviousMonth;
    }

    public void setForeignBalancePreviousMonth(BigDecimal foreignBalancePreviousMonth) {
        this.foreignBalancePreviousMonth = foreignBalancePreviousMonth;
    }

    public BigDecimal getForeignDebit() {
        return foreignDebit;
    }

    public void setForeignDebit(BigDecimal foreignDebit) {
        this.foreignDebit = foreignDebit;
    }

    public BigDecimal getForeignCredit() {
        return foreignCredit;
    }

    public void setForeignCredit(BigDecimal foreignCredit) {
        this.foreignCredit = foreignCredit;
    }

    public Boolean getHasAccountingPermission() {
        return hasAccountingPermission;
    }

    public void setHasAccountingPermission(Boolean hasAccountingPermission) {
        this.hasAccountingPermission = hasAccountingPermission;
    }

    public Boolean getHasTreasuryPermission() {
        return hasTreasuryPermission;
    }

    public void setHasTreasuryPermission(Boolean hasTreasuryPermission) {
        this.hasTreasuryPermission = hasTreasuryPermission;
    }

    public Boolean getHasPayableAccountsPermission() {
        return hasPayableAccountsPermission;
    }

    public void setHasPayableAccountsPermission(Boolean hasPayableAccountsPermission) {
        this.hasPayableAccountsPermission = hasPayableAccountsPermission;
    }

    public Boolean getHasFixedAssetsPermission() {
        return hasFixedAssetsPermission;
    }

    public void setHasFixedAssetsPermission(Boolean hasFixedAssetsPermission) {
        this.hasFixedAssetsPermission = hasFixedAssetsPermission;
    }

    public Boolean getHasWarehousePermission() {
        return hasWarehousePermission;
    }

    public void setHasWarehousePermission(Boolean hasWarehousePermission) {
        this.hasWarehousePermission = hasWarehousePermission;
    }

    public Boolean getHasReceivableAccountsPermission() {
        return hasReceivableAccountsPermission;
    }

    public void setHasReceivableAccountsPermission(Boolean hasReceivableAccountsPermission) {
        this.hasReceivableAccountsPermission = hasReceivableAccountsPermission;
    }

    public Boolean getHasCostCenter() {
        return hasCostCenter;
    }

    public void setHasCostCenter(Boolean hasCostCenter) {
        this.hasCostCenter = hasCostCenter;
    }

    public String getGroupAccountCode() {
        return groupAccountCode;
    }

    public void setGroupAccountCode(String groupAccountCode) {
        this.groupAccountCode = groupAccountCode;
    }

    public List<AccountingMovementDetail> getAccountingMovementDetailList() {
        return accountingMovementDetailList;
    }

    public void setAccountingMovementDetailList(List<AccountingMovementDetail> accountingMovementDetailList) {
        this.accountingMovementDetailList = accountingMovementDetailList;
    }

    public String getFullName() {
        return getAccountCode() + " - " + getDescription();
    }

    public String getFullNameAndCurrency() {
        return FormatUtils.toAcronym(FormatUtils.toCodeName(getAccountCode(), getDescription()), FormatUtils.toCodeName(getFinancesCurrency().getAcronym(), getFinancesCurrency().getDescription()));
    }

    @Override
    public String toString() {
        return "CashAccount{" +
                "accountCode='" + accountCode + '\'' +
                ", companyNumber='" + companyNumber + '\'' +
                ", description='" + description + '\'' +
                ", accountType='" + getAccountType() + '\'' +
                ", accountClass='" + accountClass + '\'' +
                ", movementAccount='" + movementAccount + '\'' +
                ", budgetAccount='" + budgetAccount + '\'' +
                ", debit=" + debit +
                ", credit=" + credit +
                ", nationalBalancePreviousPeriod=" + nationalBalancePreviousPeriod +
                ", nationalBalancePreviousMonth=" + nationalBalancePreviousMonth +
                ", currency='" + currency + '\'' +
                ", active='" + active + '\'' +
                ", inactiveDate=" + inactiveDate +
                ", allowIva=" + allowIva +
                ", foreignBalancePreviousPeriod=" + foreignBalancePreviousPeriod +
                ", foreignBalancePreviousMonth=" + foreignBalancePreviousMonth +
                ", foreignDebit=" + foreignDebit +
                ", foreignCredit=" + foreignCredit +
                ", hasAccountingPermission=" + hasAccountingPermission +
                ", hasTreasuryPermission=" + hasTreasuryPermission +
                ", hasPayableAccountsPermission=" + hasPayableAccountsPermission +
                ", hasFixedAssetsPermission=" + hasFixedAssetsPermission +
                ", hasWarehousePermission=" + hasWarehousePermission +
                ", hasReceivableAccountsPermission=" + hasReceivableAccountsPermission +
                ", hasCostCenter=" + hasCostCenter +
                ", groupAccountCode=" + groupAccountCode +
                '}';
    }

    public CashAccount getRootCashAccount() {
        return rootCashAccount;
    }

    public void setRootCashAccount(CashAccount rootCashAccount) {
        this.rootCashAccount = rootCashAccount;
    }

    public String getRootAccountCode() {
        return rootAccountCode;
    }

    public void setRootAccountCode(String rootAccountCode) {
        this.rootAccountCode = rootAccountCode;
    }

    public String getAccountLevel3Code() {
        return accountLevel3Code;
    }

    public void setAccountLevel3Code(String accountLevel3Code) {
        this.accountLevel3Code = accountLevel3Code;
    }

    public CashAccount getCashAccountLeve3() {
        return cashAccountLeve3;
    }

    public void setCashAccountLeve3(CashAccount cashAccountLeve3) {
        this.cashAccountLeve3 = cashAccountLeve3;
    }

    public CashAccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(CashAccountType accountType) {
        this.accountType = accountType;
    }

    public CreditState getState() {
        return state;
    }

    public void setState(CreditState state) {
        this.state = state;
    }

    public Boolean getUtil() {
        return util;
    }

    public void setUtil(Boolean util) {
        this.util = util;
    }

    public String getUtilName() {
        return utilName;
    }

    public void setUtilName(String utilName) {
        this.utilName = utilName;
    }

    /*public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }*/

}
