package com.encens.khipus.action.employees.reports;

import com.encens.khipus.model.employees.PollFormGrouppingType;
import com.encens.khipus.reports.ReportFormatter;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.BigDecimalUtil;
import com.encens.khipus.util.MathUtils;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.renderers.JCommonDrawableRenderer;
import org.jboss.seam.Component;
import org.jboss.seam.international.Messages;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Encens S.R.L.
 *
 * @author
 * @version $Id: TeacherEvaluationReportScriptlet.java  11-dic-2009 19:34:35$
 */
public class TeacherEvaluationReportScriptlet extends JRDefaultScriptlet {

    private PollFormService pollFormService = (PollFormService) Component.getInstance("pollFormService");


    @Override
    public void afterGroupInit(String s) throws JRScriptletException {
        super.beforeGroupInit(s);
        String personGroupName = "teacherGroup";

        if (s.equals(personGroupName)) {
            Long personId = getFieldAsLong("person.id");
            Long pollFormId = getFieldAsLong("pollForm.id");
            PollFormGrouppingType pollFormGrouppingType = (PollFormGrouppingType) getFieldValue("pollForm.pollFormGrouppingType");

            //System.out.println("PROCESS GROUPPP: " + personId + " " + pollFormId + " " + pollFormGrouppingType);

            String careerVariableName = "careerVar";
            String subjectVariableName = "subjectVar";
            String copyTotalVariableName = "copyTotalVar";

            this.setVariableValue(careerVariableName, calculateCareerTotal(personId, pollFormId, pollFormGrouppingType));
            this.setVariableValue(subjectVariableName, calculateSubjectTotal(personId, pollFormId));
            this.setVariableValue(copyTotalVariableName, calculateCopyTotal(personId, pollFormId));
        }
    }

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();
        Locale locale = (Locale) getParameterValue("REPORT_LOCALE");
        Long personId = getFieldAsLong("person.id");
        Long pollFormId = getFieldAsLong("pollForm.id");
        Long sectionId = getFieldAsLong("section.id");
        Integer equivalentPercent = getFieldAsInteger("pollForm.equivalentPercent");

        //System.out.println("PROCESSSSSS: " + personId + " " + pollFormId + " " + sectionId + " " + equivalentPercent);

        String evaluationVariableName = "evaluationVar";
        String evaluationListVariableName = "evaluationListVar";

        BigDecimal evaluationValue = calculateEquivalentEvaluationValue(personId, pollFormId, sectionId, pollFormService, equivalentPercent);
        List evaluationList = (List) this.getVariableValue(evaluationListVariableName);
        evaluationList.add(evaluationValue);

        this.setVariableValue(evaluationListVariableName, evaluationList);
        this.setVariableValue(evaluationVariableName, evaluationValue);

        double restValue = equivalentPercent.doubleValue() - evaluationValue.doubleValue();
        DefaultPieDataset dataset = new DefaultPieDataset();
        ChartValue<String> chartValue1 = new ChartValue<String>(ReportFormatter.formatNumber(evaluationValue, 10, 2, locale));
        ChartValue<String> chartValue2 = new ChartValue<String>(ReportFormatter.formatNumber(restValue, 10, 2, locale));
        dataset.setValue(chartValue1, evaluationValue);
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
        Color[] colors = {Color.yellow, new Color(0, 88, 38)};
        ReportFormatter.setChartColors(plot, dataset, colors);
        plot.setNoDataMessage(Messages.instance().get("Reports.chart.noData"));
        plot.setSimpleLabels(true);
        plot.setLabelFont(new Font("Verdana", Font.PLAIN, 9));
        plot.setBackgroundAlpha(0);
        this.setVariableValue("chartVar", new JCommonDrawableRenderer(chart));
    }

    private Integer getFieldAsInteger(String fieldName) throws JRScriptletException {
        Integer value = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            value = new Integer(fieldObj.toString());
        }
        return value;
    }

    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }


    private Long calculateCareerTotal(Long personId, Long pollFormId, PollFormGrouppingType grouppingType) {
        Long totalValue = pollFormService.calculateCareerTotal(personId, pollFormId, grouppingType);
        if (totalValue != null && totalValue == 0) {
            totalValue = null;
        }
        return totalValue;
    }

    private Long calculateSubjectTotal(Long personId, Long pollFormId) {
        Long totalValue = pollFormService.calculateSubjectTotal(personId, pollFormId);
        if (totalValue != null && totalValue == 0) {
            totalValue = null;
        }
        return totalValue;
    }

    private Long calculateCopyTotal(Long personId, Long pollFormId) {
        Long totalValue = pollFormService.calculateCopyTotal(personId, pollFormId);
        if (totalValue != null && totalValue == 0) {
            totalValue = null;
        }
        return totalValue;
    }

    public static BigDecimal calculateEquivalentEvaluationValue(Long personId, Long pollFormId, Long sectionId, PollFormService pollFormService, Integer equivalentPercent) {
        return BigDecimalUtil.roundBigDecimal(new BigDecimal(MathUtils.calculeRespectiveToEquivalentPercent(calculateEvaluationValue(personId, pollFormId, sectionId, pollFormService).doubleValue(), equivalentPercent)));
    }

    private static BigDecimal calculateEvaluationValue(Long personId, Long pollFormId, Long sectionId, PollFormService pollFormService) {
        BigDecimal evalValue = pollFormService.calculateEvaluationValue(personId, pollFormId, sectionId);
        return evalValue != null ? BigDecimalUtil.roundBigDecimal(evalValue) : BigDecimal.ZERO;
    }

    public static BigDecimal calculateTotalEvaluation(List<BigDecimal> evaluationList) {
        BigDecimal total = null;
        if (evaluationList != null && !evaluationList.isEmpty()) {
            BigDecimal partialValue = BigDecimal.ZERO;
            for (BigDecimal evaluationValue : evaluationList) {
                partialValue = partialValue.add(evaluationValue);
            }
            total = BigDecimalUtil.divide(partialValue, BigDecimal.valueOf(evaluationList.size()));
        }
        return total;
    }

    public static BigDecimal calculateEquivalentValue(List<BigDecimal> evaluationList, Integer equivalentPercent) {
        BigDecimal equivalentValue = null;

        BigDecimal total = calculateTotalEvaluation(evaluationList);
        if (total != null && equivalentPercent != null) {
            equivalentValue = BigDecimalUtil.divide(total.multiply(BigDecimal.valueOf(equivalentPercent)), BigDecimal.valueOf(100));
        }
        return equivalentValue;
    }
}

class ChartValue<T> implements Comparable<T> {
    private T value;

    ChartValue(T value) {
        this.value = value;
    }

    public int compareTo(Object o) {
        return (1);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}