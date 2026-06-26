package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Almacen permitido dentro de una configuracion de restriccion de vales.
 * Tabla: inv_vale_restriccion_almacen.
 *
 * @see RestrictedWarehouseVoucherConfig
 */
@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_vale_restriccion_almacen", schema = Constants.FINANCES_SCHEMA)
public class RestrictedConfigWarehouse implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idrestriccionalmacen", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idvalerestriccion", nullable = false)
    @NotNull
    private RestrictedWarehouseVoucherConfig config;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia", nullable = false),
            @JoinColumn(name = "cod_alm", referencedColumnName = "cod_alm", nullable = false)
    })
    @NotNull
    private Warehouse warehouse;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    @NotNull
    private Company company;

    public RestrictedConfigWarehouse() {
    }

    public RestrictedConfigWarehouse(RestrictedWarehouseVoucherConfig config, Warehouse warehouse) {
        this.config = config;
        this.warehouse = warehouse;
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

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
