package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for FixedAssetSubGroup
 *
 * @author
 * @version 2.0
 */
@NamedQueries({
        @NamedQuery(name = "FixedAssetSubGroup.countByCode",
                query = "select count(sg.fixedAssetSubGroupCode) " +
                        "from FixedAssetSubGroup sg " +
                        "where lower(sg.fixedAssetSubGroupCode)=lower(:fixedAssetSubGroupCode) " +
                        "and lower(sg.id.fixedAssetGroupCode)=lower(:fixedAssetGroupCode) " +
                        "and sg.id.companyNumber=:companyNumber")
})
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "af_subgrupos", schema = Constants.FINANCES_SCHEMA)
public class FixedAssetSubGroup implements BaseModel {

    @EmbeddedId
    private FixedAssetSubGroupPk id = new FixedAssetSubGroupPk();

    @Column(name = "subgrupo", nullable = false, insertable = false, updatable = false)
    private String fixedAssetSubGroupCode;

    @Column(name = "descri", nullable = false)
    @Length(max = 100)
    @NotNull
    private String description;

    @Column(name = "tasa_dep", nullable = false, precision = 6, scale = 2)
    @NotNull
    private BigDecimal depreciationRate;

    @Column(name = "duracion", nullable = false, updatable = false)
    @NotNull
    private Integer duration;

    @Column(name = "cta_vo", nullable = false, updatable = false, length = 20)
    @Length(max = 20)
    @NotNull
    private String originalValueAccount;

    @Column(name = "ctadavo", nullable = false, updatable = false, length = 20)
    @Length(max = 20)
    @NotNull
    private String accumulatedDepreciationAccount;

    @Column(name = "ctagavo", nullable = false, updatable = false, length = 20)
    @Length(max = 20)
    @NotNull
    private String expenseAccount;

    @Column(name = "cta_alm", nullable = false, updatable = false, length = 20)
    @Length(max = 20)
    @NotNull
    private String warehouseAccount;

    @Column(name = "CTAMEJ", updatable = false)
    @Length(max = 20)
    private String improvementAccount;

    @Column(name = "no_acti", nullable = false, updatable = false, length = 6)
    @Length(max = 6)
    @NotNull
    private String fixedAssetNumber;

    @Column(name = "desecho", precision = 12, scale = 2)
    private BigDecimal rubbish;

    @Column(name = "detalle", length = 250)
    @Length(max = 250)
    private String detail;

