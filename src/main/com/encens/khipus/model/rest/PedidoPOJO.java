package com.encens.khipus.model.rest;

import java.math.BigDecimal;
import java.util.List;

public class PedidoPOJO {

    private Integer codigoSucursal;
    private Integer codigoPuntoVenta;
    private Integer numeroFactura;
    private String nombreRazonSocial;
    private Integer codigoTipoDocumentoIdentidad;
    private String complemento;
    private String numeroDocumento;
    private String codigoCliente;
    private Integer codigoMetodoPago;
    private String usuario;
    private Integer codigoDocumentoSector;
    private Integer codigoMoneda;
    private Integer tipoCambio;
    private BigDecimal montoTotal;
    private BigDecimal montoTotalSujetoIva;
    private BigDecimal montoTotalMoneda;
    private BigDecimal descuentoAdicional;

    private List<DetallePedidoPOJO> detalle;

    public String getNombreRazonSocial() {
        return nombreRazonSocial;
    }

    public void setNombreRazonSocial(String nombreRazonSocial) {
        this.nombreRazonSocial = nombreRazonSocial;
    }

    public Integer getCodigoTipoDocumentoIdentidad() {
        return codigoTipoDocumentoIdentidad;
    }

    public void setCodigoTipoDocumentoIdentidad(Integer codigoTipoDocumentoIdentidad) {
        this.codigoTipoDocumentoIdentidad = codigoTipoDocumentoIdentidad;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public Integer getCodigoMetodoPago() {
        return codigoMetodoPago;
    }

    public void setCodigoMetodoPago(Integer codigoMetodoPago) {
        this.codigoMetodoPago = codigoMetodoPago;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Integer getCodigoDocumentoSector() {
        return codigoDocumentoSector;
    }

    public void setCodigoDocumentoSector(Integer codigoDocumentoSector) {
        this.codigoDocumentoSector = codigoDocumentoSector;
    }

    public Integer getCodigoMoneda() {
        return codigoMoneda;
    }

    public void setCodigoMoneda(Integer codigoMoneda) {
        this.codigoMoneda = codigoMoneda;
    }

    public Integer getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(Integer tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getMontoTotalSujetoIva() {
        return montoTotalSujetoIva;
    }

    public void setMontoTotalSujetoIva(BigDecimal montoTotalSujetoIva) {
        this.montoTotalSujetoIva = montoTotalSujetoIva;
    }

    public BigDecimal getMontoTotalMoneda() {
        return montoTotalMoneda;
    }

    public void setMontoTotalMoneda(BigDecimal montoTotalMoneda) {
        this.montoTotalMoneda = montoTotalMoneda;
    }

    public List<DetallePedidoPOJO> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetallePedidoPOJO> detalle) {
        this.detalle = detalle;
    }

    public Integer getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(Integer numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public Integer getCodigoSucursal() {
        return codigoSucursal;
    }

    public void setCodigoSucursal(Integer codigoSucursal) {
        this.codigoSucursal = codigoSucursal;
    }

    public Integer getCodigoPuntoVenta() {
        return codigoPuntoVenta;
    }

    public void setCodigoPuntoVenta(Integer codigoPuntoVenta) {
        this.codigoPuntoVenta = codigoPuntoVenta;
    }

    public BigDecimal getDescuentoAdicional() {
        return descuentoAdicional;
    }

    public void setDescuentoAdicional(BigDecimal descuentoAdicional) {
        this.descuentoAdicional = descuentoAdicional;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }
}
