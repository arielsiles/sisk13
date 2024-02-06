package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.production.RawMaterialDiscountT;
import com.encens.khipus.model.production.RawMaterialPayment;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;


@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "RawMaterialDiscount.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "descuentoacopiomp",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "descuentoacopiomp")
public class RawMaterialDiscount implements Serializable, BaseModel {

    @Id
    @Column(name = "iddescuentoacopiomp", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RawMaterialDiscount.tableGenerator")
    private Long id;

    @Column(name = "descripcion", nullable = true)
    private String description;

    /*@Column(name = "tipo", nullable = true)
    @Enumerated(EnumType.STRING)
    private RawMaterialDiscountType discountType;*/

    @Column(name = "monto", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpagoacopiomp", nullable = false)
    private com.encens.khipus.model.production.RawMaterialPayment rawMaterialPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtipodescuentoacopiomp", nullable = true)
    private RawMaterialDiscountT type;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*public RawMaterialDiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(RawMaterialDiscountType discountType) {
        this.discountType = discountType;
    }*/

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public com.encens.khipus.model.production.RawMaterialPayment getRawMaterialPayment() {
        return rawMaterialPayment;
    }

    public void setRawMaterialPayment(RawMaterialPayment rawMaterialPayment) {
        this.rawMaterialPayment = rawMaterialPayment;
    }

    public RawMaterialDiscountT getType() {
        return type;
    }

    public void setType(RawMaterialDiscountT type) {
        this.type = type;
    }
}
