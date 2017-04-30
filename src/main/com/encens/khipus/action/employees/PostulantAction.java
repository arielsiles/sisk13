package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.GenericAction;
import com.encens.khipus.framework.action.Outcome;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.PollFormService;
import com.encens.khipus.service.employees.PostulantService;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.SortUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Actions for Postulant
 *
 * @author
 */

@Name("postulantAction")
@Scope(ScopeType.CONVERSATION)
public class PostulantAction extends GenericAction<Postulant> {

    @In
    private PollFormService pollFormService;
    @In
    private PostulantService postulantService;

    private List<PostulantAcademicFormation> academicFormationList = new ArrayList<PostulantAcademicFormation>();
    private List<Experience> laboralExperienceList = new ArrayList<Experience>();
    private List<Experience> professorExperienceList = new ArrayList<Experience>();
    private List<HourAvailable> hourAvailableList = new ArrayList<HourAvailable>();
    private List<Subject> subjectResultList = new ArrayList<Subject>();
    private List<PostulantCharge> postulantChargeList = new ArrayList<PostulantCharge>();

    private PostulantAcademicFormation academicFormation = new PostulantAcademicFormation();

    private Experience laboralExperience = new Experience();
    private Experience professorExperience = new Experience();
    private HourAvailable hourAvailable = new HourAvailable();
    private PostulantCharge postulantCharge = new PostulantCharge();

    private Location location;
    private Faculty faculty;
    private Career career;
    private Subject subject;
    private boolean managerPostulant;

    public void setManagerPostulant(boolean managerPostulant) {
        this.managerPostulant = managerPostulant;
        getInstance().setPostulantType(managerPostulant ? PostulantType.MANAGER : PostulantType.PROFESSOR);
    }

    public boolean isManagerPostulant() {
        return managerPostulant;
    }

    public List<PostulantAcademicFormation> getAcademicFormationList() {
        return academicFormationList;
    }

    public void setAcademicFormationList(List<PostulantAcademicFormation> academicFormationList) {
        this.academicFormationList = academicFormationList;
    }

    public List<Experience> getLaboralExperienceList() {
        return laboralExperienceList;
    }

    public void setLaboralExperienceList(List<Experience> laboralExperienceList) {
        this.laboralExperienceList = laboralExperienceList;
    }

    public List<Experience> getProfessorExperienceList() {
        return professorExperienceList;
    }

    public void setProfessorExperienceList(List<Experience> professorExperienceList) {
        this.professorExperienceList = professorExperienceList;
    }

    public List<HourAvailable> getHourAvailableList() {
        return hourAvailableList;
    }

    public void setHourAvailableList(List<HourAvailable> hourAvailableList) {
        this.hourAvailableList = hourAvailableList;
    }

    public List<PostulantCharge> getPostulantChargeList() {
        return postulantChargeList;
    }

    public void setPostulantChargeList(List<PostulantCharge> postulantChargeList) {
        this.postulantChargeList = postulantChargeList;
    }

    public List<Subject> getSubjectResultList() {
        return subjectResultList;
    }

    public void setSubjectResultList(List<Subject> subjectResultList) {
        this.subjectResultList = subjectResultList;
    }

    public PostulantAcademicFormation getAcademicFormation() {
        return academicFormation;
    }

    public void setAcademicFormation(PostulantAcademicFormation academicFormation) {
        this.academicFormation = academicFormation;
    }

    public Experience getLaboralExperience() {
        return laboralExperience;
    }

    public void setLaboralExperience(Experience laboralExperience) {
        this.laboralExperience = laboralExperience;
    }

    public Experience getProfessorExperience() {
        return professorExperience;
    }

    public void setProfessorExperience(Experience professorExperience) {
        this.professorExperience = professorExperience;
    }

    public HourAvailable getHourAvailable() {
        return hourAvailable;
    }

