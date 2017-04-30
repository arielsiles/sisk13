package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * @author
 * @version 3.4
 */
@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "DismissalDetail.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "DETALLERETIRO")

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "DETALLERETIRO")
public class DismissalDetail implements BaseModel {

    @Id
    @Column(name = "IDDETALLERETIRO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "DismissalDetail.tableGenerator")
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    @NotNull
    private Long code;

    @Column(name = "NOTRANS")
    @Length(max = 10)
    private String transactionNumber;

    @Column(name = "ESTADO", length = Constants.STRING_LENGTH_20, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private DismissalDetailState state;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCAUSARETIRO", referencedColumnName = "IDCAUSARETIRO", nullable = false)
    @NotNull
    private DismissalCause cause;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "IDARCHIVO", referencedColumnName = "idarchivo")
    private File file;

    @Column(name = "DESCRIPCION", nullable = false, length = Constants.LONG_TEXT_LENGTH)
    @Length(max = Constants.LONG_TEXT_LENGTH)
    @NotEmpty
    private String description;

    @Column(name = "MOTIVOREVERSION", length = Constants.LONG_TEXT_LENGTH)
    @Length(max = Constants.LONG_TEXT_LENGTH)
    private String reversionCause;

    @Column(name = "MONTO", nullable = false, precision = Constants.BIG_DECIMAL_DEFAULT_PRECISION, scale = Constants.BIG_DECIMAL_DEFAULT_SCALE)
    @NotNull
    private BigDecimal amount;

    @Column(name = "MONEDA", nullable = false, length = Constants.STRING_LENGTH_20)
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "TIPOCAMBIO", precision = Constants.BIG_DECIMAL_DEFAULT_PRECISION, scale = Constants.BIG_DECIMAL_DEFAULT_SCALE)
    private BigDecimal exchangeRate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDRETIRO", nullable = false)
    @NotNull
    private Dismissal dismissal;

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

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public DismissalDetailState getState() {
        return state;
    }

    public void setState(DismissalDetailState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReversionCause() {
        return reversionCause;
    }

    public void setReversionCause(String reversionCause) {
        this.reversionCause = reversionCause;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public Dismissal getDismissal() {
        return dismissal;
    }

    public void setDismissal(Dismissal dismissal) {
        this.dismissal = dismissal;
    }

    public DismissalCause getCause() {
        return cause;
    }

    public void setCause(DismissalCause cause) {
        this.cause = cause;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
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