package com.encens.khipus.model.xproduction;

import com.encens.khipus.model.BaseModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;


@Entity
@Table(name = "costoproduccion")
public class ProductionCost implements BaseModel {

    @Id
    @Column(name = "id_tmpdet", insertable = false, updatable = false)
    private Long id;

    @Column(name = "id_tmpenc", insertable = false, updatable = false)
    private Long voucherId;

    @Column(name = "fecha")
    private Date date;

    @Column(name = "tipo_doc")
    private String documentType;

    @Column(name = "no_doc")
    private String documentNumber;

    @Column(name = "no_trans")
    private String transactionNumber;

    @Column(name = "codigo")
    private String code;

    @Column(name = "cuenta")
    private String account;

    @Column(name = "descri")
    private String description;

    @Column(name = "debe")
    private BigDecimal debit;

    @Column(name = "haber")
    private BigDecimal credit;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getDebit() {
        return debit;
    }

    public void setDebit(BigDecimal debit) {
        this.debit = debit;
    }

    public BigDecimal getCredit() {
        return credit;
    }

    public void setCredit(BigDecimal credit) {
        this.credit = credit;
    }
}
