package com.encens.khipus.util;

import org.osbosoftware.facturas.CodigoControl7;

import java.util.Date;

public class SFC {

    private String authorizationNumber;
    private Long invoiceNumber;
    private String clientNit;
    private String name;
    private Date date;
    private Double amount;
    private String key;

    public SFC(String authorizationNumber, Long invoiceNumber, String clientNit, String name, Date date, Double amount, String key) {
        this.authorizationNumber = authorizationNumber;
        this.invoiceNumber = invoiceNumber;
        this.clientNit = clientNit;
        this.name = name;
        this.date = date;
        this.amount = amount;
        this.key = key;
    }

    public String generateControlCode(){
        CodigoControl7 cc7 = new CodigoControl7();
        cc7.setNumeroAutorizacion(this.authorizationNumber);
        cc7.setNumeroFactura(this.invoiceNumber);
        cc7.setNitci(this.clientNit);
        cc7.setFechaTransaccion(this.date);
        cc7.setMonto(this.amount.intValue());
        cc7.setLlaveDosificacion(this.key);
        return cc7.obtener();
    }

    public String getClientNit() {
        return clientNit;
    }

    public void setClientNit(String clientNit) {
        this.clientNit = clientNit;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
