package com.encens.khipus.service.customers;

import com.encens.khipus.model.customers.Client;

import java.math.BigDecimal;

/**
* Created by Diego on 26/06/2014.
*/
public class OrderClient
{
    private String name;

    private int posX;
    private int posY;
    private String idOrder;
    private String type;
    private BigDecimal idDistributor;
    private String state;
    private Client cliente;
    private Boolean stockFlag;

    public String getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(String idOrder) {
        this.idOrder = idOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getIdDistributor() {
        return idDistributor;
    }

    public void setIdDistributor(BigDecimal idDistributor) {
        this.idDistributor = idDistributor;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Client getCliente() {
        return cliente;
    }

    public void setCliente(Client cliente) {
        this.cliente = cliente;
    }

    public Boolean getStockFlag() {
        return stockFlag;
    }

    public void setStockFlag(Boolean stockFlag) {
        this.stockFlag = stockFlag;
    }
}
