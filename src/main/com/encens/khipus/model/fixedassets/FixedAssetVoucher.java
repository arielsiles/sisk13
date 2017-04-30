package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity for FixedAssetVoucher
 *
 * @author
 * @version 2.24
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FixedAssetVoucher.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "afvale",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "FixedAssetVoucher.findAll", query = "select o from FixedAssetVoucher o order by o.id asc"),
                @NamedQuery(name = "FixedAssetVoucher.findFixedAssetBlockMovementByStateByMovementType", query = "select o from FixedAssetVoucher" +
                        " o where o.state=:state and o.fixedAssetVoucherType.fixedAssetMovementTypeEnum=:fixedAssetMovementTypeEnum")
        }
)

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "afvale", schema = Constants.FINANCES_SCHEMA,
        uniqueConstraints = @UniqueConstraint(columnNames = {"no_cia", "codigo"}))
public class FixedAssetVoucher implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAssetVoucher.tableGenerator")
    @Column(name = "idafvale", nullable = false, scale = 24)
    private Long id;

    @Column(name = "no_cia", length = 2)
    @Length(max = 2)
    private String companyNumber;

    @Column(name = "tipovale", nullable = false, updatable = false, length = 3)
    @Length(max = 3)
    @NotNull
    private String voucherTypeCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "tipovale", referencedColumnName = "cod_mov", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false, insertable = false, updatable = false)
    })
    private FixedAssetMovementType fixedAssetVoucherType;

    @Column(name = "codigo", nullable = false)
    private String voucherCode;

    @Column(name = "motivo", updatable = false, length = 250)
    @Length(max = 250)
    private String cause;

    @Column(name = "fecha", updatable = false)
    @Temporal(TemporalType.DATE)
    private Date movementDate;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private FixedAssetVoucherState state;

    @Column(name = "montosus", precision = 12, scale = 2)
    private BigDecimal susAmount;

    @Column(name = "montoufv", precision = 12, scale = 2)
    private BigDecimal ufvAmount;

    @Column(name = "montobs", precision = 12, scale = 2)
    private BigDecimal bsAmount;

    @Column(name = "moneda")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "tasaBsSus", precision = 16, scale = 6)
    private BigDecimal bsSusRate;

    @Column(name = "tasaBsUfv", precision = 16, scale = 6)
    private BigDecimal bsUfvRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cpcustodio", referencedColumnName = "idcontratopuesto")
    private JobContract custodianJobContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "cod_cc", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "cod_cc", length = 6)
    @Length(max = 6)
    private String costCenterCode;

    @Column(name = "creadopor", updatable = false, insertable = false)
    private Long createdById;

    @ManyToOne
    @JoinColumn(name = "creadopor")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "actualizadopor")
    private User updatedBy;

    @Column(name = "fechacreacion")
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    /*represents the executor unit*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIO", referencedColumnName = "idunidadnegocio")
    private BusinessUnit businessUnit;

    /*in case of approve registration*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDORDENCOMPRA", referencedColumnName = "ID_COM_ENCOC")
    private PurchaseOrder purchaseOrder;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "idpago", nullable = true)
    private FixedAssetPayment fixedAssetPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDAFLOCALIZACION")
    private FixedAssetLocation fixedAssetLocation;

    @Column(name = "no_trans")
    private String transactionNumber;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "fixedAssetVoucher", fetch = FetchType.LAZY)
    private List<FixedAssetMovement> fixedAssetMovementList = new ArrayList<FixedAssetMovement>(0);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
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

    public FixedAssetVoucherState getState() {
        return state;
    }

    public void setState(FixedAssetVoucherState state) {
        this.state = state;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        if (costCenter != null) {
            setCompanyNumber(costCenter.getCompanyNumber());
        }
        setCostCenterCode(costCenter != null ? costCenter.getId().getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
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

    public String getVoucherTypeCode() {
        return voucherTypeCode;
    }

    public void setVoucherTypeCode(String voucherTypeCode) {
        this.voucherTypeCode = voucherTypeCode;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
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

    public FixedAssetPayment getFixedAssetPayment() {
        return fixedAssetPayment;
    }

    public void setFixedAssetPayment(FixedAssetPayment fixedAssetPayment) {
        this.fixedAssetPayment = fixedAssetPayment;
    }

    public FixedAssetMovementType getFixedAssetVoucherType() {
        return fixedAssetVoucherType;
    }

    public void setFixedAssetVoucherType(FixedAssetMovementType fixedAssetVoucherType) {
        this.fixedAssetVoucherType = fixedAssetVoucherType;
        if (fixedAssetVoucherType != null) {
            setCompanyNumber(fixedAssetVoucherType.getCompanyNumber());
        }
        this.voucherTypeCode = this.fixedAssetVoucherType != null ? this.fixedAssetVoucherType.getMovementCode() : null;
    }

    public BigDecimal getSusAmount() {
        return susAmount;
    }

    public void setSusAmount(BigDecimal susAmount) {
        this.susAmount = susAmount;
    }

    public JobContract getCustodianJobContract() {
        return custodianJobContract;
    }

    public void setCustodianJobContract(JobContract custodianJobContract) {
        this.custodianJobContract = custodianJobContract;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public FixedAssetLocation getFixedAssetLocation() {
        return fixedAssetLocation;
    }

    public void setFixedAssetLocation(FixedAssetLocation fixedAssetLocation) {
        this.fixedAssetLocation = fixedAssetLocation;
    }

    public List<FixedAssetMovement> getFixedAssetMovementList() {
        return fixedAssetMovementList;
    }

    public void setFixedAssetMovementList(List<FixedAssetMovement> fixedAssetMovementList) {
        this.fixedAssetMovementList = fixedAssetMovementList;
    }

    public boolean isRegistrationMovement() {
        return fixedAssetVoucherType != null && FixedAssetMovementTypeEnum.ALT.equals(fixedAssetVoucherType.getFixedAssetMovementTypeEnum());
    }

    public boolean isTransferenceMovement() {
        return fixedAssetVoucherType != null && FixedAssetMovementTypeEnum.TRA.equals(fixedAssetVoucherType.getFixedAssetMovementTypeEnum());
    }

    public boolean isDischargeMovement() {
        return fixedAssetVoucherType != null && FixedAssetMovementTypeEnum.BAJ.equals(fixedAssetVoucherType.getFixedAssetMovementTypeEnum());
    }

    public boolean isImprovementMovement() {
        return fixedAssetVoucherType != null && FixedAssetMovementTypeEnum.MEJ.equals(fixedAssetVoucherType.getFixedAssetMovementTypeEnum());
    }

    public boolean isAnnulled() {
        return state != null && state.equals(FixedAssetVoucherState.ANL);
    }

    @Override
    public String toString() {
        return "FixedAssetVoucher{" +
                "id=" + id +
                ", companyNumber='" + companyNumber + '\'' +
                ", voucherTypeCode='" + voucherTypeCode + '\'' +
                ", fixedAssetVoucherType=" + fixedAssetVoucherType +
                ", voucherCode='" + voucherCode + '\'' +
                ", cause='" + cause + '\'' +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", version=" + version +
                '}';
    }
}