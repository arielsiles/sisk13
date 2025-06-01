package com.encens.khipus.model.rest;

import java.util.List;

public class ClosePosResponsePOJO {

    private Integer codigoPuntoVenta;
    private Boolean transaccion;
    private List<ClosePosMessagePOJO> mensajesList;

    public Integer getCodigoPuntoVenta() {
        return codigoPuntoVenta;
    }

    public void setCodigoPuntoVenta(Integer codigoPuntoVenta) {
        this.codigoPuntoVenta = codigoPuntoVenta;
    }

    public Boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Boolean transaccion) {
        this.transaccion = transaccion;
    }

    public List<ClosePosMessagePOJO> getMensajesList() {
        return mensajesList;
    }

    public void setMensajesList(List<ClosePosMessagePOJO> mensajesList) {
        this.mensajesList = mensajesList;
    }
}
