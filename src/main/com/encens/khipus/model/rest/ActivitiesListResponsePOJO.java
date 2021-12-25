package com.encens.khipus.model.rest;

/**
 * Created by Admin on 4/12/2021.
 */
public class ActivitiesListResponsePOJO {

    private String codigoCaeb;
    private String descripcion;
    private String tipoActividad;

    public String getCodigoCaeb() {
        return codigoCaeb;
    }

    public void setCodigoCaeb(String codigoCaeb) {
        this.codigoCaeb = codigoCaeb;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoActividad() {
        return tipoActividad;
    }

    public void setTipoActividad(String tipoActividad) {
        this.tipoActividad = tipoActividad;
    }
}
