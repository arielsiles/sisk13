package com.encens.khipus.model.rest;

/**
 * Created by Admin on 21/03/2025
 */
public class RegistroPosPOJO {

    private Integer codigoSucursal;
    private Integer codigoPuntoVenta;
    private Integer codigoTipoPuntoVenta;
    private String nombrePuntoVenta;
    private String descripcion;

    public RegistroPosPOJO(Integer codigoSucursal, Integer codigoPuntoVenta, Integer codigoTipoPuntoVenta, String nombrePuntoVenta, String descripcion) {
        this.codigoSucursal = codigoSucursal;
        this.codigoPuntoVenta = codigoPuntoVenta;
        this.codigoTipoPuntoVenta = codigoTipoPuntoVenta;
        this.nombrePuntoVenta = nombrePuntoVenta;
        this.descripcion = descripcion;
    }

    public Integer getCodigoSucursal() {
        return codigoSucursal;
    }

    public void setCodigoSucursal(Integer codigoSucursal) {
        this.codigoSucursal = codigoSucursal;
    }

    public Integer getCodigoPuntoVenta() {
        return codigoPuntoVenta;
    }

    public void setCodigoPuntoVenta(Integer codigoPuntoVenta) {
        this.codigoPuntoVenta = codigoPuntoVenta;
    }

    public Integer getCodigoTipoPuntoVenta() {
        return codigoTipoPuntoVenta;
    }

    public void setCodigoTipoPuntoVenta(Integer codigoTipoPuntoVenta) {
        this.codigoTipoPuntoVenta = codigoTipoPuntoVenta;
    }

    public String getNombrePuntoVenta() {
        return nombrePuntoVenta;
    }

    public void setNombrePuntoVenta(String nombrePuntoVenta) {
        this.nombrePuntoVenta = nombrePuntoVenta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