    public void setHourAvailable(HourAvailable hourAvailable) {
        this.hourAvailable = hourAvailable;
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

    public PostulantCharge getPostulantCharge() {
        return postulantCharge;
    }

    public void setPostulantCharge(PostulantCharge postulantCharge) {
        this.postulantCharge = postulantCharge;
    }

    public void clearPostulantCharge() {
        //noinspection NullableProblems
        getPostulantCharge().setCharge(null);
    }

    public void clearPostulantChargeBusinessUnit() {
        //noinspection NullableProblems
        getPostulantCharge().setBusinessUnit(null);
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

    @Create
    public void init() {
        Contexts.getSessionContext().set("currentCompany", new Company(Constants.defaultCompanyId, Constants.defaultCompanyName));
    }

    @Factory(value = "postulant", scope = ScopeType.STATELESS)
    public Postulant initPostulant() {
        return getInstance();
    }

    @Factory(value = "experienceType", scope = ScopeType.STATELESS)
    public ExperienceType[] getExperienceType() {
        return ExperienceType.values();
    }

    @Factory(value = "availableDay", scope = ScopeType.STATELESS)
    public AvailableDay[] getAvailableDay() {
        return AvailableDay.values();
    }

    @Override
    protected String getDisplayNameProperty() {
        return "lastName";
    }

    public Boolean isEmpty(Object string) {
        return string == null || string.toString().trim().length() == 0;
    }

    @Begin(ifOutcome = Outcome.SUCCESS, flushMode = FlushModeType.MANUAL)
    @Restrict("#{s:hasPermission('HUMANRESOURCESAPPLICANT','VIEW')}")
    public String select(Postulant instance) {
        String outcome = super.select(instance);

        setAcademicFormationList(getInstance().getAcademicFormationList());

        for (Experience experience : getInstance().getExperienceList()) {
            if (ExperienceType.LABORAL.equals(experience.getExperienceType())) {
                getLaboralExperienceList().add(experience);
            } else if (ExperienceType.PROFESSOR.equals(experience.getExperienceType())) {
                getProfessorExperienceList().add(experience);
            }
        }

        setHourAvailableList(getInstance().getHourAvailableList());
        setSubjectResultList(getInstance().getSubjectList());
        setPostulantChargeList(getInstance().getPostulantChargeList());
        managerPostulant = getInstance().getPostulantType().equals(PostulantType.MANAGER);
        return outcome;
    }

    @End
    public String create() {
        if (getHourAvailableList().size() == 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Postulant.error.hourAvailable");
            return Outcome.REDISPLAY;
        }
        if (!managerPostulant) {
            if (getSubjectResultList().size() == 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Postulant.error.subject");
                return Outcome.REDISPLAY;
            }
        } else {
            if (getPostulantChargeList().size() == 0) {
                facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Postulant.error.charge");
                return Outcome.REDISPLAY;
            }
        }

        try {

            List<Experience> experiences = new ArrayList<Experience>();
            experiences.addAll(getLaboralExperienceList());
            experiences.addAll(getProfessorExperienceList());

            postulantService.create(getInstance(), getAcademicFormationList(), experiences,
                    getHourAvailableList(), getSubjectResultList(), getPostulantChargeList());

            addCreatedMessage();

            return Outcome.SUCCESS;
        } catch (Exception e) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "Common.globalError.description");
            log.error(e.getMessage(), e);
            return Outcome.REDISPLAY;
        }
    }

