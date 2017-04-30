package com.encens.khipus.model.contacts;

/**
 * Encens Team
 *
 * @author
 * @version : EntityType, 07-10-2009 11:42:22 AM
 */
public enum EntityType {
    PERSON("Entity.type.person"), ORGANIZATION("Entity.type.organization");
    private String resourceKey;

    EntityType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
