package com.encens.khipus.model.sales;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyNumberListener;
import com.encens.khipus.model.customers.Client;
import com.encens.khipus.model.finances.FinancesCurrencyType;
import com.encens.khipus.util.Constants;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.security.Identity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Orden de Venta (area comercial). Documento cabecera-detalle que replica el
 * formato "ORDER Nº" del proveedor. Los datos del comprador se guardan como
 * snapshot (copia inmutable) ademas de la FK al {@link Client}.
 *
 * @author
 * @version 1.0
 */
@NamedQueries({
        @NamedQuery(name = "SalesOrder.countByCompanyNumber",
                query = "select count(o.id) from SalesOrder o where o.companyNumber =:companyNumber")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA, name = "SalesOrder.tableGenerator",
        table = Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "ordenventa",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@EntityListeners({CompanyNumberListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "ordenventa")
public class SalesOrder implements BaseModel {

    @Id
    @Column(name = "idordenventa", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "SalesOrder.tableGenerator")
    private Long id;

    @Column(name = "no_cia", length = 2, nullable = false)
    private String companyNumber;

    @Column(name = "nro_orden", length = 20, nullable = false)
    @NotNull
    @Length(max = 20)
    private String orderNumber;

    @Column(name = "estado", nullable = false, length = 5)
    @Enumerated(EnumType.STRING)
    @NotNull
    private SalesOrderState state = SalesOrderState.BOR;

    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date date;

    @Column(name = "fecha_entrega")
    @Temporal(TemporalType.DATE)
    private Date deliveryDate;

    /* ---- comprador (FK + snapshot) ---- */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idpersonacliente", nullable = false)
    @NotNull
    private Client client;

    @Column(name = "comprador", length = 300)
    @Length(max = 300)
    private String buyerName;

    @Column(name = "nro_registro", length = 50)
    @Length(max = 50)
    private String buyerRegNumber;

    @Column(name = "direccion", length = 500)
    @Length(max = 500)
    private String buyerAddress;

    @Column(name = "telefono", length = 50)
    @Length(max = 50)
    private String buyerPhone;

    @Column(name = "fax", length = 50)
    @Length(max = 50)
    private String buyerFax;

    @Column(name = "email", length = 150)
    @Length(max = 150)
    private String buyerEmail;

    /* ---- condiciones comerciales ---- */

    @Column(name = "moneda", nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    @NotNull
    private FinancesCurrencyType currency = FinancesCurrencyType.P;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idincoterm")
    private Incoterm incoterm;

    @Column(name = "incoterm_lugar", length = 200)
    @Length(max = 200)
    private String incotermPlace;

    @Column(name = "cotizacion", length = 100)
    @Length(max = 100)
    private String quotation;

    @Column(name = "sc_codigo", length = 100)
    @Length(max = 100)
    private String scCode;

    @Column(name = "cc_codigo", length = 100)
    @Length(max = 100)
    private String ccCode;

    @Column(name = "condiciones_pago", length = 1000)
    @Length(max = 1000)
    private String paymentConditions;

    /** Texto de terminos de cabecera copiado del catalogo (snapshot, sin relacion). */
    @Column(name = "termino_cabecera", length = 1000)
    @Length(max = 1000)
    private String headerTerm;

    @Column(name = "comentarios", length = 2000)
    @Length(max = 2000)
    private String comments;

    @Column(name = "observaciones_gerencia", length = 2000)
    @Length(max = 2000)
    private String managementObservations;

    /* ---- totales ---- */

    @Column(name = "sub_total", precision = 16, scale = 2)
    private BigDecimal subTotalAmount = BigDecimal.ZERO;

    @Column(name = "descuento", precision = 16, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "recargo", precision = 16, scale = 2)
    private BigDecimal rechargeAmount = BigDecimal.ZERO;

    @Column(name = "total", precision = 16, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /* ---- detalle y notas ---- */

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "salesOrder", cascade = CascadeType.ALL)
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OrderBy("detailNumber asc")
    private List<SalesOrderDetail> detailList = new ArrayList<SalesOrderDetail>(0);

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(schema = Constants.KHIPUS_SCHEMA, name = "ordenventa_nota_sel",
            joinColumns = @JoinColumn(name = "idordenventa"),
            inverseJoinColumns = @JoinColumn(name = "idordenventa_nota"))
    private List<SalesOrderNote> selectedNotes = new ArrayList<SalesOrderNote>(0);

    /* ---- auditoria / workflow ---- */

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    @Column(name = "fecha_revision")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sentAt;

    @Column(name = "fecha_aprobacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedAt;

    @Column(name = "aprobado_por", length = 50)
    private String approvedBy;

    @Column(name = "fecha_anulacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nullifiedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
        if (this.createdBy == null) {
            this.createdBy = getCurrentUser();
        }
    }

    private String getCurrentUser() {
        return Identity.instance().isLoggedIn() ? Identity.instance().getPrincipal().getName() : "unknown";
    }

    /**
     * Copia los datos del comprador desde el cliente (snapshot). No pisa datos ya
     * capturados si el cliente es null.
     */
    public void copyBuyerDataFromClient() {
        if (client == null) {
            return;
        }
        this.buyerName = client.getBusinessName() != null && client.getBusinessName().trim().length() > 0
                ? client.getBusinessName() : client.getFullName();
        // Nro. de registro = tipo de documento (ej. "C.N.P.J.", "NIT") + numero.
        // El prefijo sale del tipo de documento del cliente (no hardcodeado).
        String regType = (client.getInvoiceDocumentType() != null
                && client.getInvoiceDocumentType().getName() != null)
                ? client.getInvoiceDocumentType().getName() + ": " : "";
        String regNumber = client.getNitNumber() != null ? client.getNitNumber() : "";
        this.buyerRegNumber = (regType + regNumber).trim();
        this.buyerAddress = client.getAddress();
        this.buyerPhone = client.getPhone() != null ? String.valueOf(client.getPhone()) : null;
        this.buyerFax = client.getFax();
        this.buyerEmail = client.getEmail();
    }

    public boolean isDraft() {
        return SalesOrderState.BOR.equals(state);
    }

    public boolean isInReview() {
        return SalesOrderState.REV.equals(state);
    }

    public boolean isApproved() {
        return SalesOrderState.APR.equals(state);
    }

    public boolean isNullified() {
        return SalesOrderState.ANL.equals(state);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public SalesOrderState getState() {
        return state;
    }

    public void setState(SalesOrderState state) {
        this.state = state;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerRegNumber() {
        return buyerRegNumber;
    }

    public void setBuyerRegNumber(String buyerRegNumber) {
        this.buyerRegNumber = buyerRegNumber;
    }

    public String getBuyerAddress() {
        return buyerAddress;
    }

    public void setBuyerAddress(String buyerAddress) {
        this.buyerAddress = buyerAddress;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public String getBuyerFax() {
        return buyerFax;
    }

    public void setBuyerFax(String buyerFax) {
        this.buyerFax = buyerFax;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public FinancesCurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(FinancesCurrencyType currency) {
        this.currency = currency;
    }

    public Incoterm getIncoterm() {
        return incoterm;
    }

    public void setIncoterm(Incoterm incoterm) {
        this.incoterm = incoterm;
    }

    public String getIncotermPlace() {
        return incotermPlace;
    }

    public void setIncotermPlace(String incotermPlace) {
        this.incotermPlace = incotermPlace;
    }

    public String getQuotation() {
        return quotation;
    }

    public void setQuotation(String quotation) {
        this.quotation = quotation;
    }

    public String getScCode() {
        return scCode;
    }

    public void setScCode(String scCode) {
        this.scCode = scCode;
    }

    public String getCcCode() {
        return ccCode;
    }

    public void setCcCode(String ccCode) {
        this.ccCode = ccCode;
    }

    public String getPaymentConditions() {
        return paymentConditions;
    }

    public void setPaymentConditions(String paymentConditions) {
        this.paymentConditions = paymentConditions;
    }

    public String getHeaderTerm() {
        return headerTerm;
    }

    public void setHeaderTerm(String headerTerm) {
        this.headerTerm = headerTerm;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getManagementObservations() {
        return managementObservations;
    }

    public void setManagementObservations(String managementObservations) {
        this.managementObservations = managementObservations;
    }

    public BigDecimal getSubTotalAmount() {
        return subTotalAmount;
    }

    public void setSubTotalAmount(BigDecimal subTotalAmount) {
        this.subTotalAmount = subTotalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getRechargeAmount() {
        return rechargeAmount;
    }

    public void setRechargeAmount(BigDecimal rechargeAmount) {
        this.rechargeAmount = rechargeAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<SalesOrderDetail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<SalesOrderDetail> detailList) {
        this.detailList = detailList;
    }

    public List<SalesOrderNote> getSelectedNotes() {
        return selectedNotes;
    }

    public void setSelectedNotes(List<SalesOrderNote> selectedNotes) {
        this.selectedNotes = selectedNotes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public Date getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Date approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Date getNullifiedAt() {
        return nullifiedAt;
    }

    public void setNullifiedAt(Date nullifiedAt) {
        this.nullifiedAt = nullifiedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
