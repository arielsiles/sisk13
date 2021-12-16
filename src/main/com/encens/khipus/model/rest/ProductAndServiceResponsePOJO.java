package com.encens.khipus.model.rest;

import java.util.List;

/**
 * Created by Admin on 3/12/2021.
 */
public class ProductAndServiceResponsePOJO {

    private Boolean transaccion;
    private List<ProductAndServiceCodesPOJO> listaCodigos;
    private List<ProductAndServiceMessagesPOJO> mensajesList;

    public Boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Boolean transaccion) {
        this.transaccion = transaccion;
    }

    public List<ProductAndServiceCodesPOJO> getListaCodigos() {
        return listaCodigos;
    }

    public void setListaCodigos(List<ProductAndServiceCodesPOJO> listaCodigos) {
        this.listaCodigos = listaCodigos;
    }

    public List<ProductAndServiceMessagesPOJO> getMensajesList() {
        return mensajesList;
    }

    public void setMensajesList(List<ProductAndServiceMessagesPOJO> mensajesList) {
        this.mensajesList = mensajesList;
    }
}
