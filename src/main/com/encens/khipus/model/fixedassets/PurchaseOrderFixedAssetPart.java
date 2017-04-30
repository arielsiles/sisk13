package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.3
 */

@NamedQueries({
        @NamedQuery(name = "PurchaseOrderFixedAssetPart.findAllById", query = "select p from PurchaseOrderFixedAssetPart p" +
                " left join fetch p.fixedAsset left join fetch p.measureUnit left join fetch p.purchaseOrder" +
                " where p.id=:id"),
        @NamedQuery(name = "PurchaseOrderFixedAssetPart.findByPurchaseOrder", query = "select p from PurchaseOrderFixedAssetPart p" +
                " where p.purchaseOrder=:purchaseOrder"),
        @NamedQuery(name = "PurchaseOrderFixedAssetPart.countByPurchaseOrder", query = "select count(p) from PurchaseOrderFixedAssetPart p" +
                " where p.purchaseOrder=:purchaseOrder"),
        @NamedQuery(name = "PurchaseOrderFixedAssetPart.sumBsTotalAmounts",
                query = "select sum(p.unitPrice) from PurchaseOrderFixedAssetPart p where p.purchaseOrder =:purchaseOrder")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "PurchaseOrderFixedAssetPart.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "parteafoc",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({UpperCaseStringListener.class, CompanyListener.class})
@Table(name = "PARTEAFOC", schema = Constants.KHIPUS_SCHEMA)
public class PurchaseOrderFixedAssetPart implements BaseModel {

    @Id
    @Column(name = "IDPARTEAFOC", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PurchaseOrderFixedAssetPart.tableGenerator")
    private Long id;

    @Column(name = "DESCRIPCION", nullable = false, length = 250)
    @Length(max = 250)
    private String description;

    @Column(name = "PRECIOUNI", nullable = false, precision = 16, scale = 6)
    private BigDecimal unitPrice;


    @Column(name = "SERIE", nullable = true, length = 250)
    @Length(max = 250)
    private String serialNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDACTIVOFIJO", nullable = false, updatable = false)
    private FixedAsset fixedAsset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "ID_COM_ENCOC", referencedColumnName = "ID_COM_ENCOC", nullable = false, updatable = false)
    })
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "NUMEROCOMPANIA", nullable = false, updatable = true),
            @JoinColumn(name = "UNIDADMEDIDA", nullable = false, updatable = true)
    })
    private MeasureUnit measureUnit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false)
    private Company company;

    @Version
    @Column(name = "version")
    private long version;


    public PurchaseOrderFixedAssetPart(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
    }

    public PurchaseOrderFixedAssetPart() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public FixedAsset getFixedAsset() {
        return fixedAsset;
    }

    public void setFixedAsset(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public MeasureUnit getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(MeasureUnit measureUnit) {
        this.measureUnit = measureUnit;
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

    public String getFullName() {
        return (getSerialNumber() + " " + getDescription() + " " + getMeasureUnit().getFullName()).trim();
    }
}
