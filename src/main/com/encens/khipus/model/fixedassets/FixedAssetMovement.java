package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for FixedAssetMovement
 *
 * @author
 * @version 2.24
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FixedAssetMovement.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "af_movs",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "FixedAssetMovement.findAll", query = "select o from FixedAssetMovement o order by o.id asc"),
                @NamedQuery(name = "FixedAssetMovement.findFixedAssetMovementByFixedAssetByStateByMovementType", query = "select o from FixedAssetMovement" +
                        " o where o.state=:state and o.fixedAsset.fixedAssetCode=:fixedAssetCode and o.fixedAssetMovementType.fixedAssetMovementTypeEnum=:fixedAssetMovementTypeEnum"),
                @NamedQuery(name = "FixedAssetMovement.findFixedAssetMovementByPurchaseOrderAndState", query = "select o from FixedAssetMovement o left join o.fixedAssetVoucher fav " +
                        " where fav.purchaseOrder=:purchaseOrder and fav.state=:state "),
                @NamedQuery(name = "FixedAssetMovement.findFixedAssetMovementByFixedAssetVoucher", query = "select o from FixedAssetMovement" +
                        " o where o.fixedAssetVoucher=:fixedAssetVoucher "),
                @NamedQuery(name = "FixedAssetMovement.findFixedAssetMovementByFixedAssetVoucherAndState", query = "select o from FixedAssetMovement" +
                        " o where o.fixedAssetVoucher=:fixedAssetVoucher and o.state=:state"),
                @NamedQuery(name = "FixedAssetMovement.countFixedAssetMovementByState", query = "select count(o.fixedAssetCode) from FixedAsset" +
                        " o where o.state=:state and o.companyNumber=:companyNumber"),
                @NamedQuery(name = "FixedAssetMovement.findFixedAssetMovementListByFixedAssetByMovementType", query = "select o from FixedAssetMovement" +
                        " o where o.fixedAsset=:fixedAsset and o.fixedAssetMovementType.fixedAssetMovementTypeEnum=:fixedAssetMovementTypeEnum "),
                @NamedQuery(name = "FixedAssetMovement.findFixedAssetMovementListByFixedAssetByMovementTypeAndState", query = "select o from FixedAssetMovement" +
                        " o where o.fixedAsset=:fixedAsset and o.fixedAssetMovementType.fixedAssetMovementTypeEnum=:fixedAssetMovementTypeEnum and o.state=:state"),
                @NamedQuery(name = "FixedAssetMovement.findLastApprovedFixedAssetMovement", query = "select o from FixedAssetMovement" +
                        " o where o.id=(select MAX(fam.id) from FixedAssetMovement fam where fam.fixedAsset.id =:fixedAssetId) and o.state=com.encens.khipus.model.fixedassets.FixedAssetMovementState.APR"),
                @NamedQuery(name = "FixedAssetMovement.getMovementsSum", query = "select new com.encens.khipus.util.CurrencyValuesContainer(SUM(o.ufvAmount), SUM(o.bsAmount)) from FixedAssetMovement o " +
                        " LEFT JOIN o.fixedAsset fixedAsset LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                        " where fixedAssetGroup.id =:fixedAssetGroupId and o.movementDate >=:initDate and o.movementDate<=:endDate and o.fixedAssetMovementType.fixedAssetMovementTypeEnum=:movementType"),
                @NamedQuery(name = "FixedAssetMovement.getMovementsSumByGroupAndSubGroup", query = "select new com.encens.khipus.util.CurrencyValuesContainer(SUM(o.ufvAmount), SUM(o.bsAmount)) from FixedAssetMovement o " +
                        " LEFT JOIN o.fixedAsset fixedAsset LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                        " where fixedAssetGroup.id =:fixedAssetGroupId and fixedAssetSubGroup.id =:fixedAssetSubGroupId and o.movementDate >=:initDate and o.movementDate<=:endDate " +
                        " and o.fixedAssetMovementType.fixedAssetMovementTypeEnum=:movementType"),
                @NamedQuery(name = "FixedAssetMovement.getMovementsSumByFixedAsset", query = "select new com.encens.khipus.util.CurrencyValuesContainer(SUM(o.ufvAmount), SUM(o.bsAmount)) from FixedAssetMovement o " +
                        " LEFT JOIN o.fixedAsset fixedAsset LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                        " where fixedAssetGroup.id =:fixedAssetGroupId and fixedAssetSubGroup.id =:fixedAssetSubGroupId and o.movementDate >=:initDate and o.movementDate<=:endDate " +
                        " and o.fixedAssetMovementType.fixedAssetMovementTypeEnum=:movementType and fixedAsset.id=:fixedAssetId "),
                @NamedQuery(name = "FixedAssetMovement.getMovementsSumBefore", query = "select new com.encens.khipus.util.CurrencyValuesContainer(SUM(o.ufvAmount), SUM(o.bsAmount)) from FixedAssetMovement o " +
                        " LEFT JOIN o.fixedAsset fixedAsset LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                        " where fixedAssetGroup.id =:fixedAssetGroupId and o.movementDate <:upToDate"),
                @NamedQuery(name = "FixedAssetMovement.getMovementsSumBySubGroupBefore", query = "select new com.encens.khipus.util.CurrencyValuesContainer(SUM(o.ufvAmount), SUM(o.bsAmount)) from FixedAssetMovement o " +
                        " LEFT JOIN o.fixedAsset fixedAsset LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                        " where fixedAssetGroup.id =:fixedAssetGroupId and o.movementDate <:upToDate and fixedAssetSubGroup.id =:fixedAssetSubGroupId"),
                @NamedQuery(name = "FixedAssetMovement.getMovementsSumByFixedAssetBefore", query = "select new com.encens.khipus.util.CurrencyValuesContainer(SUM(o.ufvAmount), SUM(o.bsAmount)) from FixedAssetMovement o " +
                        " LEFT JOIN o.fixedAsset fixedAsset LEFT JOIN fixedAsset.fixedAssetSubGroup fixedAssetSubGroup LEFT JOIN fixedAssetSubGroup.fixedAssetGroup fixedAssetGroup " +
                        " where fixedAssetGroup.id =:fixedAssetGroupId and o.movementDate <:upToDate and fixedAssetSubGroup.id =:fixedAssetSubGroupId and fixedAsset.id =:fixedAssetId"),
                @NamedQuery(name = "FixedAssetMovement.countByFixedAsset",
                        query = "select count(fam) from FixedAssetMovement fam where fam.fixedAsset=:fixedAsset"),
                @NamedQuery(name = "FixedAssetMovement.findFixedAssetMovementByTypeAndState", query = "select o from FixedAssetMovement o" +
                        " where o.fixedAsset=:fixedAsset and o.fixedAssetMovementType.fixedAssetMovementTypeEnum=:fixedAssetMovementTypeEnum and o.state=:fixedAssetMovementState ")
        }
)

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "af_movs", schema = Constants.FINANCES_SCHEMA)
public class FixedAssetMovement implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAssetMovement.tableGenerator")
    @Column(name = "id_af_movs", nullable = false, updatable = false)
    private Long id;

    /*already defined in pk, so properties in null*/
    @Column(name = "no_cia", nullable = false, updatable = false)
    @Length(max = 2)
    private String companyNumber;

    /*already defined in pk, so properties in null*/
    @Column(name = "cod_mov", nullable = false, updatable = false)
    @Length(max = 3)
    private String movementCode;

    @Column(name = "no_trans")
    @Length(max = 10)
    private String transactionNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDACTIVO", referencedColumnName = "IDACTIVO", nullable = false)
    private FixedAsset fixedAsset;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            /*already defined in pk, so properties in null*/
            @JoinColumn(name = "cod_mov", referencedColumnName = "cod_mov", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, insertable = false, updatable = false)
    })
    private FixedAssetMovementType fixedAssetMovementType;

    @Column(name = "no_mov")
    private Long movementNumber;

    @Column(name = "motivo")
    @Length(max = 250)
    private String cause;

    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date movementDate;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private FixedAssetMovementState state;

    @Column(name = "tipo_compro")
    @Length(max = 2)
    private String voucherType;

    @Column(name = "no_compro", length = 10)
    @Length(max = 10)
    private String voucherNumber;

    @Column(name = "monto", precision = 12, scale = 2)
    private BigDecimal amount;

    @Transient
    private BigDecimal susAmount;

    @Column(name = "montoUfv", precision = 12, scale = 2)
    private BigDecimal ufvAmount;

    @Column(name = "montoBs", precision = 12, scale = 2)
    private BigDecimal bsAmount;

    @Column(name = "moneda")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "tasaBsSus", precision = 16, scale = 6)
    private BigDecimal bsSusRate;

    @Column(name = "tasaBsUfv", precision = 16, scale = 6)
    private BigDecimal bsUfvRate;

    @Column(name = "tasaBsSusMesAnt", precision = 16, scale = 6)
    private BigDecimal lastMonthBsSusRate;

    @Column(name = "tasaBsUfvMesAnt", precision = 16, scale = 6)
    private BigDecimal lastMonthBsUfvRate;

    @Column(name = "dep_ini", precision = 12, scale = 2)
    private BigDecimal initialDepreciation;

    @Transient
    private BigDecimal susInitialDepreciation;

    @Transient
    private BigDecimal ufvInitialDepreciation;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false, referencedColumnName = "no_cia"),
            @JoinColumn(name = "cuenta", nullable = false, insertable = false, updatable = false, referencedColumnName = "cuenta")
    })
    private CashAccount fixedAssetMovementCashAccount;

    @Column(name = "cuenta", length = 20)
    @Length(max = 20)
    private String fixedAssetMovementAccount;

    @ManyToOne
    @JoinColumn(name = "custodio", nullable = false)
    private Employee custodian;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "cod_cc", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "cod_cc", length = 6)
    @Length(max = 6)
    private String costCenterCode;

    @ManyToOne
    @JoinColumn(name = "custodio_ant")
    private Employee lastCustodian;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "cod_cc_ant", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter lastCostCenter;

    @Column(name = "cod_cc_ant", length = 6)
    @Length(max = 6)
    private String lastCostCenterCode;

    @Column(name = "no_usr", nullable = false, length = 4)
    @NotNull
    @Length(max = 4)
    private String userNumber;

    @Column(name = "fecha_cre")
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /*represents the executor unit*/
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIO", referencedColumnName = "idunidadnegocio")
    private BusinessUnit businessUnit;

    /*represents the executor unit*/
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIOANT", referencedColumnName = "idunidadnegocio")
    private BusinessUnit lastBusinessUnit;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "IDPAGO", nullable = true)
    private FixedAssetPayment fixedAssetPayment;

    /* the FixedAssetVoucher associated if the movement is product of a block movement*/
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idafvale")
    private FixedAssetVoucher fixedAssetVoucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDAFLOCALIZACION_ANT")
    private FixedAssetLocation lastFixedAssetLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDAFLOCALIZACION_NVO")
    private FixedAssetLocation newFixedAssetLocation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FixedAssetMovementType getFixedAssetMovementType() {
        return fixedAssetMovementType;
    }

    public void setFixedAssetMovementType(FixedAssetMovementType fixedAssetMovementType) {
        this.fixedAssetMovementType = fixedAssetMovementType;
        setMovementCode(fixedAssetMovementType.getId().getMovementCode());
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public FixedAsset getFixedAsset() {
        return fixedAsset;
    }

    public void setFixedAsset(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
    }

    public Long getMovementNumber() {
        return movementNumber;
    }

    public void setMovementNumber(Long movementNumber) {
        this.movementNumber = movementNumber;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public Date getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }

    public FixedAssetMovementState getState() {
        return state;
    }

    public void setState(FixedAssetMovementState state) {
        this.state = state;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public BigDecimal getInitialDepreciation() {
        return initialDepreciation;
    }

    public void setInitialDepreciation(BigDecimal initialDepreciation) {
        this.initialDepreciation = initialDepreciation;
    }

    public String getFixedAssetMovementAccount() {
        return fixedAssetMovementAccount;
    }

    public void setFixedAssetMovementAccount(String fixedAssetMovementAccount) {
        this.fixedAssetMovementAccount = fixedAssetMovementAccount;
    }

    public Employee getCustodian() {
        return custodian;
    }

    public void setCustodian(Employee custodian) {
        this.custodian = custodian;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCompanyNumber(costCenter != null ? costCenter.getCompanyNumber() : null);
        setCostCenterCode(costCenter != null ? costCenter.getId().getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public Employee getLastCustodian() {
        return lastCustodian;
    }

    public void setLastCustodian(Employee lastCustodian) {
        this.lastCustodian = lastCustodian;
    }

    public String getLastCostCenterCode() {
        return lastCostCenterCode;
    }

    public void setLastCostCenterCode(String lastCostCenterCode) {
        this.lastCostCenterCode = lastCostCenterCode;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getMovementCode() {
        return movementCode;
    }

    public void setMovementCode(String movementCode) {
        this.movementCode = movementCode;
    }

    public CostCenter getLastCostCenter() {
        return lastCostCenter;
    }

    public void setLastCostCenter(CostCenter lastCostCenter) {
        this.lastCostCenter = lastCostCenter;
        setCompanyNumber(lastCostCenter != null ? lastCostCenter.getCompanyNumber() : null);
        setLastCostCenterCode(lastCostCenter != null ? lastCostCenter.getId().getCode() : null);
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public BusinessUnit getLastBusinessUnit() {
        return lastBusinessUnit;
    }

    public void setLastBusinessUnit(BusinessUnit lastBusinessUnit) {
        this.lastBusinessUnit = lastBusinessUnit;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public BigDecimal getBsSusRate() {
        return bsSusRate;
    }

    public void setBsSusRate(BigDecimal bsSusRate) {
        this.bsSusRate = bsSusRate;
    }

    public BigDecimal getBsUfvRate() {
        return bsUfvRate;
    }

    public void setBsUfvRate(BigDecimal bsUfvRate) {
        this.bsUfvRate = bsUfvRate;
    }

    public BigDecimal getLastMonthBsSusRate() {
        return lastMonthBsSusRate;
    }

    public void setLastMonthBsSusRate(BigDecimal lastMonthBsSusRate) {
        this.lastMonthBsSusRate = lastMonthBsSusRate;
    }

    public BigDecimal getLastMonthBsUfvRate() {
        return lastMonthBsUfvRate;
    }

    public void setLastMonthBsUfvRate(BigDecimal lastMonthBsUfvRate) {
        this.lastMonthBsUfvRate = lastMonthBsUfvRate;
    }

    public BigDecimal getAmount() {
        return amount = BigDecimalUtil.multiply(this.ufvAmount, this.bsUfvRate);
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getSusAmount() {
        return susAmount = BigDecimalUtil.divide(
                BigDecimalUtil.multiply(
                        this.ufvAmount, this.bsUfvRate
                ),
                this.bsSusRate
        );
    }

    public void setSusAmount(BigDecimal susAmount) {
        this.susAmount = susAmount;
    }

    public BigDecimal getUfvAmount() {
        return ufvAmount;
    }

    public void setUfvAmount(BigDecimal ufvAmount) {
        this.ufvAmount = ufvAmount;
    }

    public BigDecimal getBsAmount() {
        return bsAmount;
    }

    public void setBsAmount(BigDecimal bsAmount) {
        this.bsAmount = bsAmount;
    }

    public BigDecimal getSusInitialDepreciation() {
        return susInitialDepreciation = BigDecimalUtil.multiply(this.initialDepreciation, this.bsSusRate);
    }

    public void setSusInitialDepreciation(BigDecimal susInitialDepreciation) {
        this.susInitialDepreciation = susInitialDepreciation;
    }

    public BigDecimal getUfvInitialDepreciation() {
        return ufvInitialDepreciation = BigDecimalUtil.multiply(this.initialDepreciation, this.bsUfvRate);
    }

    public void setUfvInitialDepreciation(BigDecimal ufvInitialDepreciation) {
        this.ufvInitialDepreciation = ufvInitialDepreciation;
    }

    public CashAccount getFixedAssetMovementCashAccount() {
        return fixedAssetMovementCashAccount;
    }

    public void setFixedAssetMovementCashAccount(CashAccount fixedAssetMovementCashAccount) {
        this.fixedAssetMovementCashAccount = fixedAssetMovementCashAccount;
        setCompanyNumber(fixedAssetMovementCashAccount != null ? fixedAssetMovementCashAccount.getCompanyNumber() : null);
        setFixedAssetMovementAccount(fixedAssetMovementCashAccount != null ? fixedAssetMovementCashAccount.getAccountCode() : null);
    }

    public FixedAssetPayment getFixedAssetPayment() {
        return fixedAssetPayment;
    }

    public void setFixedAssetPayment(FixedAssetPayment fixedAssetPayment) {
        this.fixedAssetPayment = fixedAssetPayment;
    }

    public FixedAssetVoucher getFixedAssetVoucher() {
        return fixedAssetVoucher;
    }

    public void setFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher) {
        this.fixedAssetVoucher = fixedAssetVoucher;
    }

    public FixedAssetLocation getLastFixedAssetLocation() {
        return lastFixedAssetLocation;
    }

    public void setLastFixedAssetLocation(FixedAssetLocation lastFixedAssetLocation) {
        this.lastFixedAssetLocation = lastFixedAssetLocation;
    }

    public FixedAssetLocation getNewFixedAssetLocation() {
        return newFixedAssetLocation;
    }

    public void setNewFixedAssetLocation(FixedAssetLocation newFixedAssetLocation) {
        this.newFixedAssetLocation = newFixedAssetLocation;
    }
}
