package com.encens.khipus.model.rest;

import java.math.BigDecimal;
import java.util.List;

public class PedidoPOJO {

    private String nombreRazonSocial; //: RAMIREZ
    private int codigoTipoDocumentoIdentidad; // 1:CI - CEDULA DE IDENTIDAD | 2:CEX - CEDULA DE IDENTIDAD DE EXTRANJERO | 5:NIT - NÚMERO DE IDENTIFICACIÓN TRIBUTARIA
    private String numeroDocumento; //: "5153244"
    private String codigoCliente; //: "10101010"
    private int codigoMetodoPago; // 1:EFECTIVO | 3:CHEQUE | 6:PAGO POSTERIOR | 7:TRANSFERENCIA BANCARIA | 8:DEPOSITO EN CUENTA |
    private String usuario; // "milena"
    private int codigoDocumentoSector; // 1:FACTURA COMPRA-VENTA (sector-document-types)
    private int codigoMoneda; // 1:BOLIVIANO
    private int tipoCambio; // 1,
    private BigDecimal montoTotal; // 500
    private BigDecimal montoTotalSujetoIva; // 500
    private BigDecimal montoTotalMoneda;

    private List<DetallePedidoPOJO> detalle;

    public String getNombreRazonSocial() {
        return nombreRazonSocial;
    }

    public void setNombreRazonSocial(String nombreRazonSocial) {
        this.nombreRazonSocial = nombreRazonSocial;
    }

    public int getCodigoTipoDocumentoIdentidad() {
        return codigoTipoDocumentoIdentidad;
    }

    public void setCodigoTipoDocumentoIdentidad(int codigoTipoDocumentoIdentidad) {
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

    public int getCodigoMetodoPago() {
        return codigoMetodoPago;
    }

    public void setCodigoMetodoPago(int codigoMetodoPago) {
        this.codigoMetodoPago = codigoMetodoPago;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public int getCodigoDocumentoSector() {
        return codigoDocumentoSector;
    }

    public void setCodigoDocumentoSector(int codigoDocumentoSector) {
        this.codigoDocumentoSector = codigoDocumentoSector;
    }

    public int getCodigoMoneda() {
        return codigoMoneda;
    }

    public void setCodigoMoneda(int codigoMoneda) {
        this.codigoMoneda = codigoMoneda;
    }

    public int getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(int tipoCambio) {
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
}
