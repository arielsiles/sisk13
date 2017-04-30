package com.encens.khipus.model.production;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 15/10/13
 * Time: 20:29
 * To change this template use File | Settings | File Templates.
 */
public enum StatePayRoll {
    PENDING("PENDING", "PENDING"),
    APPROVED("APPROVED", "APPROVED");
    //EXECUTED("EXECUTED", "EXECUTED");
    private String value;
    private String state;

    private StatePayRoll(String state, String value) {
        this.value = value;
        this.state = state;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
