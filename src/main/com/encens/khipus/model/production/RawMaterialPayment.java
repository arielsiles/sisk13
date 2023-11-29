package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.Voucher;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "RawMaterialPayment.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pagoacopiomp",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
@Table(schema = Constants.KHIPUS_SCHEMA, name = "pagoacopiomp")
public class RawMaterialPayment implements Serializable, BaseModel {

    @Id
    @Column(name = "idpagoacopiomp", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RawMaterialPayment.tableGenerator")
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha", nullable = true)
    private Date date;

    @Temporal(TemporalType.DATE)
    @Column(name = "fechaaprobacion", nullable = true)
    private Date approvalDate;

    @Column(name = "beneficiario", nullable = true)
    private String beneficiary;

    @Column(name = "tipopago", nullable = true)
    @Enumerated(EnumType.STRING)
    private RawMaterialPaymentType paymentType;

    @Column(name = "montopago", precision = 12, scale = 2, nullable = true)
    private BigDecimal paymentAmount;

    @Column(name = "estado", nullable = true)
    @Enumerated(EnumType.STRING)
    private RawMaterialPaymentState state = RawMaterialPaymentState.PENDING;

    @Column(name = "glosa", nullable = true)
    private String gloss;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tmpenc", nullable = true)
    private Voucher voucher;

    @Version
    @Column(name = "version", nullable = true)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproductormateriaprima", nullable = true)
    private RawMaterialProducer producer;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "rawMaterialPayment")
    private List<RawMaterialPaymentDetail> rawMaterialPaymentDetaiList = new ArrayList<RawMaterialPaymentDetail>(0);

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date creationDate) {
        this.date = creationDate;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    public RawMaterialPaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(RawMaterialPaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public RawMaterialPaymentState getState() {
        return state;
    }

    public void setState(RawMaterialPaymentState state) {
        this.state = state;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
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

    public RawMaterialProducer getProducer() {
        return producer;
    }

    public void setProducer(RawMaterialProducer producer) {
        this.producer = producer;
    }

    public List<RawMaterialPaymentDetail> getRawMaterialPaymentDetaiList() {
        return rawMaterialPaymentDetaiList;
    }

    public void setRawMaterialPaymentDetaiList(List<RawMaterialPaymentDetail> rawMaterialPaymentDetaiList) {
        this.rawMaterialPaymentDetaiList = rawMaterialPaymentDetaiList;
    }
}
