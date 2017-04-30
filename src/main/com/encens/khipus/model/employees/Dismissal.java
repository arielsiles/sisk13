package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.File;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * @author
 * @version 3.4
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "Dismissal.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE,
        pkColumnValue = "RETIRO")

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "RETIRO")
public class Dismissal implements BaseModel {
    @Id
    @Column(name = "IDRETIRO", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Dismissal.tableGenerator")
    private Long id;

    @Column(name = "CODIGO", nullable = false)
    @NotNull
    private Long code;

    @Column(name = "DIASTRABAJADOS")
    private Integer workedDays;

    @Column(name = "MONTO", precision = Constants.BIG_DECIMAL_DEFAULT_PRECISION, scale = Constants.BIG_DECIMAL_DEFAULT_SCALE)
    private BigDecimal amount;

    @Column(name = "MONEDA")
    @Enumerated(EnumType.STRING)
    private FinancesCurrencyType currency;

    @Column(name = "FECHARETIRO")
    @Temporal(TemporalType.DATE)
    private Date layOffDate;

    @Column(name = "ESTADO", length = Constants.STRING_LENGTH_20, nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull
    private DismissalState state;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "IDARCHIVO", referencedColumnName = "idarchivo")
    private File file;

    @Column(name = "DESCRIPCION", nullable = false, length = Constants.LONG_TEXT_LENGTH)
    @Length(max = Constants.LONG_TEXT_LENGTH)
    @NotEmpty
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDCOMPANIA", nullable = false, updatable = false)
    @NotNull
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IDCONTRATO", referencedColumnName = "idcontrato", nullable = false)
    @NotNull
    private Contract contract;

    @OneToMany(mappedBy = "dismissal", fetch = FetchType.LAZY)
    private Collection<DismissalDetail> dismissalDetailList;

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

    public Integer getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(Integer workedDays) {
        this.workedDays = workedDays;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public Date getLayOffDate() {
        return layOffDate;
    }

    public void setLayOffDate(Date layOffDate) {
        this.layOffDate = layOffDate;
    }

    public DismissalState getState() {
        return state;
    }

    public void setState(DismissalState state) {
        this.state = state;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
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

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Collection<DismissalDetail> getDismissalDetailList() {
        return dismissalDetailList;
    }

    public void setDismissalDetailList(Collection<DismissalDetail> dismissalDetailList) {
        this.dismissalDetailList = dismissalDetailList;
    }
}