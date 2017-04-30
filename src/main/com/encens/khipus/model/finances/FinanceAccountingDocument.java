package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.25
 */

@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cxp_lcompras", schema = Constants.FINANCES_SCHEMA)
public class FinanceAccountingDocument implements BaseModel {
    @EmbeddedId
    private FinanceAccountingDocumentPk id = new FinanceAccountingDocumentPk();

    @Column(name = "COD_CONTROL", length = 20)
    private String controlCode;

    @Column(name = "FECHA", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "NIT", length = 20)
    @Length(max = 20)
    private String nit;

    @Column(name = "RAZON_SOCIAL", length = 100)
    @Length(max = 100)
    private String socialName;

    @Column(name = "IMPORTE", precision = 16, scale = 2)
    private BigDecimal amount;

    @Column(name = "EXENTO", precision = 16, scale = 2)
    private BigDecimal exempt;

    @Column(name = "ICE", precision = 16, scale = 2)
    private BigDecimal ice;

    @Column(name = "IMPUESTO", precision = 16, scale = 2)
    private BigDecimal tax;

    @Column(name = "NO_TRANS", nullable = false, insertable = true, updatable = false)
    @Length(max = 10)
    private String transactionNumber;

    public FinanceAccountingDocumentPk getId() {
        return id;
    }

    public void setId(FinanceAccountingDocumentPk id) {
        this.id = id;
    }

    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getSocialName() {
        return socialName;
    }

    public void setSocialName(String socialName) {
        this.socialName = socialName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getExempt() {
        return exempt;
    }

    public void setExempt(BigDecimal exempt) {
        this.exempt = exempt;
    }

    public BigDecimal getIce() {
        return ice;
    }

    public void setIce(BigDecimal ice) {
        this.ice = ice;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }
}
