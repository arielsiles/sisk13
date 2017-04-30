package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.FinalEvaluationFormService;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to Genarate final evaluation summary report
 *
 * @author
 * @version $Id: FinalEvaluationSummaryReportAction.java  28-jun-2010 19:02:19$
 */
@Name("finalEvaluationSummaryReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FINALEVALUATIONSUMMARYREPORT','VIEW')}")
public class FinalEvaluationSummaryReportAction extends GenericReportAction {

    @In
    private PollFormService pollFormService;
    @In
    private FinalEvaluationFormService finalEvaluationFormService;

    private Cycle cycle;
    private PollForm studentPollForm;
    private PollForm careerManagerPollForm;
    private PollForm autoevaluationPollForm;
    private PollForm teacherPollForm;
    private String idNumber;
    private Location location;
    private Faculty faculty;
    private Career career;

    public void generateReport() {
        log.debug("Generating FinalEvaluationSummaryReportAction............................");

        Map params = new HashMap();
        params.putAll(readReportParamsInfo());
        super.generateReport("finalSummaryEvalReport", "/employees/reports/finalEvaluationSummaryReport.jrxml", PageFormat.LETTER, PageOrientation.PORTRAIT, MessageUtils.getMessage("Reports.finalEvalSummary.title"), params);
    }

    @Override
    protected String getEjbql() {

        return "SELECT DISTINCT " +
                "location.id," +
                "location.name," +
                "person.id," +
                "person.lastName," +
                "person.maidenName," +
                "person.firstName" +
                " FROM PollCopy pollCopy" +
                " LEFT JOIN pollCopy.faculty faculty" +
                " LEFT JOIN faculty.location location" +
                " LEFT JOIN pollCopy.career career" +
                " LEFT JOIN pollCopy.person person" +
                " LEFT JOIN pollCopy.pollForm pollForm" +
                " WHERE person IS NOT NULL" +
                " AND (pollForm.id = " + studentPollForm.getId() +
                " OR pollForm.id = " + careerManagerPollForm.getId() +
                " OR pollForm.id = " + autoevaluationPollForm.getId() +
                " OR pollForm.id = " + teacherPollForm.getId() + ")";

    }

    @Create
    public void init() {
        restrictions = new String[]{"pollCopy.company=#{currentCompany}",
                "person.idNumber=#{finalEvaluationSummaryReportAction.idNumber}",
                "location=#{finalEvaluationSummaryReportAction.location}",
                "faculty=#{finalEvaluationSummaryReportAction.faculty}",
                "career=#{finalEvaluationSummaryReportAction.career}"};

        sortProperty = " location.id," +
                " person.lastName," +
                " person.maidenName," +
                " person.firstName," +
                " person.id";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map readReportParamsInfo() {
        Map paramMap = new HashMap();

        paramMap.put("studentPollFormParam", studentPollForm);
        paramMap.put("careerManagerPollFormParam", careerManagerPollForm);
        paramMap.put("autoevaluationPollFormParam", autoevaluationPollForm);
        paramMap.put("teacherPollFormParam", teacherPollForm);
        paramMap.put("cycleParam", cycle);

        return paramMap;
    }

    private String paramAsString(Object value) {
        return value != null ? value.toString() : "";
    }

    /**
     * Compose header info to final poll form evaluation report
     *
     * @param finalEvaluationForm
     * @return String
     */
    private String composeFinalPollFormHeaderInfo(FinalEvaluationForm finalEvaluationForm) {
        String headerInfo = "";

        headerInfo = headerInfo + MessageUtils.getMessage("Reports.pollForm.code") + ": " + (finalEvaluationForm.getCode() != null ? finalEvaluationForm.getCode() : "") + "\n";
        headerInfo = headerInfo + MessageUtils.getMessage("Reports.pollForm.revise") + ": " + (finalEvaluationForm.getRevision() != null ? finalEvaluationForm.getRevision() : "") + "\n";
        headerInfo = headerInfo + MessageUtils.getMessage("Reports.pollForm.approvalDate") + ": " + (finalEvaluationForm.getApprovalDate() != null ? DateUtils.format(finalEvaluationForm.getApprovalDate(), MessageUtils.getMessage("patterns.date")) : "");
        return headerInfo;
    }


    public Cycle getCycle() {
        return cycle;
    }

    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    public PollForm getStudentPollForm() {
        return studentPollForm;
    }

    public void setStudentPollForm(PollForm studentPollForm) {
        this.studentPollForm = studentPollForm;
    }

    public PollForm getCareerManagerPollForm() {
        return careerManagerPollForm;
    }

    public void setCareerManagerPollForm(PollForm careerManagerPollForm) {
        this.careerManagerPollForm = careerManagerPollForm;
    }

    public PollForm getAutoevaluationPollForm() {
        return autoevaluationPollForm;
    }

    public void setAutoevaluationPollForm(PollForm autoevaluationPollForm) {
        this.autoevaluationPollForm = autoevaluationPollForm;
    }

    public PollForm getTeacherPollForm() {
        return teacherPollForm;
    }

    public void setTeacherPollForm(PollForm teacherPollForm) {
        this.teacherPollForm = teacherPollForm;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
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


    public List<PollForm> getPollFormList() {
        return pollFormService.getPollFormByCycle(getCycle());
    }

    public void refreshCycle() {
        setStudentPollForm(null);
        setCareerManagerPollForm(null);
        setAutoevaluationPollForm(null);
        setTeacherPollForm(null);
    }

    public void refreshLocation() {
        setFaculty(null);
        setCareer(null);
    }

    public void refreshFaculty() {
        setCareer(null);
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

}
