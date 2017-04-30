package com.encens.khipus.action.employees.reports;

import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.finances.OrganizationalUnit;
import com.encens.khipus.model.finances.Salary;
import com.encens.khipus.service.employees.AcademicPlanningSummaryService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.Constants;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.math.BigDecimal;

/**
 * Encens S.R.L.
 * Scriptlet to managements calculations to academic planning report
 *
 * @author
 * @version $Id: AcademicPlanningSummaryReportScriptlet.java  09-jul-2010 12:56:50$
 */
public class AcademicPlanningSummaryReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(AcademicPlanningSummaryReportScriptlet.class);
    private AcademicPlanningSummaryService academicPlanningSummaryService;

    public AcademicPlanningSummaryReportScriptlet() {
        academicPlanningSummaryService = (AcademicPlanningSummaryService) Component.getInstance("academicPlanningSummaryService");
    }

    public void beforeDetailEval() throws JRScriptletException {
        log.debug("Process detail.................");
        super.beforeDetailEval();

        Long employeeCode = getFieldAsLong("academicPlanning.employeeCode");
        Integer scheduleCharge = getFieldAsInteger("academicPlanning.scheduleCharge");

        OrganizationalUnit organizationalUnit = (OrganizationalUnit) getParameterValue("organizationalUnitParam");
        Cycle cycle = (Cycle) getParameterValue("cycleParam");
        BigDecimal susToBsExchangeRate = (BigDecimal) getParameterValue("susToBsExchangeParam");

        log.debug("Employee Code:" + employeeCode);
        Salary salary = academicPlanningSummaryService.findTeacherSalary(organizationalUnit, cycle, (employeeCode != null ? String.valueOf(employeeCode) : null));

        setTeacherSalary(salary, scheduleCharge, susToBsExchangeRate);
    }

    /**
     * Calculate and set teacher salary as Bs and Sus
     * @param salary
     * @param scheduleCharge
     * @param susToBsExchangeRate
     * @throws JRScriptletException
     */
    private void setTeacherSalary(Salary salary, Integer scheduleCharge, BigDecimal susToBsExchangeRate) throws JRScriptletException {
        BigDecimal bsSalary = null;
        BigDecimal susSalary = null;
        if (salary != null && scheduleCharge != null && salary.getCurrency() != null) {
            if (Constants.currencyIdBs.equals(salary.getCurrency().getId())) {
                bsSalary = BigDecimalUtil.multiply(salary.getAmount(), BigDecimal.valueOf(scheduleCharge));
                susSalary = BigDecimalUtil.divide(bsSalary, susToBsExchangeRate);
            } else if (Constants.currencyIdSus.equals(salary.getCurrency().getId())) {
                susSalary = BigDecimalUtil.multiply(salary.getAmount(), BigDecimal.valueOf(scheduleCharge));
                bsSalary = BigDecimalUtil.multiply(susSalary, susToBsExchangeRate);
            }
        }

        this.setVariableValue("salaryBsVar", bsSalary);
        this.setVariableValue("salarySusVar", susSalary);
    }

    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }

    private Integer getFieldAsInteger(String fieldName) throws JRScriptletException {
        Integer value = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            value = new Integer(fieldObj.toString());
        }
        return value;
    }
}

