package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.employees.GenericPollReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.PollFormGrouppingType;
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
 * This class implements the report logic for the PollByPerson report
 *
 * @author
 */

@Name("pollByPersonReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('POLLBYPERSONREPORT','VIEW')}")
public class PollByPersonReportAction extends GenericPollReportAction {

    private PollFormGrouppingType pollFormGrouppingType = PollFormGrouppingType.SUBJECT;

    public PollFormGrouppingType getPollFormGrouppingType() {
        return pollFormGrouppingType;
    }

    public void setPollFormGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        this.pollFormGrouppingType = pollFormGrouppingType;
    }

    @Create
    public void init() {
        restrictions =
                new String[]{"pollCopy.company=#{currentCompany}",
                        "pollCopy.pollForm=#{pollByPersonReportAction.pollForm}",
                        "pollCopy.person.idNumber=#{pollByPersonReportAction.idNumber}",
                        "pollCopy.revisionDate>=#{pollByPersonReportAction.initRevisionDate}",
                        "pollCopy.revisionDate<=#{pollByPersonReportAction.endRevisionDate}",
                        "pollCopy.faculty.location=#{pollByPersonReportAction.location}",
                        "pollCopy.faculty=#{pollByPersonReportAction.faculty}"};
        sortProperty =
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
    }

    @Override
    protected String getEjbql() {
        return "SELECT pollCopy.id," +
                "pollCopy.person.id," +
                "pollCopy.career.id," +
                "pollCopy.faculty.id," +
                "pollCopy.faculty.location.id," +
                "pollCopy.pollForm.id," +
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
                " FROM PollCopy pollCopy LEFT JOIN pollCopy.person.extensionSite extensionSite";
    }


    public void generateReport() {
        Map params = new HashMap();
        params.put("SHOW_TOTAL_STUDENTS", Boolean.FALSE);

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
                        " FROM PollCopy pollCopy " +
                        " WHERE pollCopy.person.id=$P{personIdParam} AND" +
                        " pollCopy.faculty IS NOT NULL";
        String[] pollByCareerRestrictions = new String[]{"pollCopy.company=#{currentCompany}",
                "pollCopy.pollForm=#{pollByPersonReportAction.pollForm}",
                "pollCopy.person.idNumber=#{pollByPersonReportAction.idNumber}",
                "pollCopy.revisionDate>=#{pollByPersonReportAction.initRevisionDate}",
                "pollCopy.revisionDate<=#{pollByPersonReportAction.endRevisionDate}",
                "pollCopy.faculty.location=#{pollByPersonReportAction.location}",
                "pollCopy.faculty=#{pollByPersonReportAction.faculty}"};
        String pollByCareerOrder =
                "pollCopy.person.id," +
                        "pollCopy.subject.name," +
                        "pollCopy.subject.id";


        TypedReportData reportData = super.generateSubReport(
                "pollByCareerSubReport",
                "/employees/reports/pollByCareerSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport("pollByCareerSubReport", pollByCareerSelect, Arrays.asList(pollByCareerRestrictions), pollByCareerOrder),
                params);

        log.debug("The subreport was generated...... " + reportData.getJasperReport());

        params.put("pollByCareerSubReport", reportData.getJasperReport());
        super.generateReport(
                "pollByPersonReport",
                "/employees/reports/pollByPersonReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Reports.pollByPersonReport"), params);
    }
}