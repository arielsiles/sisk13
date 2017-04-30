package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.FinalEvaluationFormService;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.DateUtils;
import com.encens.khipus.util.MessageUtils;
import com.jatun.titus.reportgenerator.util.TypedReportData;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 * Action to generate teacher final evaluation report
 *
 * @author
 * @version $Id: FinalTeacherEvaluationReportAction.java  15-jun-2010 18:31:19$
 */
@Name("finalTeacherEvaluationReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('FINALTEACHEREVALUATIONREPORT','VIEW')}")
public class FinalTeacherEvaluationReportAction extends GenericReportAction {
    @In
    private FacesMessages facesMessages;

    @In
    private PollFormService pollFormService;
    @In
    private FinalEvaluationFormService finalEvaluationFormService;

    private Cycle cycle;
    private PollForm studentPollForm;
    private PollForm careerManagerPollForm;
    private PollForm autoevaluationPollForm;
    private String idNumber;
    private Location location;
    private Faculty faculty;
    private Career career;

    public void generateReport() {
        log.debug("Generating FinalTeacherEvaluationReportAction............................");
        //find final form information data
        FinalEvaluationForm finalEvaluationForm = finalEvaluationFormService.getFinalEvaluationFormByCycleAndType(getCycle(), FinalEvaluationFormType.PROFESSOR);

        if (hasErrorMessages(finalEvaluationForm)) {
            //show error messages
            return;
        }

        Map params = new HashMap();

        addPollCopyCareerSubReport(params);
        addPollCopyFacultySubReport(params);
        addFormPunctuationRangeSubReport(params, finalEvaluationForm);

        params.putAll(readReportParamsInfo(finalEvaluationForm));
        super.generateReport("finalTeacherEvalReport", "/employees/reports/finalTeacherEvaluationReport.jrxml", PageFormat.LETTER, PageOrientation.PORTRAIT, finalEvaluationForm.getSubtitle(), params);
    }

