package com.encens.khipus.model.rest;

public class CancelBillPOJO {

    private Integer cancellationReasonCode;
    private String cuf;

    public Integer getCancellationReasonCode() {
        return cancellationReasonCode;
    }

    public void setCancellationReasonCode(Integer cancellationReasonCode) {
        this.cancellationReasonCode = cancellationReasonCode;
    }

    public String getCuf() {
        return cuf;
    }

    public void setCuf(String cuf) {
        this.cuf = cuf;
    }
}
