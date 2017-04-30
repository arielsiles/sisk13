package com.encens.khipus.action.employees;

import com.encens.khipus.framework.action.QueryDataModel;
import com.encens.khipus.model.contacts.Person;
import com.encens.khipus.model.employees.*;
import com.encens.khipus.service.employees.PollFormService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version 1.1.7
 */
@Name("pollCopyDataModel")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasPermission('POLLCOPY','VIEW')}")
public class PollCopyDataModel extends QueryDataModel<Long, PollCopy> {
    @In
    private PollFormService pollFormService;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Long pollFormId;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private String idNumber;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Date initRevisionDate;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Date endRevisionDate;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Long locationId;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Long facultyId;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Long careerId;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Long subjectId;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private String studentCode;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private String employeeCode;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Integer page = 1;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private String currentSortProperty = null;

    @Out(required = false, scope = ScopeType.SESSION)
    @In(required = false, scope = ScopeType.SESSION)
    private Boolean currentSortAsc = true;

    private static final String[] RESTRICTIONS = {
            "pollCopy.pollForm.id = #{pollCopyDataModel.pollFormId}",
            "pollCopy.person.idNumber like concat(#{pollCopyDataModel.idNumber}, '%')",
            "pollCopy.revisionDate >= #{pollCopyDataModel.initRevisionDate}",
            "pollCopy.revisionDate <= #{pollCopyDataModel.endRevisionDate}",
            "pollCopy.faculty.location.id = #{pollCopyDataModel.locationId}",
            "pollCopy.faculty.id = #{pollCopyDataModel.facultyId}",
            "pollCopy.career.id = #{pollCopyDataModel.careerId}",
            "pollCopy.subject.id = #{pollCopyDataModel.subjectId}",
            "pollCopy.evaluator = #{pollCopyDataModel.criteria.evaluator}",
            "pollCopy.person = #{pollCopyDataModel.criteria.person}",
            "pollCopy.evaluator.id in (select est.id from Student est where lower(est.studentCode) like concat(lower(#{pollCopyDataModel.studentCode}), '%'))",
            "pollCopy.evaluator.id in (select emp.id from Employee emp where lower(emp.employeeCode) like concat(lower(#{pollCopyDataModel.employeeCode}), '%'))"};

    @Override
    public String getEjbql() {
        return "select pollCopy from PollCopy pollCopy" +
                " left join fetch pollCopy.pollForm pollForm" +
                " left join fetch pollCopy.cycle cycle" +
                " left join fetch pollCopy.academicPeriod academicPeriod" +
                " left join fetch pollCopy.faculty faculty" +
                " left join fetch faculty.location location" +
                " left join fetch pollCopy.career career" +
                " left join fetch pollCopy.subject subject" +
                " left join fetch pollCopy.evaluator evaluator" +
                " left join fetch pollCopy.person";
    }

    @Override
    public List<String> getRestrictions() {
        return Arrays.asList(RESTRICTIONS);
    }

    @Create
    public void init() {
        setSortProperty("pollCopy.revisionDate");
        setSortAsc(true);
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

    public Long getPollFormId() {
        return pollFormId;
    }

    public void setPollFormId(Long pollFormId) {
        this.pollFormId = pollFormId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public Long getCareerId() {
        return careerId;
    }

    public void setCareerId(Long careerId) {
        this.careerId = careerId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public void refleshLocation() {
        setFacultyId(null);
        setCareerId(null);
        setSubjectId(null);
    }

    public void refleshFaculty() {
        setCareerId(null);
        setSubjectId(null);
    }

    public void refleshCareer() {
        setSubjectId(null);
    }

    public List<Location> getLocationList() {
        return pollFormService.getLocationList();
    }

    public List<Faculty> getFacultyList() {
        return pollFormService.getFacultyList(new Location(getLocationId()));
    }

    public List<Career> getCareerList() {
        return pollFormService.getCareerList(new Faculty(getFacultyId()));
    }

    public List<Subject> getSubjectList() {
        return pollFormService.getSubjectList(new Career(getCareerId()));
    }

    public void clear() {
        setPage(1);
        setPollFormId(null);
        setEndRevisionDate(null);
        setInitRevisionDate(null);
        setIdNumber(null);
        setLocationId(null);
        setFacultyId(null);
        setCareerId(null);
        setSubjectId(null);
        setStudentCode(null);
        setEmployeeCode(null);
        super.clear();
        super.search();
    }

    @Override
    public int getPage() {
        return page != null ? page.intValue() : 0;
    }

    @Override
    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public String getSortProperty() {
        return currentSortProperty;
    }

    @Override
    public void setSortProperty(String sortProperty) {
        if (this.currentSortProperty != null) {
            setSortAsc(!this.currentSortProperty.equals(sortProperty) || !isSortAsc());
        }
        this.currentSortProperty = sortProperty;
    }

    @Override
    public boolean isSortAsc() {
        return currentSortAsc != null ? currentSortAsc.booleanValue() : true;
    }

    @Override
    public void setSortAsc(boolean sortAsc) {
        this.currentSortAsc = sortAsc;
    }

    public void assignEvaluator(Person person) {
        getCriteria().setEvaluator(person);
    }

    public void clearEvaluator() {
        getCriteria().setEvaluator(null);
    }

    public void assignPerson(Person person) {
        getCriteria().setPerson(person);
    }

    public void clearPerson() {
        getCriteria().setEvaluator(null);
    }
}
