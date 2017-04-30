package com.encens.khipus.model.customers;


import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Diego on 19/04/2015.
 */
@Entity
@Table(name = "articulospromocion")
public class ArticulosPromocion implements Serializable {
    @Id
    @Column(name = "idarticulospromocion")
    private Long id;
    @Column(name = "CANTIDAD")
    private Integer cantidad;
    @JoinColumn(name="IDPROMOCION",referencedColumnName = "IDPROMOCION")
    @ManyToOne(optional = false)
    private Promocion promocion;
    @JoinColumn(name="IDVENTAARTICULO",referencedColumnName = "IDVENTAARTICULO")
    @ManyToOne(optional = false)
    private Ventaarticulo ventaarticulo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Promocion getPromocion() {
        return promocion;
    }

    public void setPromocion(Promocion promocion) {
        this.promocion = promocion;
    }

    public Ventaarticulo getVentaarticulo() {
        return ventaarticulo;
    }

    public void setVentaarticulo(Ventaarticulo ventaarticulo) {
        this.ventaarticulo = ventaarticulo;
    }
}
