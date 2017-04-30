package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.MeasureUnit;
import com.encens.khipus.model.purchases.FixedAssetPurchaseOrderDetail;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.17
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "PurchaseOrderDetailPart.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "partedetoc",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "PurchaseOrderDetailPart.findNextNumber",
                query = "select max(purchaseOrderPart.number) from PurchaseOrderDetailPart purchaseOrderPart where purchaseOrderPart.detail =:detail"),
        @NamedQuery(name = "PurchaseOrderDetailPart.findByDetail",
                query = "select purchaseOrderPart from PurchaseOrderDetailPart purchaseOrderPart where purchaseOrderPart.detail =:detail")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({UpperCaseStringListener.class, CompanyListener.class})
@Table(name = "PARTEDETOC", schema = Constants.KHIPUS_SCHEMA)
public class PurchaseOrderDetailPart implements BaseModel {
    @Id
    @Column(name = "IDPARTEDETOC", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PurchaseOrderDetailPart.tableGenerator")
    private Long id;

    @Column(name = "DESCRIPCION", nullable = false, length = 250)
    @Length(max = 250)
    private String description;

    @Column(name = "NUMERO", nullable = false)
    private Long number;

    @Column(name = "PRECIOUNI", nullable = false, precision = 16, scale = 6)
    private BigDecimal unitPrice;

    @Column(name = "TOTAL", nullable = false, precision = 16, scale = 6)
    private BigDecimal totalPrice;

    @Version
    @Column(name = "version")
    private long version;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDDETALLEAFOC", insertable = true, updatable = false, nullable = false)
    private FixedAssetPurchaseOrderDetail detail;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumns({
            @JoinColumn(name = "NUMEROCOMPANIA", nullable = false, updatable = true, insertable = true),
            @JoinColumn(name = "UNIDADMEDIDA", nullable = false, updatable = true, insertable = true)
    })
    private MeasureUnit measureUnit;


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

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
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

    public FixedAssetPurchaseOrderDetail getDetail() {
        return detail;
    }

    public void setDetail(FixedAssetPurchaseOrderDetail detail) {
        this.detail = detail;
    }

    public MeasureUnit getMeasureUnit() {
        return measureUnit;
    }

    public void setMeasureUnit(MeasureUnit measureUnit) {
        this.measureUnit = measureUnit;
    }
}
