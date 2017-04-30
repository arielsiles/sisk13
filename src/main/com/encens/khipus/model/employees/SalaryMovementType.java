package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CashAccount;
import com.encens.khipus.util.FormatUtils;
import com.encens.khipus.util.MessageUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Income Outcome Type
 *
 * @author
 */
@NamedQueries({
        @NamedQuery(name = "SalaryMovementType.loadSalaryMovementType",
                query = "select salaryMovementType from SalaryMovementType salaryMovementType" +
                        " left join fetch salaryMovementType.cashAccount cashAccount" +
                        " where salaryMovementType.id=:id"),
        @NamedQuery(name = "SalaryMovementType.countByName",
                query = "select count(salaryMovementType) from SalaryMovementType salaryMovementType" +
                        " where salaryMovementType.id<>:id and lower(salaryMovementType.name)=lower(:name)"),
        @NamedQuery(name = "SalaryMovementType.countByMovementTypeAndByDefault",
                query = "select count(salaryMovementType) from SalaryMovementType salaryMovementType" +
                        " where salaryMovementType.id<>:id and salaryMovementType.movementType=:movementType and salaryMovementType.byDefault=:byDefault"),
        @NamedQuery(name = "SalaryMovementType.findDefaultByMovementType",
                query = "select salaryMovementType from SalaryMovementType salaryMovementType" +
                        " where salaryMovementType.movementType=:movementType and salaryMovementType.byDefault=:byDefault")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "SalaryMovementType.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "tipomovsueldo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class, CompanyNumberListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "tipomovsueldo", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nombre", "idcompania"})
})
public class SalaryMovementType implements BaseModel {

    @Id
    @Column(name = "idtipomovsueldo", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SalaryMovementType.tableGenerator")
    private Long id;

    @Column(name = "nombre", length = 255, nullable = false)
    @Length(max = 255)
    @NotEmpty
    private String name;

    @Column(name = "tipo", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    @NotNull
    private MovementType movementType;

    @Column(name = "NO_CIA", length = 2)
    @Length(max = 2)
    @NotNull
    private String companyNumber;

    @Column(name = "CUENTACTB", length = 20)
    @Length(max = 20)
    @NotNull
    private String cashAccountCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "NO_CIA", insertable = false, updatable = false, referencedColumnName = "NO_CIA"),
            @JoinColumn(name = "CUENTACTB", insertable = false, updatable = false, referencedColumnName = "CUENTA")
    })
    private CashAccount cashAccount;

    @Column(name = "PORDEFECTO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean byDefault;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCashAccountCode() {
        return cashAccountCode;
    }

    public void setCashAccountCode(String cashAccountCode) {
        this.cashAccountCode = cashAccountCode;
    }

    public CashAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(CashAccount cashAccount) {
        this.cashAccount = cashAccount;
        setCashAccountCode(this.cashAccount != null ? this.cashAccount.getAccountCode() : null);
        setCompanyNumber(this.cashAccount != null ? this.cashAccount.getCompanyNumber() : null);
    }

    public Boolean getByDefault() {
        return byDefault;
    }

    public void setByDefault(Boolean byDefault) {
        this.byDefault = byDefault;
    }

    public String getFullName() {
        return FormatUtils.concatBySeparator(" - ", getName(), MessageUtils.getMessage(getMovementType().getResourceKey()));
    }
}