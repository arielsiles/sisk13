package com.encens.khipus.model.production;

public enum ProductionCollectionState {
    PENDING(1,"RawMaterialCollectionSession.state.pending"),
    APPROVED(2,"RawMaterialCollectionSession.state.approved");
    private int code;
    private String resourceKey;

    ProductionCollectionState(int code,String resourceKey){
        this.resourceKey = resourceKey;
        this.code = code;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
