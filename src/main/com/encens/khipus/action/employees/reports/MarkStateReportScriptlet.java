package com.encens.khipus.action.employees.reports;

import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author
 * @version 3.3
 */
public class MarkStateReportScriptlet extends JRDefaultScriptlet {

    @SuppressWarnings({"unchecked"})
    public void beforeDetailEval() throws JRScriptletException {
        Map horaryBandStateSemaphore = (Map) getVariableValue("horaryBandStateSemaphore");
        if (horaryBandStateSemaphore == null) {
            horaryBandStateSemaphore = new HashMap();
        }
        Object horaryBandStateId = getFieldValue("horaryBandState.id");
        Date marTime = (Date) getFieldValue("markState.marTime");

        Date planningHour;
        Date differenceHour = new Date(0, 0, 0, 0, 0, 0);
        String hourType;

        Boolean input = horaryBandStateSemaphore.put(horaryBandStateId, true) == null;
        if (input) {
            planningHour = (Date) getFieldValue("horaryBandState.initHour");
            hourType = MessageUtils.getMessage("Reports.markState.input");
        } else {
            planningHour = (Date) getFieldValue("horaryBandState.endHour");
            hourType = MessageUtils.getMessage("Reports.markState.output");
        }

        if (marTime != null) {
            long difference;
            Calendar calendar = DateUtils.toDateCalendar(new Date());
            if (marTime.compareTo(planningHour) < 0) {
                difference = planningHour.getTime() - marTime.getTime();
            } else {
                difference = marTime.getTime() - planningHour.getTime();
            }
            calendar.set(Calendar.MINUTE, (int) (((difference / 1000)) / 60));
            differenceHour = calendar.getTime();
        }
        setVariableValue("planningHour", planningHour);
        setVariableValue("differenceHour", differenceHour);
        setVariableValue("hourType", hourType);
        setVariableValue("horaryBandStateSemaphore", horaryBandStateSemaphore);
    }
}
