package com.encens.khipus.model.rest;

import java.io.Serializable;

/**
 * Created by Admin on 4/12/2021.
 */
public class SignificantEventCodePOJO implements Serializable {

    private Integer codigoClasificador;
    private String descripcion;

    public SignificantEventCodePOJO(){}

    public SignificantEventCodePOJO(Integer codigoClasificador, String descripcion) {
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

    public String toString(){
        return getCodigoClasificador() + "-" + getDescripcion();
    }

}
