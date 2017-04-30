package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.2.9
 */

@NamedQueries({
        @NamedQuery(name = "PayableRelatedDocument.findConciliationNumber",
                query = "select distinct payableRelatedDocument.conciliationNumber " +
                        " from  PayableRelatedDocument payableRelatedDocument" +
                        " where payableRelatedDocument.transactionNumber = :transactionNumber"),
        @NamedQuery(name = "PayableRelatedDocument.countByConciliationAndTransactionNumber",
                query = "select count(payableRelatedDocument.conciliationNumber)  " +
                        " from  PayableRelatedDocument payableRelatedDocument" +
                        " where payableRelatedDocument.conciliationNumber in (:conciliationNumberList) and payableRelatedDocument.transactionNumber=:transactionNumber"),
        @NamedQuery(name = "PayableRelatedDocument.sumByTransactionNumber",
                query = "select sum(payableRelatedDocument.amount*payableRelatedDocument.exchangeRate)  " +
                        " from  PayableRelatedDocument payableRelatedDocument" +
                        " where payableRelatedDocument.transactionNumber = :transactionNumber")
})


@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cxp_docrel", schema = Constants.FINANCES_SCHEMA)
public class PayableRelatedDocument implements BaseModel {

    @EmbeddedId
    private PayableRelatedDocumentPk id = new PayableRelatedDocumentPk();

    @Column(name = "NO_CIA", insertable = false, updatable = false)
    private String companyNumber;

    @Column(name = "NO_TRANS", insertable = false, updatable = false)
    private String transactionNumber;

    @Column(name = "NO_CONCI", insertable = false, updatable = false)
    private String conciliationNumber;

    @Column(name = "MONTO", precision = 16, scale = 2)
    private BigDecimal amount;

    @Column(name = "TC", precision = 10, scale = 2)
    private BigDecimal exchangeRate;

    public PayableRelatedDocument() {
    }

    public PayableRelatedDocument(String companyNumber, String transactionNumber, String conciliationNumber, BigDecimal exchangeRate, BigDecimal amount) {
        setId(new PayableRelatedDocumentPk(companyNumber, transactionNumber, conciliationNumber));
        this.companyNumber = companyNumber;
        this.transactionNumber = transactionNumber;
        this.conciliationNumber = conciliationNumber;
        this.exchangeRate = exchangeRate;
        this.amount = amount;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getConciliationNumber() {
        return conciliationNumber;
    }

    public void setConciliationNumber(String conciliationNumber) {
        this.conciliationNumber = conciliationNumber;
    }

    public PayableRelatedDocumentPk getId() {
        return id;
    }

    public void setId(PayableRelatedDocumentPk id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override
    public String toString() {
        return "PayableRelatedDocument{" +
                "id=" + id +
                ", companyNumber='" + companyNumber + '\'' +
                ", transactionNumber='" + transactionNumber + '\'' +
                ", conciliationNumber='" + conciliationNumber + '\'' +
                ", amount=" + amount +
                ", exchangeRate=" + exchangeRate +
                '}';
    }
}
