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
@Table(name = "libroventas", schema = Constants.KHIPUS_SCHEMA)
public class SalesBook implements BaseModel {

    @Id
    @Column(name = "idlibroventa", insertable = false, updatable = false)
    private String id;

    @Column(name = "nro_nit_cliente", length = 20, insertable = false, updatable = false)
    @Length(max = 20)
    private String nit;

    @Column(name = "nombre_razon_social_cliente", length = 100, insertable = false, updatable = false)
    @Length(max = 100)
    private String socialName;

    @Column(name = "nro_de_factura", length = 20, insertable = false, updatable = false)
    private String invoiceNumber;

    @Column(name = "nro_de_autorizacion", length = 6, insertable = false, updatable = false)
    private String authorizationNumber;

    @Column(name = "fecha", insertable = false, updatable = false)
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column(name = "total_factura", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal amount;

    @Column(name = "total_ice", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal ice;

    @Column(name = "importes_exentos", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal exempt;

    @Column(name = "importe_neto", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal netAmount;

    @Column(name = "debito_fiscal", precision = 16, scale = 2, insertable = false, updatable = false)
    private BigDecimal tax;

    @Column(name = "estado", insertable = false, updatable = false)
    private String status;

    @Column(name = "codigo_de_control", length = 20, insertable = false, updatable = false)
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
