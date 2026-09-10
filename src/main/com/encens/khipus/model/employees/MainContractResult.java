package com.encens.khipus.model.employees;

/**
 * Que quedo con la marca de contrato principal despues de guardar un contrato.
 * <p/>
 * Existe para que la pantalla pueda decirlo. La marca decide quien tiene AFP, vacaciones y
 * antiguedad, asi que cambiarla en silencio es exactamente lo que no puede pasar.
 *
 * @author
 * @version 6.1.0
 */
public enum MainContractResult {

    /** Este contrato quedo como principal y antes no habia ninguno. */
    DESIGNATED("MainContract.designated"),

    /** Este contrato quedo como principal y le saco la marca a otro. */
    MOVED("MainContract.moved"),

    /** Quedo como secundario porque la persona ya tiene otro principal. */
    SECONDARY("MainContract.secondary"),

    /**
     * La persona tiene contratos abiertos y NINGUNO es principal. Es un estado valido -pasa
     * cuando solo le quedan eventuales corriendo- pero tiene que verse: sin principal no hay
     * AFP ni vacaciones, y eso no puede ocurrir por olvido.
     */
    WITHOUT_MAIN("MainContract.withoutMain");

    private String resourceKey;

    MainContractResult(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
