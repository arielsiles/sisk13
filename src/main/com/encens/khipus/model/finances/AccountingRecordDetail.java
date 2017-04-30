package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.ChristmasPayroll;
import com.encens.khipus.model.employees.FiscalProfessorPayroll;
import com.encens.khipus.model.employees.GeneralPayroll;
import com.encens.khipus.model.employees.ManagersPayroll;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * AccountingRecordDetail
 *
 * @author
 * @version 2.2
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "AccountingRecordDetail.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "detregcontable",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "detregcontable")
public class AccountingRecordDetail implements BaseModel {

    @Id
    @Column(name = "iddetregcontable", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "AccountingRecordDetail.tableGenerator")
    private Long id;

    @Column(name = "numerocompania", nullable = false)
    private String companyNumber;

    @Column(name = "numctabanaria", length = 20, nullable = false)
    @Length(max = 20)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanillaadministrativos")
    private ManagersPayroll managersPayroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanillageneral")
    private GeneralPayroll generalPayroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanillaaguinaldo")
    private ChristmasPayroll christmasPayroll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanilladocentelaboral")
    private FiscalProfessorPayroll fiscalProfessorPayroll;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idregistrocontable", nullable = false)
    @NotNull
    private AccountingRecord accountingRecord;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    @NotNull
    private long version;

    public AccountingRecordDetail() {
    }

    public AccountingRecordDetail(String companyNumber, String accountNumber, ManagersPayroll managersPayroll, AccountingRecord accountingRecord) {
        this.companyNumber = companyNumber;
        this.accountNumber = accountNumber;
        this.managersPayroll = managersPayroll;
        this.accountingRecord = accountingRecord;
    }

    public AccountingRecordDetail(String companyNumber, String accountNumber, GeneralPayroll generalPayroll, AccountingRecord accountingRecord) {
        this.companyNumber = companyNumber;
        this.accountNumber = accountNumber;
        this.generalPayroll = generalPayroll;
        this.accountingRecord = accountingRecord;
    }

    public AccountingRecordDetail(String companyNumber, String accountNumber, ChristmasPayroll christmasPayroll, AccountingRecord accountingRecord) {
        this.companyNumber = companyNumber;
        this.accountNumber = accountNumber;
        this.christmasPayroll = christmasPayroll;
        this.accountingRecord = accountingRecord;
    }

    public AccountingRecordDetail(String companyNumber, String accountNumber, FiscalProfessorPayroll fiscalProfessorPayroll, AccountingRecord accountingRecord) {
        this.companyNumber = companyNumber;
        this.accountNumber = accountNumber;
        this.fiscalProfessorPayroll = fiscalProfessorPayroll;
        this.accountingRecord = accountingRecord;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public ManagersPayroll getManagersPayroll() {
        return managersPayroll;
    }

    public void setManagersPayroll(ManagersPayroll managersPayroll) {
        this.managersPayroll = managersPayroll;
    }

    public GeneralPayroll getGeneralPayroll() {
        return generalPayroll;
    }

    public void setGeneralPayroll(GeneralPayroll generalPayroll) {
        this.generalPayroll = generalPayroll;
    }

    public ChristmasPayroll getChristmasPayroll() {
        return christmasPayroll;
    }

    public void setChristmasPayroll(ChristmasPayroll christmasPayroll) {
        this.christmasPayroll = christmasPayroll;
    }

    public FiscalProfessorPayroll getFiscalProfessorPayroll() {
        return fiscalProfessorPayroll;
    }

    public void setFiscalProfessorPayroll(FiscalProfessorPayroll fiscalProfessorPayroll) {
        this.fiscalProfessorPayroll = fiscalProfessorPayroll;
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
