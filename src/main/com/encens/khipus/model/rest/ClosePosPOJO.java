package com.encens.khipus.model.rest;

public class ClosePosPOJO {

    private Integer codigoSucursal;
    private Integer codigoPuntoVenta;
    private Integer codigoPuntoVentaCerrar;

    public ClosePosPOJO(Integer codigoSucursal, Integer codigoPuntoVenta, Integer codigoPuntoVentaCerrar) {
        this.codigoSucursal = codigoSucursal;
        this.codigoPuntoVenta = codigoPuntoVenta;
        this.codigoPuntoVentaCerrar = codigoPuntoVentaCerrar;
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

    public Integer getCodigoPuntoVentaCerrar() {
        return codigoPuntoVentaCerrar;
    }

    public void setCodigoPuntoVentaCerrar(Integer codigoPuntoVentaCerrar) {
        this.codigoPuntoVentaCerrar = codigoPuntoVentaCerrar;
    }
}
