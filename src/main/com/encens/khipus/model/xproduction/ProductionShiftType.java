package com.encens.khipus.model.xproduction;

public enum ProductionShiftType {

    NIGHT("N", "ProductionShift.nightShift"),
    DAY("D", "ProductionShift.dayShift");

    private String type;
    private String resourceKey;

    ProductionShiftType(String type, String resourceKey) {
        this.setType(type);
        this.setResourceKey(resourceKey);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
