package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.BankEntity;
import com.encens.khipus.model.employees.Currency;
import com.encens.khipus.model.employees.Employee;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Bank Account
 *
 * @author
 */

@NamedQueries({
        @NamedQuery(name = "BankAccount.findByDefaulAccount", query = "select b from BankAccount b where b.employee=:employee and b.defaultAccount=:defaultAccount"),
        @NamedQuery(name = "BankAccount.findByEmployeeDefaulAccount", query = "select b from BankAccount b where b.employee.id =:employeeId and b.defaultAccount=:defaultAccount"),
        @NamedQuery(name = "BankAccount.countByDefaulAccount", query = "select count(b) from BankAccount b where b.employee=:employee and b.defaultAccount=:defaultAccount"),
        @NamedQuery(name = "BankAccount.countByDefaulAccountAndBankAccount", query = "select count(b) from BankAccount b where b.employee=:employee and b.defaultAccount=:defaultAccount and b <> :bankAccount")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "BankAccount.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "cuentabancaria",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "cuentabancaria", uniqueConstraints = @UniqueConstraint(columnNames = {"identidadbancaria", "idempleado", "numerocuenta"}))
public class BankAccount implements BaseModel {

    @Id
    @Column(name = "idcuentabancaria", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BankAccount.tableGenerator")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idempleado", referencedColumnName = "idempleado", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "identidadbancaria", referencedColumnName = "identidadbancaria", nullable = false)
    private BankEntity bankEntity;

    @Column(name = "numerocuenta", length = 150, nullable = false)
    @Length(max = 150)
    private String accountNumber;

    @Column(name = "codigocliente", length = 150, nullable = false)
    @Length(max = 150)
    private String clientCod;

    @Column(name = "cuentapordefecto", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean defaultAccount;

    @ManyToOne
    @JoinColumn(name = "idmoneda", nullable = false)
    private Currency currency;

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

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getClientCod() {
        return clientCod;
    }

    public void setClientCod(String clientCod) {
        this.clientCod = clientCod;
    }

    public BankEntity getBankEntity() {
        return bankEntity;
    }

    public void setBankEntity(BankEntity bankEntity) {
        this.bankEntity = bankEntity;
    }

    public Boolean getDefaultAccount() {
        return defaultAccount;
    }

    public void setDefaultAccount(Boolean defaultAccount) {
        this.defaultAccount = defaultAccount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
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