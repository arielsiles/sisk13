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
 * Catalogo de Tipos de Bolsa/Envase para los Vales de Despacho de Productos
 * Terminados. Tabla: inv_tipo_envase.
 *
 * Cada tipo define el nombre del envase (ej. "Big Bag") y la etiqueta de
 * capacidad por bolsa (ej. "1 tonelada", "1/2 tonelada"). Se usa para generar
 * automaticamente el texto de la columna TOTAL ENTREGADO del certificado:
 *   {bolsas} bolsas {nombre} de {capacidad}
 * El peso por bolsa en Kg ({@link #unitCapacityKg}) es opcional y sirve para la
 * validacion suave (bolsas * capacidad ~= peso de la linea).
 */
@NamedQueries({
        @NamedQuery(name = "InventoryPackaging.findAllActive",
                query = "select p from InventoryPackaging p where p.state = com.encens.khipus.model.warehouse.DispatchCatalogState.VIG order by p.name"),
        @NamedQuery(name = "InventoryPackaging.countByName",
                query = "select count(p) from InventoryPackaging p where lower(p.name) = lower(:name) and lower(p.capacityLabel) = lower(:capacityLabel)"),
        @NamedQuery(name = "InventoryPackaging.countByNameAndDifferent",
                query = "select count(p) from InventoryPackaging p where lower(p.name) = lower(:name) and lower(p.capacityLabel) = lower(:capacityLabel) and p.id <> :id")
})

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_tipo_envase", schema = Constants.FINANCES_SCHEMA)
public class InventoryPackaging implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtipoenvase", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 80)
    @NotNull
    @Length(max = 80)
    private String name;

    @Column(name = "etiqueta_capacidad", nullable = false, length = 60)
    @NotNull
    @Length(max = 60)
    private String capacityLabel;

    @Column(name = "capacidad_kg", precision = 12, scale = 3)
    private BigDecimal unitCapacityKg;

    /**
     * Peso real promedio por bolsa al pesarla en balanza (incluye el peso de
     * la bolsa misma). Distinto de {@link #unitCapacityKg} que es la
     * capacidad NETA. Se imprime en la OBSERVACION del DETALLE EN PESO de
     * la Nota de Remision. Si esta null, no se imprime nada.
     */
    @Column(name = "peso_bruto_promedio_kg", precision = 12, scale = 3)
    private BigDecimal averageGrossWeightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 3)
    @NotNull
    private DispatchCatalogState state = DispatchCatalogState.VIG;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapacityLabel() {
        return capacityLabel;
    }

    public void setCapacityLabel(String capacityLabel) {
        this.capacityLabel = capacityLabel;
    }

    public BigDecimal getUnitCapacityKg() {
        return unitCapacityKg;
    }

    public void setUnitCapacityKg(BigDecimal unitCapacityKg) {
        this.unitCapacityKg = unitCapacityKg;
    }

    public BigDecimal getAverageGrossWeightKg() {
        return averageGrossWeightKg;
    }

    public void setAverageGrossWeightKg(BigDecimal averageGrossWeightKg) {
        this.averageGrossWeightKg = averageGrossWeightKg;
    }

    public DispatchCatalogState getState() {
        return state;
    }

    public void setState(DispatchCatalogState state) {
        this.state = state;
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

    public String getFullName() {
        if (capacityLabel == null || capacityLabel.trim().isEmpty()) {
            return name;
        }
        return name + " - " + capacityLabel;
    }
}
