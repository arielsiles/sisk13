package com.encens.khipus.model.contacts;

/**
 * Encens S.R.L.
 * Template type enum
 * @author
 * @version $Id: TemplateType.java  24-feb-2010 14:54:13$
 */
public enum TemplateType {
    CONTRACT("TemplateType.contract");

    private String resourceKey;

    TemplateType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
