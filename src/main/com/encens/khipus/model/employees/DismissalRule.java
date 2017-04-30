package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Range;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "DismissalRule.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "REGLARETIRO")
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "REGLARETIRO",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"CODIGO", "IDCOMPANIA"}),
                @UniqueConstraint(columnNames = {"NOMBRE", "IDCOMPANIA"})
        })
public class DismissalRule implements BaseModel {
    @Id
    @Column(name = "IDREGLARETIRO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DismissalRule.tableGenerator")
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    @NotNull
    private Long code;

    @Column(name = "NOMBRE", length = 250, nullable = false)
    @NotEmpty
    @Length(max = 250)
    private String name;

    @Column(name = "ACTIVO", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean active;

    @Column(name = "TIPO", nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    private DismissalRuleType dismissalRuleType;

    @Column(name = "TIPOMONTO", nullable = false, length = 20)
    @NotNull
    @Enumerated(EnumType.STRING)
    private AmountType amountType;

    @Column(name = "MONTO", nullable = false, precision = 13, scale = 2)
    @NotNull
    @Range(min = 0)
    private BigDecimal amount;

    @Column(name = "OCURRENCIA")
    @Range(min = 0)
    private Integer ocurrence;

    @Column(name = "MONEDA")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public DismissalRuleType getDismissalRuleType() {
        return dismissalRuleType;
    }

    public void setDismissalRuleType(DismissalRuleType dismissalRuleType) {
        this.dismissalRuleType = dismissalRuleType;
    }

    public AmountType getAmountType() {
        return amountType;
    }

    public void setAmountType(AmountType amountType) {
        this.amountType = amountType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getOcurrence() {
        return ocurrence;
    }

    public void setOcurrence(Integer ocurrence) {
        this.ocurrence = ocurrence;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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