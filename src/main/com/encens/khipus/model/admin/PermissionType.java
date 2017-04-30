package com.encens.khipus.model.admin;

/**
 * Enumeration for Permission types
 *
 * @author:
 */

public enum PermissionType {
    VIEW(1),
    CREATE(2),
    UPDATE(4),
    DELETE(8);

    private int value;

    private PermissionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
