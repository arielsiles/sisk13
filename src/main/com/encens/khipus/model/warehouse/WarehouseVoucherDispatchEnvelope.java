package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Envase (bolsa fisica) del Vale de Despacho de Productos Terminados.
 * Tabla: inv_valedespacho_envase.
 * <p>
 * Una fila por bolsa, generada automaticamente al APROBAR el despacho:
 * por cada {@link WarehouseVoucherDispatchDetail} se crean
 * {@code bagsCount} envases con correlativos consecutivos arrancando en
 * {@code bagsFromNumber} del detalle.
 * <p>
 * En estado APROBADO el operador edita {@link #envelopeDetail} y
 * {@link #netWeightApproxKg} desde el formulario del despacho. En estado
 * FINALIZADO los envases quedan inmutables y solo se imprime el reporte
 * "Detalle de Envases Carguio".
 */
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_valedespacho_envase", schema = Constants.FINANCES_SCHEMA)
public class WarehouseVoucherDispatchEnvelope implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idenvase", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idvaledespacho", nullable = false)
    private WarehouseVoucherDispatch dispatch;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "iddetalledespacho", nullable = false)
    private WarehouseVoucherDispatchDetail detail;

    @Column(name = "numero_correlativo", nullable = false)
    @NotNull
    private Integer correlativeNumber;

    @Column(name = "codigo_identificacion", nullable = false, length = 120)
    @NotNull
    @Length(max = 120)
    private String identificationCode;

    @Column(name = "detalle_envase", length = 255)
    @Length(max = 255)
    private String envelopeDetail;

    @Column(name = "peso_neto_aprox_kg", precision = 12, scale = 3)
    private BigDecimal netWeightApproxKg;

    @Column(name = "createdby", length = 4)
    @Length(max = 4)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createddate")
    private Date createdDate;

    @Column(name = "updatedby", length = 4)
    @Length(max = 4)
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updateddate")
    private Date updatedDate;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    /* =============== Getters / Setters =============== */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WarehouseVoucherDispatch getDispatch() {
        return dispatch;
    }

    public void setDispatch(WarehouseVoucherDispatch dispatch) {
        this.dispatch = dispatch;
    }

    public WarehouseVoucherDispatchDetail getDetail() {
        return detail;
    }

    public void setDetail(WarehouseVoucherDispatchDetail detail) {
        this.detail = detail;
    }

    public Integer getCorrelativeNumber() {
        return correlativeNumber;
    }

    public void setCorrelativeNumber(Integer correlativeNumber) {
        this.correlativeNumber = correlativeNumber;
    }

    public String getIdentificationCode() {
        return identificationCode;
    }

    public void setIdentificationCode(String identificationCode) {
        this.identificationCode = identificationCode;
    }

    public String getEnvelopeDetail() {
        return envelopeDetail;
    }

    public void setEnvelopeDetail(String envelopeDetail) {
        this.envelopeDetail = envelopeDetail;
    }

    public BigDecimal getNetWeightApproxKg() {
        return netWeightApproxKg;
    }

    public void setNetWeightApproxKg(BigDecimal netWeightApproxKg) {
        this.netWeightApproxKg = netWeightApproxKg;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
