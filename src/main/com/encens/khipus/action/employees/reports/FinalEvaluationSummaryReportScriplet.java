package com.encens.khipus.action.employees.reports;

import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.PollForm;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.query.EntityQueryFactory;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import org.jboss.seam.Component;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: FinalEvaluationSummaryReportScriplet.java  29-jun-2010 22:37:03$
 */
public class FinalEvaluationSummaryReportScriplet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(FinalEvaluationSummaryReportScriplet.class);

    private PollFormService pollFormService = (PollFormService) Component.getInstance("pollFormService");

    public void beforeDetailEval() throws JRScriptletException {
        log.debug("Process detail.................");
        super.beforeDetailEval();
        Long personId = getFieldAsLong("person.id");
        PollForm studentPollForm = (PollForm) getParameterValue("studentPollFormParam");
        PollForm careerManagerPollForm = (PollForm) getParameterValue("careerManagerPollFormParam");
        PollForm autoevaluationPollForm = (PollForm) getParameterValue("autoevaluationPollFormParam");
        PollForm teacherPollForm = (PollForm) getParameterValue("teacherPollFormParam");
        Cycle cycle = (Cycle) getParameterValue("cycleParam");

        //todo: this is temporal.. optimize to process only one at a time
        processTeacherEvaluation(studentPollForm, careerManagerPollForm, autoevaluationPollForm, personId);
        processCareerManagerEvaluation(teacherPollForm, personId);
    }

    /**
     * Calculate and set values to teacher evaluation
     *
     * @param studentPollForm
     * @param careerManagerPollForm
     * @param autoevaluationPollForm
     * @param personId
     * @throws JRScriptletException
     */
    private void processTeacherEvaluation(PollForm studentPollForm, PollForm careerManagerPollForm, PollForm autoevaluationPollForm, Long personId) throws JRScriptletException {
        BigDecimal studentEvalValue = calculateEquivalentFormEvaluationValue(studentPollForm, personId);
        BigDecimal careerManagerEvalValue = calculateEquivalentFormEvaluationValue(careerManagerPollForm, personId);
        BigDecimal autoEvalValue = calculateEquivalentFormEvaluationValue(autoevaluationPollForm, personId);
        BigDecimal finalEvalPunctuation = BigDecimalUtil.sum(studentEvalValue, careerManagerEvalValue, autoEvalValue);

        this.setVariableValue("studentEvalVar", studentEvalValue);
        this.setVariableValue("careerManagerEvalVar", careerManagerEvalValue);
        this.setVariableValue("autoEvalVar", autoEvalValue);
        this.setVariableValue("finalTeacherEvalVar", finalEvalPunctuation);
    }

    /**
     * CAlculate an set values to career manager evaluation
     *
     * @param teacherPollForm
     * @param personId
     * @throws JRScriptletException
     */
    private void processCareerManagerEvaluation(PollForm teacherPollForm, Long personId) throws JRScriptletException {
        BigDecimal teacherEvalValue = calculateEquivalentFormEvaluationValue(teacherPollForm, personId);

        this.setVariableValue("teacherEvalVar", teacherEvalValue);
        this.setVariableValue("finalCareerManagerEvalVar", teacherEvalValue);
    }

    /**
     * Calculate the equivalent value of all poll copy of this poll form
     *
     * @param pollForm
     * @param personId
     * @return BigDecimal
     */
    private BigDecimal calculateEquivalentFormEvaluationValue(PollForm pollForm, Long personId) {
        log.debug("calculateEquivalentFormEvaluationValue.....");

        String ejbql =
                "SELECT DISTINCT " +
                        "pollForm.id," +
                        "section.id," +
                        "section.sequence" +
                        " FROM PollCopy pollCopy" +
                        " LEFT JOIN pollCopy.pollForm pollForm" +
                        " LEFT JOIN pollCopy.person person" +
                        " LEFT JOIN pollCopy.pollForm.sectionList section" +
                        " WHERE person.id=" + personId + " AND pollForm.id=" + pollForm.getId();

        String[] restrictions = new String[]{
                "pollCopy.faculty.location=#{finalEvaluationSummaryReportAction.location}",
                "pollCopy.faculty=#{finalEvaluationSummaryReportAction.faculty}",
                "pollCopy.career=#{finalEvaluationSummaryReportAction.career}"};
        String orderBy = "section.sequence";

        //execute the Query
        List resultList = EntityQueryFactory.createQuery(ejbql, restrictions, orderBy).getResultList();

        java.util.List<BigDecimal> evaluationList = new ArrayList<BigDecimal>();
        for (int i = 0; i < resultList.size(); i++) {
            Object[] rowResult = (Object[]) resultList.get(i);
            Long sectionId = (Long) rowResult[1];

            BigDecimal equivalentEvalValue = TeacherEvaluationReportScriptlet.calculateEquivalentEvaluationValue(personId, pollForm.getId(), sectionId, pollFormService, pollForm.getEquivalentPercent());
            evaluationList.add(equivalentEvalValue);
        }
        BigDecimal evalValue = TeacherEvaluationReportScriptlet.calculateTotalEvaluation(evaluationList);
        return evalValue != null ? evalValue : BigDecimal.ZERO;
    }

    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }
}

