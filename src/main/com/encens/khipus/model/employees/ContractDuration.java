package com.encens.khipus.model.employees;

/**
 * Si el contrato tiene un fin acordado o no.
 * <p/>
 * Es un eje distinto de la modalidad: hay eventuales indefinidos y eventuales a plazo fijo, y la
 * misma persona pasa de uno a otro sin cambiar de modalidad. Por eso no cuelga de
 * `ModalidadContrato`, que fue el primer diseno y estaba mal.
 * <p/>
 * Existe para que la fecha de fin deje de ser un vencimiento administrativo que hay que renovar
 * a mano todos los anos, y vuelva a ser lo que debe: el registro de cuando termino la relacion.
 *
 * @author
 * @version 6.1.0
 */
public enum ContractDuration {

    /** Sin fin acordado. La fecha de fin queda vacia hasta que haya una baja. */
    INDEFINITE("ContractDuration.indefinite"),

    /** Con fin acordado. La fecha de fin es obligatoria y forma parte del acuerdo. */
    FIXED_TERM("ContractDuration.fixedTerm");

    private String resourceKey;

    ContractDuration(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
