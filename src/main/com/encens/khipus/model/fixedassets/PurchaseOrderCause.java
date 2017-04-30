package com.encens.khipus.model.fixedassets;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;


/**
 * PurchaseOrderCause entity
 *
 * @author
 * @version 2.26
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PurchaseOrderCause.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "motivoordencomp",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "PurchaseOrderCause.findAll", query = "select o from PurchaseOrderCause o"),
        @NamedQuery(name = "PurchaseOrderCause.maxCode",
                query = "select max(o.code) from PurchaseOrderCause o")
})

@Entity
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "motivoordencomp",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"idcompania", "codigo"}),
                @UniqueConstraint(columnNames = {"idcompania", "nombre"})})
public class PurchaseOrderCause implements BaseModel {

    @Id
    @Column(name = "idmotivoordenc", nullable = false, scale = 24)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PurchaseOrderCause.tableGenerator")
    private Long id;

    @Column(name = "codigo", nullable = false)
    @NotNull
    private Integer code;

    @Column(name = "nombre", length = 250, nullable = false)
    @Length(max = 250)
    @NotNull
    private String name;

    @Column(name = "descripcion", length = 250)
    @Length(max = 250)
    private String description;

    @Column(name = "requiereactivos", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean requiresFixedAssets;

    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private PurchaseOrderCauseType type;

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

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequiresFixedAssets() {
        return requiresFixedAssets;
    }

    public void setRequiresFixedAssets(Boolean requiresFixedAssets) {
        this.requiresFixedAssets = requiresFixedAssets;
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

    public PurchaseOrderCauseType getType() {
        return type;
    }

    public void setType(PurchaseOrderCauseType type) {
        this.type = type;
    }

    public boolean isFixedassetPartsPurchase() {
        return PurchaseOrderCauseType.FIXEDASSET_PARTS_PURCHASE.equals(getType());
    }

    public boolean isFixedassetPurchase() {
        return PurchaseOrderCauseType.FIXEDASSET_PURCHASE.equals(getType());
    }

    public String getFullName() {
        return getCode() + " - " + getName();
    }
}