    @Column(name = "requierepartes")
    @Type(type = com.encens.khipus.model.usertype.StringBooleanUserType.NAME)
    private Boolean requireParts;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "grupo", nullable = false, insertable = false, updatable = false)
    })
    private FixedAssetGroup fixedAssetGroup;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false, referencedColumnName = "no_cia"),
            @JoinColumn(name = "cta_vo", nullable = false, insertable = false, updatable = false, referencedColumnName = "cuenta")
    })
    private CashAccount originalValueCashAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false, referencedColumnName = "no_cia"),
            @JoinColumn(name = "ctadavo", nullable = false, insertable = false, updatable = false, referencedColumnName = "cuenta")
    })
    private CashAccount accumulatedDepreciationCashAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false, referencedColumnName = "no_cia"),
            @JoinColumn(name = "ctagavo", nullable = false, insertable = false, updatable = false, referencedColumnName = "cuenta")
    })
    private CashAccount expenseCashAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false, referencedColumnName = "no_cia"),
            @JoinColumn(name = "cta_alm", nullable = false, insertable = false, updatable = false, referencedColumnName = "cuenta")
    })
    private CashAccount warehouseCashAccount;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false, referencedColumnName = "no_cia"),
            @JoinColumn(name = "CTAMEJ", nullable = false, insertable = false, updatable = false, referencedColumnName = "cuenta")
    })
    private CashAccount improvementCashAccount;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDepreciationRate() {
        return depreciationRate;
    }

    public void setDepreciationRate(BigDecimal depreciationRate) {
        this.depreciationRate = depreciationRate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getOriginalValueAccount() {
        return originalValueAccount;
    }

    public void setOriginalValueAccount(String originalValueAccount) {
        this.originalValueAccount = originalValueAccount;
    }

    public String getAccumulatedDepreciationAccount() {
        return accumulatedDepreciationAccount;
    }

    public void setAccumulatedDepreciationAccount(String accumulatedDepreciationAccount) {
        this.accumulatedDepreciationAccount = accumulatedDepreciationAccount;
    }

    public String getExpenseAccount() {
        return expenseAccount;
    }

    public void setExpenseAccount(String expenseAccount) {
        this.expenseAccount = expenseAccount;
    }

    public String getFixedAssetNumber() {
        return fixedAssetNumber;
    }

    public void setFixedAssetNumber(String fixedAssetNumber) {
        this.fixedAssetNumber = fixedAssetNumber;
    }

    public FixedAssetGroup getFixedAssetGroup() {
        return fixedAssetGroup;
    }

    public void setFixedAssetGroup(FixedAssetGroup fixedAssetGroup) {
        this.fixedAssetGroup = fixedAssetGroup;
        this.id.setFixedAssetGroupCode(fixedAssetGroup != null ? fixedAssetGroup.getId().getGroupCode() : null);
    }

    public FixedAssetSubGroupPk getId() {
        return id;
    }

    public void setId(FixedAssetSubGroupPk id) {
        this.id = id;
    }

    public CashAccount getOriginalValueCashAccount() {
        return originalValueCashAccount;
    }

    public void setOriginalValueCashAccount(CashAccount originalValueCashAccount) {
        this.originalValueCashAccount = originalValueCashAccount;
        this.setOriginalValueAccount(originalValueCashAccount != null ? originalValueCashAccount.getAccountCode() : null);
    }

    public String getWarehouseAccount() {
        return warehouseAccount;
    }

    public void setWarehouseAccount(String warehouseAccount) {
        this.warehouseAccount = warehouseAccount;
    }

    public CashAccount getWarehouseCashAccount() {
        return warehouseCashAccount;
    }

    public void setWarehouseCashAccount(CashAccount warehouseCashAccount) {
        this.warehouseCashAccount = warehouseCashAccount;
        setWarehouseAccount(warehouseCashAccount != null ? warehouseCashAccount.getAccountCode() : null);

    }

    public CashAccount getAccumulatedDepreciationCashAccount() {
        return accumulatedDepreciationCashAccount;
    }

    public void setAccumulatedDepreciationCashAccount(CashAccount accumulatedDepreciationCashAccount) {
        this.accumulatedDepreciationCashAccount = accumulatedDepreciationCashAccount;
        setAccumulatedDepreciationAccount(accumulatedDepreciationCashAccount != null ? accumulatedDepreciationCashAccount.getAccountCode() : null);
    }

    public CashAccount getExpenseCashAccount() {
        return expenseCashAccount;
    }

    public void setExpenseCashAccount(CashAccount expenseCashAccount) {
        this.expenseCashAccount = expenseCashAccount;
        setExpenseAccount(expenseCashAccount != null ? expenseCashAccount.getAccountCode() : null);
    }

    public String getImprovementAccount() {
        return improvementAccount;
    }

    public void setImprovementAccount(String improvementAccount) {
        this.improvementAccount = improvementAccount;
    }

    public CashAccount getImprovementCashAccount() {
        return improvementCashAccount;
    }

    public void setImprovementCashAccount(CashAccount improvementCashAccount) {
        this.improvementCashAccount = improvementCashAccount;
        setImprovementAccount(improvementCashAccount != null ? improvementCashAccount.getAccountCode() : null);
    }

    public String getFullName() {
        return getId().getFixedAssetSubGroupCode() + " - " + getDescription();
    }

    public BigDecimal getRubbish() {
        return rubbish;
    }

    public void setRubbish(BigDecimal rubbish) {
        this.rubbish = rubbish;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getFixedAssetSubGroupCode() {
        return fixedAssetSubGroupCode;
    }

    public void setFixedAssetSubGroupCode(String fixedAssetSubGroupCode) {
        this.fixedAssetSubGroupCode = fixedAssetSubGroupCode;
    }

    public Boolean getRequireParts() {
        return requireParts;
    }

    public void setRequireParts(Boolean requireParts) {
        this.requireParts = requireParts;
    }
}
