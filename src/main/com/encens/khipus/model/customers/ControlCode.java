package com.encens.khipus.model.customers;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 30/12/14
 * Time: 9:20
 * To change this template use File | Settings | File Templates.
 */
public class ControlCode {
    private String nitEmpresa;
    private Integer numberInvoice;
    private String numeroAutorizacion;
    private Date fechaEmision;
    private Double total;
    private Double importeBaseCreditFisical;
    private String codigoControl;
    private String nitCliente;
    private String importeICE;
    private String importeVentasGrabadas;
    private String importeSujetoCreditoFisical;
    private String descuentosBonificaciones;
    private String keyQR;

    public ControlCode(String nitEmpresa,
                       Integer numberInvoice,
                       String numeroAutorizacion,
                       Date fechaEmision,
                       Double total,
                       Double importeBaseCreditFisical,
                       String codigoControl,
                       String nitCliente) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            this.fechaEmision = dateFormat.parse(dateFormat.format(fechaEmision));
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.nitEmpresa = nitEmpresa;
        this.numberInvoice = numberInvoice;
        this.numeroAutorizacion = numeroAutorizacion;
        this.total = total;
        this.importeBaseCreditFisical = importeBaseCreditFisical;
        this.codigoControl = codigoControl;
        this.nitCliente = nitCliente;
        this.importeICE = "0.00";
        this.importeVentasGrabadas = "0.00";
        this.importeSujetoCreditoFisical = "0.00";
        this.descuentosBonificaciones = "0.00";
    }

    public ControlCode(String nitEmpresa,
                       Integer numberInvoice,
                       String numeroAutorizacion,
                       Date fechaEmision,
                       Double total,
                       Double importeBaseCreditFisical,
                       String nitCliente) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            this.fechaEmision = dateFormat.parse(dateFormat.format(fechaEmision));
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.nitEmpresa = nitEmpresa;
        this.numberInvoice = numberInvoice;
        this.numeroAutorizacion = numeroAutorizacion;
        this.total = total;
        this.importeBaseCreditFisical = importeBaseCreditFisical;
        this.nitCliente = nitCliente;
        this.importeICE = "0.00";
        this.importeVentasGrabadas = "0.00";
        this.importeSujetoCreditoFisical = "0.00";
        this.descuentosBonificaciones = "0.00";
    }

    public ControlCode(String nitEmpresa,
                       Integer numberInvoice,
                       String numeroAutorizacion,
                       Date fechaEmision,
                       Double total,
                       Double importeBaseCreditFisical,
                       String codigoControl,
                       String nitCliente,
                       String importeICE,
                       String importeVentasGrabadas,
                       String importeSujetoCreditoFisical,
                       String descuentosBonificaciones) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            this.fechaEmision = dateFormat.parse(dateFormat.format(fechaEmision));
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.nitEmpresa = nitEmpresa;
        this.numberInvoice = numberInvoice;
        this.numeroAutorizacion = numeroAutorizacion;
        this.total = total;
        this.importeBaseCreditFisical = importeBaseCreditFisical;
        this.codigoControl = codigoControl;
        this.nitCliente = nitCliente;
        this.importeICE = importeICE;
        this.importeVentasGrabadas = importeVentasGrabadas;
        this.importeSujetoCreditoFisical = importeSujetoCreditoFisical;
        this.descuentosBonificaciones = descuentosBonificaciones;
    }

    public ControlCode(String nitEmpresa,
                       Integer numberInvoice,
                       String numeroAutorizacion,
                       Date fechaEmision,
                       Double total,
                       Double importeBaseCreditFisical,
                       String nitCliente,
                       String importeICE,
                       String importeVentasGrabadas,
                       String importeSujetoCreditoFisical,
                       String descuentosBonificaciones) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            this.fechaEmision = dateFormat.parse(dateFormat.format(fechaEmision));
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        this.nitEmpresa = nitEmpresa;
        this.numberInvoice = numberInvoice;
        this.numeroAutorizacion = numeroAutorizacion;
        this.total = total;
        this.importeBaseCreditFisical = importeBaseCreditFisical;
        this.nitCliente = nitCliente;
        this.importeICE = importeICE;
        this.importeVentasGrabadas = importeVentasGrabadas;
        this.importeSujetoCreditoFisical = importeSujetoCreditoFisical;
        this.descuentosBonificaciones = descuentosBonificaciones;
    }

    public void generarCodigoQR()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy");
        String date = sdf.format(fechaEmision);
        DecimalFormat format = new DecimalFormat("#.00");
        this.keyQR =  nitEmpresa +"|"+
                numberInvoice +"|"+
                numeroAutorizacion +"|"+
                date +"|"+
                format.format(total) +"|"+
                format.format(importeBaseCreditFisical) +"|"+
                codigoControl +"|"+
                nitCliente +"|"+
                importeICE +"|"+
                importeVentasGrabadas +"|"+
                importeSujetoCreditoFisical +"|"+
                descuentosBonificaciones;
    }

    public String getNitEmpresa() {
        return nitEmpresa;
    }

    public void setNitEmpresa(String nitEmpresa) {
        this.nitEmpresa = nitEmpresa;
    }

    public Integer getNumberInvoice() {
        return numberInvoice;
    }

    public void setNumberInvoice(Integer numberInvoice) {
        this.numberInvoice = numberInvoice;
    }

    public String getNumeroAutorizacion() {
        return numeroAutorizacion;
    }

    public void setNumeroAutorizacion(String numeroAutorizacion) {
        this.numeroAutorizacion = numeroAutorizacion;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Double getImporteBaseCreditFisical() {
        return importeBaseCreditFisical;
    }

    public void setImporteBaseCreditFisical(Double importeBaseCreditFisical) {
        this.importeBaseCreditFisical = importeBaseCreditFisical;
    }

    public String getCodigoControl() {
        return codigoControl;
    }

    public void setCodigoControl(String codigoControl) {
        this.codigoControl = codigoControl;
    }

    public String getNitCliente() {
        return nitCliente;
    }

    public void setNitCliente(String nitCliente) {
        this.nitCliente = nitCliente;
    }

    public String getImporteICE() {
        return importeICE;
    }

    public void setImporteICE(String importeICE) {
        this.importeICE = importeICE;
    }

    public String getImporteVentasGrabadas() {
        return importeVentasGrabadas;
    }

    public void setImporteVentasGrabadas(String importeVentasGrabadas) {
        this.importeVentasGrabadas = importeVentasGrabadas;
    }

    public String getImporteSujetoCreditoFisical() {
        return importeSujetoCreditoFisical;
    }

    public void setImporteSujetoCreditoFisical(String importeSujetoCreditoFisical) {
        this.importeSujetoCreditoFisical = importeSujetoCreditoFisical;
    }

    public String getDescuentosBonificaciones() {
        return descuentosBonificaciones;
    }

    public void setDescuentosBonificaciones(String descuentosBonificaciones) {
        this.descuentosBonificaciones = descuentosBonificaciones;
    }

    public String getKeyQR() {
        return keyQR;
    }

    public void setKeyQR(String keyQR) {
        this.keyQR = keyQR;
    }
}
