package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Currency;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Digits;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Entity for Salary
 *
 * @author
 */
@NamedQueries({
        @NamedQuery(name = "Salary.findByTypeCurrencyAndAmount", query = "select s from Salary s where s.kindOfSalary=:kindOfSalary" +
                " and  s.currency=:currency and s.amount=:amount order by  s.id "),
        @NamedQuery(name = "Salary.cleanSalaryByIdList",
                query = "DELETE FROM Salary salary " +
                        " WHERE salary.id in(:salaryIdList)")
})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Salary.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "sueldo",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "sueldo")
public class Salary implements BaseModel {

    @Id
    @Column(name = "idsueldo", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Salary.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idtiposueldo", nullable = false)
    private KindOfSalary kindOfSalary;

    @Column(name = "cantidad", nullable = false, precision = 13, scale = 2)
    @Digits(integerDigits = 13, fractionalDigits = 2)
    @NotNull
    private BigDecimal amount;

    @Column(name = "haberbasico", precision = 13, scale = 2)
    @Digits(integerDigits = 13, fractionalDigits = 2)
    private BigDecimal basicAmount;

    @ManyToOne
    @JoinColumn(name = "idmoneda")
    private Currency currency;

    @Column(name = "descripcion", length = 200)
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Salary() {
    }

    public Salary(Salary salary) {
        setKindOfSalary(salary.getKindOfSalary());
        setAmount(salary.getAmount());
        setCurrency(salary.getCurrency());
        setDescription(salary.getDescription());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
        setBasicAmount(amount);
    }

    public BigDecimal getBasicAmount() {
        return basicAmount;
    }

    public void setBasicAmount(BigDecimal basicAmount) {
        this.basicAmount = basicAmount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public KindOfSalary getKindOfSalary() {
        return kindOfSalary;
    }

    public void setKindOfSalary(KindOfSalary kindOfSalary) {
        this.kindOfSalary = kindOfSalary;
    }

    public String getSalaryCurrency() {
        return amount + " " + currency;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getFullName() {
        return getAmount() + " " + getCurrency().getSymbol();
    }
}