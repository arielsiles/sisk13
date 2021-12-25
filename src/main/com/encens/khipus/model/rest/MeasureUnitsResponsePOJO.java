package com.encens.khipus.model.rest;

import java.util.List;

/**
 * Created by Admin on 4/12/2021.
 */
public class MeasureUnitsResponsePOJO {

    private Boolean transaccion;
    private List<MeasureUnitsCodesPOJO> listaCodigos;

    public Boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Boolean transaccion) {
        this.transaccion = transaccion;
    }

    public List<MeasureUnitsCodesPOJO> getListaCodigos() {
        return listaCodigos;
    }

    public void setListaCodigos(List<MeasureUnitsCodesPOJO> listaCodigos) {
        this.listaCodigos = listaCodigos;
    }
}
