package com.encens.khipus.action.employees.reports;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.PollForm;
import com.encens.khipus.model.employees.TeacherEvaluation;
import com.encens.khipus.reports.ReportFormatter;
import com.encens.khipus.service.academics.TeacherEvaluationService;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.query.EntityQueryFactory;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.renderers.JCommonDrawableRenderer;
import org.jboss.seam.Component;
import org.jboss.seam.international.Messages;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Encens S.R.L.
 * Scriptlet to teacher final evaluation report
 *
 * @author
 * @version $Id: FinalTeacherEvaluationReportScriptlet.java  17-jun-2010 18:01:40$
 */
public class FinalTeacherEvaluationReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(FinalTeacherEvaluationReportScriptlet.class);

    private PollFormService pollFormService = (PollFormService) Component.getInstance("pollFormService");
    private TeacherEvaluationService teacherEvaluationService = (TeacherEvaluationService) Component.getInstance("teacherEvaluationService");
    private GenericService genericService = (GenericService) Component.getInstance("genericService");

    @Override
    public void afterGroupInit(String s) throws JRScriptletException {
        log.debug("Process teacher group....");
        super.beforeGroupInit(s);
        String personGroupName = "teacherGroup";

        if (s.equals(personGroupName)) {
            Long personId = getFieldAsLong("person.id");
            PollForm studentPollForm = (PollForm) getParameterValue("studentPollFormParam");
            PollForm careerManagerPollForm = (PollForm) getParameterValue("careerManagerPollFormParam");
            PollForm autoevaluationPollForm = (PollForm) getParameterValue("autoevaluationPollFormParam");
            Cycle cycle = (Cycle) getParameterValue("cycleParam");

            TeacherEvaluation teacherEvaluation = findTeacherEvaluationViewData(personId, cycle);


            setStudentEvaluationResult(studentPollForm, personId, teacherEvaluation);
            setCareerManagerEvaluationResult(careerManagerPollForm, personId, teacherEvaluation);
            setAutoEvaluationResult(autoevaluationPollForm, personId);

            setPorcentualEvaluationResult(studentPollForm, careerManagerPollForm, autoevaluationPollForm, personId);
        }
    }

    private void setStudentEvaluationResult(PollForm studentPollForm, Long personId, TeacherEvaluation teacherEvaluation) throws JRScriptletException {
        Locale locale = (Locale) getParameterValue("REPORT_LOCALE");

        Integer totalQuantity = academicSystemStudentCount(teacherEvaluation);
        Integer criteriaEvaluationQuantity = calculateCriteriaEvaluationQuantity(totalQuantity, studentPollForm);
        Integer realPollQuantity = calculateRealPollCopy(studentPollForm, personId);
        Integer deficiency = criteriaEvaluationQuantity - realPollQuantity;
        BigDecimal percentDeficiency = calculatePercentDeficiency(criteriaEvaluationQuantity, realPollQuantity);

        this.setVariableValue("studentAcademicSystemVar", totalQuantity);
        this.setVariableValue("studentEvalCriteriaVar", criteriaEvaluationQuantity);
        this.setVariableValue("studentPollVar", realPollQuantity);
        this.setVariableValue("studentDeficiencyVar", deficiency);
        this.setVariableValue("studentPercentDeficiencyVar", ReportFormatter.formatNumber(percentDeficiency, 10, 2, locale) + "%");
    }

    private void setCareerManagerEvaluationResult(PollForm careerManagerPollForm, Long personId, TeacherEvaluation teacherEvaluation) throws JRScriptletException {
        Locale locale = (Locale) getParameterValue("REPORT_LOCALE");

        Integer totalQuantity = academicSystemCareerManagerCount(teacherEvaluation);
        Integer criteriaEvaluationQuantity = calculateCriteriaEvaluationQuantity(totalQuantity, careerManagerPollForm);
        Integer realPollQuantity = calculateRealPollCopy(careerManagerPollForm, personId);
        Integer deficiency = criteriaEvaluationQuantity - realPollQuantity;
        BigDecimal percentDeficiency = calculatePercentDeficiency(criteriaEvaluationQuantity, realPollQuantity);

        this.setVariableValue("careerManagerAcademicSystemVar", totalQuantity);
        this.setVariableValue("careerManagerEvalCriteriaVar", criteriaEvaluationQuantity);
        this.setVariableValue("careerManagerPollVar", realPollQuantity);
        this.setVariableValue("careerManagerDeficiencyVar", deficiency);
        this.setVariableValue("careerManagerPercentDeficiencyVar", ReportFormatter.formatNumber(percentDeficiency, 10, 2, locale) + "%");
    }

    private void setAutoEvaluationResult(PollForm autoevaluationPollForm, Long personId) throws JRScriptletException {
        Locale locale = (Locale) getParameterValue("REPORT_LOCALE");

        Integer totalQuantity = calculateRealPollCopy(autoevaluationPollForm, personId);
        Integer criteriaEvaluationQuantity = calculateCriteriaEvaluationQuantity(totalQuantity, autoevaluationPollForm);
        Integer deficiency = criteriaEvaluationQuantity - totalQuantity;
        BigDecimal percentDeficiency = calculatePercentDeficiency(criteriaEvaluationQuantity, totalQuantity);

        this.setVariableValue("autoevaluationAcademicSystemVar", totalQuantity);
        this.setVariableValue("autoevaluationEvalCriteriaVar", criteriaEvaluationQuantity);
        this.setVariableValue("autoevaluationPollVar", totalQuantity);
        this.setVariableValue("autoevaluationDeficiencyVar", deficiency);
        this.setVariableValue("autoevaluationPercentDeficiencyVar", ReportFormatter.formatNumber(percentDeficiency, 10, 2, locale) + "%");
    }

    private void setPorcentualEvaluationResult(PollForm studentPollForm, PollForm careerManagerPollForm, PollForm autoevaluationPollForm, Long personId) throws JRScriptletException {
        Locale locale = (Locale) getParameterValue("REPORT_LOCALE");

        BigDecimal studentEvalValue = calculateEquivalentFormEvaluationValue(studentPollForm, personId);
        BigDecimal careerManagerEvalValue = calculateEquivalentFormEvaluationValue(careerManagerPollForm, personId);
        BigDecimal autoEvalValue = calculateEquivalentFormEvaluationValue(autoevaluationPollForm, personId);
        BigDecimal finalEvalPunctuation = BigDecimalUtil.sum(studentEvalValue, careerManagerEvalValue, autoEvalValue);

        this.setVariableValue("studentEvalVar", composeFormEvaluationValue(studentEvalValue, studentPollForm, locale));
        this.setVariableValue("careerManagerEvalVar", composeFormEvaluationValue(careerManagerEvalValue, careerManagerPollForm, locale));
        this.setVariableValue("autoEvalVar", composeFormEvaluationValue(autoEvalValue, autoevaluationPollForm, locale));
        this.setVariableValue("finalEvalVar", ReportFormatter.formatNumber(finalEvalPunctuation, 10, 2, locale));

        setChartFinalEval(finalEvalPunctuation, locale);
    }

    private void setChartFinalEval(BigDecimal finalEvalPunctuation, Locale locale) throws JRScriptletException {
        BigDecimal restValue = BigDecimalUtil.subtract(BigDecimal.valueOf(100), finalEvalPunctuation);

        DefaultPieDataset dataset = new DefaultPieDataset();
        ChartValue<String> chartValue1 = new ChartValue<String>(ReportFormatter.formatNumber(finalEvalPunctuation, 10, 2, locale) + "%");
        ChartValue<String> chartValue2 = new ChartValue<String>(ReportFormatter.formatNumber(restValue, 10, 2, locale) + "%");
        dataset.setValue(chartValue1, finalEvalPunctuation);
        dataset.setValue(chartValue2, restValue);

        JFreeChart chart =
                ChartFactory.createPieChart(
                        "",
                        dataset,
                        false,
                        false,
                        false
                );
        PiePlot plot = (PiePlot) chart.getPlot();
        Color[] colors = {new Color(36, 112, 204), new Color(109, 175, 65)};
        ReportFormatter.setChartColors(plot, dataset, colors);
        plot.setNoDataMessage(Messages.instance().get("Reports.chart.noData"));
        plot.setSimpleLabels(true);
        plot.setLabelFont(new Font("Verdana", Font.PLAIN, 9));
        plot.setBackgroundAlpha(0);

        this.setVariableValue("chartVar", new JCommonDrawableRenderer(chart));
    }

    private String composeFormEvaluationValue(BigDecimal evalValue, PollForm pollForm, Locale locale) {
        return ReportFormatter.formatNumber(evalValue, 10, 2, locale) + "/" + pollForm.getEquivalentPercent();
    }

    private Integer calculateCriteriaEvaluationQuantity(Integer totalQuantity, PollForm pollForm) {
        BigDecimal percentageValue = BigDecimalUtil.getPercentage(BigDecimal.valueOf(totalQuantity), BigDecimal.valueOf(pollForm.getSamplePercent()));
        return BigDecimalUtil.roundBigDecimal(percentageValue, 0).intValue();
    }

    private BigDecimal calculatePercentDeficiency(Integer criteriaEvaluationQuantity, Integer realPollQuantity) {
        BigDecimal percentDeficiency = BigDecimal.ZERO;
        if (criteriaEvaluationQuantity > 0) {
            percentDeficiency = BigDecimalUtil.divide(BigDecimal.valueOf(realPollQuantity).multiply(BigDecimalUtil.ONE_HUNDRED), BigDecimal.valueOf(criteriaEvaluationQuantity));
        }
        return percentDeficiency;
    }

    private Integer calculateRealPollCopy(PollForm pollForm, Long personId) {
        log.debug("Calculate real poll copy in form.....");

        String ejbql = "SELECT " +
                "count(pollCopy)" +
                " FROM PollCopy pollCopy" +
                " LEFT JOIN pollCopy.pollForm pollForm" +
                " LEFT JOIN pollCopy.person person" +
                " WHERE person.id=" + personId + " AND pollForm.id=" + pollForm.getId();

        String[] restrictions = new String[]{
                "pollCopy.faculty.location=#{finalTeacherEvaluationReportAction.location}",
                "pollCopy.faculty=#{finalTeacherEvaluationReportAction.faculty}",
                "pollCopy.career=#{finalTeacherEvaluationReportAction.career}"};

        //execute the Query
        Long countPollCopy = (Long) EntityQueryFactory.createQuery(ejbql, restrictions).getSingleResult();

        return countPollCopy != null ? countPollCopy.intValue() : 0;
    }

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
                "pollCopy.faculty.location=#{finalTeacherEvaluationReportAction.location}",
                "pollCopy.faculty=#{finalTeacherEvaluationReportAction.faculty}",
                "pollCopy.career=#{finalTeacherEvaluationReportAction.career}"};
        String orderBy = "section.sequence";

        //execute the Query
        List resultList = EntityQueryFactory.createQuery(ejbql, restrictions, orderBy).getResultList();

        List<BigDecimal> evaluationList = new ArrayList<BigDecimal>();
        for (Object aResultList : resultList) {
            Object[] rowResult = (Object[]) aResultList;
            Long sectionId = (Long) rowResult[1];

            BigDecimal equivalentEvalValue = TeacherEvaluationReportScriptlet.calculateEquivalentEvaluationValue(personId, pollForm.getId(), sectionId, pollFormService, pollForm.getEquivalentPercent());
            evaluationList.add(equivalentEvalValue);
        }
        BigDecimal evalValue = TeacherEvaluationReportScriptlet.calculateTotalEvaluation(evaluationList);
        return evalValue != null ? evalValue : BigDecimal.ZERO;
    }

    private TeacherEvaluation findTeacherEvaluationViewData(Long personId, Cycle cycle) {
        TeacherEvaluation teacherEvaluation = null;
        Employee employee = null;
        try {
            employee = genericService.findById(Employee.class, personId);
        } catch (EntryNotFoundException e) {
            log.debug("Not found employee..." + personId, e);
        }

        if (employee != null && employee.getEmployeeCode() != null && cycle != null) {
            Long employeeCode = Long.valueOf(employee.getEmployeeCode());
            Integer period = cycle.getCycleType().getPeriod();
            Integer gestion = cycle.getGestion().getYear();

            teacherEvaluation = teacherEvaluationService.getTeacherEvaluation(employeeCode, period, gestion);
        }

        return teacherEvaluation;
    }

    private Integer academicSystemStudentCount(TeacherEvaluation teacherEvaluation) {
        log.debug("Teacher evaluation view..." + teacherEvaluation);
        Integer studentQuantity = 0;
        if (teacherEvaluation != null && teacherEvaluation.getNumberOfStudents() != null) {
            studentQuantity = teacherEvaluation.getNumberOfStudents();
        }

        return studentQuantity;
    }

    private Integer academicSystemCareerManagerCount(TeacherEvaluation teacherEvaluation) {
        log.debug("career teacher evaluation view..." + teacherEvaluation);
        Integer careerQuantity = 0;
        if (teacherEvaluation != null && teacherEvaluation.getNumberOfCareers() != null) {
            careerQuantity = teacherEvaluation.getNumberOfCareers();
        }
        return careerQuantity;
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

