package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Entity for FixedAsset were the main currency is UFV.
 *
 * @author
 * @version 2.3
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FixedAsset.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "af_activos",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "FixedAsset.findAll", query = "select o from FixedAsset o order by o.fixedAssetCode asc"),
                @NamedQuery(name = "FixedAsset.findFixedAsset", query = "select o from FixedAsset o where o.id=:id"),
                @NamedQuery(name = "FixedAsset.findFixedAssetByState", query = "select o from FixedAsset" +
                        " o where o.state=:state"),
                @NamedQuery(name = "FixedAsset.findFixedAssetByPurchaseOrder", query = "select o from FixedAsset" +
                        " o where o.purchaseOrder=:purchaseOrder and o.state=:state"),
                @NamedQuery(name = "FixedAsset.findFixedAssetListByFixedAssetVoucher", query = "select fixedAsset from FixedAsset fixedAsset" +
                        " where fixedAsset in (select fixedAssetMovement.fixedAsset from FixedAssetMovement fixedAssetMovement where fixedAssetMovement.fixedAssetVoucher=:fixedAssetVoucher) "),
                @NamedQuery(name = "FixedAsset.findFixedAssetListByFixedAssetPurchaseOrder", query = "select fixedAsset from FixedAsset fixedAsset" +
                        " where fixedAsset in (select purchaseOrdersFixedAssetCollection.fixedAsset from PurchaseOrdersFixedAssetCollection purchaseOrdersFixedAssetCollection where purchaseOrdersFixedAssetCollection.purchaseOrder=:purchaseOrder) "),
                @NamedQuery(name = "FixedAsset.findFixedAssetListByFixedAssetVoucherAndMovementState", query = "select fixedAsset from FixedAsset fixedAsset" +
                        " where fixedAsset in " +
                        " (select fixedAssetMovement.fixedAsset from FixedAssetMovement fixedAssetMovement " +
                        " where fixedAssetMovement.fixedAssetVoucher=:fixedAssetVoucher and fixedAssetMovement.state=:fixedAssetMovementState ) "),
                @NamedQuery(name = "FixedAsset.findTdpFixedAssetsToAdjust", query = "select o from FixedAsset" +
                        " o where o.state=:state and (o.adjustDate<:adjustDate or o.adjustDate is null) "),
                @NamedQuery(name = "FixedAsset.countFixedAssetByState", query = "select count(o.fixedAssetCode) from FixedAsset" +
                        " o where o.state=:state and o.companyNumber=:companyNumber"),
                @NamedQuery(name = "FixedAsset.changeFixedAssetState", query = "update FixedAsset o set o.state=:newState where o.state=:oldState"),
                @NamedQuery(name = "FixedAsset.findFixedAssetsByCustodianByState", query = "select o from FixedAsset o where o.state=:state" +
                        " and o.custodianJobContract.contract.employee=:custodian"),
                @NamedQuery(name = "FixedAsset.findFixedAssetByCode", query = "select o from FixedAsset o where o.fixedAssetCode=:fixedAssetCode " +
                        " and o.fixedAssetGroupCode=:fixedAssetGroupCode and o.fixedAssetSubGroupCode=:fixedAssetSubGroupCode and o.companyNumber=:companyNumber" +
                        " order by o.id"),
                @NamedQuery(name = "FixedAsset.countByCode",
                        query = "select count(f.fixedAssetCode) " +
                                "from FixedAsset f " +
                                "where f.fixedAssetCode=:fixedAssetCode " +
                                " and f.companyNumber=:companyNumber"),
                @NamedQuery(name = "FixedAsset.maxByGroupAndSubGroup",
                        query = "select max(f.fixedAssetCode) from FixedAsset f where f.fixedAssetGroupCode=:fixedAssetGroupCode " +
                                " and f.fixedAssetSubGroupCode=:fixedAssetSubGroupCode and f.companyNumber=:companyNumber"),
                @NamedQuery(name = "FixedAsset.countByBarCode",
                        query = "select count(o) from FixedAsset o where o.barCode=:barCode " +
                                " and o.companyNumber=:companyNumber"),
                @NamedQuery(name = "FixedAsset.findByBarCode",
                        query = "select fixedAsset from FixedAsset fixedAsset where fixedAsset.barCode=:barCode ")
        }
)

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "af_activos", schema = Constants.FINANCES_SCHEMA)
@Filter(name = com.encens.khipus.util.Constants.BUSINESS_UNIT_FILTER_NAME)
public class FixedAsset implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAsset.tableGenerator")
    @Column(name = "idactivo", nullable = false, updatable = false)
    private Long id;

    @Column(name = "cod_acti")
    private Long fixedAssetCode;

    @Column(name = "codbarras")
    private String barCode;

    @Column(name = "no_cia", length = 2, nullable = false, updatable = false)
    @Length(max = 2)
    @NotNull
    private String companyNumber;

    /* this field should be the same as the observation field in PurchaseOrder */
    @Column(name = "descri", length = 250)
    @Length(max = 250)
    private String description;

    @Column(name = "detalle", length = 250, nullable = false)
    @Length(max = 250)
    @NotNull
    private String detail;

    /* the dimensions of the fixedAsset */
    @Column(name = "medida", length = 250)
    @Length(max = 250)
    private String measurement;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private FixedAssetState state;

    @Column(name = "serie", length = 255)
    @Length(max = 255)
    private String sequence;

    @Column(name = "marca", length = 255)
    @Length(max = 255)
    private String trademark;

    @Column(name = "modelo", length = 255)
    @Length(max = 255)
    private String model;

    @Column(name = "fch_alta")
    @Temporal(TemporalType.DATE)
    private Date registrationDate;

    @Column(name = "fch_baja")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "fch_ajuste")
    @Temporal(TemporalType.DATE)
    private Date adjustDate;

    @Column(name = "duracion")
    private Integer duration;

    @Column(name = "tasa_dep", nullable = false, precision = 7, scale = 2)
    private BigDecimal depreciationRate;

    @Column(name = "revaluo", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean revaluation = Boolean.FALSE;

    @Column(name = "vobs", nullable = false, precision = 12, scale = 2)
    private BigDecimal bsOriginalValue;

    @Column(name = "vosus", nullable = false, precision = 12, scale = 2)
    private BigDecimal susOriginalValue;

    @Column(name = "voufv", nullable = false, precision = 12, scale = 2)
    private BigDecimal ufvOriginalValue;

    @Column(name = "mej", precision = 12, scale = 2)
    private BigDecimal improvement;

    @Column(name = "dep_vo", precision = 12, scale = 2)
    private BigDecimal depreciation;

    @Column(name = "dep_acu_vo", precision = 12, scale = 2)
    private BigDecimal acumulatedDepreciation;

    @Column(name = "desecho", precision = 12, scale = 2)
    private BigDecimal rubbish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", updatable = false, insertable = false),
            @JoinColumn(name = "cod_cc", referencedColumnName = "cod_cc", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "cod_cc")
    private String costCenterCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            /*already defined in pk, so properties in null*/
            @JoinColumn(name = "no_cia", nullable = false, updatable = false, insertable = false),
            @JoinColumn(name = "grupo", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "subgrupo", nullable = false, updatable = false, insertable = false)
    })
    private FixedAssetSubGroup fixedAssetSubGroup;

    @Column(name = "subgrupo", length = 3, nullable = false)
    @Length(max = 3)
    @NotNull
    private String fixedAssetSubGroupCode;

    @Column(name = "grupo", length = 3, nullable = false)
    @Length(max = 3)
    @NotNull
    private String fixedAssetGroupCode;

    @Column(name = "moneda")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currencyType;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /*represents the executor unit*/
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idunidadnegocio", referencedColumnName = "idunidadnegocio")
    private BusinessUnit businessUnit;

    @Column(name = "tasabssus", nullable = false, precision = 16, scale = 6)
    private BigDecimal bsSusRate;

    @Column(name = "tasabsufv", nullable = false, precision = 16, scale = 6)
    private BigDecimal bsUfvRate;

    @Column(name = "ulttasabssus", nullable = false, precision = 16, scale = 6)
    private BigDecimal lastBsSusRate;

    @Column(name = "ulttasabsufv", nullable = false, precision = 16, scale = 6)
    private BigDecimal lastBsUfvRate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "id_com_encoc", referencedColumnName = "id_com_encoc", updatable = false, insertable = false)
    })
    private PurchaseOrder purchaseOrder;

    @Column(name = "id_com_encoc")
    private Long purchaseOrderCode;

    @Column(name = "mesesgarantia")
    @Range(min = 0, max = 999)
    private Integer monthsGuaranty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontratopuesto", referencedColumnName = "idcontratopuesto")
    private JobContract custodianJobContract;

    @ManyToMany(mappedBy = "fixedAssets")
    private List<FixedAssetMaintenanceRequest> fixedAssetMaintenanceRequestList;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idfoto", referencedColumnName = "idarchivo")
    private File photo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idaflocalizacion")
    private FixedAssetLocation fixedAssetLocation;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "fixedAsset")
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<FixedAssetPart> fixedAssetPartList;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public FixedAssetState getState() {
        return state;
    }

    public void setState(FixedAssetState state) {
        this.state = state;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getTrademark() {
        return trademark;
    }

    public void setTrademark(String trademark) {
        this.trademark = trademark;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public BigDecimal getDepreciationRate() {
        return depreciationRate;
    }

    public void setDepreciationRate(BigDecimal depreciationRate) {
        this.depreciationRate = depreciationRate;
    }

    public BigDecimal getBsOriginalValue() {
        return bsOriginalValue;
    }

    public void setBsOriginalValue(BigDecimal bsOriginalValue) {
        this.bsOriginalValue = bsOriginalValue;
    }

    public BigDecimal getImprovement() {
        return improvement;
    }

    public void setImprovement(BigDecimal improvement) {
        this.improvement = improvement;
    }

    public BigDecimal getDepreciation() {
        return depreciation;
    }

    public void setDepreciation(BigDecimal depreciation) {
        this.depreciation = depreciation;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public BigDecimal getAcumulatedDepreciation() {
        return acumulatedDepreciation;
    }

    public void setAcumulatedDepreciation(BigDecimal acumulatedDepreciation) {
        this.acumulatedDepreciation = acumulatedDepreciation;
    }

    public BigDecimal getRubbish() {
        return rubbish;
    }

    public void setRubbish(BigDecimal rubbish) {
        this.rubbish = rubbish;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
        setCompanyNumber(costCenter != null ? costCenter.getCompanyNumber() : null);
        setCostCenterCode(costCenter != null ? costCenter.getCode() : null);
    }

    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public FixedAssetSubGroup getFixedAssetSubGroup() {
        return fixedAssetSubGroup;
    }

    public void setFixedAssetSubGroup(FixedAssetSubGroup fixedAssetSubGroup) {
        this.fixedAssetSubGroup = fixedAssetSubGroup;
        setFixedAssetSubGroupCode(null != fixedAssetSubGroup ? fixedAssetSubGroup.getId().getFixedAssetSubGroupCode() : null);
        setFixedAssetGroupCode(null != fixedAssetSubGroup ? fixedAssetSubGroup.getId().getFixedAssetGroupCode() : null);
        setDuration(null != fixedAssetSubGroup ? fixedAssetSubGroup.getDuration() : null);
        setDepreciationRate(null != fixedAssetSubGroup ? fixedAssetSubGroup.getDepreciationRate() : null);
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getFixedAssetSubGroupCode() {
        return fixedAssetSubGroupCode;
    }

    public void setFixedAssetSubGroupCode(String fixedAssetSubGroupCode) {
        this.fixedAssetSubGroupCode = fixedAssetSubGroupCode;
    }

    public String getFixedAssetGroupCode() {
        return fixedAssetGroupCode;
    }

    public void setFixedAssetGroupCode(String fixedAssetGroupCode) {
        this.fixedAssetGroupCode = fixedAssetGroupCode;
    }

    public FinancesCurrencyType getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(FinancesCurrencyType currencyType) {
        this.currencyType = currencyType;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }


    public BigDecimal getSusOriginalValue() {
        return susOriginalValue;
    }

    public void setSusOriginalValue(BigDecimal susOriginalValue) {
        this.susOriginalValue = susOriginalValue;
    }

    public BigDecimal getUfvOriginalValue() {
        return ufvOriginalValue;
    }

    public void setUfvOriginalValue(BigDecimal ufvOriginalValue) {
        this.ufvOriginalValue = ufvOriginalValue;
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

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
        setPurchaseOrderCode(purchaseOrder != null ? purchaseOrder.getId() : null);
    }

    public Long getPurchaseOrderCode() {
        return purchaseOrderCode;
    }

    public void setPurchaseOrderCode(Long purchaseOrderCode) {
        this.purchaseOrderCode = purchaseOrderCode;
    }

    public Long getFixedAssetCode() {
        return fixedAssetCode;
    }

    public void setFixedAssetCode(Long fixedAssetCode) {
        this.fixedAssetCode = fixedAssetCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getLastBsSusRate() {
        return lastBsSusRate;
    }

    public void setLastBsSusRate(BigDecimal lastBsSusRate) {
        this.lastBsSusRate = lastBsSusRate;
    }

    public BigDecimal getLastBsUfvRate() {
        return lastBsUfvRate;
    }

    public void setLastBsUfvRate(BigDecimal lastBsUfvRate) {
        this.lastBsUfvRate = lastBsUfvRate;
    }

    public Date getAdjustDate() {
        return adjustDate;
    }

    public void setAdjustDate(Date adjustDate) {
        this.adjustDate = adjustDate;
    }

    public Integer getMonthsGuaranty() {
        return monthsGuaranty;
    }

    public void setMonthsGuaranty(Integer monthsGuaranty) {
        this.monthsGuaranty = monthsGuaranty;
    }

    public JobContract getCustodianJobContract() {
        return custodianJobContract;
    }

    public void setCustodianJobContract(JobContract custodianJobContract) {
        this.custodianJobContract = custodianJobContract;
        setBusinessUnit(custodianJobContract != null ? custodianJobContract.getJob().getOrganizationalUnit().getBusinessUnit() : null);
        setCostCenter(custodianJobContract != null ? custodianJobContract.getJob().getOrganizationalUnit().getCostCenter() : null);
    }

    public List<FixedAssetMaintenanceRequest> getFixedAssetMaintenanceRequestList() {
        return fixedAssetMaintenanceRequestList;
    }

    public void setFixedAssetMaintenanceRequestList(List<FixedAssetMaintenanceRequest> fixedAssetMaintenanceRequestList) {
        this.fixedAssetMaintenanceRequestList = fixedAssetMaintenanceRequestList;
    }

    public File getPhoto() {
        return photo;
    }

    public void setPhoto(File photo) {
        this.photo = photo;
    }

    public FixedAssetLocation getFixedAssetLocation() {
        return fixedAssetLocation;
    }

    public void setFixedAssetLocation(FixedAssetLocation fixedAssetLocation) {
        this.fixedAssetLocation = fixedAssetLocation;
    }

    public List<FixedAssetPart> getFixedAssetPartList() {
        return fixedAssetPartList;
    }

    public void setFixedAssetPartList(List<FixedAssetPart> fixedAssetPartList) {
        this.fixedAssetPartList = fixedAssetPartList;
    }

    public String getFullName() {
        return getId() + " - " + getBarCode() + " - " + getDetail();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FixedAsset && this.getId().equals(((FixedAsset) obj).getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Boolean getRevaluation() {
        return revaluation;
    }

    public void setRevaluation(Boolean revaluation) {
        this.revaluation = revaluation;
    }
}
