/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.encens.khipus.model.customers;

import com.encens.khipus.model.warehouse.ProductItem;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

//import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Diego
 */
@Entity
@Table(name = "promocion")
//@XmlRootElement
public class Promocion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "idpromocion")
    private Long idpromocion;
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "fechainicio")
    @Temporal(TemporalType.DATE)
    private Date fechainicio;
    @Column(name = "fechafin")
    @Temporal(TemporalType.DATE)
    private Date fechafin;
    @Column(name = "estado")
    private String estado;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "total")
    private Double total;
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "promocion")
    private Collection<ArticulosPromocion> articulosPromocions;
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia"),
            @JoinColumn(name = "cod_art", referencedColumnName = "cod_art")})
    @ManyToOne(optional = false)
    private ProductItem invArticulos;

    public Promocion() {
    }

    public Promocion(Long idpromocion) {
        this.idpromocion = idpromocion;
    }

    public Long getIdpromocion() {
        return idpromocion;
    }

    public void setIdpromocion(Long idpromocion) {
        this.idpromocion = idpromocion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Date getFechainicio() {
        return fechainicio;
    }

    public void setFechainicio(Date fechainicio) {
        this.fechainicio = fechainicio;
    }

    public Date getFechafin() {
        return fechafin;
    }

    public void setFechafin(Date fechafin) {
        this.fechafin = fechafin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Collection<ArticulosPromocion> getArticulosPromocions() {
        return articulosPromocions;
    }

    public void setArticulosPromocions(Collection<ArticulosPromocion> articulosPromocions) {
        this.articulosPromocions = articulosPromocions;
    }

    public ProductItem getInvArticulos() {
        return invArticulos;
    }

    public void setInvArticulos(ProductItem invArticulos) {
        this.invArticulos = invArticulos;
    }
}
