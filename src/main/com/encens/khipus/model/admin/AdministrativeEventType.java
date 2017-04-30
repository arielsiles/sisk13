package com.encens.khipus.model.admin;

/**
 * Enumeration for Administrative Event types
 *
 * @author
 * @version 2.18
 */
public enum AdministrativeEventType {
    ADMINISTRATIVE_NOTIFICATION("AdministrativeEventType.administrativeNotification");

    private String resourceKey;

    AdministrativeEventType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
