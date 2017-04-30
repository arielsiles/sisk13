package com.encens.khipus.action.employees;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import java.util.Date;
import java.util.List;

/**
 * x
 * GenericPollReportAction
 *
 * @author
 * @version 1.1.2
 */
@Name("genericPollReportAction")
@Scope(ScopeType.PAGE)
public class GenericPollReportAction extends GenericReportAction {

    @In
    private PollFormService pollFormService;

    private PollForm pollForm;

    private String idNumber;

    private Date initRevisionDate;

    private Date endRevisionDate;

    private Location location;

    private Faculty faculty;

    private Career career;

    private Subject subject;


    public PollForm getPollForm() {
        return pollForm;
    }

    public void setPollForm(PollForm pollForm) {
        this.pollForm = pollForm;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public Date getInitRevisionDate() {
        return initRevisionDate;
    }

    public void setInitRevisionDate(Date initRevisionDate) {
        this.initRevisionDate = initRevisionDate;
    }

    public Date getEndRevisionDate() {
        return endRevisionDate;
    }

    public void setEndRevisionDate(Date endRevisionDate) {
        this.endRevisionDate = endRevisionDate;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public Career getCareer() {
        return career;
    }

    public void setCareer(Career career) {
        this.career = career;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void refleshLocation() {
        setFaculty(null);
        setCareer(null);
        setSubject(null);
    }

    public void refleshFaculty() {
        setCareer(null);
        setSubject(null);
    }

    public void refleshCareer() {
        setSubject(null);
    }

    public List<Location> getLocationList() {
        return pollFormService.getLocationList();
    }

    public List<Faculty> getFacultyList() {
        return pollFormService.getFacultyList(getLocation());
    }

    public List<Career> getCareerList() {
        return pollFormService.getCareerList(getFaculty());
    }

    public List<Subject> getSubjectList() {
        return pollFormService.getSubjectList(getCareer());
    }

    /**
     * Compose report title to poll form evaluations reports
     * @return String
     */
    protected String composePollFormReportTitle() {
        String title = MessageUtils.getMessage("Reports.careerManagerEvaluation.reportOf");
        PollForm pollForm = getPollForm();
        if (pollForm != null) {
            title = title + (pollForm.getTitle() != null ? " " + pollForm.getTitle() : "") + (pollForm.getSubTitle() != null ? "\n" + pollForm.getSubTitle() : "");
        }
        return title;
    }

    /**
     * Compose header info to poll form evaluations reports
     * @return String
     */
    protected String composePollFormHeaderInfo() {
        String headerInfo = "";
        PollForm pollForm = getPollForm();

        headerInfo = headerInfo + MessageUtils.getMessage("Reports.pollForm.code") + ": " + (pollForm != null && pollForm.getCode() != null ? pollForm.getCode() : "") + "\n";
        headerInfo = headerInfo + MessageUtils.getMessage("Reports.pollForm.revise") + ": " + (pollForm != null && pollForm.getReview() != null ? pollForm.getReview() : "") + "\n";
        headerInfo = headerInfo + MessageUtils.getMessage("Reports.pollForm.approvalDate") + ": " + (pollForm != null && pollForm.getApprovalDate() != null ? DateUtils.format(pollForm.getApprovalDate(), MessageUtils.getMessage("patterns.date")) : "");
        return headerInfo;
    }
}
