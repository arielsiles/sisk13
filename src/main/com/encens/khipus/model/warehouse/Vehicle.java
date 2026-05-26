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
 * Catalogo de Vehiculos de Transporte para los Vales de Despacho.
 * Tabla: inv_vehiculo.
 *
 * Relacion M:N inversa (mappedBy) con Driver: un vehiculo puede ser operado
 * por varios conductores. La relacion la posee {@link Driver#vehicles}.
 */
@NamedQueries({
        @NamedQuery(name = "Vehicle.findAllActive",
                query = "select v from Vehicle v where v.state = com.encens.khipus.model.warehouse.DispatchCatalogState.VIG order by v.plate"),
        @NamedQuery(name = "Vehicle.findByDriver",
                query = "select v from Driver d join d.vehicles v " +
                        "where d.id = :driverId " +
                        "and v.state = com.encens.khipus.model.warehouse.DispatchCatalogState.VIG " +
                        "order by v.plate"),
        @NamedQuery(name = "Vehicle.countByPlate",
                query = "select count(v) from Vehicle v where lower(v.plate) = lower(:plate)"),
        @NamedQuery(name = "Vehicle.countByPlateAndDifferent",
                query = "select count(v) from Vehicle v where lower(v.plate) = lower(:plate) and v.id <> :id")
})

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_vehiculo", schema = Constants.FINANCES_SCHEMA)
public class Vehicle implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idvehiculo", nullable = false)
    private Long id;

    @Column(name = "placa", nullable = false, length = 20)
    @NotNull
    @Length(max = 20)
    private String plate;

    @Column(name = "marca", length = 50)
    @Length(max = 50)
    private String brand;

    @Column(name = "color", length = 30)
    @Length(max = 30)
    private String color;

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

    @ManyToMany(mappedBy = "vehicles", fetch = FetchType.LAZY)
    private List<Driver> drivers = new ArrayList<Driver>();

    /* =============== Getters / Setters =============== */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public List<Driver> getDrivers() {
        return drivers;
    }

    public void setDrivers(List<Driver> drivers) {
        this.drivers = drivers;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(plate != null ? plate : "");
        if (brand != null && !brand.trim().isEmpty()) {
            sb.append(" - ").append(brand);
        }
        if (color != null && !color.trim().isEmpty()) {
            sb.append(" (").append(color).append(")");
        }
        return sb.toString();
    }
}
