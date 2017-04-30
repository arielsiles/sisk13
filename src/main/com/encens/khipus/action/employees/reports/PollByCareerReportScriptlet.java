package com.encens.khipus.action.employees.reports;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import java.util.List;

/**
 * Encens S.R.L.
 *
 * @author
 */
public class PollByCareerReportScriptlet extends JRDefaultScriptlet {

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        List<Integer> professorsIdList = (List<Integer>) this.getVariableValue("professorsIdList");
        Integer professorsId = getFieldAsInteger("person.id");
        if (!professorsIdList.contains(professorsId)) {
            professorsIdList.add(professorsId);
        }

        this.setVariableValue("professorsCountVar", professorsIdList.size());
    }

    private Integer getFieldAsInteger(String fieldName) throws JRScriptletException {
        Integer integerValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            integerValue = new Integer(fieldObj.toString());
        }
        return integerValue;
    }

}
