package com.encens.khipus.action.employees.reports;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Scriptlet to management work contribution by cost center report
 * @author
 * @version 3.4
 */
public class WorkContributionByCostCenterReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(this.getClass());

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        String costCenterCode = (String) this.getFieldValue("costCenter.code");
        Long pensionFundOrganizationId = getFieldAsLong("pensionFundOrganization.id");
        BigDecimal totalGrained  = getFieldAsBigDecimal("fiscalPayroll.totalGrained");
        BigDecimal cns  = getFieldAsBigDecimal("tributaryPayroll.cns");
        BigDecimal afp = getFieldAsBigDecimal("fiscalPayroll.retentionAFP");
        BigDecimal professionalRisk = getFieldAsBigDecimal("tributaryPayroll.patronalProffesionalRiskRetentionAFP");
        BigDecimal proHome = getFieldAsBigDecimal("tributaryPayroll.patronalProHomeRetentionAFP");
        BigDecimal solidary = getFieldAsBigDecimal("tributaryPayroll.patronalSolidaryRetentionAFP");

        Map valuesMap = (Map) this.getVariableValue("valuesMapVar");
        valuesMap = WorkContributionReportUtil.totalizeValues(valuesMap, costCenterCode, pensionFundOrganizationId, totalGrained, cns, afp, professionalRisk, proHome, solidary);

        this.setVariableValue("valuesMapVar", valuesMap);
    }

    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }

    private BigDecimal getFieldAsBigDecimal(String fieldName) throws JRScriptletException {
        BigDecimal bigDecimalValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            bigDecimalValue = new BigDecimal(fieldObj.toString());
        }
        return bigDecimalValue;
    }
}
