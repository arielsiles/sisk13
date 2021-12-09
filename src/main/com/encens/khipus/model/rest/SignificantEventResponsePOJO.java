package com.encens.khipus.model.rest;

import java.util.List;

/**
 * Created by Admin on 4/12/2021.
 */
public class SignificantEventResponsePOJO {

    private boolean transaccion;
    private List<SignificantEventCodePOJO> listaCodigos;

    public boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(boolean transaccion) {
        this.transaccion = transaccion;
    }

    public List<SignificantEventCodePOJO> getListaCodigos() {
        return listaCodigos;
    }

    public void setListaCodigos(List<SignificantEventCodePOJO> listaCodigos) {
        this.listaCodigos = listaCodigos;
    }
}
