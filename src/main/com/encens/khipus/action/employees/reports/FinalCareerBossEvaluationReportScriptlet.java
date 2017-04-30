package com.encens.khipus.action.employees.reports;

import com.encens.khipus.exception.EntryNotFoundException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Career;
import com.encens.khipus.model.employees.Cycle;
import com.encens.khipus.model.employees.PollForm;
import com.encens.khipus.model.employees.TeachersForCareer;
import com.encens.khipus.reports.ReportFormatter;
import com.encens.khipus.service.academics.TeachersForCareerService;
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
 *
 * @author
 * @version $Id: FinalCareerBossEvaluationReportScriptlet.java  24-jun-2010 18:23:38$
 */
public class FinalCareerBossEvaluationReportScriptlet extends JRDefaultScriptlet {
    private Log log = Logging.getLog(FinalCareerBossEvaluationReportScriptlet.class);

    private PollFormService pollFormService = (PollFormService) Component.getInstance("pollFormService");
    private GenericService genericService = (GenericService) Component.getInstance("genericService");
    private TeachersForCareerService teachersForCareerService = (TeachersForCareerService) Component.getInstance("teachersForCareerService");

    @Override
    public void afterGroupInit(String s) throws JRScriptletException {
        log.debug("Process group....");
        super.beforeGroupInit(s);
        String personGroupName = "teacherGroup";

        if (s.equals(personGroupName)) {
            Long personId = getFieldAsLong("person.id");
            PollForm teacherPollForm = (PollForm) getParameterValue("teacherPollFormParam");
            Cycle cycle = (Cycle) getParameterValue("cycleParam");

            setNumeralEvaluationResult(teacherPollForm, personId, cycle);
            setPorcentualEvaluationResult(teacherPollForm, personId);
        }
    }

    private void setNumeralEvaluationResult(PollForm teacherPollForm, Long personId, Cycle cycle) throws JRScriptletException {
        Locale locale = (Locale) getParameterValue("REPORT_LOCALE");

        Integer totalQuantity = academicSystemTeacherCount(teacherPollForm, personId, cycle);
        Integer criteriaEvaluationQuantity = calculateCriteriaEvaluationQuantity(totalQuantity, teacherPollForm);
        Integer realPollQuantity = calculateRealPollCopy(teacherPollForm, personId);
        Integer deficiency = criteriaEvaluationQuantity - realPollQuantity;
        BigDecimal percentDeficiency = calculatePercentDeficiency(criteriaEvaluationQuantity, realPollQuantity);

        this.setVariableValue("teacherAcademicSystemVar", totalQuantity);
        this.setVariableValue("teacherEvalCriteriaVar", criteriaEvaluationQuantity);
        this.setVariableValue("teacherPollVar", realPollQuantity);
        this.setVariableValue("teacherDeficiencyVar", deficiency);
        this.setVariableValue("teacherPercentDeficiencyVar", ReportFormatter.formatNumber(percentDeficiency, 10, 2, locale) + "%");
    }

    private void setPorcentualEvaluationResult(PollForm teacherPollForm, Long personId) throws JRScriptletException {
        Locale locale = (Locale) getParameterValue("REPORT_LOCALE");

        BigDecimal teacherEvalValue = calculateEquivalentFormEvaluationValue(teacherPollForm, personId);

        this.setVariableValue("teacherEvalVar", composeFormEvaluationValue(teacherEvalValue, teacherPollForm, locale));
        this.setVariableValue("finalEvalVar", ReportFormatter.formatNumber(teacherEvalValue, 10, 2, locale));

        setChartFinalEval(teacherEvalValue, locale);
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
                "pollCopy.faculty.location=#{careerBossEvaluationReportAction.location}",
                "pollCopy.faculty=#{careerBossEvaluationReportAction.faculty}",
                "pollCopy.career=#{careerBossEvaluationReportAction.career}"};

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
                        " LEFT JOIN pollForm.sectionList section" +
                        " WHERE person.id=" + personId + " AND pollForm.id=" + pollForm.getId();

        String[] restrictions = new String[]{
                "pollCopy.faculty.location=#{careerBossEvaluationReportAction.location}",
                "pollCopy.faculty=#{careerBossEvaluationReportAction.faculty}",
                "pollCopy.career=#{careerBossEvaluationReportAction.career}"};
        String orderBy = "section.sequence";

        //execute the Query
        List resultList = EntityQueryFactory.createQuery(ejbql, restrictions, orderBy).getResultList();

        java.util.List<BigDecimal> evaluationList = new ArrayList<BigDecimal>();
        for (Object aResultList : resultList) {
            Object[] rowResult = (Object[]) aResultList;
            Long sectionId = (Long) rowResult[1];

            BigDecimal equivalentEvalValue = TeacherEvaluationReportScriptlet.calculateEquivalentEvaluationValue(personId, pollForm.getId(), sectionId, pollFormService, pollForm.getEquivalentPercent());
            evaluationList.add(equivalentEvalValue);
        }
        BigDecimal evalValue = TeacherEvaluationReportScriptlet.calculateTotalEvaluation(evaluationList);
        return evalValue != null ? evalValue : BigDecimal.ZERO;
    }

    private Career findPollCopyCareer(PollForm pollForm, Long personId) {
        log.debug("findPollCopyCareer.......");
        Career career = null;

        String ejbql =
                "SELECT " +
                        "career.id" +
                        " FROM PollCopy pollCopy" +
                        " LEFT JOIN pollCopy.pollForm pollForm" +
                        " LEFT JOIN pollCopy.person person" +
                        " LEFT JOIN pollCopy.career career" +
                        " WHERE person.id=" + personId + " AND pollForm.id=" + pollForm.getId();

        String[] restrictions = new String[]{
                "pollCopy.faculty.location=#{careerBossEvaluationReportAction.location}",
                "pollCopy.faculty=#{careerBossEvaluationReportAction.faculty}",
                "career=#{careerBossEvaluationReportAction.career}"};

        //execute the Query
        List resultList = EntityQueryFactory.createQuery(ejbql, restrictions).getResultList();
        Long careerId = null;
        if (!resultList.isEmpty()) {
            careerId = (Long) resultList.get(0);
        }

        if (careerId != null) {
            try {
                career = genericService.findById(Career.class, careerId);
            } catch (EntryNotFoundException e) {
                log.debug("Not found career..." + careerId, e);
            }
        }
        return career;
    }

    private Integer academicSystemTeacherCount(PollForm pollForm, Long personId, Cycle cycle) {
        log.debug("Teacher acadenic system evaluation view...");
        Integer teacherQuantity = 0;
        TeachersForCareer teachersForCareer = findTeachersForCareerViewData(pollForm, personId, cycle);
        if (teachersForCareer != null) {
            teacherQuantity = teachersForCareer.getNumberOfTeachers();
        }
        return teacherQuantity;
    }

    private TeachersForCareer findTeachersForCareerViewData(PollForm pollForm, Long personId, Cycle cycle) {
        TeachersForCareer teachersForCareer = null;

        Career career = findPollCopyCareer(pollForm, personId);
        if (career != null && career.getCode() != null && cycle != null) {
            Integer locationId = career.getFaculty().getLocation().getId().intValue();
            String studyPlan = career.getCode();
            Integer period = cycle.getCycleType().getPeriod();
            Integer gestion = cycle.getGestion().getYear();

            teachersForCareer = teachersForCareerService.getCareer(locationId, studyPlan, period, gestion);
        }

        return teachersForCareer;
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

