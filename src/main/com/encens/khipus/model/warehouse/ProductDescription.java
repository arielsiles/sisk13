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
 * Catalogo de descripciones tecnicas por producto. Una entrada describe el
 * producto en lenguaje extenso (norma, presentacion, especificaciones) y se
 * reutiliza en los Vales de Despacho para imprimir la celda DESCRIPCION del
 * reporte "Hoja de Ruta".
 *
 * Tabla: inv_descripcion_producto.
 *
 * Ciclo de estados (ver {@link CatalogApprovalState}):
 *   BORRADOR -> APROBADO -> INACTIVO   (sin vuelta atras)
 * Solo APROBADO aparece en el selectPopUp del despacho. APROBADO/INACTIVO
 * son inmutables (politica: si hay que cambiar el texto, se inactiva y se
 * crea una nueva).
 *
 * Un producto puede tener N descripciones, varias pueden estar APROBADAS
 * simultaneamente (el operador elige cual aplica al despacho).
 */
@NamedQueries({
        @NamedQuery(name = "ProductDescription.findApprovedByProduct",
                query = "select d from ProductDescription d "
                        + "where d.productItemCompanyNumber = :companyNumber "
                        + "and d.productItemCode = :productItemCode "
                        + "and d.state = com.encens.khipus.model.warehouse.CatalogApprovalState.APROBADO "
                        + "order by d.id desc")
})
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_descripcion_producto", schema = Constants.FINANCES_SCHEMA)
public class ProductDescription implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddescripcion_producto", nullable = false)
    private Long id;

    @Column(name = "no_cia_art", nullable = false, length = 2)
    @NotNull
    @Length(max = 2)
    private String productItemCompanyNumber;

    @Column(name = "cod_art", nullable = false, length = 6)
    @NotNull
    @Length(max = 6)
    private String productItemCode;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia_art", referencedColumnName = "no_cia",
                    nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "cod_art", referencedColumnName = "cod_art",
                    nullable = false, insertable = false, updatable = false)
    })
    private ProductItem productItem;

    /**
     * Nombre descriptivo corto. Es lo que se muestra en el dropdown del
     * despacho (en vez del texto largo). Nullable para no romper registros
     * existentes; getDisplayName() hace fallback al resumen de la descripcion.
     */
    @Column(name = "nombre", length = 120)
    @Length(max = 120)
    private String name;

    @Lob
    @Column(name = "descripcion", nullable = false)
    @NotNull
    private String description;

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

    public String getProductItemCompanyNumber() {
        return productItemCompanyNumber;
    }

    public void setProductItemCompanyNumber(String productItemCompanyNumber) {
        this.productItemCompanyNumber = productItemCompanyNumber;
    }

    public String getProductItemCode() {
        return productItemCode;
    }

    public void setProductItemCode(String productItemCode) {
        this.productItemCode = productItemCode;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
        if (productItem != null) {
            this.productItemCompanyNumber = productItem.getId().getCompanyNumber();
            this.productItemCode = productItem.getId().getProductItemCode();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    /**
     * Etiqueta para el dropdown del despacho: el nombre descriptivo si fue
     * cargado; si no (registros antiguos), cae al resumen de la descripcion.
     */
    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        return getShortDescription();
    }

    /** Resumen corto (primeros 60 caracteres) para listas y mensajes. */
    public String getShortDescription() {
        if (description == null) {
            return "";
        }
        String d = description.trim();
        if (d.length() <= 60) {
            return d;
        }
        return d.substring(0, 60) + "...";
    }
}
