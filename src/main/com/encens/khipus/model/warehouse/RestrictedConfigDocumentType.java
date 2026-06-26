package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Tipo de Documento permitido dentro de una configuracion de restriccion de
 * vales. Tabla: inv_vale_restriccion_tipodoc.
 *
 * @see RestrictedWarehouseVoucherConfig
 */
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_vale_restriccion_tipodoc", schema = Constants.FINANCES_SCHEMA)
public class RestrictedConfigDocumentType implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrestricciontipodoc", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idvalerestriccion", nullable = false)
    @NotNull
    private RestrictedWarehouseVoucherConfig config;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false),
            @JoinColumn(name = "cod_doc", referencedColumnName = "cod_doc", nullable = false)
    })
    @NotNull
    private WarehouseDocumentType documentType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public RestrictedConfigDocumentType() {
    }

    public RestrictedConfigDocumentType(RestrictedWarehouseVoucherConfig config, WarehouseDocumentType documentType) {
        this.config = config;
        this.documentType = documentType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RestrictedWarehouseVoucherConfig getConfig() {
        return config;
    }

    public void setConfig(RestrictedWarehouseVoucherConfig config) {
        this.config = config;
    }

    public WarehouseDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(WarehouseDocumentType documentType) {
        this.documentType = documentType;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
