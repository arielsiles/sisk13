package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * PurchaseOrdersFixedAssetCollection entity
 *
 * @author
 * @version 2.26
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "PurchaseOrdersFixedAssetCollection.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "ordencomactivo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "PurchaseOrdersFixedAssetCollection.findAll", query = "select o from PurchaseOrdersFixedAssetCollection o"),
        @NamedQuery(name = "PurchaseOrdersFixedAssetCollection.findByPurchaseOrderAndFixedAsset",
                query = "select o from PurchaseOrdersFixedAssetCollection o where o.purchaseOrder=:purchaseOrder and o.fixedAsset=:fixedAsset"),
        @NamedQuery(name = "PurchaseOrdersFixedAssetCollection.findByPurchaseOrder",
                query = "select o from PurchaseOrdersFixedAssetCollection o where o.purchaseOrder=:purchaseOrder")
})

@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "ordencomactivo")
public class PurchaseOrdersFixedAssetCollection implements BaseModel {

    @Id
    @Column(name = "idordencomactivo", nullable = false, scale = 24)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PurchaseOrdersFixedAssetCollection.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idordencompra", referencedColumnName = "ID_COM_ENCOC", nullable = false)
    @NotNull
    private PurchaseOrder purchaseOrder;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idactivo", referencedColumnName = "IDACTIVO", nullable = false)
    private FixedAsset fixedAsset;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false)
    private Company company;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public FixedAsset getFixedAsset() {
        return fixedAsset;
    }

    public void setFixedAsset(FixedAsset fixedAsset) {
        this.fixedAsset = fixedAsset;
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
}