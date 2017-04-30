package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * @author
 * @version 3.5
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "AccountingRecordRelatedTransaction.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "reltransregctb",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "reltransregctb")
public class AccountingRecordRelatedTransaction implements BaseModel {

    @Id
    @Column(name = "idreltransregctb", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "AccountingRecordRelatedTransaction.tableGenerator")
    private Long id;

    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountingRecordRelatedTransactionType type;

    @Column(name = "notrans", length = 10, nullable = false)
    private String transactionNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idregistrocontable", nullable = false)
    private AccountingRecord accountingRecord;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public AccountingRecordRelatedTransaction() {
    }

    public AccountingRecordRelatedTransaction(AccountingRecordRelatedTransactionType type, String transactionNumber) {
        this.type = type;
        this.transactionNumber = transactionNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AccountingRecordRelatedTransactionType getType() {
        return type;
    }

    public void setType(AccountingRecordRelatedTransactionType type) {
        this.type = type;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public AccountingRecord getAccountingRecord() {
        return accountingRecord;
    }

    public void setAccountingRecord(AccountingRecord accountingRecord) {
        this.accountingRecord = accountingRecord;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
