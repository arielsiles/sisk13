package com.encens.khipus.action.employees.reports;

import com.encens.khipus.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Util to calculate values for crosstab in work contribution summary
 * by cost center report. Values be saved in Map with composed keys
 * @author
 * @version 3.4
 */
public class WorkContributionReportUtil {
    private static String SEPARATOR_KEY = "_";

    private static String CNS_KEY = "CNS";
    private static String AFP_KEY = "AFP";
    private static String PATRONAL_PROFESSIONALRISK_KEY = "PROFESSIONALRISK";
    private static String PATRONAL_PROHOME_KEY = "PROHOME";
    private static String PATRONAL_SOLIDARY_KEY = "SOLIDARY";

    private static String EMPLOYEE_TOTALGRAINED_KEY = "EMPLTOTALGRAINED";
    private static String AFPORGANIZATION_TOTAL_KEY = "ORGTOTAL";
    private static String TOTAL_KEY = "TOTAL";

    public static String composeCNSKey(String costCenterCode) {
        return CNS_KEY + SEPARATOR_KEY + costCenterCode;
    }

    public static String composeCNSTotalKey() {
        return CNS_KEY;
    }

    public static String composeAFPKey(String costCenterCode, Object organizationId) {
        return AFP_KEY + SEPARATOR_KEY + costCenterCode + SEPARATOR_KEY + organizationId;
    }

    public static String composeAFPTotalKey(Object organizationId) {
        return AFP_KEY + SEPARATOR_KEY + organizationId;
    }

    public static String composeProfessionalRiskKey(String costCenterCode, Object organizationId) {
        return PATRONAL_PROFESSIONALRISK_KEY + SEPARATOR_KEY + costCenterCode + SEPARATOR_KEY + organizationId;
    }

    public static String composeProfessionalRiskTotalKey(Object organizationId) {
        return PATRONAL_PROFESSIONALRISK_KEY + SEPARATOR_KEY + organizationId;
    }

    public static String composeProHomeKey(String costCenterCode, Object organizationId) {
        return PATRONAL_PROHOME_KEY + SEPARATOR_KEY + costCenterCode + SEPARATOR_KEY + organizationId;
    }

    public static String composeProHomeTotalKey(Object organizationId) {
        return PATRONAL_PROHOME_KEY + SEPARATOR_KEY + organizationId;
    }

    public static String composeSolidaryKey(String costCenterCode, Object organizationId) {
        return PATRONAL_SOLIDARY_KEY + SEPARATOR_KEY + costCenterCode + SEPARATOR_KEY + organizationId;
    }

    public static String composeSolidaryTotalKey(Object organizationId) {
        return PATRONAL_SOLIDARY_KEY + SEPARATOR_KEY + organizationId;
    }

    public static String composeOrganizationSubTotalKey(String costCenterCode, Object organizationId) {
        return AFPORGANIZATION_TOTAL_KEY + SEPARATOR_KEY + costCenterCode + SEPARATOR_KEY + organizationId;
    }

    public static String composeOrganizationSubTotalTotalKey(Object organizationId) {
        return AFPORGANIZATION_TOTAL_KEY + SEPARATOR_KEY + organizationId;
    }

    public static String composeTotalGrainedKey(String costCenterCode, Object organizationId) {
        return EMPLOYEE_TOTALGRAINED_KEY + SEPARATOR_KEY + costCenterCode + SEPARATOR_KEY + organizationId;
    }

    public static String composeTotalGrainedTotalKey(Object organizationId) {
        return EMPLOYEE_TOTALGRAINED_KEY + SEPARATOR_KEY + organizationId;
    }

    public static String composeTotalKey(String costCenterCode) {
        return TOTAL_KEY + SEPARATOR_KEY + costCenterCode;
    }

    public static String composeTotalKey() {
        return TOTAL_KEY;
    }

    /**
     * Process all required values, sum, totalize and save in valuesMap with your composed key.
     * @param valuesMap
     * @param costCenterCode
     * @param organizationId
     * @param totalGrained
     * @param cns
     * @param afp
     * @param professionalRisk
     * @param proHome
     * @param solidary
     * @return Map
     */
    public static Map<String, BigDecimal> totalizeValues(Map<String, BigDecimal> valuesMap, String costCenterCode, Object organizationId,
                                                         BigDecimal totalGrained, BigDecimal cns, BigDecimal afp, BigDecimal professionalRisk, BigDecimal proHome, BigDecimal solidary) {

        addValue(composeCNSKey(costCenterCode), cns, valuesMap);
        addValue(composeCNSTotalKey(), cns, valuesMap);

        addValue(composeTotalGrainedKey(costCenterCode, organizationId), totalGrained, valuesMap);
        addValue(composeTotalGrainedTotalKey(organizationId), totalGrained, valuesMap);

        addValue(composeAFPKey(costCenterCode, organizationId), afp, valuesMap);
        addValue(composeAFPTotalKey(organizationId), afp, valuesMap);

        addValue(composeProfessionalRiskKey(costCenterCode, organizationId), professionalRisk, valuesMap);
        addValue(composeProfessionalRiskTotalKey(organizationId), professionalRisk, valuesMap);

        addValue(composeProHomeKey(costCenterCode, organizationId), proHome, valuesMap);
        addValue(composeProHomeTotalKey(organizationId), proHome, valuesMap);

        addValue(composeSolidaryKey(costCenterCode, organizationId), solidary, valuesMap);
        addValue(composeSolidaryTotalKey(organizationId), solidary, valuesMap);

        BigDecimal organizationSubTotal = BigDecimalUtil.sum(afp, professionalRisk, proHome, solidary);
        addValue(composeOrganizationSubTotalKey(costCenterCode, organizationId), organizationSubTotal, valuesMap);
        addValue(composeOrganizationSubTotalTotalKey(organizationId), organizationSubTotal, valuesMap);

        BigDecimal total = BigDecimalUtil.sum(organizationSubTotal, cns);
        addValue(composeTotalKey(costCenterCode), total, valuesMap);
        addValue(composeTotalKey(), total, valuesMap);

        return valuesMap;
    }

    private static void addValue(String key, BigDecimal value, Map<String, BigDecimal> valuesMap) {

        BigDecimal totalValue = BigDecimal.ZERO;
        if (valuesMap.containsKey(key)) {
            totalValue = valuesMap.get(key);
        }

        if (value != null) {
            totalValue = BigDecimalUtil.sum(totalValue, value);
        }

        valuesMap.put(key, totalValue);
    }

    public static BigDecimal getValue(String key, Map<String, BigDecimal> valuesMap) {
        BigDecimal value = BigDecimal.ZERO;
        if (valuesMap.containsKey(key)) {
            value = valuesMap.get(key);
        }
        return value;
    }
}
