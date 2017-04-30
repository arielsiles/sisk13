package com.encens.khipus.model.customers;

import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.CashBox;
import com.encens.khipus.model.finances.TaxRule;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Invoice entity
 *
 * @author
 * @version $Id: Invoice.java 2008-9-10 10:55:52 $
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Invoice.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "factura",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)
/*
@org.hibernate.annotations.GenericGenerator(
        name = "invoice-number-uuid",
        strategy = "uuid"
)   */

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "factura")
public class Invoice {

    @Id
    @Column(name = "idfactura", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Invoice.tableGenerator")
    private Long id;

    @Column(name = "numerofactura", length = 50, nullable = false)
    //TODO, this should be an primary key also, so it must be a compound PK...
    //@GeneratedValue(generator = "invoice-number-uuid")
    private String number;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha", nullable = false)
    private Date creationDate;

    @Column(name = "apellidopaterno", length = 200)
    private String lastName;

    @Column(name = "apellidomaterno", length = 200)
    private String maidenName;

    @Column(name = "nombres", length = 200)
    private String firstName;

    @Column(name = "razonsocial", length = 200)
    private String organizationName;

    @Column(name = "nodoctributario", length = 100)
    private String taxDocumentNumber;

    //TODO change to an Enum
    @Column(name = "tipodoctibutario", length = 50)
    private String taxDocumentType;

    @Column(name = "importetotal", precision = 13, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "totaldescuento", precision = 13, scale = 2, nullable = false)
    private BigDecimal totalDiscount;

    //TODO change to an Enum
    @Column(name = "estado", length = 50)
    private String state;

    //TODO change to an Enum
    @Column(name = "formapago", length = 50)
    private String payMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcaja", referencedColumnName = "idcaja")
    private CashBox cashBox;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idcliente", referencedColumnName = "idcliente")
    private Customer customer;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idfactura", referencedColumnName = "idfactura", nullable = false)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<InvoiceDetail> details = new ArrayList<InvoiceDetail>(0);

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "descuentofactura",
            joinColumns = @JoinColumn(name = "idfactura"),
            inverseJoinColumns = @JoinColumn(name = "iddescuentocliente"),
            schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA
    )
    @OrderBy("name asc")
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<CustomerDiscountRule> discountRules = new ArrayList<CustomerDiscountRule>(0);

    @ManyToOne
    @JoinColumn(name = "idreglatributaria", insertable = false)
    private TaxRule taxRule;

    @Transient
    private Integer totalProductsQuantity = 0;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    @PrePersist
    void prePersist() {
        creationDate = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getTaxDocumentNumber() {
        return taxDocumentNumber;
    }

    public void setTaxDocumentNumber(String taxDocumentNumber) {
        this.taxDocumentNumber = taxDocumentNumber;
    }

    public String getTaxDocumentType() {
        return taxDocumentType;
    }

    public void setTaxDocumentType(String taxDocumentType) {
        this.taxDocumentType = taxDocumentType;
    }

    public BigDecimal getTotalAmount() {
        totalAmount = refreshTotalAmount();
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(BigDecimal totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public CashBox getCashBox() {
        return cashBox;
    }

    public void setCashBox(CashBox cashBox) {
        this.cashBox = cashBox;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<InvoiceDetail> getDetails() {
        return details;
    }

    public void setDetails(List<InvoiceDetail> details) {
        this.details = details;
    }

    private BigDecimal refreshTotalAmount() {
        BigDecimal total = new BigDecimal(0.0);
        totalProductsQuantity = 0;
        for (InvoiceDetail detail : details) {
            total = total.add(detail.getTotalAmount());
            totalProductsQuantity += detail.getQuantity();
        }
        return total;
    }

    public Integer getTotalProductsQuantity() {
        return totalProductsQuantity;
    }

    public List<CustomerDiscountRule> getDiscountRules() {
        return discountRules;
    }

    public void setDiscountRules(List<CustomerDiscountRule> discountRules) {
        this.discountRules = discountRules;
    }

    public TaxRule getTaxRule() {
        return taxRule;
    }

    public void setTaxRule(TaxRule taxRule) {
        this.taxRule = taxRule;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
