package com.encens.khipus.model.rest;

public class PointOfSaleTypeCode {

    private Integer codigoClasificador;
    private String descripcion;


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

    /*@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PointOfSaleTypeCode that = (PointOfSaleTypeCode) obj;
        return Objects.equals(codigoClasificador, that.codigoClasificador);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoClasificador);
    }

    @Override
    public String toString() {
        return codigoClasificador + " - " + descripcion;
    }*/
}