    @Override
    protected void addCreatedMessage() {
        facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "Postulant.createdSuccess");
    }

    public String addAcademicFormation() {
        if (!isEmpty(getAcademicFormation().getName()) && !isEmpty(getAcademicFormation().getUniversity())) {
            getAcademicFormationList().add(getAcademicFormation());
            SortUtils.orderByProperty(getAcademicFormationList(), AcademicFormationType.class, "academicFormationType");
        }

        setAcademicFormation(new PostulantAcademicFormation());

        return Outcome.REDISPLAY;
    }

    public void removeAcademicFormation(int index) {
        getAcademicFormationList().remove(index);
    }

    public String addLaboralExperience() {
        if (!isEmpty(getLaboralExperience().getPlace()) && !isEmpty(getLaboralExperience().getPosition())
                && !isEmpty(getLaboralExperience().getInitDate())) {
            getLaboralExperience().setExperienceType(ExperienceType.LABORAL);
            getLaboralExperienceList().add(getLaboralExperience());
        }
        setLaboralExperience(new Experience());
        return Outcome.REDISPLAY;
    }

    public void removeLaboralExperience(int index) {
        getLaboralExperienceList().remove(index);
    }

    public String addProfessorExperience() {
        if (!isEmpty(getProfessorExperience().getPlace()) && !isEmpty(getProfessorExperience().getPosition())
                && !isEmpty(getProfessorExperience().getInitDate())) {
            getProfessorExperience().setExperienceType(ExperienceType.PROFESSOR);
            getProfessorExperienceList().add(getProfessorExperience());
        }
        setProfessorExperience(new Experience());
        return Outcome.REDISPLAY;
    }

    public void removeProfessorExperience(int index) {
        getProfessorExperienceList().remove(index);
    }

    public String addHourAvailable() {
        if (!isEmpty(getHourAvailable().getInitHour())
                && !isEmpty(getHourAvailable().getEndHour())
                && !isEmpty(getHourAvailable().getAvailableDay())) {
            getHourAvailableList().add(getHourAvailable());
            SortUtils.orderByProperty(getHourAvailableList(), AvailableDay.class, "availableDay");
        }

        setHourAvailable(new HourAvailable());

        return Outcome.REDISPLAY;
    }

    public void removeHourAvailable(int index) {
        getHourAvailableList().remove(index);
    }

    public String addPostulantCharge() {
        if (null != getPostulantCharge().getBusinessUnit() &&
                null != getPostulantCharge().getCharge()) {
            getPostulantChargeList().add(getPostulantCharge());
            setPostulantCharge(new PostulantCharge());
        }
        return Outcome.REDISPLAY;
    }

    public void removePostulantCharge(int index) {
        getPostulantChargeList().remove(index);
    }

    public String addSubject() {
        if (getSubject() != null) {
            getFaculty().setLocation(getLocation());
            getCareer().setFaculty(getFaculty());
            getSubject().setCareer(getCareer());
            getSubjectResultList().add(getSubject());
        }

        setLocation(null);
        setFaculty(null);
        setCareer(null);
        setSubject(null);

        return Outcome.REDISPLAY;
    }

    public void removeSubject(int index) {
        getSubjectResultList().remove(index);
    }

    public String getInternationalPrise() {
        return getInstance().getInternationalPrise() != null ? getInstance().getInternationalPrise().getValue() : null;
    }

    public void setInternationalPrise(String internationalPrise) {
        if (getInstance().getInternationalPrise() == null) {
            getInstance().setInternationalPrise(new Text(internationalPrise));
        } else {
            getInstance().getInternationalPrise().setValue(internationalPrise);
        }
    }

    public String getNationalPrise() {
        return getInstance().getNationalPrise() != null ? getInstance().getNationalPrise().getValue() : null;
    }

    public void setNationalPrise(String nationalPrise) {
        if (getInstance().getNationalPrise() == null) {
            getInstance().setNationalPrise(new Text(nationalPrise));
        } else {
            getInstance().getNationalPrise().setValue(nationalPrise);
        }
    }

    public String getBooks() {
        return getInstance().getBooks() != null ? getInstance().getBooks().getValue() : null;
    }

    public void setBooks(String books) {
        if (getInstance().getBooks() == null) {
            getInstance().setBooks(new Text(books));
        } else {
            getInstance().getBooks().setValue(books);
        }
    }

    public String getInternationalArticles() {
        return getInstance().getInternationalArticles() != null ? getInstance().getInternationalArticles().getValue() : null;
    }

    public void setInternationalArticles(String internationalArticles) {
        if (getInstance().getInternationalArticles() == null) {
            getInstance().setInternationalArticles(new Text(internationalArticles));
        } else {
            getInstance().getInternationalArticles().setValue(internationalArticles);
        }
    }

    public String getNationalArticles() {
        return getInstance().getNationalArticles() != null ? getInstance().getNationalArticles().getValue() : null;
    }

    public void setNationalArticles(String nationalArticles) {
        if (getInstance().getNationalArticles() == null) {
            getInstance().setNationalArticles(new Text(nationalArticles));
        } else {
            getInstance().getNationalArticles().setValue(nationalArticles);
        }
    }
}