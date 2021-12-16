package com.encens.khipus.model.rest;

import java.util.List;

/**
 * Created by Admin on 3/12/2021.
 */
public class PaymentMethodTypeResponsePOJO {

    private Boolean transaccion;
    private List<PaymentMethodTypeCodesPOJO> listaCodigos;

    public Boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Boolean transaccion) {
        this.transaccion = transaccion;
    }

    public List<PaymentMethodTypeCodesPOJO> getListaCodigos() {
        return listaCodigos;
    }

    public void setListaCodigos(List<PaymentMethodTypeCodesPOJO> listaCodigos) {
        this.listaCodigos = listaCodigos;
    }
}
