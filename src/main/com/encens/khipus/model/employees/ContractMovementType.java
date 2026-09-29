package com.encens.khipus.model.employees;

/**
 * Que le paso al contrato.
 *
 * @author
 * @version 6.1.0
 */
public enum ContractMovementType {

    /** Cambio de modalidad, de duracion o de la fecha de fin acordada. */
    CONDITION_CHANGE("ContractMovementType.conditionChange"),

    /** La persona se fue. Escribe la fecha de fin y la de salida. */
    TERMINATION("ContractMovementType.termination");

    private String resourceKey;

    ContractMovementType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