    @Override
    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "person.id," +
                "person.lastName," +
                "person.maidenName," +
                "person.firstName" +
                " FROM PollCopy pollCopy" +
                " LEFT JOIN pollCopy.person person" +
                " LEFT JOIN pollCopy.pollForm pollForm" +
                " WHERE person IS NOT NULL" +
                " AND (pollForm.id = " + studentPollForm.getId() +
                " OR pollForm.id = " + careerManagerPollForm.getId() +
                " OR pollForm.id = " + autoevaluationPollForm.getId() + ")";
    }

    @Create
    public void init() {
        restrictions = new String[]{"pollCopy.company=#{currentCompany}",
                "person.idNumber=#{finalTeacherEvaluationReportAction.idNumber}",
                "pollCopy.faculty.location=#{finalTeacherEvaluationReportAction.location}",
                "pollCopy.faculty=#{finalTeacherEvaluationReportAction.faculty}",
                "pollCopy.career=#{finalTeacherEvaluationReportAction.career}"};

        sortProperty = " person.lastName," +
                " person.maidenName," +
                " person.firstName," +
                " person.id";
    }

    /**
     * Read report params
     *
     * @return Map
     */
    private Map readReportParamsInfo(FinalEvaluationForm finalEvaluationForm) {
        Map paramMap = new HashMap();
        paramMap.put("titleParam", paramAsString(finalEvaluationForm.getSubtitle()));
        paramMap.put("headerInfoParam", composeFinalPollFormHeaderInfo(finalEvaluationForm));

        paramMap.put("studentPollFormParam", studentPollForm);
        paramMap.put("careerManagerPollFormParam", careerManagerPollForm);
        paramMap.put("autoevaluationPollFormParam", autoevaluationPollForm);
        paramMap.put("cycleParam", cycle);

        paramMap.put("gestionParam", paramAsString(getCycle().getGestion().getYear()));
        paramMap.put("objetiveParam", finalEvaluationForm.getTarget() != null ? paramAsString(finalEvaluationForm.getTarget().getValue()) : "");
        paramMap.put("methodologyParam", finalEvaluationForm.getMethodology() != null ? paramAsString(finalEvaluationForm.getMethodology().getValue()) : "");

        paramMap.put("studentFormNameParam", studentPollForm.getCode() + " " + studentPollForm.getTitle() + " " + studentPollForm.getSubTitle());
        paramMap.put("studentFormDescriptionParam", paramAsString(studentPollForm.getDescription()));
        paramMap.put("studentFormEquivalentPercentParam", paramAsString(studentPollForm.getEquivalentPercent()) + "%");
        paramMap.put("studentFormSamplePercentParam", paramAsString(studentPollForm.getSamplePercent()) + "%");

        paramMap.put("careerManagerFormNameParam", careerManagerPollForm.getCode() + " " + careerManagerPollForm.getTitle() + " " + careerManagerPollForm.getSubTitle());
        paramMap.put("careerManagerFormDescriptionParam", paramAsString(careerManagerPollForm.getDescription()));
        paramMap.put("careerManagerFormEquivalentPercentParam", paramAsString(careerManagerPollForm.getEquivalentPercent()) + "%");
        paramMap.put("careerManagerFormSamplePercentParam", paramAsString(careerManagerPollForm.getSamplePercent()) + "%");

        paramMap.put("autoevaluationFormNameParam", autoevaluationPollForm.getCode() + " " + autoevaluationPollForm.getTitle() + " " + autoevaluationPollForm.getSubTitle());
        paramMap.put("autoevaluationFormDescriptionParam", paramAsString(autoevaluationPollForm.getDescription()));
        paramMap.put("autoevaluationFormEquivalentPercentParam", paramAsString(autoevaluationPollForm.getEquivalentPercent()) + "%");

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

    private void addPollCopyCareerSubReport(Map mainReportParams) {
        log.debug("Generating addPollCopyCareerSubReport.............................");
        String subReportKey = "POLLCOPYCAREERSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT DISTINCT " +
                "career.id," +
                "career.name" +
                " FROM PollCopy pollCopy" +
                " LEFT JOIN pollCopy.career career" +
                " LEFT JOIN pollCopy.pollForm pollForm" +
                " LEFT JOIN pollCopy.person person" +
                " WHERE person.id=$P{personIdParam}";

        String[] restrictions = new String[]{
                "pollForm=#{finalTeacherEvaluationReportAction.studentPollForm}",
                "pollCopy.faculty.location=#{finalTeacherEvaluationReportAction.location}",
                "pollCopy.faculty=#{finalTeacherEvaluationReportAction.faculty}",
                "career=#{finalTeacherEvaluationReportAction.career}"};
        String orderBy = "career.name";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/employees/reports/pollCopyCareerSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    private void addPollCopyFacultySubReport(Map mainReportParams) {
        log.debug("Generating addPollCopyFacultySubReport.............................");
        String subReportKey = "POLLCOPYFACULTYSUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT DISTINCT " +
                "faculty.id," +
                "faculty.name" +
                " FROM PollCopy pollCopy" +
                " LEFT JOIN pollCopy.faculty faculty" +
                " LEFT JOIN pollCopy.pollForm pollForm" +
                " LEFT JOIN pollCopy.person person" +
                " WHERE person.id=$P{personIdParam}";

        String[] restrictions = new String[]{
                "pollForm=#{finalTeacherEvaluationReportAction.studentPollForm}",
                "faculty.location=#{finalTeacherEvaluationReportAction.location}",
                "faculty=#{finalTeacherEvaluationReportAction.faculty}",
                "pollCopy.career=#{finalTeacherEvaluationReportAction.career}"};
        String orderBy = "faculty.name";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/employees/reports/pollCopyFacultySubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    private void addFormPunctuationRangeSubReport(Map mainReportParams, FinalEvaluationForm finalEvaluationForm) {
        log.debug("Generating addFormPunctuationRangeSubReport.............................");
        String subReportKey = "PUNCTUATIONRANGESUBREPORT";

        Map<String, Object> params = new HashMap<String, Object>();

        String ejbql = "SELECT " +
                "punctuationRange.name," +
                "punctuationRange.interpretation" +
                " FROM FinalEvaluationPunctuationRange punctuationRange" +
                " LEFT JOIN punctuationRange.finalEvaluationForm finalEvaluationForm" +
                " WHERE finalEvaluationForm.id=" + finalEvaluationForm.getId();

        String[] restrictions = new String[]{};
        String orderBy = "punctuationRange.position";

        //generate the sub report
        TypedReportData subReportData = super.generateSubReport(
                subReportKey,
                "/employees/reports/punctuationRangeSubReport.jrxml",
                PageFormat.LETTER,
                PageOrientation.PORTRAIT,
                createQueryForSubreport(subReportKey, ejbql, Arrays.asList(restrictions), orderBy),
                params);

        //add in main report params
        mainReportParams.putAll(subReportData.getReportParams());
        mainReportParams.put(subReportKey, subReportData.getJasperReport());
    }

    /**
     * Validate required fields
     *
     * @param finalEvaluationForm
     * @return true or false
     */
    private boolean hasErrorMessages(FinalEvaluationForm finalEvaluationForm) {
        boolean hasError = false;
        if (finalEvaluationForm == null) {
            //add error message
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Reports.finalTeacherEvaluation.error.evalForm", getCycle().getName());
            hasError = true;
        }
        return hasError;
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
