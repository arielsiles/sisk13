package com.encens.khipus.model.finances;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @version 1.0
 */
@Entity
@Table(name = "LIBROVENTAS", schema = Constants.KHIPUS_SCHEMA)
public class SalesBook implements BaseModel {

    @Id
    @Column(name = "IDLIBROVENTA", insertable = false, updatable = false)
    private String id;

    @Column(name = "NRO_NIT_CLIENTE", length = 20, insertable = false, updatable = false)
    @Length(max = 20)
    private String nit;

    @Column(name = "NOMBRE_RAZON_SOCIAL_CLIENTE", length = 100, insertable = false, updatable = false)
    @Length(max = 100)
    private String socialName;

    @Column(name = "NRO_DE_FACTURA", length = 20, insertable = false, updatable = false)
    private String invoiceNumber;

    @Column(name = "NRO_DE_AUTORIZACION", length = 6, insertable = false, updatable = false)
    private String authorizationNumber;

    @Column(name = "FECHA", insertable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "TOTAL_FACTURA", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal amount;

    @Column(name = "TOTAL_ICE", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal ice;

    @Column(name = "IMPORTES_EXENTOS", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal exempt;

    @Column(name = "IMPORTE_NETO", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal netAmount;

    @Column(name = "DEBITO_FISCAL", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal tax;

    @Column(name = "ESTADO", insertable = false, updatable = false)
    private String status;

    @Column(name = "CODIGO_DE_CONTROL", length = 20, insertable = false, updatable = false)
    private String controlCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getSocialName() {
        return socialName;
    }

    public void setSocialName(String socialName) {
        this.socialName = socialName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getAuthorizationNumber() {
        return authorizationNumber;
    }

    public void setAuthorizationNumber(String authorizationNumber) {
        this.authorizationNumber = authorizationNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getIce() {
        return ice;
    }

    public void setIce(BigDecimal ice) {
        this.ice = ice;
    }

    public BigDecimal getExempt() {
        return exempt;
    }

    public void setExempt(BigDecimal exempt) {
        this.exempt = exempt;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }
}
