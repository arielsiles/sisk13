package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.8
 */
@Entity
@EntityListeners(UpperCaseStringListener.class)
@Table(name = "cg_detplanti", schema = Constants.FINANCES_SCHEMA)
public class AccountingTemplateDetail implements BaseModel {

    @Id
    @Column(name = "ID_CG_DETPLANTI", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_PLANTI", referencedColumnName = "COD_PLANTI", updatable = false, insertable = false)
    })
    private AccountingTemplate accountingTemplate;

    @ManyToOne(optional = true)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "CUENTA", referencedColumnName = "CUENTA", updatable = false, insertable = false)
    })
    private CashAccount cashAccount;


    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_CC", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;


    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "COD_UNI", referencedColumnName = "COD_UEJ", updatable = false, insertable = false)
    })
    private FinancesExecutorUnit executorUnit;

    @Column(name = "REF", length = 60)
    private String reference;

    @Column(name = "DEBE", precision = 16, scale = 2)
    private BigDecimal debit;

    @Column(name = "HABER", precision = 16, scale = 2)
    private BigDecimal credit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountingTemplate getAccountingTemplate() {
        return accountingTemplate;
    }

    public void setAccountingTemplate(AccountingTemplate accountingTemplate) {
        this.accountingTemplate = accountingTemplate;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public FinancesExecutorUnit getExecutorUnit() {
        return executorUnit;
    }

    public void setExecutorUnit(FinancesExecutorUnit executorUnit) {
        this.executorUnit = executorUnit;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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
}
