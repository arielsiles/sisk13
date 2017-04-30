package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Entity for Currency
 *
 * @author
 */


@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Currency.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "moneda",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries(
        {
                @NamedQuery(name = "Currency.findCurrency", query = "select o from Currency o where o.id=:id")
        }
)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "moneda")
public class Currency implements BaseModel {

    @Id
    @Column(name = "idmoneda", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Currency.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String name;

    @Column(name = "codigomoneda", nullable = false, length = 20)
    @Length(max = 200)
    private String currencyCode;

    @Column(name = "simbolo", nullable = false)
    private String symbol;

    @Column(name = "descripcion", nullable = true, length = 50)
    @Length(max = 50)
    private String description;

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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", symbol='" + symbol + '\'' +
                ", version=" + version +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullName() {
        return FormatUtils.toAcronym(getName(), getSymbol());
    }

    public String getNameSymbol() {
        return FormatUtils.toCodeName(getName(), getSymbol());
    }
}