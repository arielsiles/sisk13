package com.encens.khipus.model.rest;

import java.util.List;

public class ReceptionResponsePOJO {

    private String codigoDescripcion;
    private Integer codigoEstado;
    private String codigoRecepcion;
    private List<ReceptionMessagePOJO> mensajesList;
    private Boolean transaccion;

    public String getCodigoDescripcion() {
        return codigoDescripcion;
    }

    public void setCodigoDescripcion(String codigoDescripcion) {
        this.codigoDescripcion = codigoDescripcion;
    }

    public Integer getCodigoEstado() {
        return codigoEstado;
    }

    public void setCodigoEstado(Integer codigoEstado) {
        this.codigoEstado = codigoEstado;
    }

    public String getCodigoRecepcion() {
        return codigoRecepcion;
    }

    public void setCodigoRecepcion(String codigoRecepcion) {
        this.codigoRecepcion = codigoRecepcion;
    }

    public List<ReceptionMessagePOJO> getMensajesList() {
        return mensajesList;
    }

    public void setMensajesList(List<ReceptionMessagePOJO> mensajesList) {
        this.mensajesList = mensajesList;
    }

    public Boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Boolean transaccion) {
        this.transaccion = transaccion;
    }
}
