package com.encens.khipus.model.rest;

/**
 * Created by Admin on 3/12/2021.
 */
public class ProductAndServicePOJO {

    private Integer codigoSucursal;
    private Integer codigoPuntoVenta;

    public ProductAndServicePOJO() {
    }

    public ProductAndServicePOJO(Integer codigoSucursal, Integer codigoPuntoVenta) {
        this.codigoSucursal = codigoSucursal;
        this.codigoPuntoVenta = codigoPuntoVenta;
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
}
