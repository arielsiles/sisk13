package com.encens.khipus.model.employees;

/**
 * Motivo de una fecha especial.
 * <p/>
 * Hasta la 6.1.0 una fecha especial solo decia si era con goce de haber y a quien aplicaba,
 * asi que no habia forma de reportar por causa ni de distinguir una baja por maternidad de
 * una vacacion. El motivo es lo que permite responder por que a alguien no se le descuenta
 * un dia, y lo que necesita la maternidad para no consumir vacaciones.
 *
 * @author
 * @version 6.1.0
 */
public enum SpecialDateReason {

    HOLIDAY("SpecialDateReason.holiday"),
    PERSONAL_LEAVE("SpecialDateReason.personalLeave"),
    LATENESS_JUSTIFICATION("SpecialDateReason.latenessJustification"),
    MATERNITY_LEAVE("SpecialDateReason.maternityLeave"),
    COMPENSATORY("SpecialDateReason.compensatory"),
    REST("SpecialDateReason.rest"),
    VACATION("SpecialDateReason.vacation");

    private String resourceKey;

    SpecialDateReason(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
