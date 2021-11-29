package com.encens.khipus.model.rest;

public class CancelBillPOJO {

    private Integer codigoSucursal;
    private Integer codigoPuntoVenta;
    private Integer codigoMotivo;
    private String cuf;

    public CancelBillPOJO(Integer codigoSucursal, Integer codigoPuntoVenta, Integer codigoMotivo, String cuf) {
        this.codigoSucursal = codigoSucursal;
        this.codigoPuntoVenta = codigoPuntoVenta;
        this.codigoMotivo = codigoMotivo;
        this.cuf = cuf;
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

    public Integer getCodigoMotivo() {
        return codigoMotivo;
    }

    public void setCodigoMotivo(Integer codigoMotivo) {
        this.codigoMotivo = codigoMotivo;
    }

    public String getCuf() {
        return cuf;
    }

    public void setCuf(String cuf) {
        this.cuf = cuf;
    }
}
