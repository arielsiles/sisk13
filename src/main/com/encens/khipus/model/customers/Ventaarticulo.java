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

/**
 *
 * @author Diego
 */
@Entity
@Table(name = "ventaarticulo")
public class Ventaarticulo implements Serializable {

    @Id
    @Column(name = "idventaarticulo")
    private Long idventaarticulo;
    @Column(name = "precio")
    private Double precio;
    @Column(name = "tipo")
    private String tipo;
    @JoinColumns({
            @JoinColumn(name = "no_cia", referencedColumnName = "no_cia"),
            @JoinColumn(name = "cod_art", referencedColumnName = "cod_art")})
    @ManyToOne(optional = false)
    private ProductItem productItem;
    @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "ventaarticulo")
    private Collection<ArticulosPromocion> articulosPromocions;
    @JoinColumn(name="idpromocion",referencedColumnName = "idpromocion")
    @ManyToOne
    private Promocion promocion;

    private static final long serialVersionUID = 1L;

    public Ventaarticulo() {
    }

    public Ventaarticulo(Long idventaarticulo) {
        this.idventaarticulo = idventaarticulo;
    }

    public Long getIdventaarticulo() {
        return idventaarticulo;
    }

    public void setIdventaarticulo(Long idventaarticulo) {
        this.idventaarticulo = idventaarticulo;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public ProductItem getInvArticulos() {
        return productItem;
    }

    public void setInvArticulos(ProductItem productItem) {
        this.productItem = productItem;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String type) {
        this.tipo = type;
    }

    public ProductItem getProductItem() {
        return productItem;
    }

    public void setProductItem(ProductItem productItem) {
        this.productItem = productItem;
    }

    public Promocion getPromocion() {
        return promocion;
    }

    public void setPromocion(Promocion promocion) {
        this.promocion = promocion;
    }

    public Collection<ArticulosPromocion> getArticulosPromocions() {
        return articulosPromocions;
    }

    public void setArticulosPromocions(Collection<ArticulosPromocion> articulosPromocions) {
        this.articulosPromocions = articulosPromocions;
    }
}
