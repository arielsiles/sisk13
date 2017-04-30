package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 2.26
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "SMNRate.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "TasaSMN",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
@NamedQueries({
        @NamedQuery(name = "SMNRate.findByActiveSMNRate",
                query = "select rate from SMNRate rate where rate.active =:active")
})


@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "TASASMN")
public class SMNRate implements BaseModel {

    @Id
    @Column(name = "IDTASASMN", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SMNRate.tableGenerator")
    private Long id;

    @Column(name = "TASA", nullable = false, precision = 13, scale = 2)
    private BigDecimal rate;

    @Column(name = "ACTIVO", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean active;

    @Version
    @Column(name = "VERSION", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
