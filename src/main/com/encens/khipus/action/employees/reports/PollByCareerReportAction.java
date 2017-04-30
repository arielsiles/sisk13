package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.employees.GenericPollReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.PollFormGrouppingType;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;

/**
 * Encens S.R.L.
 *
 * @author
 */
@Name("pollByCareerReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('POLLBYCAREERREPORT','VIEW')}")
public class PollByCareerReportAction extends GenericPollReportAction {

    private PollFormGrouppingType pollFormGrouppingType = PollFormGrouppingType.SUBJECT;

    public PollFormGrouppingType getPollFormGrouppingType() {
        return pollFormGrouppingType;
    }

    public void setPollFormGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        this.pollFormGrouppingType = pollFormGrouppingType;
    }

    @Create
    public void init() {
        restrictions = new String[]{"pollCopy.company=#{currentCompany}",
                "pollCopy.pollForm=#{pollByCareerReportAction.pollForm}",
                "pollCopy.person.idNumber=#{pollByCareerReportAction.idNumber}",
                "pollCopy.revisionDate>=#{pollByCareerReportAction.initRevisionDate}",
                "pollCopy.revisionDate<=#{pollByCareerReportAction.endRevisionDate}",
                "pollCopy.faculty.location=#{pollByCareerReportAction.location}",
                "pollCopy.faculty=#{pollByCareerReportAction.faculty}"};
        sortProperty =
                "pollCopy.subject.career.faculty.location.name," +
                        "pollCopy.subject.career.faculty.location.id," +
                        "pollCopy.subject.career.name," +
                        "pollCopy.subject.career.id," +
                        "pollCopy.person.lastName," +
                        "pollCopy.person.maidenName," +
                        "pollCopy.person.firstName," +
                        "pollCopy.person.id," +
                        "pollCopy.subject.name," +
                        "pollCopy.subject.id";
    }

    protected String getEjbql() {
        return "SELECT pollCopy.id," +
                "             pollCopy.person.id," +
                "             pollCopy.subject.id," +
                "             pollCopy.subject.career.id," +
                "             pollCopy.subject.career.faculty.id," +
                "             pollCopy.subject.career.faculty.location.id," +
                "             pollCopy.person.firstName," +
                "             pollCopy.person.maidenName," +
                "             pollCopy.person.lastName," +
                "             pollCopy.person.idNumber," +
                "             pollCopy.subject.name," +
                "             pollCopy.subject.code," +
                "             pollCopy.subject.career.name," +
                "             pollCopy.subject.career.code," +
                "             pollCopy.subject.career.faculty.name," +
                "             pollCopy.subject.career.faculty.code," +
                "             pollCopy.subject.career.faculty.location.name," +
                "             pollCopy.subject.career.faculty.location.code" +
                " FROM        PollCopy pollCopy ";
    }

    public void generate() {
        log.debug("generating pollByCareerReport...................................... ");
//        setReportFormat(ReportFormat.PDF);

        super.generateReport(
                "pollByCareerReport",
                "/employees/reports/pollByCareerReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                messages.get("Reports.pollByCareerReport"),
                new HashMap());
    }
}
