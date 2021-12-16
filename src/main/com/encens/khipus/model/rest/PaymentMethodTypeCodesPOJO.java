package com.encens.khipus.model.rest;

/**
 * Created by Admin on 3/12/2021.
 */
public class PaymentMethodTypeCodesPOJO {

    private Integer codigoClasificador;
    private String descripcion;

    public PaymentMethodTypeCodesPOJO() {
    }

    public PaymentMethodTypeCodesPOJO(Integer codigoClasificador, String descripcion) {
        this.codigoClasificador = codigoClasificador;
        this.descripcion = descripcion;
    }

    public Integer getCodigoClasificador() {
        return codigoClasificador;
    }

    public void setCodigoClasificador(Integer codigoClasificador) {
        this.codigoClasificador = codigoClasificador;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
