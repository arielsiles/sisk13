package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.finances.CostCenter;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Entity for FixedAssetDepreciationRecord
 *
 * @author
 * @version 2.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "FixedAssetDepreciationRecord.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "af_hdepre",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "FixedAssetDepreciationRecord.findDepreciationAmountForGroupUpTo",
                        query = "select new com.encens.khipus.util.CurrencyValuesContainer(SUM(fadr.depreciation), SUM(fadr.bsDepreciation)) from FixedAssetDepreciationRecord fadr LEFT JOIN fadr.fixedAsset fa " +
                                "LEFT JOIN fa.fixedAssetSubGroup fasg LEFT JOIN fasg.fixedAssetGroup fag " +
                                "where fag.id=:fixedAssetGroupId and fadr.depreciationDate<:dateRange"),
                @NamedQuery(name = "FixedAssetDepreciationRecord.findDepreciationAmountForGroupAndSubGroupUpTo",
                        query = "select new com.encens.khipus.util.CurrencyValuesContainer(SUM(fadr.depreciation), SUM(fadr.bsDepreciation)) from FixedAssetDepreciationRecord fadr LEFT JOIN fadr.fixedAsset fa " +
                                "LEFT JOIN fa.fixedAssetSubGroup fasg LEFT JOIN fasg.fixedAssetGroup fag " +
                                "where fag.id=:fixedAssetGroupId and fasg.id =:fixedAssetSubGroupId and fadr.depreciationDate<:dateRange "),
                @NamedQuery(name = "FixedAssetDepreciationRecord.findDepreciationAmountForFixedAssetUpTo",
                        query = "select new com.encens.khipus.util.CurrencyValuesContainer(SUM(fadr.depreciation), SUM(fadr.bsDepreciation)) from FixedAssetDepreciationRecord fadr LEFT JOIN fadr.fixedAsset fa " +
                                "LEFT JOIN fa.fixedAssetSubGroup fasg LEFT JOIN fasg.fixedAssetGroup fag " +
                                "where fag.id=:fixedAssetGroupId and fasg.id =:fixedAssetSubGroupId and fadr.depreciationDate<:dateRange and fa.id=:fixedAssetId "),
                @NamedQuery(name = "FixedAssetDepreciationRecord.getBsDepreciationsSum", query = "select SUM(o.bsDepreciation) from FixedAssetDepreciationRecord o" +
                        " where o.fixedAsset=:fixedAsset")
        }
)


@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "af_hdepre", schema = Constants.FINANCES_SCHEMA)
public class FixedAssetDepreciationRecord implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FixedAssetDepreciationRecord.tableGenerator")
    @Column(name = "IDHDEPRE", nullable = false, updatable = false)
    private Long id;

    @Column(name = "fecha_en_proceso", nullable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date processDate;

    @Column(name = "no_cia", nullable = false, updatable = false)
    @Length(max = 2)
    @NotNull
    private String companyNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDACTIVO", referencedColumnName = "IDACTIVO", nullable = false)
    private FixedAsset fixedAsset;

    @Column(name = "val_tot", precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "tasa_dep", precision = 7, scale = 2)
    private BigDecimal depreciationRate;

    @Column(name = "moneda")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "dep_vo", precision = 12, scale = 2)
    private BigDecimal depreciation;

    @Column(name = "dep_vo_bs", precision = 12, scale = 2)
    private BigDecimal bsDepreciation;

    @Column(name = "dep_acu_vo", precision = 12, scale = 2)
    private BigDecimal acumulatedDepreciation;

    @Column(name = "dep_acu_vo_bs", precision = 12, scale = 2)
    private BigDecimal bsAccumulatedDepreciation;

    /*represents the executor unit*/
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUNIDADNEGOCIO", referencedColumnName = "idunidadnegocio")
    private BusinessUnit businessUnit;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "NO_CIA", updatable = false, insertable = false),
            @JoinColumn(name = "cod_cc", referencedColumnName = "COD_CC", updatable = false, insertable = false)
    })
    private CostCenter costCenter;

    @Column(name = "cod_cc")
    @Length(max = 6)
    private String costCenterCode;

    @ManyToOne
    @JoinColumn(name = "custodio", nullable = false)
    private Employee custodian;

    @Column(name = "tasaBsSus", precision = 16, scale = 6)
    private BigDecimal bsSusRate;

    @Column(name = "tasaBsUfv", nullable = false, precision = 16, scale = 6)
    private BigDecimal bsUfvRate;

    /* the date in wich the FixedAsset was depreciated*/
    @Column(name = "fecha", updatable = false)
    @Temporal(TemporalType.DATE)
    private Date depreciationDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public FixedAsset getFixedAsset() {
        return fixedAsset;
    }

    public void setFixedAsset(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getDepreciationRate() {
        return depreciationRate;
    }

    public void setDepreciationRate(BigDecimal depreciationRate) {
        this.depreciationRate = depreciationRate;
    }

    public BigDecimal getDepreciation() {
        return depreciation;
    }

    public void setDepreciation(BigDecimal depreciation) {
        this.depreciation = depreciation;
    }

    public BigDecimal getAcumulatedDepreciation() {
        return acumulatedDepreciation;
    }

    public void setAcumulatedDepreciation(BigDecimal acumulatedDepreciation) {
        this.acumulatedDepreciation = acumulatedDepreciation;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
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

    public Employee getCustodian() {
        return custodian;
    }

    public void setCustodian(Employee custodian) {
        this.custodian = custodian;
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

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public Date getDepreciationDate() {
        return depreciationDate;
    }

    public void setDepreciationDate(Date depreciationDate) {
        this.depreciationDate = depreciationDate;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    public BigDecimal getBsDepreciation() {
        return bsDepreciation;
    }

    public void setBsDepreciation(BigDecimal bsDepreciation) {
        this.bsDepreciation = bsDepreciation;
    }

    public BigDecimal getBsAccumulatedDepreciation() {
        return bsAccumulatedDepreciation;
    }

    public void setBsAccumulatedDepreciation(BigDecimal bsAccumulatedDepreciation) {
        this.bsAccumulatedDepreciation = bsAccumulatedDepreciation;
    }
}
