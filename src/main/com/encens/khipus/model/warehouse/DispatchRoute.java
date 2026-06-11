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
 * Catalogo de Rutas de Despacho. Una ruta agrupa origen, destino, lista de
 * paradas e imagen del mapa, y se reutiliza por N despachos para imprimir la
 * "Hoja de Ruta". La imagen vive en este catalogo (LONGBLOB), NUNCA replicada
 * al despacho - el crecimiento de la BD es funcion del numero de rutas
 * registradas, no del numero de despachos.
 *
 * Tabla: inv_ruta_despacho.
 *
 * Ciclo de estados (ver {@link CatalogApprovalState}):
 *   BORRADOR -> APROBADO -> INACTIVO   (sin vuelta atras)
 * Solo APROBADO aparece en el selectPopUp del despacho. APROBADO/INACTIVO
 * son inmutables (politica: si la ruta cambia, se inactiva y se crea nueva,
 * para no alterar registros historicos que la referencian).
 */
@NamedQueries({
        @NamedQuery(name = "DispatchRoute.findApproved",
                query = "select r from DispatchRoute r "
                        + "where r.state = com.encens.khipus.model.warehouse.CatalogApprovalState.APROBADO "
                        + "order by r.name"),
        @NamedQuery(name = "DispatchRoute.countByName",
                query = "select count(r) from DispatchRoute r where lower(r.name) = lower(:name)"),
        @NamedQuery(name = "DispatchRoute.countByNameAndDifferent",
                query = "select count(r) from DispatchRoute r where lower(r.name) = lower(:name) and r.id <> :id")
})
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_ruta_despacho", schema = Constants.FINANCES_SCHEMA)
public class DispatchRoute implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idruta", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    @NotNull
    @Length(max = 120)
    private String name;

    @Column(name = "origen_texto", length = 200)
    @Length(max = 200)
    private String originText;

    @Column(name = "destino_texto", length = 200)
    @Length(max = 200)
    private String destinationText;

    @Lob
    @Column(name = "paradas", nullable = false)
    @NotNull
    private String waypoints;

    @Lob
    @Column(name = "imagen_mapa")
    private byte[] mapImage;

    @Column(name = "imagen_mapa_content_type", length = 50)
    @Length(max = 50)
    private String mapImageContentType;

    @Column(name = "distancia_km", precision = 8, scale = 2)
    private BigDecimal estimatedDistanceKm;

    @Column(name = "duracion_horas", precision = 6, scale = 2)
    private BigDecimal estimatedDurationHours;

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

    public String getOriginText() {
        return originText;
    }

    public void setOriginText(String originText) {
        this.originText = originText;
    }

    public String getDestinationText() {
        return destinationText;
    }

    public void setDestinationText(String destinationText) {
        this.destinationText = destinationText;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(String waypoints) {
        this.waypoints = waypoints;
    }

    public byte[] getMapImage() {
        return mapImage;
    }

    public void setMapImage(byte[] mapImage) {
        this.mapImage = mapImage;
    }

    public String getMapImageContentType() {
        return mapImageContentType;
    }

    public void setMapImageContentType(String mapImageContentType) {
        this.mapImageContentType = mapImageContentType;
    }

    public BigDecimal getEstimatedDistanceKm() {
        return estimatedDistanceKm;
    }

    public void setEstimatedDistanceKm(BigDecimal estimatedDistanceKm) {
        this.estimatedDistanceKm = estimatedDistanceKm;
    }

    public BigDecimal getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(BigDecimal estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
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

    public boolean isHasMapImage() {
        return mapImage != null && mapImage.length > 0;
    }
}
