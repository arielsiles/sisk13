package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Catalogo de Observaciones de Despacho. Cada entrada tiene un NOMBRE
 * descriptivo (corto, lo que el operador elige en el dropdown) y el TEXTO de
 * la observacion (lo que se registra en el despacho).
 *
 * Tabla: inv_observacion_despacho.
 *
 * Ciclo de estados (ver {@link CatalogApprovalState}):
 *   BORRADOR -> APROBADO -> INACTIVO   (sin vuelta atras)
 * Solo APROBADO aparece en el dropdown del despacho. APROBADO/INACTIVO son
 * inmutables (politica: si hay que cambiar el texto, se inactiva y se crea
 * una nueva).
 *
 * NOTA: el despacho sigue guardando el TEXTO de la observacion en su columna
 * inv_valedespacho.observacion (denormalizado al guardar). Asi los reportes
 * ya aprobados que leen ese texto NO cambian de comportamiento; aqui solo se
 * cambia la forma de capturarlo (texto libre -> seleccion de catalogo).
 */
@NamedQueries({
        @NamedQuery(name = "DispatchObservation.findApproved",
                query = "select o from DispatchObservation o "
                        + "where o.state = com.encens.khipus.model.warehouse.CatalogApprovalState.APROBADO "
                        + "order by o.name")
})
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_observacion_despacho", schema = Constants.FINANCES_SCHEMA)
public class DispatchObservation implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idobservacion", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    @NotNull
    @Length(max = 120)
    private String name;

    @Column(name = "observacion", nullable = false, length = 1000)
    @NotNull
    @Length(max = 1000)
    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    @NotNull
    private CatalogApprovalState state = CatalogApprovalState.BORRADOR;

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

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public CatalogApprovalState getState() {
        return state;
    }

    public void setState(CatalogApprovalState state) {
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

    /* =============== Convenience =============== */

    public boolean isEditable() {
        return state == CatalogApprovalState.BORRADOR;
    }

    public boolean isApproved() {
        return state == CatalogApprovalState.APROBADO;
    }

    public boolean isInactive() {
        return state == CatalogApprovalState.INACTIVO;
    }

    /** Resumen corto (primeros 80 caracteres) del texto, para listas. */
    public String getShortObservation() {
        if (observation == null) {
            return "";
        }
        String d = observation.trim();
        if (d.length() <= 80) {
            return d;
        }
        return d.substring(0, 80) + "...";
    }
}
