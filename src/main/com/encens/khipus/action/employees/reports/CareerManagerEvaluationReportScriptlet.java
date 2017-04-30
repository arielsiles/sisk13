package com.encens.khipus.action.employees.reports;

import com.encens.khipus.service.employees.PollFormService;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;

/**
 * Encens S.R.L.
 * Scriptlet to render career manager evaluation report
 *
 * @author
 * @version $Id: CareerManagerEvaluationReportScriptlet.java  17-dic-2009 16:11:23$
 */
public class CareerManagerEvaluationReportScriptlet extends JRDefaultScriptlet {

    private PollFormService pollFormService = (PollFormService) Component.getInstance("pollFormService");

    @Override
    public void afterGroupInit(String s) throws JRScriptletException {
        super.beforeGroupInit(s);
        String managerGroupName = "managerGroup";
        String criteriaGroupName = "criteriaGroup";

        if (s.equals(managerGroupName)) {
            Long personId = getFieldAsLong("person.id");
            Long pollFormId = getFieldAsLong("pollForm.id");
            Long facultyId = getFieldAsLong("faculty.id");
            Long careerId = getFieldAsLong("career.id");

//            System.out.println("PROCESS GROUPPP: " + personId + " " + pollFormId);

            String copyTotalVariableName = "copyTotalVar";
            this.setVariableValue(copyTotalVariableName, calculatePollCopyByFacultyCareer(personId, pollFormId, facultyId, careerId));
        }

        if (s.equals(criteriaGroupName)) {
            Long questionId = getFieldAsLong("question.id");
            this.setVariableValue("questionContentVar", readQuestionContent(questionId));
        }
    }

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
    }

    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }

    private String readQuestionContent(Long questionId) {
        String questionContent = null;
        if (questionId != null) {
            questionContent = pollFormService.readQuestionContent(questionId);
        }
        return questionContent;
    }

    private Long calculatePollCopyByFacultyCareer(Long personId, Long pollFormId, Long facultyId, Long careerId) {
        Long totalValue = pollFormService.calculatePollCopyByFacultyCareer(personId, pollFormId, facultyId, careerId);
        if (totalValue != null && totalValue == 0) {
            totalValue = null;
        }
        return totalValue;
    }
}
