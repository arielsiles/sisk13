package com.encens.khipus.model.employees;

/**
 * Conceptos de aporte que un {@link SIPContributionRegime} puede eximir.
 * <p/>
 * El helper {@link #contributes(SIPContributionRegime, SIPContributionConcept)} centraliza la
 * regla de "sin regimen se aporta todo", para que un catalogo vacio o un contrato sin regimen
 * asignado calculen exactamente igual que antes de existir esta configuracion.
 *
 * @author
 * @version 6.0.129
 */
public enum SIPContributionConcept {

    INDIVIDUAL_ACCOUNT("SIPContributionRegime.contributesIndividualAccount"),
    COMMON_RISK("SIPContributionRegime.contributesCommonRisk"),
    SOLIDARY("SIPContributionRegime.contributesSolidary"),
    COMISSION("SIPContributionRegime.contributesComission"),
    NATIONAL_SOLIDARY("SIPContributionRegime.contributesNationalSolidary"),
    PATRONAL("SIPContributionRegime.contributesPatronal"),
    CNS("SIPContributionRegime.contributesCns");

    private String resourceKey;

    SIPContributionConcept(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    /**
     * @return true si el concepto se le cobra al trabajador bajo ese regimen. Un regimen nulo
     *         cobra todos los conceptos.
     */
    public static boolean contributes(SIPContributionRegime regime, SIPContributionConcept concept) {
        if (null == regime) {
            return true;
        }
        Boolean value;
        switch (concept) {
            case INDIVIDUAL_ACCOUNT:
                value = regime.getContributesIndividualAccount();
                break;
            case COMMON_RISK:
                value = regime.getContributesCommonRisk();
                break;
            case SOLIDARY:
                value = regime.getContributesSolidary();
                break;
            case COMISSION:
                value = regime.getContributesComission();
                break;
            case NATIONAL_SOLIDARY:
                value = regime.getContributesNationalSolidary();
                break;
            case PATRONAL:
                value = regime.getContributesPatronal();
                break;
            case CNS:
                value = regime.getContributesCns();
                break;
            default:
                value = Boolean.TRUE;
        }
        /* una bandera nula se interpreta como "si aporta", nunca como exencion silenciosa */
        return null == value || value;
    }
}
