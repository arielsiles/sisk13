package com.encens.khipus.model.rest;

import java.util.List;

/**
 * Created by Admin on 4/12/2021.
 */
public class ActivitiesResponsePOJO {

    private Boolean transaccion;
    private List<ActivitiesListResponsePOJO> listaActividades;


    public Boolean getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Boolean transaccion) {
        this.transaccion = transaccion;
    }

    public List<ActivitiesListResponsePOJO> getListaActividades() {
        return listaActividades;
    }

    public void setListaActividades(List<ActivitiesListResponsePOJO> listaActividades) {
        this.listaActividades = listaActividades;
    }
}
