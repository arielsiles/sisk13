package com.encens.khipus.model.rest;

import java.util.List;

/**
 * Created by Admin on 4/12/2021.
 */
public class PointOfSaleTypesResponsePOJO {

    private Boolean transaccion;
    private List<PointOfSaleTypeCode> listaCodigos;

    public Boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Boolean transaccion) {
        this.transaccion = transaccion;
    }

    public List<PointOfSaleTypeCode> getListaCodigos() {
        return listaCodigos;
    }

    public void setListaCodigos(List<PointOfSaleTypeCode> listaCodigos) {
        this.listaCodigos = listaCodigos;
    }
}
