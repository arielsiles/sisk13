package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * @author
 * @version 2.40
 */

@NamedQueries({
        @NamedQuery(name = "RotatoryFundDocumentType.findById", query = "select documentType from RotatoryFundDocumentType documentType " +
                " left join fetch documentType.foreignCashAccount  foreignCashAccount" +
                " left join fetch documentType.nationalCashAccount  nationalCashAccount" +
                " where documentType.id=:rotatoryFundDocumentTypeId")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "RotatoryFundDocumentType.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "TIPODOCFONDOROTA",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, CompanyNumberListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "TIPODOCFONDOROTA",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"IDCOMPANIA", "CODIGO"}),
                @UniqueConstraint(columnNames = {"IDCOMPANIA", "NOMBRE"})
        })
public class RotatoryFundDocumentType implements BaseModel {

    @Id
    @Column(name = "IDTIPODOCFONDOROTA", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "RotatoryFundDocumentType.tableGenerator")
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    @NotNull
    private Long code;

    @Column(name = "NOMBRE", nullable = false, length = 250)
    @Length(max = 250)
    @NotNull
    private String name;

    @Column(name = "DESCRIPCION", length = 1000)
    @Length(max = 1000)
    private String description;

    @Column(name = "TIPOFONDOROTATORIO", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private RotatoryFundType rotatoryFundType;

    @Column(name = "RESTRICCIONCAMPO", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private RotatoryFundDocumentTypeFieldRestriction fieldRestriction;

    @Column(name = "CUENTACTBMN", length = 20)
    @Length(max = 20)
    private String nationalCashAccountCode;

    @Column(name = "CUENTACTBME", length = 20)
    @Length(max = 20)
    private String foreignCashAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", insertable = false, updatable = false, referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "CUENTACTBMN", insertable = false, updatable = false, referencedColumnName = "CUENTA")
    })
    private CashAccount nationalCashAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", insertable = false, updatable = false, referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "CUENTACTBME", insertable = false, updatable = false, referencedColumnName = "CUENTA")
    })
    private CashAccount foreignCashAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", insertable = false, updatable = false, referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "CUENTACTBAJMN", insertable = false, updatable = false, referencedColumnName = "CUENTA")
    })
    private CashAccount adjustmentNationalCashAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", insertable = false, updatable = false, referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "CUENTACTBAJME", insertable = false, updatable = false, referencedColumnName = "CUENTA")
    })
    private CashAccount adjustmentForeignCashAccount;

    @Column(name = "CUENTACTBAJMN", length = 20)
    @Length(max = 20)
    private String adjustmentNationalCashAccountCode;

    @Column(name = "CUENTACTBAJME", length = 20)
    @Length(max = 20)
    private String adjustmentForeignCashAccountCode;

    @Column(name = "ACTIVO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private boolean active;

    @Column(name = "NO_CIA", length = 2)
    @Length(max = 2)
    private String companyNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Column(name = "COLUMNAPLANILLA", length = 20)
    @Enumerated(EnumType.STRING)
    private PayrollColumnType payrollColumnType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_USR")
    private FinanceUser financeUser;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
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

    public RotatoryFundType getRotatoryFundType() {
        return rotatoryFundType;
    }

    public void setRotatoryFundType(RotatoryFundType rotatoryFundType) {
        this.rotatoryFundType = rotatoryFundType;
    }

    public RotatoryFundDocumentTypeFieldRestriction getFieldRestriction() {
        return fieldRestriction;
    }

    public void setFieldRestriction(RotatoryFundDocumentTypeFieldRestriction fieldRestriction) {
        this.fieldRestriction = fieldRestriction;
    }

    public String getNationalCashAccountCode() {
        return nationalCashAccountCode;
    }

    public void setNationalCashAccountCode(String nationalCashAccountCode) {
        this.nationalCashAccountCode = nationalCashAccountCode;
    }

    public String getForeignCashAccountCode() {
        return foreignCashAccountCode;
    }

    public void setForeignCashAccountCode(String foreignCashAccountCode) {
        this.foreignCashAccountCode = foreignCashAccountCode;
    }

    public CashAccount getNationalCashAccount() {
        return nationalCashAccount;
    }

    public void setNationalCashAccount(CashAccount nationalCashAccount) {
        this.nationalCashAccount = nationalCashAccount;
        setNationalCashAccountCode(this.nationalCashAccount != null ? this.nationalCashAccount.getAccountCode() : null);
    }

    public CashAccount getForeignCashAccount() {
        return foreignCashAccount;
    }

    public void setForeignCashAccount(CashAccount foreignCashAccount) {
        this.foreignCashAccount = foreignCashAccount;
        setForeignCashAccountCode(this.foreignCashAccount != null ? this.foreignCashAccount.getAccountCode() : null);
    }

    public String getAdjustmentNationalCashAccountCode() {
        return adjustmentNationalCashAccountCode;
    }

    public void setAdjustmentNationalCashAccountCode(String adjustmentNationalCashAccountCode) {
        this.adjustmentNationalCashAccountCode = adjustmentNationalCashAccountCode;
    }

    public String getAdjustmentForeignCashAccountCode() {
        return adjustmentForeignCashAccountCode;
    }

    public void setAdjustmentForeignCashAccountCode(String adjustmentForeignCashAccountCode) {
        this.adjustmentForeignCashAccountCode = adjustmentForeignCashAccountCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
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

    public Boolean isFieldRestrictionCashAccountDefinedByDefault() {
        return RotatoryFundDocumentTypeFieldRestriction.CASH_ACCOUNT_DEFINED_BY_DEFAULT.equals(getFieldRestriction());
    }

    public Boolean isFieldRestrictionCashAccountDefinedByUser() {
        return RotatoryFundDocumentTypeFieldRestriction.CASH_ACCOUNT_DEFINED_BY_USER.equals(getFieldRestriction());
    }

    public String getFullName() {
        return getCode() + " - " + getName();
    }

    public PayrollColumnType getPayrollColumnType() {
        return payrollColumnType;
    }

    public void setPayrollColumnType(PayrollColumnType payrollColumnType) {
        this.payrollColumnType = payrollColumnType;
    }

    public CashAccount getAdjustmentNationalCashAccount() {
        return adjustmentNationalCashAccount;
    }

    public void setAdjustmentNationalCashAccount(CashAccount adjustmentNationalCashAccount) {
        this.adjustmentNationalCashAccount = adjustmentNationalCashAccount;
        setAdjustmentNationalCashAccountCode(this.adjustmentNationalCashAccount != null ? this.adjustmentNationalCashAccount.getAccountCode() : null);
    }

    public CashAccount getAdjustmentForeignCashAccount() {
        return adjustmentForeignCashAccount;
    }

    public void setAdjustmentForeignCashAccount(CashAccount adjustmentForeignCashAccount) {
        this.adjustmentForeignCashAccount = adjustmentForeignCashAccount;
        setAdjustmentForeignCashAccountCode(this.adjustmentForeignCashAccount != null ? this.adjustmentForeignCashAccount.getAccountCode() : null);
    }

    public FinanceUser getFinanceUser() {
        return financeUser;
    }

    public void setFinanceUser(FinanceUser financeUser) {
        this.financeUser = financeUser;
    }
}
