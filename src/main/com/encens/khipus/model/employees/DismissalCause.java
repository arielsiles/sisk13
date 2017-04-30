package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * @author
 * @version 3.4
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "DismissalCause.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "CAUSARETIRO")
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "CAUSARETIRO",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"CODIGO", "IDCOMPANIA"}),
                @UniqueConstraint(columnNames = {"NOMBRE", "IDCOMPANIA"})
        })
public class DismissalCause implements BaseModel {
    @Id
    @Column(name = "IDCAUSARETIRO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DismissalCause.tableGenerator")
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    @NotNull
    private Long code;

    @Column(name = "NOMBRE", length = 250, nullable = false)
    @NotEmpty
    @Length(max = 250)
    private String name;

    @Column(name = "PERMITEPAGOS", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean payable;

    @Column(name = "ACTIVO", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean active;

    @Column(name = "DESCRIPCION", length = 1000)
    @Length(max = 1000)
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false, insertable = true)
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

    public Boolean getPayable() {
        return payable;
    }

    public void setPayable(Boolean payable) {
        this.payable = payable;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getFullName() {
        return FormatUtils.concatDashSeparated(getCode(), getName());
    }
}