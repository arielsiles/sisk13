package com.encens.khipus.model.rest;

import java.util.List;

/**
 * Created by Admin on 3/12/2021.
 */
public class ProductAndServiceCodesPOJO {

    private String codigoActividad;
    private Integer codigoProducto;
    private Integer descripcionProducto;
    private List<String> nandina;

    public String getCodigoActividad() {
        return codigoActividad;
    }

    public void setCodigoActividad(String codigoActividad) {
        this.codigoActividad = codigoActividad;
    }

    public Integer getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(Integer codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public Integer getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(Integer descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public List<String> getNandina() {
        return nandina;
    }

    public void setNandina(List<String> nandina) {
        this.nandina = nandina;
    }
}
