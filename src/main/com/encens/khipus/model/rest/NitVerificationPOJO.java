package com.encens.khipus.model.rest;

public class NitVerificationPOJO {

    private Integer codigoSucursal;
    private Integer codigoPuntoVenta;
    private Long nitParaVerificacion;

    public NitVerificationPOJO(Integer codigoSucursal, Integer codigoPuntoVenta, Long nitParaVerificacion) {
        this.codigoSucursal = codigoSucursal;
        this.codigoPuntoVenta = codigoPuntoVenta;
        this.nitParaVerificacion = nitParaVerificacion;
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

    public Long getNitParaVerificacion() {
        return nitParaVerificacion;
    }

    public void setNitParaVerificacion(Long nitParaVerificacion) {
        this.nitParaVerificacion = nitParaVerificacion;
    }
}
