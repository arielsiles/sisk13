package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.io.Serializable;


@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "RawMaterialPaymentDetail.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "detallepagoacopiomp",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "detallepagoacopiomp")
public class RawMaterialPaymentDetail implements Serializable, BaseModel {

    @Id
    @Column(name = "iddetallepagoacopiomp", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RawMaterialPaymentDetail.tableGenerator")
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpagoacopiomp", nullable = false)
    private RawMaterialPayment rawMaterialPayment;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "idacopiomp", nullable = false)
    private CollectMaterial collectMaterial;

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

    public RawMaterialPayment getRawMaterialPayment() {
        return rawMaterialPayment;
    }

    public void setRawMaterialPayment(RawMaterialPayment rawMaterialPayment) {
        this.rawMaterialPayment = rawMaterialPayment;
    }

    public CollectMaterial getCollectMaterial() {
        return collectMaterial;
    }

    public void setCollectMaterial(CollectMaterial collectMaterial) {
        this.collectMaterial = collectMaterial;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
