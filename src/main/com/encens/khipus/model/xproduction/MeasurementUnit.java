package com.encens.khipus.model.xproduction;

/**
 * Encens Team
 *
 * @author
 * @version
 */
public enum MeasurementUnit {
    KG("MeasurementUnit.KG"),
    GR("MeasurementUnit.GR"),
    LT("MeasurementUnit.LT"),
    ML("MeasurementUnit.ML");

    private String resourceKey;

    MeasurementUnit(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
