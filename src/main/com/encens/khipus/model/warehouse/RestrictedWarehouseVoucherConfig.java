package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de la configuracion de restriccion de Vales de Almacen por usuario.
 * Tabla: inv_vale_restriccion.
 *
 * Diseno OPT-IN: un usuario queda restringido SOLO si tiene una fila aqui con
 * active=true. Las tres listas de detalle (tipos de documento, almacenes y
 * articulos) definen lo que ese usuario puede ver/usar al crear o editar un
 * vale. Una lista vacia = sin restriccion en esa dimension.
 *
 * @see RestrictedConfigDocumentType
 * @see RestrictedConfigWarehouse
 * @see RestrictedConfigProductItem
 */
@NamedQueries({
        @NamedQuery(name = "RestrictedWarehouseVoucherConfig.findActiveByUser",
                query = "select c from RestrictedWarehouseVoucherConfig c " +
                        "where c.user.id = :userId and c.active = true"),
        @NamedQuery(name = "RestrictedWarehouseVoucherConfig.countByUserAndDifferent",
                query = "select count(c) from RestrictedWarehouseVoucherConfig c " +
                        "where c.user.id = :userId and c.id <> :id"),
        @NamedQuery(name = "RestrictedWarehouseVoucherConfig.countByUser",
                query = "select count(c) from RestrictedWarehouseVoucherConfig c " +
                        "where c.user.id = :userId")
})
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_vale_restriccion", schema = Constants.FINANCES_SCHEMA)
public class RestrictedWarehouseVoucherConfig implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idvalerestriccion", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", nullable = false)
    @NotNull
    private User user;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean active = Boolean.TRUE;

    @OneToMany(mappedBy = "config", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<RestrictedConfigDocumentType> documentTypes = new ArrayList<RestrictedConfigDocumentType>();

    @OneToMany(mappedBy = "config", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<RestrictedConfigWarehouse> warehouses = new ArrayList<RestrictedConfigWarehouse>();

    @OneToMany(mappedBy = "config", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<RestrictedConfigProductItem> productItems = new ArrayList<RestrictedConfigProductItem>();

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<RestrictedConfigDocumentType> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<RestrictedConfigDocumentType> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public List<RestrictedConfigWarehouse> getWarehouses() {
        return warehouses;
    }

    public void setWarehouses(List<RestrictedConfigWarehouse> warehouses) {
        this.warehouses = warehouses;
    }

    public List<RestrictedConfigProductItem> getProductItems() {
        return productItems;
    }

    public void setProductItems(List<RestrictedConfigProductItem> productItems) {
        this.productItems = productItems;
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

    /* ---- helpers para administrar las listas desde el action ---- */

    public void addDocumentType(WarehouseDocumentType documentType) {
        for (RestrictedConfigDocumentType item : documentTypes) {
            if (item.getDocumentType() != null
                    && item.getDocumentType().getId().equals(documentType.getId())) {
                return;
            }
        }
        documentTypes.add(new RestrictedConfigDocumentType(this, documentType));
    }

    public void removeDocumentType(RestrictedConfigDocumentType item) {
        documentTypes.remove(item);
    }

    public void addWarehouse(Warehouse warehouse) {
        for (RestrictedConfigWarehouse item : warehouses) {
            if (item.getWarehouse() != null
                    && item.getWarehouse().getId().equals(warehouse.getId())) {
                return;
            }
        }
        warehouses.add(new RestrictedConfigWarehouse(this, warehouse));
    }

    public void removeWarehouse(RestrictedConfigWarehouse item) {
        warehouses.remove(item);
    }

    public void addProductItem(ProductItem productItem) {
        for (RestrictedConfigProductItem item : productItems) {
            if (item.getProductItem() != null
                    && item.getProductItem().getId().equals(productItem.getId())) {
                return;
            }
        }
        productItems.add(new RestrictedConfigProductItem(this, productItem));
    }

    public void removeProductItem(RestrictedConfigProductItem item) {
        productItems.remove(item);
    }

    /* ---- conteos para el listado (transient, no mapeados: la entidad usa field access) ---- */

    public int getDocumentTypeCount() {
        return documentTypes != null ? documentTypes.size() : 0;
    }

    public int getWarehouseCount() {
        return warehouses != null ? warehouses.size() : 0;
    }

    public int getProductItemCount() {
        return productItems != null ? productItems.size() : 0;
    }
}
