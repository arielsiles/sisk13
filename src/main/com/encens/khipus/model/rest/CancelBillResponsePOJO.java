package com.encens.khipus.model.rest;

import java.util.List;

public class CancelBillResponsePOJO {

    private String codigoDescripcion;
    private Integer codigoEstado;
    private String codigoRecepcion;
    private Boolean transaccion;
    private List<CancelBillResponseMessagePOJO> mensajesList;

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

    public Boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Boolean transaccion) {
        this.transaccion = transaccion;
    }

    public List<CancelBillResponseMessagePOJO> getMensajesList() {
        return mensajesList;
    }

    public void setMensajesList(List<CancelBillResponseMessagePOJO> mensajesList) {
        this.mensajesList = mensajesList;
    }
}
