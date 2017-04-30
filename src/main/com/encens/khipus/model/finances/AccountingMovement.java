package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * AccountingMovement
 *
 * @author
 * @version 2.5
 */
@NamedQueries({
        @NamedQuery(name = "AccountingMovement.findByTransactionNumber",
                query = "select accountingMovement from AccountingMovement accountingMovement" +
                        " where accountingMovement.maximumTransactionNumber=:maximumTransactionNumber" +
                        " order by accountingMovement.voucherType,accountingMovement.voucherNumber")
})
@Entity
@EntityListeners({CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(name = "cg_movmae", schema = Constants.FINANCES_SCHEMA)
public class AccountingMovement implements BaseModel {

    @EmbeddedId
    private AccountingMovementPk id = new AccountingMovementPk();

    @Column(name = "NO_CIA", nullable = false, updatable = false, insertable = false)
    private String companyNumber;

    @Column(name = "TIPO_COMPRO", nullable = false, updatable = false, insertable = false)
    private String voucherType;

    @Column(name = "NO_COMPRO", nullable = false, updatable = false, insertable = false)
    private String voucherNumber;

    @Column(name = "FECHA")
    @Temporal(TemporalType.DATE)
    private Date recordDate;

    @Column(name = "FECHA_CRE")
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Column(name = "GLOSA", length = 1000)
    @Length(max = 1000)
    private String gloss;

    @Column(name = "ORIGEN", length = 6)
    @Length(max = 6)
    private String sourceModule;

    @Column(name = "NO_IMP", length = 6)
    @Length(max = 6)
    private String printerNumber;

    @Column(name = "REGISTRADO", length = 3)
    @Length(max = 3)
    private String recordType;

    @Column(name = "NO_USR", length = 4)
    @Length(max = 4)
    private String userNumber;

    @Column(name = "MONTO_TOTAL_MN", precision = 16, scale = 2)
    private BigDecimal amount;

    @Column(name = "MAX_NO_TRANS")
    @Length(max = 10)
    private String maximumTransactionNumber;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "accountingMovement", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private List<AccountingMovementDetail> accountingMovementDetailList = new ArrayList<AccountingMovementDetail>(0);

    public AccountingMovementPk getId() {
        return id;
    }

    public void setId(AccountingMovementPk id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    public Date getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public String getPrinterNumber() {
        return printerNumber;
    }

    public void setPrinterNumber(String printerNumber) {
        this.printerNumber = printerNumber;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMaximumTransactionNumber() {
        return maximumTransactionNumber;
    }

    public void setMaximumTransactionNumber(String maximumTransactionNumber) {
        this.maximumTransactionNumber = maximumTransactionNumber;
    }

    public List<AccountingMovementDetail> getAccountingMovementDetailList() {
        return accountingMovementDetailList;
    }

    public void setAccountingMovementDetailList(List<AccountingMovementDetail> accountingMovementDetailList) {
        this.accountingMovementDetailList = accountingMovementDetailList;
    }

    public String getFullName() {
        return getVoucherType() + "-" + getVoucherNumber();
    }

    public Boolean getTransferenceVoucher() {
        return AccountingMovementVoucherType.TR.name().equalsIgnoreCase(getVoucherType());
    }

    public Boolean getEntryVoucher() {
        return AccountingMovementVoucherType.IN.name().equalsIgnoreCase(getVoucherType());
    }

    public Boolean getExpenseVoucher() {
        return AccountingMovementVoucherType.EG.name().equalsIgnoreCase(getVoucherType());
    }

    public Boolean getGeneralVoucher() {
        return AccountingMovementVoucherType.GE.name().equalsIgnoreCase(getVoucherType());
    }

    @Override
    public String toString() {
        return "AccountingMovement{" +
                "companyNumber='" + companyNumber + '\'' +
                ", voucherType='" + voucherType + '\'' +
                ", voucherNumber='" + voucherNumber + '\'' +
                ", recordDate=" + recordDate +
                ", createDate=" + createDate +
                ", gloss='" + gloss + '\'' +
                ", sourceModule='" + sourceModule + '\'' +
                ", printerNumber='" + printerNumber + '\'' +
                ", recordType='" + recordType + '\'' +
                ", userNumber='" + userNumber + '\'' +
                '}';
    }
}
