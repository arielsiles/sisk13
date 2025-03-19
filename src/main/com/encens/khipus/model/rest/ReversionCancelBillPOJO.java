package com.encens.khipus.model.rest;

public class ReversionCancelBillPOJO {

    private Integer codigoSucursal;
    private Integer codigoPuntoVenta;
    private String cuf;

    public ReversionCancelBillPOJO(Integer codigoSucursal, Integer codigoPuntoVenta, String cuf) {
        this.codigoSucursal = codigoSucursal;
        this.codigoPuntoVenta = codigoPuntoVenta;
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

    public String getCuf() {
        return cuf;
    }

    public void setCuf(String cuf) {
        this.cuf = cuf;
    }
}
