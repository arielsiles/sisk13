package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.employees.GenericPollReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.jatun.titus.reportgenerator.util.TypedReportData;
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
 *
 * @author
 * @version $Id: TeacherEvaluationReportAction.java  10-dic-2009 18:11:38$
 */
@Name("teacherEvaluationReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('TEACHEREVALUATIONREPORT','VIEW')}")
public class TeacherEvaluationReportAction extends GenericPollReportAction {


    public void generateReport() {
        log.debug("Generating TeacherEvaluationReportAction.............................");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("SHOW_TOTAL_STUDENTS", Boolean.TRUE);
        TypedReportData pollByCareerReportData = generateCallByCareerSubReport();
        params.putAll(pollByCareerReportData.getReportParams());
        params.put("pollByCareerSubReport", pollByCareerReportData.getJasperReport());

        log.debug("Generating pollByPersonHeaderSubReport.................................... ");

        TypedReportData pollByPersonReportData = generateCallByPersonSubReport();

        params.putAll(pollByPersonReportData.getReportParams());
        params.put("pollByPersonHeaderSubReport", pollByPersonReportData.getJasperReport());

        //add report params
        params.putAll(readReportParamsInfo());

        super.generateReport("teacherEvaluationReport", "/employees/reports/teacherEvaluationReport.jrxml", PageFormat.LETTER, PageOrientation.PORTRAIT, composePollFormReportTitle(), params);
    }

    @Create
    public void init() {
        restrictions = new String[]{"pollCopy.company=#{currentCompany}",
                "pollCopy.pollForm=#{teacherEvaluationReportAction.pollForm}",
                "pollCopy.person.idNumber=#{teacherEvaluationReportAction.idNumber}",
                "pollCopy.revisionDate>=#{teacherEvaluationReportAction.initRevisionDate}",
                "pollCopy.revisionDate<=#{teacherEvaluationReportAction.endRevisionDate}",
                "pollCopy.faculty.location=#{teacherEvaluationReportAction.location}",
                "pollCopy.faculty=#{teacherEvaluationReportAction.faculty}",
                "pollCopy.career=#{teacherEvaluationReportAction.career}"};

        sortProperty = " pollCopy.person.lastName," +
                " pollCopy.person.maidenName," +
                " pollCopy.person.firstName," +
                " pollCopy.person.id," +
                " section.sequence";
    }

    @Override
    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "pollCopy.person.id," +
                "pollCopy.person.lastName," +
                "pollCopy.person.maidenName," +
                "pollCopy.person.firstName," +
                "pollCopy.pollForm.id," +
                "pollCopy.pollForm.subTitle," +
                "section.id," +
                "section.title," +
                "pollCopy.pollForm.pollFormGrouppingType," +
                "pollCopy.pollForm.equivalentPercent," +
                "section.sequence" +
                " FROM PollCopy pollCopy" +
                " LEFT JOIN pollCopy.pollForm.sectionList section" +
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

    private TypedReportData generateCallByCareerSubReport() {
        log.debug("Generating pollByCareerSubReport.................................... ");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("SHOW_TOTAL_STUDENTS", Boolean.TRUE);
        String pollByCareerSelect =
                "SELECT pollCopy.id," +
                        "pollCopy.person.id," +
                        "pollCopy.subject.id," +
                        "pollCopy.career.id," +
                        "pollCopy.faculty.id," +
                        "pollCopy.faculty.location.id," +
                        "pollCopy.person.firstName," +
                        "pollCopy.person.maidenName," +
                        "pollCopy.person.lastName," +
                        "pollCopy.person.idNumber," +
                        "pollCopy.subject.name," +
                        "pollCopy.subject.code," +
                        "pollCopy.career.name," +
                        "pollCopy.career.code," +
                        "pollCopy.faculty.name," +
                        "pollCopy.faculty.code," +
                        "pollCopy.faculty.location.name," +
                        "pollCopy.faculty.location.code" +
                        " FROM PollCopy pollCopy" +
                        " WHERE pollCopy.person.id=$P{personIdParam} AND" +
                        " pollCopy.faculty IS NOT NULL";
        String[] pollByCareerRestrictions = new String[]{
                "pollCopy.company=#{currentCompany}",
                "pollCopy.pollForm=#{teacherEvaluationReportAction.pollForm}",
                "pollCopy.revisionDate>=#{teacherEvaluationReportAction.initRevisionDate}",
                "pollCopy.revisionDate<=#{teacherEvaluationReportAction.endRevisionDate}",
                "pollCopy.faculty.location=#{teacherEvaluationReportAction.location}",
                "pollCopy.faculty=#{teacherEvaluationReportAction.faculty}",
                "pollCopy.career=#{teacherEvaluationReportAction.career}"
        };
        String pollByCareerOrder =
                "pollCopy.person.id," +
                        "pollCopy.subject.name," +
                        "pollCopy.subject.id";
        return (super.generateSubReport(
                "pollByCareerSubReport",
                "/employees/reports/pollByCareerSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport("pollByCareerSubReport", pollByCareerSelect, Arrays.asList(pollByCareerRestrictions), pollByCareerOrder),
                params));
    }

    private TypedReportData generateCallByPersonSubReport() {
        log.debug("Generating pollByPersonHeaderSubReport.................................... ");

        Map<String, Object> params = new HashMap<String, Object>();
        String pollByPersonSelect =
                "SELECT pollCopy.id," +
                        "pollCopy.person.id," +
                        "pollCopy.career.id," +
                        "pollCopy.faculty.id," +
                        "pollCopy.faculty.location.id," +
                        "pollCopy.person.firstName," +
                        "pollCopy.person.maidenName," +
                        "pollCopy.person.lastName," +
                        "pollCopy.person.idNumber," +
                        "extensionSite.extension," +
                        "pollCopy.career.name," +
                        "pollCopy.career.code," +
                        "pollCopy.faculty.name," +
                        "pollCopy.faculty.code," +
                        "pollCopy.faculty.location.name," +
                        "pollCopy.faculty.location.code" +
                        " FROM PollCopy pollCopy LEFT JOIN pollCopy.person.extensionSite extensionSite" +
                        " WHERE pollCopy.person.id=$P{personIdParam} AND" +
                        " pollCopy.faculty IS NOT NULL";

        String[] pollByPersonRestrictions = new String[]{
                "pollCopy.company=#{currentCompany}",
                "pollCopy.pollForm=#{teacherEvaluationReportAction.pollForm}",
                "pollCopy.revisionDate>=#{teacherEvaluationReportAction.initRevisionDate}",
                "pollCopy.revisionDate<=#{teacherEvaluationReportAction.endRevisionDate}",
                "pollCopy.faculty.location=#{teacherEvaluationReportAction.location}",
                "pollCopy.faculty=#{teacherEvaluationReportAction.faculty}",
                "pollCopy.career=#{teacherEvaluationReportAction.career}"
        };
        String pollByPersonOrder =
                "pollCopy.faculty.location.name," +
                        "pollCopy.faculty.location.id," +
                        "pollCopy.person.lastName," +
                        "pollCopy.person.maidenName," +
                        "pollCopy.person.firstName," +
                        "pollCopy.person.id," +
                        "pollCopy.faculty.name," +
                        "pollCopy.faculty.id," +
                        "pollCopy.career.name," +
                        "pollCopy.career.id";

        return (super.generateSubReport(
                "pollByPersonHeaderSubReport",
                "/employees/reports/pollByPersonHeaderSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport("pollByPersonHeaderSubReport", pollByPersonSelect, Arrays.asList(pollByPersonRestrictions), pollByPersonOrder),
                params));
    }
}
