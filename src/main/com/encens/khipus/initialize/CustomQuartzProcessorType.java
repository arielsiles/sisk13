package com.encens.khipus.initialize;

/**
 * Enum for CustomQuartzProcessorType
 *
 * @author
 * @version 3.5
 */
public enum CustomQuartzProcessorType {
    INTERVAL_DURATION("CustomQuartzProcessorType.INTERVAL_DURATION"),
    INTERVAL_CRON("CustomQuartzProcessorType.INTERVAL_CRON");

    private String resourceKey;

    CustomQuartzProcessorType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
