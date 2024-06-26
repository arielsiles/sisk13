package com.encens.khipus.model.rest;

public class ReceptionMessagePOJO {

    private Integer codigo;
    private String descripcion;
    private Boolean advertencia;
    private Integer numeroArchivo;
    private Integer numeroDetalle;

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getNumeroArchivo() {
        return numeroArchivo;
    }

    public void setNumeroArchivo(Integer numeroArchivo) {
        this.numeroArchivo = numeroArchivo;
    }

    public Integer getNumeroDetalle() {
        return numeroDetalle;
    }

    public void setNumeroDetalle(Integer numeroDetalle) {
        this.numeroDetalle = numeroDetalle;
    }

    public Boolean getAdvertencia() {
        return advertencia;
    }

    public void setAdvertencia(Boolean advertencia) {
        this.advertencia = advertencia;
    }
}
