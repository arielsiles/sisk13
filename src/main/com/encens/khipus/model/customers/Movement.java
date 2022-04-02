package com.encens.khipus.model.customers;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.finances.VoucherDetail;
import com.encens.khipus.util.Constants;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * Date: 12/11/13
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "Movement.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "movimiento",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Table(name = "movimiento",schema = Constants.CASHBOX_SCHEMA)
public class Movement implements BaseModel {

    @Id
    @Column(name = "idmovimiento", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Movement.tableGenerator")
    private Long id;

    @Temporal(value = TemporalType.DATE)
    @Column(name = "fecha_factura")
    private Date date;

    @Column(name = "glosa")
    private String gloss;

    @Column(name = "estado")
    private String state;

    /*@Column(name = "TIPOEMISION")
    @Enumerated(EnumType.STRING)
    private InvoiceEmissionType emissionType;*/

    @Column(name = "tipoemision")
    private String emissionType;

    @Column(name = "nrofactura")
    private Integer number;

    @Column(name = "nit_cliente")
    private String nit;

    @Column(name = "razon_social")
    private String name;

    @Column(name = "importe_total")
    private BigDecimal amount;

    @Column(name = "importe_ice_iehd_tasas")
    private BigDecimal amountIce;

    @Column(name = "export_exentas")
    private BigDecimal exemptExport;

    @Column(name = "ventas_grab_tasacero")
    private BigDecimal taxedSalesZero;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    @Column(name = "descuentos")
    private BigDecimal discount;

    @Column(name = "importe_para_debito_fiscal")
    private BigDecimal amountFiscalDebit;

    @Column(name = "debito_fiscal")
    private BigDecimal fiscalDebit;

    @Column(name = "codigocontrol")
    private String controlCode;

    @Column(name = "tipopago")
    private String paymentType;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "fecharegistro")
    private Date registrationDate;

    @Column(name = "codigo_qr")
    private String qrCode;

    @Column(name = "nro_autorizacion")
    private String authorizationNumber;

    @Column(name = "cuf")
    private String cuf;

    @Column(name = "fechasin")
    private String fechaSin;

    @Column(name = "leyenda")
    private String leyenda;

    @Column(name = "descri")
    private String descri;

    @Column(name = "codestado")
    private String codigoEstado;

    @Column(name = "codigorec")
    private String codigoRecepcion;

    @Column(name = "factura")
    @Lob
    private String factura;

    /** **/

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_tmpdet", nullable = true, insertable = true, updatable = false)
    private VoucherDetail voucherDetailFiscalDebit;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idpedidos", nullable = true, insertable = true, updatable = false)
    private CustomerOrder customerOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getGloss() {
        return gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountIce() {
        return amountIce;
    }

    public void setAmountIce(BigDecimal amountIce) {
        this.amountIce = amountIce;
    }

    public BigDecimal getExemptExport() {
        return exemptExport;
    }

    public void setExemptExport(BigDecimal exemptExport) {
        this.exemptExport = exemptExport;
    }

    public BigDecimal getTaxedSalesZero() {
        return taxedSalesZero;
    }

    public void setTaxedSalesZero(BigDecimal taxedSalesZero) {
        this.taxedSalesZero = taxedSalesZero;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getAmountFiscalDebit() {
        return amountFiscalDebit;
    }

    public void setAmountFiscalDebit(BigDecimal amountFiscalDebit) {
        this.amountFiscalDebit = amountFiscalDebit;
    }

    public String getControlCode() {
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        this.controlCode = controlCode;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public VoucherDetail getVoucherDetailFiscalDebit() {
        return voucherDetailFiscalDebit;
    }

    public void setVoucherDetailFiscalDebit(VoucherDetail voucherDetailFiscalDebit) {
        this.voucherDetailFiscalDebit = voucherDetailFiscalDebit;
    }

    public CustomerOrder getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(CustomerOrder customerOrder) {
        this.customerOrder = customerOrder;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getAuthorizationNumber() {
        return authorizationNumber;
    }

    public void setAuthorizationNumber(String authorizationNumber) {
        this.authorizationNumber = authorizationNumber;
    }

    public BigDecimal getFiscalDebit() {
        return fiscalDebit;
    }

    public void setFiscalDebit(BigDecimal fiscalDebit) {
        this.fiscalDebit = fiscalDebit;
    }

    /** SIN DATA **/
    public String getCuf() {
        return cuf;
    }

    public void setCuf(String cuf) {
        this.cuf = cuf;
    }

    public String getFechaSin() {
        return fechaSin;
    }

    public void setFechaSin(String fechaSin) {
        this.fechaSin = fechaSin;
    }

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public String getDescri() {
        return descri;
    }

    public void setDescri(String descri) {
        this.descri = descri;
    }

    public String getCodigoEstado() {
        return codigoEstado;
    }

    public void setCodigoEstado(String codigoEstado) {
        this.codigoEstado = codigoEstado;
    }

    public String getCodigoRecepcion() {
        return codigoRecepcion;
    }

    public void setCodigoRecepcion(String codigoRecepcion) {
        this.codigoRecepcion = codigoRecepcion;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public String getEmissionType() {
        return emissionType;
    }

    public void setEmissionType(String emissionType) {
        this.emissionType = emissionType;
    }
}
