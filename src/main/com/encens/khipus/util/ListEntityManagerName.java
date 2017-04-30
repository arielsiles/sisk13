package com.encens.khipus.util;

/**
 * @author
 * @version 2.22
 */
public enum ListEntityManagerName {
    DEFAULT_LIST("listEntityManager"),
    BUSINESS_UNIT_LIST("businessUnitListEntityManager");

    private String name;

    ListEntityManagerName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
