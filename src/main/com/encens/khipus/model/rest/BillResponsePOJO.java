package com.encens.khipus.model.rest;

public class BillResponsePOJO {

    private Integer numeroFactura;
    private String tipo;
    private String cuf;
    private String direccion;
    private Long fecha;
    private String leyenda;
    private ReceptionResponsePOJO respuestaRecepcion;
    private String hash;
    private String factura;

    public Integer getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(Integer numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getCuf() {
        return cuf;
    }

    public void setCuf(String cuf) {
        this.cuf = cuf;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Long getFecha() {
        return fecha;
    }

    public void setFecha(Long fecha) {
        this.fecha = fecha;
    }

    public String getLeyenda() {
        return leyenda;
    }

    public void setLeyenda(String leyenda) {
        this.leyenda = leyenda;
    }

    public ReceptionResponsePOJO getRespuestaRecepcion() {
        return respuestaRecepcion;
    }

    public void setRespuestaRecepcion(ReceptionResponsePOJO respuestaRecepcion) {
        this.respuestaRecepcion = respuestaRecepcion;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
