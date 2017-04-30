package com.encens.khipus.model.contacts;

/**
 * Encens Team
 *
 * @author
 * @version : Gender, 26-11-2009 07:41:43 PM
 */
public enum Gender {
    MAN("Gender.manAcronym", "Gender.man"),
    WOMAN("Gender.womanAcronym", "Gender.woman");

    private String acronymResourceKey;
    private String resourceKey;

    Gender(String acronymResourceKey, String resourceKey) {
        this.acronymResourceKey = acronymResourceKey;
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getAcronymResourceKey() {
        return acronymResourceKey;
    }

    public void setAcronymResourceKey(String acronymResourceKey) {
        this.acronymResourceKey = acronymResourceKey;
    }
}
