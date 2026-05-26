package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Catalogo de Conductores para los Vales de Despacho de Productos Terminados.
 * Tabla: inv_conductor.
 *
 * Relacion M:N con Vehicle a traves de inv_conductor_vehiculo.
 */
@NamedQueries({
        @NamedQuery(name = "Driver.findAllActive",
                query = "select d from Driver d where d.state = com.encens.khipus.model.warehouse.DispatchCatalogState.VIG order by d.name"),
        @NamedQuery(name = "Driver.countByLicense",
                query = "select count(d) from Driver d where lower(d.license) = lower(:license)"),
        @NamedQuery(name = "Driver.countByLicenseAndDifferent",
                query = "select count(d) from Driver d where lower(d.license) = lower(:license) and d.id <> :id")
})

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_conductor", schema = Constants.FINANCES_SCHEMA)
public class Driver implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idconductor", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    @NotNull
    @Length(max = 120)
    private String name;

    @Column(name = "licencia", nullable = false, length = 30)
    @NotNull
    @Length(max = 30)
    private String license;

    @Column(name = "celular", length = 30)
    @Length(max = 30)
    private String phone;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "inv_conductor_vehiculo",
            schema = Constants.FINANCES_SCHEMA,
            joinColumns = @JoinColumn(name = "idconductor"),
            inverseJoinColumns = @JoinColumn(name = "idvehiculo"))
    private List<Vehicle> vehicles = new ArrayList<Vehicle>();

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

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public String getFullName() {
        if (license == null || license.trim().isEmpty()) {
            return name;
        }
        return name + " (" + license + ")";
    }
}
