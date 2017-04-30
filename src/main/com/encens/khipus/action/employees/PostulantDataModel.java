package com.encens.khipus.action.employees;

import com.encens.khipus.exception.employees.MalformedEntityQueryCompoundConditionException;
import com.encens.khipus.framework.action.EntityQuery;
import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.admin.BusinessUnit;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.util.ValidatorUtil;
import com.encens.khipus.util.query.EntityQueryCompoundCondition;
import com.encens.khipus.util.query.EntityQueryConditionOperator;
import com.encens.khipus.util.query.EntityQuerySingleCondition;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Data model for Postulant
 *
 * @author
 */

@Name("postulantDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('HUMANRESOURCESAPPLICANT','VIEW')}")
public class PostulantDataModel extends QueryDataModel<Long, Postulant> {
    private static final String[] RESTRICTIONS = {
            "postulant.registryDate >= #{postulantDataModel.initRegisterDate}",
            "postulant.registryDate <= #{postulantDataModel.endRegisterDate}",
            "subject.career.faculty = #{postulantDataModel.faculty}",
            "subject.career = #{postulantDataModel.career}",
            "subject = #{postulantDataModel.subject}",
            "postulantCharge.charge = #{postulantDataModel.charge}",
//          "new Long(experience.initDate - experience.endDate) <= new Long(#{postulantDataModel.experienceDuration})",
            "experience.experienceType = #{postulantDataModel.experienceType}",

            "lower(academicFormation.name) like concat('%', concat(lower( #{postulantDataModel.academicFormationName}), '%'))",
            "academicFormation.academicFormationType = #{postulantDataModel.academicFormationType}",

            "lower(postulant.idNumber) like concat(lower( #{postulantDataModel.criteria.idNumber}), '%')",
            "lower(postulant.lastName) like concat('%', concat(lower( #{postulantDataModel.criteria.lastName}), '%'))",
            "lower(postulant.maidenName) like concat('%', concat(lower( #{postulantDataModel.criteria.maidenName}), '%'))",
            "lower(postulant.firstName) like concat('%', concat(lower( #{postulantDataModel.criteria.firstName}), '%'))",
            "postulant.postulantType =#{postulantDataModel.criteria.postulantType}"};

    @In
    private PollFormService pollFormService;
    @Logger
    protected Log log;

    private Location location;
    private BusinessUnit businessUnit;

    private Faculty faculty;

    private Career career;

    private Subject subject;
    private Charge charge;

    private Date initRegisterDate;

    private Date endRegisterDate;

    private Date experienceDuration;

    private ExperienceType experienceType;

    private String academicFormationName;

    private AcademicFormationType academicFormationType;

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

    public Charge getCharge() {
        return charge;
    }

    public void setCharge(Charge charge) {
        this.charge = charge;
    }

    public boolean professorPostulantType() {
        PostulantType postulantType = getCriteria().getPostulantType();
        return postulantType != null && postulantType.equals(PostulantType.PROFESSOR);
    }

    @Create
    public void init() {
        sortProperty = "postulant.registryDate,postulant.lastName,postulant.maidenName,postulant.firstName";
        sortAsc = false;
    }

    @Override
    public String getEjbql() {
        return "select distinct postulant from Postulant postulant" +
                " left join postulant.academicFormationList academicFormation" +
                " left join postulant.experienceList experience" +
                " left join postulant.subjectList subject " +
                " left join postulant.postulantChargeList postulantCharge " +
                " left join postulantCharge.businessUnit businessUnit " +
                " left join subject.career career " +
                " left join career.faculty faculty " +
                " left join faculty.location location ";
    }

    @Override
    protected void postInitEntityQuery(EntityQuery entityQuery) {
        entityQuery.setEjbql(addConditions(getEjbql()));
    }

    public String addConditions(String ejbql) {

        EntityQueryCompoundCondition entityQueryCompoundCondition = new EntityQueryCompoundCondition();
        String restrictionResult = "";
        try {
            entityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition(" location.id =#{postulantDataModel.location.id} "));
            entityQueryCompoundCondition.addConditionOperator(EntityQueryConditionOperator.OR);
            entityQueryCompoundCondition.addCondition(new EntityQuerySingleCondition(" businessUnit.id = #{postulantDataModel.businessUnit.id} "));
            restrictionResult = entityQueryCompoundCondition.compile();
        } catch (MalformedEntityQueryCompoundConditionException e) {
            log.error("Malformed entity query compound condition exception, condition will not be added", e);
        }
        if (!ValidatorUtil.isBlankOrNull(restrictionResult)) {
            ejbql += "where ";
            ejbql += restrictionResult;
        }

        return ejbql;
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
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
    public void clearPostulantCharge() {
        setCharge(null);
    }

    @SuppressWarnings({"NullableProblems"})
    public void clearLocation() {
        setLocation(null);
    }

    @SuppressWarnings({"NullableProblems"})
    public void postulantTypeChanged() {
        if (null != getCriteria().getPostulantType()) {
            if (getCriteria().getPostulantType().equals(PostulantType.PROFESSOR)) {
                clearPostulantCharge();
                setBusinessUnit(null);
            } else {
                clearLocation();
                setFaculty(null);
                setCareer(null);
                setSubject(null);
            }
        }
    }

    @SuppressWarnings({"NullableProblems"})
    public void cleanSearchFilters() {
        setInitRegisterDate(null);
        setEndRegisterDate(null);
        setLocation(null);
        setFaculty(null);
        setCareer(null);
        setSubject(null);
        setCharge(null);
        setBusinessUnit(null);
        setExperienceType(null);
        setAcademicFormationName(null);
        setAcademicFormationType(null);
        getCriteria().setIdNumber(null);
        getCriteria().setLastName(null);
        getCriteria().setMaidenName(null);
        getCriteria().setFirstName(null);
    }
}