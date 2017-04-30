package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.fixedassets.FixedAssetVoucher;
import com.encens.khipus.model.purchases.PurchaseOrder;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for DiscountComment
 *
 * @author
 * @version 3.0
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "DiscountComment.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "comentariodesc",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "DiscountComment.findCauseByPurchaseOrderId",
                        query = "select discountComment.cause, discountComment.creationDate from DiscountComment discountComment " +
                                "where discountComment.purchaseOrder.id=:purchaseOrderId " +
                                "order by discountComment.code"),
                @NamedQuery(name = "DiscountComment.findCauseByRotatoryFundId",
                        query = "select discountComment.cause, discountComment.creationDate from DiscountComment discountComment " +
                                "where discountComment.rotatoryFund.id=:rotatoryFundId " +
                                "order by discountComment.code")
        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "comentariodesc", schema = Constants.KHIPUS_SCHEMA,
        uniqueConstraints = {@UniqueConstraint(columnNames = {"codigo", "idcompania"})})
public class DiscountComment implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DiscountComment.tableGenerator")
    @Column(name = "idcomentariodesc", nullable = false, scale = 24)
    private Long id;

    @Column(name = "codigo", nullable = false)
    private Long code;

    @Column(name = "tipo", nullable = false, length = 30)
    @NotNull
    @Enumerated(EnumType.STRING)
    private DiscountCommentType type;

    @Column(name = "motivo", nullable = false, length = 1000)
    @Length(max = 1000)
    @NotEmpty
    private String cause;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idafvale", referencedColumnName = "idafvale")
    private FixedAssetVoucher fixedAssetVoucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COM_ENCOC", referencedColumnName = "ID_COM_ENCOC")
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDFONDOROTATORIO", referencedColumnName = "IDFONDOROTATORIO")
    private RotatoryFund rotatoryFund;

    @ManyToOne
    @JoinColumn(name = "creadopor", nullable = false)
    @NotNull
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "actualizadopor")
    private User updatedBy;

    @Column(name = "fechacreacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date creationDate;

    @Column(name = "fechamodificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

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

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public FixedAssetVoucher getFixedAssetVoucher() {
        return fixedAssetVoucher;
    }

    public void setFixedAssetVoucher(FixedAssetVoucher fixedAssetVoucher) {
        this.fixedAssetVoucher = fixedAssetVoucher;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public DiscountCommentType getType() {
        return type;
    }

    public void setType(DiscountCommentType type) {
        this.type = type;
    }

    public RotatoryFund getRotatoryFund() {
        return rotatoryFund;
    }

    public void setRotatoryFund(RotatoryFund rotatoryFund) {
        this.rotatoryFund = rotatoryFund;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}