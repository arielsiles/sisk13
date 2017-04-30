package com.encens.khipus.action.employees.reports;

import com.encens.khipus.reports.ReportFormatter;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.BigDecimalUtil;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRRenderable;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.renderers.JCommonDrawableRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Encens S.R.L.
 * scriptlet to render criteria evaluation sub report
 *
 * @author
 * @version $Id: CriteriaValueEvaluationSubReportScriptlet.java  22-feb-2010 14:42:10$
 */
public class CriteriaValueEvaluationSubReportScriptlet extends JRDefaultScriptlet {

    private PollFormService pollFormService;

    public void beforeDetailEval() throws JRScriptletException {
        super.beforeDetailEval();

        setPollFormService((PollFormService) getParameterValue("POLLFORMSERVICE_PARAM"));

        Long personId = (Long) getParameterValue("personIdParam");
        Long pollFormId = (Long) getParameterValue("pollFormIdParam");
        Long questionId = (Long) getParameterValue("questionIdParam");
        Long facultyId = (Long) getParameterValue("facultyIdParam");
        Long careerId = (Long) getParameterValue("careerIdParam");

        Long evaluationCriteriaValueId = getFieldAsLong("evaluationCriteriaValue.id");
        String criteriaName = (String) this.getFieldValue("evaluationCriteriaValue.title");

//        System.out.println("PROCESSSSSS: " + personId + " " + pollFormId + " " + questionId + " " + evaluationCriteriaValueId);

        Long evaluationValue = countAssertQuestionEvaluationCriteriaValueByPerson(personId, pollFormId, questionId, evaluationCriteriaValueId, facultyId, careerId);

        this.setVariableValue("criteriaEvaluationVar", evaluationValue);

        //add in list variables to render pie image
        ((List) this.getVariableValue("criteriaEvaluationListVar")).add(evaluationValue);
        ((List) this.getVariableValue("criteriaValueTitleListVar")).add(criteriaName);
    }

    private Long getFieldAsLong(String fieldName) throws JRScriptletException {
        Long longValue = null;
        Object fieldObj = this.getFieldValue(fieldName);
        if (fieldObj != null && fieldObj.toString().length() > 0) {
            longValue = new Long(fieldObj.toString());
        }
        return longValue;
    }

    private Long countAssertQuestionEvaluationCriteriaValueByPerson(Long personId, Long pollFormId, Long questionId, Long evaluationCriteriaValueId, Long facultyId, Long careerId) {
        Long countAssertQuestionCriteria = pollFormService.countAssertQuestionEvaluationCriteriaValueByPerson(personId, pollFormId, questionId, evaluationCriteriaValueId, facultyId, careerId);
        if (countAssertQuestionCriteria == null) {
            countAssertQuestionCriteria = Long.valueOf(0);
        }
        return countAssertQuestionCriteria;
    }

    public static Long calculateTotalPollEvaluated(List<Long> evaluationList) {
        Long total = null;
        if (evaluationList != null && !evaluationList.isEmpty()) {
            total = Long.valueOf(0);
            for (Long evaluationValue : evaluationList) {
                total = total + evaluationValue;
            }
        }
        return total;
    }

    public static JRRenderable renderPieImage(List<Long> evaluationList, List<String> criteriaNameList) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        Long total = calculateTotalPollEvaluated(evaluationList);
        if (total > 0) {
            for (int i = 0; i < evaluationList.size(); i++) {
                String criteriaName = criteriaNameList.get(i);
                Long evaluationValue = evaluationList.get(i);

                BigDecimal percentValue = BigDecimalUtil.roundBigDecimal(BigDecimal.valueOf((evaluationValue * 100) / total));
                try {
                    dataset.setValue(criteriaName + " " + percentValue + "%", percentValue);
                }
                catch (Exception ex) {
                    System.out.println("ERROR EN GRAAFICO......... " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        JFreeChart chart =
                ChartFactory.createPieChart(
                        "",
                        dataset,
                        false,
                        false,
                        false
                );
        PiePlot plot = (PiePlot) chart.getPlot();
        Color[] colors = {new Color(67, 149, 192), new Color(197, 79, 39), new Color(126, 174, 62), new Color(133, 85, 151), new Color(27, 196, 209)};
        ReportFormatter.setChartColors(plot, dataset, colors);
        //plot.setNoDataMessage("not data");
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 9));
        //plot.setIgnoreZeroValues(true);
        plot.setIgnoreNullValues(true);
        plot.setBackgroundAlpha(0);

        return new JCommonDrawableRenderer(chart);
    }

    public PollFormService getPollFormService() {
        return pollFormService;
    }

    public void setPollFormService(PollFormService pollFormService) {
        this.pollFormService = pollFormService;
    }
}
