package com.encens.khipus.model.rest;

/**
 * Created by Admin on 3/12/2021.
 */
public class SetOfflineModePOJO {

    private Integer codigoSucursal;
    private Integer codigoPuntoVenta;
    private String cafc;
    private Integer eventCode;

    public SetOfflineModePOJO(Integer codigoSucursal, Integer codigoPuntoVenta, String cafc, Integer eventCode) {
        this.codigoSucursal = codigoSucursal;
        this.codigoPuntoVenta = codigoPuntoVenta;
        this.cafc = cafc;
        this.eventCode = eventCode;
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

    public String getCafc() {
        return cafc;
    }

    public void setCafc(String cafc) {
        this.cafc = cafc;
    }

    public Integer getEventCode() {
        return eventCode;
    }

    public void setEventCode(Integer eventCode) {
        this.eventCode = eventCode;
    }
}
