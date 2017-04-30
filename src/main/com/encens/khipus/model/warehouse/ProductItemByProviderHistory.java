package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.Provide;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.27
 */
@NamedQueries(
        {
                @NamedQuery(name = "ProductItemByProviderHistory.findAll", query = "select o from ProductItemByProviderHistory o "),
                @NamedQuery(name = "ProductItemByProviderHistory.findLastByProductItemAndProvider",
                        query = "select max(h.id) from ProductItemByProviderHistory h where h.provide.provider=:provider and h.provide.productItem=:productItem")
        }
)

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ProductItemByProviderHistory.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "HISTORIALARTICULOPROV",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "HISTORIALARTICULOPROV")

public class ProductItemByProviderHistory implements BaseModel {

    @Id
    @Column(name = "IDHISTORIALARTICULOPROV", nullable = false, scale = 24)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ProductItemByProviderHistory.tableGenerator")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ID_ARTICULO_POR_PROVEEDOR", nullable = false)
    private Provide provide;

    @Column(name = "COSTO_UNI", precision = 16, scale = 6, nullable = false)
    @NotNull
    private BigDecimal unitCost;

    @Column(name = "FECHA")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false)
    @NotNull
    private Company company;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Provide getProvide() {
        return provide;
    }

    public void setProvide(Provide provide) {
        this.provide = provide;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
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
