package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Tax Percentage
 *
 * @author:
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "TaxPercentage.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "porcentajetributario",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "porcentajetributario", uniqueConstraints = @UniqueConstraint(columnNames = {"idcompania", "idtipoporcentajetrib", "porcentaje"}))
public class TaxPercentage implements BaseModel {

    @Id
    @Column(name = "idporcentajetributario", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TaxPercentage.tableGenerator")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idtipoporcentajetrib", nullable = false)
    private TaxPercentageType taxPercentageType;

    @Column(name = "porcentaje", precision = 13, scale = 2, nullable = false)
    private BigDecimal percentage;

    @Column(name = "descripcion")
    @Lob
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

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaxPercentageType getTaxPercentageType() {
        return taxPercentageType;
    }

    public void setTaxPercentageType(TaxPercentageType taxPercentageType) {
        this.taxPercentageType = taxPercentageType;
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
