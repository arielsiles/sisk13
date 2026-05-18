package com.encens.khipus.model.warehouse;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;

/**
 * Catalogo de Lugares de Despacho/Entrega para los Vales de Despacho
 * de Productos Terminados. Tabla: inv_lugardespacho.
 */
@NamedQueries({
        @NamedQuery(name = "DispatchPlace.findAll",
                query = "select p from DispatchPlace p where p.active = true order by p.code"),
        @NamedQuery(name = "DispatchPlace.findByKind",
                query = "select p from DispatchPlace p where p.active = true " +
                        "and (p.kind = :kind or p.kind = com.encens.khipus.model.warehouse.DispatchPlaceKind.AMBOS) " +
                        "order by p.code"),
        @NamedQuery(name = "DispatchPlace.countByCode",
                query = "select count(p) from DispatchPlace p where lower(p.code) = lower(:code)"),
        @NamedQuery(name = "DispatchPlace.countByCodeAndDifferent",
                query = "select count(p) from DispatchPlace p where lower(p.code) = lower(:code) and p.id <> :id")
})

@Entity
@Filter(name = Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(name = "inv_lugardespacho", schema = Constants.FINANCES_SCHEMA)
public class DispatchPlace implements BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlugardespacho", nullable = false)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 20)
    @NotNull
    @Length(max = 20)
    private String code;

    @Column(name = "descripcion", nullable = false, length = 150)
    @NotNull
    @Length(max = 150)
    private String description;

    @Column(name = "direccion", length = 250)
    @Length(max = 250)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @NotNull
    private DispatchPlaceKind kind = DispatchPlaceKind.AMBOS;

    @Column(name = "activo", nullable = false)
    @NotNull
    private Boolean active = Boolean.TRUE;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public DispatchPlaceKind getKind() {
        return kind;
    }

    public void setKind(DispatchPlaceKind kind) {
        this.kind = kind;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
        return code + " - " + description;
    }
}
