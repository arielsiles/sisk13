package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 21/12/14
 * Time: 2:03
 * To change this template use File | Settings | File Templates.
 */
@TableGenerator(name = "Dosage_Generator",
        table = "SECUENCIA",
        pkColumnName = "TABLA",
        valueColumnName = "VALOR",
        pkColumnValue = "DOSIFICACIONES",
        allocationSize = 10)

@Entity
@Table(name = "DOSIFICACIONES",schema = Constants.CASHBOX_SCHEMA)

public class Dosage implements BaseModel {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Dosage_Generator")
    private Long id;

    @Column(name = "NROAUTORIZACION", nullable = false, columnDefinition = "DECIMAL(15,0)")
    private BigDecimal numberAuthorization = new BigDecimal(0.0);

    @Temporal(value = TemporalType.DATE)
    @Column(name = "FECHA_VENCIMIENTO")
    private Date dateExpiration;

    @Column(name="LLAVE")
    private String key;

    @Column(name="ACTIVO")
    private String state;

    @Column(name="EST_COD")
    private String estCod;

    @Column(name = "FACTURADEL", nullable = false, columnDefinition = "DECIMAL(10,0)")
    private BigDecimal invoiceFrom = new BigDecimal(0.0);

    @Column(name = "FACTURAAL", nullable = false, columnDefinition = "DECIMAL(10,0)")
    private BigDecimal invoiceTo = new BigDecimal(0.0);

    @Column(name = "NRO_ACTUAL", nullable = false, columnDefinition = "DECIMAL(10,0)")
    private BigDecimal numberCurrent = new BigDecimal(0.0);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getNumberAuthorization() {
        return numberAuthorization;
    }

    public void setNumberAuthorization(BigDecimal numberAuthorization) {
        this.numberAuthorization = numberAuthorization;
    }

    public Date getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(Date dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getEstCod() {
        return estCod;
    }

    public void setEstCod(String estCod) {
        this.estCod = estCod;
    }

    public BigDecimal getInvoiceFrom() {
        return invoiceFrom;
    }

    public void setInvoiceFrom(BigDecimal invoiceFrom) {
        this.invoiceFrom = invoiceFrom;
    }

    public BigDecimal getInvoiceTo() {
        return invoiceTo;
    }

    public void setInvoiceTo(BigDecimal invoiceTo) {
        this.invoiceTo = invoiceTo;
    }

    public BigDecimal getNumberCurrent() {
        return numberCurrent;
    }

    public void setNumberCurrent(BigDecimal numberCurrent) {
        this.numberCurrent = numberCurrent;
    }
}
