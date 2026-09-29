package com.encens.khipus.model.employees;

/**
 * Como pertenece una persona a un grupo.
 *
 * @author
 * @version 6.1.0
 */
public enum WorkGroupMembershipType {

    /** Su grupo de siempre. Sin fecha de fin mientras siga ahi. */
    BASE("WorkGroupMembershipType.base"),

    /**
     * Prestada a otro grupo por unos dias, tipicamente por una emergencia de produccion.
     * <p/>
     * Pisa a la pertenencia base sin borrarla, y <b>exige fecha de fin</b>: un prestamo sin
     * fecha de fin no es un prestamo, es un cambio de grupo. Vencido el plazo la persona vuelve
     * a su grupo sola, sin que nadie tenga que acordarse de devolverla.
     */
    LOAN("WorkGroupMembershipType.loan");

    private String resourceKey;

    WorkGroupMembershipType(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
