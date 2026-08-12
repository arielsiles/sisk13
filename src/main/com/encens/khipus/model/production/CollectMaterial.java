package com.encens.khipus.model.production;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.usertype.IntegerBooleanUserType;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.jboss.seam.security.Identity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@TableGenerator(name = "CollectMaterial.tableGenerator",
        table = "secuencia",
        pkColumnName = "tabla",
        valueColumnName = "valor",
        pkColumnValue = "acopiomp",
        allocationSize = Constants.SEQUENCE_ALLOCATION_SIZE)

@Table(name = "acopiomp")
@Entity
@Filter(name = "companyFilter")
@EntityListeners(CompanyListener.class)
public class CollectMaterial implements Serializable, BaseModel {

    @Id
    @Column(name = "idacopiomp", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "CollectMaterial.tableGenerator")
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "fecha", nullable = false)
    private Date date;

    @Column(name = "codigo", nullable = false, length = 50)
    private String code;

    @Column(name = "pesoprov", precision = 12, scale = 2, nullable = false)
    private BigDecimal providerWeight;

    @Column(name = "pesoneto", precision = 12, scale = 2, nullable = false)
    private BigDecimal netWeight;

    @Column(name = "pesobal", precision = 12, scale = 2, nullable = false)
    private BigDecimal balanceWeight;

    @Column(name = "precio", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "boleta", nullable = false)
    private String ticket;

    @Column(name = "tienefac", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean hasInvoice = Boolean.FALSE;

    @Column(name = "formulario", nullable = false)
    private String form;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private CollectMaterialState state = CollectMaterialState.PEN;

    @Column(name = "conta", nullable = false)
    @Type(type = IntegerBooleanUserType.NAME)
    private Boolean accountigFlag = Boolean.FALSE;

    @Column(name = "chofer", nullable = false)
    private String driver;

    @Column(name = "observacion", nullable = true)
    private String observation;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /** Auditoria: mismo patron y mismos nombres de columna que Voucher (sf_tmpenc). */
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Asiento contable (sf_tmpenc.id_tmpenc) que genero la contabilizacion de ESTE acopio.
     * Se guarda como Long y no como relacion: sf_tmpenc vive en FINANCES_SCHEMA, que no
     * siempre coincide con el schema de la aplicacion, y no se necesita navegar el asiento
     * completo, solo consultar su estado al revertir.
     * NULL en lo contabilizado con el esquema viejo (un asiento 'IA' por dia para varios
     * acopios): esos acopios no son revertibles por el sistema.
     */
    @Column(name = "id_tmpenc")
    private Long voucherId;

    /** Marcas de correccion. Copia de la ultima fila de acopiomp_reversion, para pintar el
     *  listado sin joins; el historial autoritativo esta en la bitacora. */
    @Column(name = "nro_reversiones", nullable = false)
    private Integer revertCount = 0;

    @Column(name = "ultima_reversion_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastRevertAt;

    @Column(name = "ultima_reversion_by")
    private String lastRevertBy;

    @Column(name = "ultima_reversion_motivo")
    private String lastRevertReason;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.createdBy = getCurrentUser();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
        this.updatedBy = getCurrentUser();
    }

    private String getCurrentUser() {
        return Identity.instance().isLoggedIn() ? Identity.instance().getPrincipal().getName() : "unknown";
    }

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idzonaproductiva", nullable = false)
    private ProductiveZone productiveZone;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idproductormateriaprima", nullable = false)
    private RawMaterialProducer producer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idmetaproductoproduccion", nullable = false)
    private MetaProduct metaProduct;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "idempleado", nullable = true, updatable = true, insertable = true)
    private Employee receptionEmployee;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(BigDecimal netWeight) {
        this.netWeight = netWeight;
    }

    public BigDecimal getBalanceWeight() {
        return balanceWeight;
    }

    public void setBalanceWeight(BigDecimal balanceWeight) {
        this.balanceWeight = balanceWeight;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public MetaProduct getMetaProduct() {
        return metaProduct;
    }

    public void setMetaProduct(MetaProduct metaProduct) {
        this.metaProduct = metaProduct;
    }

    public CollectMaterialState getState() {
        return state;
    }

    public void setState(CollectMaterialState state) {
        this.state = state;
    }

    public RawMaterialProducer getProducer() {
        return producer;
    }

    public void setProducer(RawMaterialProducer producer) {
        this.producer = producer;
    }

    public ProductiveZone getProductiveZone() {
        return productiveZone;
    }

    public void setProductiveZone(ProductiveZone productiveZone) {
        this.productiveZone = productiveZone;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public Employee getReceptionEmployee() {
        return receptionEmployee;
    }

    public void setReceptionEmployee(Employee receptionEmployee) {
        this.receptionEmployee = receptionEmployee;
    }

    /*@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CollectMaterial objetoA = (CollectMaterial) obj;
        return id == objetoA.id;
    }*/

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CollectMaterial && this.getId().equals(((CollectMaterial) obj).getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Boolean getAccountigFlag() {
        return accountigFlag;
    }

    public void setAccountigFlag(Boolean accountigFlag) {
        this.accountigFlag = accountigFlag;
    }

    public Boolean getHasInvoice() {
        return hasInvoice;
    }

    public void setHasInvoice(Boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }

    public BigDecimal getProviderWeight() {
        return providerWeight;
    }

    public void setProviderWeight(BigDecimal providerWeight) {
        this.providerWeight = providerWeight;
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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    public Integer getRevertCount() {
        return revertCount;
    }

    public void setRevertCount(Integer revertCount) {
        this.revertCount = revertCount;
    }

    public Date getLastRevertAt() {
        return lastRevertAt;
    }

    public void setLastRevertAt(Date lastRevertAt) {
        this.lastRevertAt = lastRevertAt;
    }

    public String getLastRevertBy() {
        return lastRevertBy;
    }

    public void setLastRevertBy(String lastRevertBy) {
        this.lastRevertBy = lastRevertBy;
    }

    public String getLastRevertReason() {
        return lastRevertReason;
    }

    public void setLastRevertReason(String lastRevertReason) {
        this.lastRevertReason = lastRevertReason;
    }

    /** El acopio fue revertido y vuelto a procesar al menos una vez. */
    public boolean isReverted() {
        return revertCount != null && revertCount > 0;
    }

    public BigDecimal getAverageWeight() {

        // Verificar y convertir valores nulos a cero si es necesario
        BigDecimal providerWeight = (getProviderWeight() != null) ? getProviderWeight() : BigDecimal.ZERO;
        BigDecimal balanceWeight = (getBalanceWeight() != null) ? getBalanceWeight() : BigDecimal.ZERO;

        // Calcular el promedio entre providerWeight y balanceWeight (considerando valores nulos como cero)
        BigDecimal sum = BigDecimalUtil.sum(providerWeight, balanceWeight);
        //BigDecimal average = sum.divide(new BigDecimal("2"), 2, BigDecimal.ROUND_HALF_UP); // Redondeo a 2 decimales
        BigDecimal average = BigDecimalUtil.divide(sum, BigDecimalUtil.toBigDecimal(2));

        return average;
    }
}
