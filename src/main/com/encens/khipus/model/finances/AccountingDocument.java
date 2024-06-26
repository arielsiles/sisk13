package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 2.25
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "AccountingDocument.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "documentocontable",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@SequenceGenerator(name = "AccountingDocument.sequenceGenerator", sequenceName = Constants.FINANCES_SCHEMA + ".sf_trans")

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "documentocontable")
@Inheritance(strategy = InheritanceType.JOINED)
public class AccountingDocument implements BaseModel {
    @Id
    @Column(name = "iddocumentocontable", nullable = false, scale = 24)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "AccountingDocument.tableGenerator")
    private Long id;

    @Column(name = "codigocontrol", length = 20)
    @Length(max = 20)
    private String controlCode;

    @Column(name = "direccion", length = 50)
    @Length(max = 50)
    private String address;

    @Column(name = "exento", precision = 12, scale = 2)
    private BigDecimal exempt;

    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "tasas", precision = 12, scale = 2)
    private BigDecimal rates;

    @Column(name = "nocf", precision = 12, scale = 2)
    private BigDecimal noTaxCredit;

    @Column(name = "ice", precision = 12, scale = 2)
    private BigDecimal ice;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Column(name = "importe", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "importeneto", precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "iva", precision = 12, scale = 2)
    private BigDecimal iva;

    @Column(name = "nit", length = 20)
    @Length(max = 20)
    private String nit;

    @Column(name = "nombre", length = 100)
    @Length(max = 100)
    private String name;

    @Column(name = "descuentos", precision = 12, scale = 2)
    private BigDecimal discounts;

    @Column(name = "numero", length = 20)
    @Length(max = 20)
    private String number;

    @Column(name = "numeroautorizacion", length = 150)
    @Length(max = 150)
    private String authorizationNumber;

    @GeneratedValue(generator = "AccountingDocument.sequenceGenerator")
    @Column(name = "numerotransaccion", length = 20)
    @Length(max = 20)
    private String transactionNumber;

    @Column(name = "regcompro")
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    private Boolean hasVoucher = false;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getExempt() {
        return exempt;
    }

    public void setExempt(BigDecimal exempt) {
        this.exempt = exempt;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getIce() {
        return ice;
    }

    public void setIce(BigDecimal ice) {
        this.ice = ice;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAuthorizationNumber() {
        return authorizationNumber;
    }

    public void setAuthorizationNumber(String authorizationNumber) {
        this.authorizationNumber = authorizationNumber;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public Boolean getHasVoucher() {
        return hasVoucher;
    }

    public void setHasVoucher(Boolean hasVoucher) {
        this.hasVoucher = hasVoucher;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public BigDecimal getRates() {
        return rates;
    }

    public void setRates(BigDecimal rates) {
        this.rates = rates;
    }

    public BigDecimal getDiscounts() {
        return discounts;
    }

    public void setDiscounts(BigDecimal discounts) {
        this.discounts = discounts;
    }

    public BigDecimal getNoTaxCredit() {
        return noTaxCredit;
    }

    public void setNoTaxCredit(BigDecimal noTaxCredit) {
        this.noTaxCredit = noTaxCredit;
    }
}
