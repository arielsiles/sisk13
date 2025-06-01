package com.encens.khipus.model.rest;

import java.util.List;

/**
 * Created by Admin on 21/03/2025
 */
public class RegistroPosResponsePOJO {

    private Boolean transaccion;
    private Integer codigoPuntoVenta;
    private List<RegistroPosMessage> mensajesList;

    public Boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Boolean transaccion) {
        this.transaccion = transaccion;
    }

    public Integer getCodigoPuntoVenta() {
        return codigoPuntoVenta;
    }

    public void setCodigoPuntoVenta(Integer codigoPuntoVenta) {
        this.codigoPuntoVenta = codigoPuntoVenta;
    }

    public List<RegistroPosMessage> getMensajesList() {
        return mensajesList;
    }

    public void setMensajesList(List<RegistroPosMessage> mensajesList) {
        this.mensajesList = mensajesList;
    }
}
