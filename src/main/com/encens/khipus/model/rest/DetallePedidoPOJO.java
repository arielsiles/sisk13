package com.encens.khipus.model.rest;

public class DetallePedidoPOJO {

    private String actividadEconomica; //: "105000",
    private String codigoProductoSin; //": 22221,
    private String codigoProducto; // "JN-22221",
    private int unidadMedida; //": 57:UNIDAD (BIENES)
    private String descripcion; //": "LECHE EVAPORADA",
    private int cantidad; //": 10,
    private Double precioUnitario; //": 7.5,
    private Double montoDescuento; //": 0,
    private Double subTotal; //": 75

    public String getActividadEconomica() {
        return actividadEconomica;
    }

    public void setActividadEconomica(String actividadEconomica) {
        this.actividadEconomica = actividadEconomica;
    }

    public String getCodigoProductoSin() {
        return codigoProductoSin;
    }

    public void setCodigoProductoSin(String codigoProductoSin) {
        this.codigoProductoSin = codigoProductoSin;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public int getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(int unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getMontoDescuento() {
        return montoDescuento;
    }

    public void setMontoDescuento(Double montoDescuento) {
        this.montoDescuento = montoDescuento;
    }

    public Double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }
}
