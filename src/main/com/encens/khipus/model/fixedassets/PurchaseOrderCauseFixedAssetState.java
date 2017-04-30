package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * PurchaseOrderCauseFixedAssetState entity
 *
 * @author
 * @version 2.26
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "PurchaseOrderCauseFixedAssetState.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "motivoocestadoaf",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "PurchaseOrderCauseFixedAssetState.findAll", query = "select o from PurchaseOrderCauseFixedAssetState o"),
        @NamedQuery(name = "PurchaseOrderCauseFixedAssetState.findFixedAssetStatesByPurchaseOrderCause",
                query = "select o.state from PurchaseOrderCauseFixedAssetState o where o.purchaseOrderCause=:purchaseOrderCause"),
        @NamedQuery(name = "PurchaseOrderCauseFixedAssetState.findByPurchaseOrderCause",
                query = "select o from PurchaseOrderCauseFixedAssetState o where o.purchaseOrderCause=:purchaseOrderCause"),
        @NamedQuery(name = "PurchaseOrderCauseFixedAssetState.findFixedAssetStateByPurchaseOrderCause",
                query = "select o from PurchaseOrderCauseFixedAssetState o where o.purchaseOrderCause=:purchaseOrderCause and o.state=:state")
})

@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "motivoocestadoaf")
public class PurchaseOrderCauseFixedAssetState implements BaseModel {

    @Id
    @Column(name = "idmotivoocestadoaf", nullable = false, scale = 24)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PurchaseOrderCauseFixedAssetState.tableGenerator")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idmotivoordenc", referencedColumnName = "idmotivoordenc")
    @NotNull
    private PurchaseOrderCause purchaseOrderCause;

    @Column(name = "estadoaf", nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private FixedAssetState state;

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

    public PurchaseOrderCause getPurchaseOrderCause() {
        return purchaseOrderCause;
    }

    public void setPurchaseOrderCause(PurchaseOrderCause purchaseOrderCause) {
        this.purchaseOrderCause = purchaseOrderCause;
    }

    public FixedAssetState getState() {
        return state;
    }

    public void setState(FixedAssetState state) {
        this.state = state;
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