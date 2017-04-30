package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.employees.GenericPollReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.PollFormGrouppingType;
import com.encens.khipus.service.employees.PollFormService;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to manage career manager evaluation report
 *
 * @author
 * @version $Id: CareerManagerEvaluationReportAction.java  16-dic-2009 11:23:42$
 */
@Name("careerManagerEvaluationReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('CAREERMANAGEREVALUATIONREPORT','VIEW')}")
public class CareerManagerEvaluationReportAction extends GenericPollReportAction {

    private PollFormGrouppingType pollFormGrouppingType = PollFormGrouppingType.CAREER;

    public PollFormGrouppingType getPollFormGrouppingType() {
        return pollFormGrouppingType;
    }

    public void setPollFormGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        this.pollFormGrouppingType = pollFormGrouppingType;
    }

    public void generateReport() {
        log.debug("Generating CareerManagerEvaluationReportAction.......................");

        Map params = new HashMap();
        //add report params
        params.putAll(readReportParamsInfo());

        //add criteria evaluation sub report
        addCriteriaValueEvaluationSubReport("CRITERIAVALUEEVALUATIONSUBREPORT", params);

        super.generateReport("managerEvalReport", "/employees/reports/CareerManagerEvaluationReport.jrxml", PageFormat.LETTER, PageOrientation.PORTRAIT, composePollFormReportTitle(), params);
    }

    @Create
    public void init() {
        restrictions = new String[]{"pollCopy.company=#{currentCompany}",
                "pollCopy.pollForm=#{careerManagerEvaluationReportAction.pollForm}",
                "pollCopy.person.idNumber=#{careerManagerEvaluationReportAction.idNumber}",
                "pollCopy.revisionDate>=#{careerManagerEvaluationReportAction.initRevisionDate}",
                "pollCopy.revisionDate<=#{careerManagerEvaluationReportAction.endRevisionDate}",
                "pollCopy.faculty.location=#{careerManagerEvaluationReportAction.location}",
                "pollCopy.faculty=#{careerManagerEvaluationReportAction.faculty}",
                "pollCopy.career=#{careerManagerEvaluationReportAction.career}"};

        sortProperty = " pollCopy.person.lastName," +
                " pollCopy.person.maidenName," +
                " pollCopy.person.firstName," +
                " pollCopy.person.id," +
                " pollCopy.faculty.name," +
                " pollCopy.faculty.id," +
                " pollCopy.career.name," +
                " pollCopy.career.id," +
                " question.evaluationCriteria.id," +
                " question.id," +
                " question.sequence";
    }

    @Override
    protected String getEjbql() {

        return "SELECT DISTINCT " +
                "pollCopy.person.id," +
                "pollCopy.person.lastName," +
                "pollCopy.person.maidenName," +
                "pollCopy.person.firstName," +
                "pollCopy.faculty.name," +
                "pollCopy.career.name," +
                "pollCopy.pollForm.id," +
                "pollCopy.pollForm.subTitle," +
                "section.id," +
                "question.id," +
                "question.evaluationCriteria.id," +
                "pollCopy.faculty.id," +
                "pollCopy.career.id," +
                "question.sequence" +
                " FROM PollCopy pollCopy" +
                " LEFT JOIN pollCopy.pollForm.sectionList section" +
                " LEFT JOIN section.questionList question" +
                " WHERE pollCopy.person IS NOT NULL";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map readReportParamsInfo() {
        Map paramMap = new HashMap();
        paramMap.put("titleParam", composePollFormReportTitle());
        paramMap.put("headerInfoParam", composePollFormHeaderInfo());
        return paramMap;
    }

    /**
     * Add evaluation sub report in main report
     *
     * @param subReportKey     key of sub report
     * @param mainReportParams main report params
     */
    private void addCriteriaValueEvaluationSubReport(String subReportKey, Map mainReportParams) {
        log.debug("Generating addCriteriaValueEvaluationSubReport.............................");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("POLLFORMSERVICE_PARAM", (PollFormService) Component.getInstance("pollFormService"));


        String ejbql = "SELECT " +
                "evaluationCriteriaValue.id," +
                "evaluationCriteriaValue.sequence," +
                "evaluationCriteriaValue.title" +
                " FROM EvaluationCriteriaValue evaluationCriteriaValue" +
                " WHERE evaluationCriteriaValue.evaluationCriteria.id = $P{evaluationCriteriaIdParam}";

        String[] restrictions = new String[]{};
        String orderBy = "evaluationCriteriaValue.sequence";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/employees/reports/criteriaValueEvaluationSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

}
