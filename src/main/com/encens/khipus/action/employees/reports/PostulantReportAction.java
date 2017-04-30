package com.encens.khipus.action.employees.reports;

import com.encens.khipus.action.reports.GenericReportAction;
import com.encens.khipus.action.reports.PageFormat;
import com.encens.khipus.action.reports.PageOrientation;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.MessageUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encens S.R.L.
 *
 * @author
 * @version 3.4
 */
@Name("postulantReportAction")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('POSTULANTREPORT','VIEW')}")
public class PostulantReportAction extends GenericReportAction {

    @In
    private PollFormService pollFormService;

    private String idNumber;

    private String lastName;

    private String maidenName;

    private String firstName;
    private BusinessUnit businessUnit;
    private Charge charge;
    private PostulantType postulantType;

    private Location location;

    private Faculty faculty;

    private Career career;

    private Subject subject;

    private Date initRegisterDate;

    private Date endRegisterDate;

    private Date experienceDuration;

    private ExperienceType experienceType;

    private String academicFormationName;

    private AcademicFormationType academicFormationType;


    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        this.maidenName = maidenName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public BusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(BusinessUnit businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public PostulantType getPostulantType() {
        return postulantType;
    }

    public void setPostulantType(PostulantType postulantType) {
        this.postulantType = postulantType;
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

    public Date getInitRegisterDate() {
        return initRegisterDate;
    }

    public void setInitRegisterDate(Date initRegisterDate) {
        this.initRegisterDate = initRegisterDate;
    }

    public Date getEndRegisterDate() {
        return endRegisterDate;
    }

    public void setEndRegisterDate(Date endRegisterDate) {
        this.endRegisterDate = endRegisterDate;
    }

    public Date getExperienceDuration() {
        return experienceDuration;
    }

    public void setExperienceDuration(Date experienceDuration) {
        this.experienceDuration = experienceDuration;
    }

    public ExperienceType getExperienceType() {
        return experienceType;
    }

    public void setExperienceType(ExperienceType experienceType) {
        this.experienceType = experienceType;
    }

    public String getAcademicFormationName() {
        return academicFormationName;
    }

    public void setAcademicFormationName(String academicFormationName) {
        this.academicFormationName = academicFormationName;
    }

    public AcademicFormationType getAcademicFormationType() {
        return academicFormationType;
    }

    public void setAcademicFormationType(AcademicFormationType academicFormationType) {
        this.academicFormationType = academicFormationType;
    }

    @SuppressWarnings({"NullableProblems"})
    public void refleshLocation() {
        setFaculty(null);
        setCareer(null);
        setSubject(null);
    }

    @SuppressWarnings({"NullableProblems"})
    public void refleshFaculty() {
        setCareer(null);
        setSubject(null);
    }

    @SuppressWarnings({"NullableProblems"})
    public void refleshCareer() {
        setSubject(null);
    }

    public boolean professorPostulantType() {
        PostulantType postulantType = getPostulantType();
        return postulantType != null && postulantType.equals(PostulantType.PROFESSOR);
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

    @SuppressWarnings({"NullableProblems"})
    public void postulantTypeChanged() {
        if (null != getPostulantType()) {
            if (getPostulantType().equals(PostulantType.PROFESSOR)) {
                clearPostulantCharge();
                setBusinessUnit(null);
            } else {
                setLocation(null);
                setFaculty(null);
                setCareer(null);
                setSubject(null);
            }
        }
    }

    @Override
    protected String getEjbql() {
        return "SELECT DISTINCT " +
                "postulant.id," +
                "postulant.lastName," +
                "postulant.maidenName," +
                "postulant.firstName," +
                "postulant.birthDay," +
                "postulant.birthPlace," +
                "postulant.idNumber," +
                "postulant.expendedPlace," +
                "postulant.email," +
                "postulant.phoneNumber," +
                "postulant.cellPhoneNumber," +
                "postulant.registryDate," +
                "postulant.postulantType" +
                " FROM Postulant postulant" +
                " LEFT JOIN postulant.academicFormationList academicFormation" +
                " LEFT JOIN postulant.experienceList experience" +
                " LEFT JOIN postulant.subjectList subject" +
                " LEFT JOIN postulant.postulantChargeList postulantCharge" +
                " LEFT JOIN postulantCharge.businessUnit businessUnit";
    }

    @Create
    public void init() {
        restrictions = new String[]{"postulant.registryDate >= #{postulantReportAction.initRegisterDate}",
                "postulant.registryDate <= #{postulantReportAction.endRegisterDate}",
                "subject.career.faculty.location = #{postulantReportAction.location}",
                "businessUnit = #{postulantReportAction.businessUnit}",
                "postulantCharge.charge = #{postulantReportAction.charge}",
                "postulant.postulantType = #{postulantReportAction.postulantType}",
                "subject.career.faculty = #{postulantReportAction.faculty}",
                "subject.career = #{postulantReportAction.career}",
                "subject = #{postulantReportAction.subject}",
                "experience.experienceType = #{postulantReportAction.experienceType}",
                "lower(academicFormation.name) like concat('%',concat(lower(#{postulantReportAction.academicFormationName}),'%'))",
                "academicFormation.academicFormationType = #{postulantReportAction.academicFormationType}",
                "lower(postulant.idNumber) like concat(lower(#{postulantReportAction.idNumber}),'%')",
                "lower(postulant.lastName) like concat(lower(#{postulantReportAction.lastName}),'%')",
                "lower(postulant.maidenName) like concat(lower(#{postulantReportAction.maidenName}),'%')",
                "lower(postulant.firstName) like concat(lower(#{postulantReportAction.firstName}),'%')"};
        sortProperty = "postulant.registryDate";
    }

    public void generateReport() {
        Map params = new HashMap();
        super.generateReport("pollByPersonReport", "/employees/reports/postulantReport.jrxml", PageFormat.LETTER, PageOrientation.LANDSCAPE, MessageUtils.getMessage("Reports.postulantReport"), params);

    }

    @SuppressWarnings({"NullableProblems"})
    public void clearPostulantCharge() {
        setCharge(null);
    }

}
