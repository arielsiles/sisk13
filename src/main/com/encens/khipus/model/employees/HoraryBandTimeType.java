package com.encens.khipus.model.employees;

/**
 * HoraryBandTimeType
 *
 * @author
 * @version 1.1.0
 */
public enum HoraryBandTimeType {
    HTE("HoraryBandTimeType.hte"),
    HCL("HoraryBandTimeType.hcl"),
    HPR("HoraryBandTimeType.hrp"),
    HR("HoraryBandTimeType.hr"),
    HLB("HoraryBandTimeType.hlb"),
    HCP("HoraryBandTimeType.hcp"),
    ANP("HoraryBandTimeType.anp"),
    THE("HoraryBandTimeType.the");
    private String resourceKey;

    HoraryBandTimeType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

}
