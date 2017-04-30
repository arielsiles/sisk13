package com.encens.khipus.service.customers;

import java.math.BigDecimal;

/**
* Created by Diego on 01/07/2014.
*/
public class OrderItem
{
    private String nameItem;
    private String CodArt;
    private BigDecimal IdAccount;
    private String NoCia;
    private int posX;
    private int posY;
    private String type;

    public OrderItem(String type) {
        this.type = type;
    }

    public OrderItem() {
    }

    public String getNoCia() {
        return NoCia;
    }

    public void setNoCia(String noCia) {
        NoCia = noCia;
    }

    public String getNameItem() {
        return nameItem;
    }

    public void setNameItem(String nameItem) {
        this.nameItem = nameItem;
    }

    public String getCodArt() {
        return CodArt;
    }

    public void setCodArt(String codArt) {
        CodArt = codArt;
    }

    public BigDecimal getIdAccount() {
        return IdAccount;
    }

    public void setIdAccount(BigDecimal idAccount) {
        IdAccount = idAccount;
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
}
