package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * @author:
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "TaxRule.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "reglatributaria",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "reglatributaria", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcompania", "numeroorden"}),
        @UniqueConstraint(columnNames = {"idcompania", "numeroserie"})})
public class TaxRule implements BaseModel {

    @Id
    @Column(name = "idreglatributaria", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TaxRule.tableGenerator")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idtipodosificacion", nullable = false)
    private DosageType dosageType;

    @ManyToOne
    @JoinColumn(name = "idporcentajetributario", nullable = false)
    private TaxPercentage taxPercentage;

    @ManyToOne
    @JoinColumn(name = "idusuario", nullable = true)
    private User user;

    @Column(name = "numeroorden", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "numeroserie", nullable = false, length = 50)
    private String serialNumber;

    @Column(name = "numerofacturainicio", nullable = false)
    private Long startInvoiceNumber;

    @Column(name = "numerofacturafin", nullable = false)
    private Long endInvoiceNumber;

    @Column(name = "numerofacturaactual", nullable = false)
    private Long currentInvoiceNumber;

    @Column(name = "cantidadasignada", nullable = false)
    private Integer assignedAmount;

    @Column(name = "cantidadminima", nullable = false)
    private Integer minimalAmount;

    @Column(name = "periododosificacion", nullable = false, length = 150)
    private String requestDosage;

    @Column(name = "fechapedido", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateRequest;

    @Column(name = "fechadosificacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDosage;

    @Column(name = "notas", nullable = true)
    @Lob
    private String notes;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DosageType getDosageType() {
        return dosageType;
    }

    public void setDosageType(DosageType dosageType) {
        this.dosageType = dosageType;
    }

    public TaxPercentage getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(TaxPercentage taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Long getStartInvoiceNumber() {
        return startInvoiceNumber;
    }

    public void setStartInvoiceNumber(Long startInvoiceNumber) {
        this.startInvoiceNumber = startInvoiceNumber;
    }

    public Long getEndInvoiceNumber() {
        return endInvoiceNumber;
    }

    public void setEndInvoiceNumber(Long endInvoiceNumber) {
        this.endInvoiceNumber = endInvoiceNumber;
    }

    public Long getCurrentInvoiceNumber() {
        return currentInvoiceNumber;
    }

    public void setCurrentInvoiceNumber(Long currentInvoiceNumber) {
        this.currentInvoiceNumber = currentInvoiceNumber;
    }

    public Integer getAssignedAmount() {
        return assignedAmount;
    }

    public void setAssignedAmount(Integer assignedAmount) {
        this.assignedAmount = assignedAmount;
    }

    public Integer getMinimalAmount() {
        return minimalAmount;
    }

    public void setMinimalAmount(Integer minimalAmount) {
        this.minimalAmount = minimalAmount;
    }

    public String getRequestDosage() {
        return requestDosage;
    }

    public void setRequestDosage(String requestDosage) {
        this.requestDosage = requestDosage;
    }

    public Date getDateRequest() {
        return dateRequest;
    }

    public void setDateRequest(Date dateRequest) {
        this.dateRequest = dateRequest;
    }

    public Date getDateDosage() {
        return dateDosage;
    }

    public void setDateDosage(Date dateDosage) {
        this.dateDosage = dateDosage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
