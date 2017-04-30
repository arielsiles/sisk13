package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : PollForm, 21-10-2009 11:39:19 AM
 */
@NamedQueries({
        @NamedQuery(name = "PollForm.countByCareerGrouppingByCareer", query = "select count(distinct pollCopy.career) from PollCopy pollCopy where " +
                "pollCopy.career is not null and pollCopy.pollForm.id =:pollFormId and " +
                "pollCopy.person is not null and pollCopy.person.id =:personId"),
        @NamedQuery(name = "PollForm.countByCareerGrouppingBySubject", query = "select count(distinct pollCopy.subject.career) from PollCopy pollCopy where " +
                "pollCopy.subject is not null and pollCopy.pollForm.id =:pollFormId and " +
                "pollCopy.person is not null and pollCopy.person.id =:personId"),
        @NamedQuery(name = "PollForm.countBySubject", query = "select count(distinct pollCopy.subject) from PollCopy pollCopy where " +
                "pollCopy.subject is not null and pollCopy.pollForm.id =:pollFormId and " +
                "pollCopy.person is not null and pollCopy.person.id =:personId"),
        @NamedQuery(name = "PollForm.countByPollCopy", query = "select count(pollCopy) from PollCopy pollCopy where " +
                "pollCopy.pollForm.id =:pollFormId and pollCopy.person is not null and pollCopy.person.id =:personId"),
        @NamedQuery(name = "PollForm.avgBySection", query = "select avg(pollPunctuation.evaluationCriteriaValue.value) " +
                "from PollCopy pollCopy Left Join pollCopy.pollPunctuationList pollPunctuation " +
                "where  pollCopy.pollForm.id =:pollFormId and pollCopy.person is not null and pollCopy.person.id =:personId " +
                " and pollPunctuation.question.section.id=:sectionId"),
        @NamedQuery(name = "PollForm.countAssertQuestionEvaluationCriteriaValueByPerson", query = "select count(pollPunctuation) " +
                "from PollPunctuation pollPunctuation " +
                "where pollPunctuation.pollCopy.pollForm.id =:pollFormId and pollPunctuation.pollCopy.person is not null and pollPunctuation.pollCopy.person.id =:personId " +
                " and pollPunctuation.question.id =:questionId and pollPunctuation.evaluationCriteriaValue.id =:evaluationCriteriaValueId" +
                " and (pollPunctuation.pollCopy.faculty is null or pollPunctuation.pollCopy.faculty.id =:facultyId) " +
                " and (pollPunctuation.pollCopy.career is null or pollPunctuation.pollCopy.career.id =:careerId)"),
        @NamedQuery(name = "PollForm.countPollCopyByPersonFacultyCareer", query = "select count(pollCopy) from PollCopy pollCopy where " +
                "pollCopy.pollForm.id =:pollFormId and pollCopy.person is not null and pollCopy.person.id =:personId" +
                " and (pollCopy.faculty is null or pollCopy.faculty.id =:facultyId)" +
                " and (pollCopy.career is null or pollCopy.career.id =:careerId)"),
        @NamedQuery(name = "PollForm.findByCycle", query = "SELECT pollForm FROM PollForm pollForm" +
                " WHERE pollForm.cycle =:cycle")

})

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PollForm.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "formularioencuesta",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "formularioencuesta", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idcompania", "titulo"}),
        @UniqueConstraint(columnNames = {"idcompania", "codigo"})
})
public class PollForm implements BaseModel {

    @Id
    @Column(name = "idformularioencuesta", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PollForm.tableGenerator")
    private Long id;

    @Column(name = "codigo", length = 100, nullable = false)
    private String code;

    @Column(name = "titulo", length = 250, nullable = false)
    private String title;

    @Column(name = "subtitulo", length = 250, nullable = false)
    private String subTitle;

    @Column(name = "descripcion", nullable = false)
    @Lob
    private String description;

    @Column(name = "fechaaprobacion", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date approvalDate = new Date();

    @Column(name = "revision", nullable = false)
    private Integer review;

    @Column(name = "tipoagrupacion", length = 30, nullable = true)
    @Enumerated(EnumType.STRING)
    private PollFormGrouppingType pollFormGrouppingType;

    @Column(name = "restricagrupa", length = 30, nullable = true)
    @Enumerated(EnumType.STRING)
    private FieldRestrictionType gruppingRestriction;

    @Column(name = "restricciclo", length = 30, nullable = true)
    @Enumerated(EnumType.STRING)
    private FieldRestrictionType cycleRestriction;

    @Column(name = "restricperiodaca", length = 30, nullable = true)
    @Enumerated(EnumType.STRING)
    private FieldRestrictionType academicPeriodRestriction;

    @Column(name = "restricpersona", length = 30, nullable = true)
    @Enumerated(EnumType.STRING)
    private FieldRestrictionType personRestriction;

    @OneToMany(mappedBy = "pollForm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("sequence asc")
    private List<Section> sectionList = new ArrayList<Section>(0);

    @OneToMany(mappedBy = "pollForm", fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("revisionDate asc,revisionNumber asc")
    private List<PollCopy> pollCopyList = new ArrayList<PollCopy>(0);

    @Column(name = "porcentajeequiva", nullable = false)
    private Integer equivalentPercent;

    @Column(name = "porcentajemuestra", nullable = false)
    private Integer samplePercent;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "idciclo", nullable = false)
    private Cycle cycle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Section> getSectionList() {
        return sectionList;
    }

    public void setSectionList(List<Section> sectionList) {
        this.sectionList = sectionList;
    }

    public List<PollCopy> getPollCopyList() {
        return pollCopyList;
    }

    public void setPollCopyList(List<PollCopy> pollCopyList) {
        this.pollCopyList = pollCopyList;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public PollFormGrouppingType getPollFormGrouppingType() {
        return pollFormGrouppingType;
    }

    public void setPollFormGrouppingType(PollFormGrouppingType pollFormGrouppingType) {
        this.pollFormGrouppingType = pollFormGrouppingType;
    }

    public FieldRestrictionType getGruppingRestriction() {
        return gruppingRestriction;
    }

    public void setGruppingRestriction(FieldRestrictionType gruppingRestriction) {
        this.gruppingRestriction = gruppingRestriction;
    }

    public FieldRestrictionType getCycleRestriction() {
        return cycleRestriction;
    }

    public void setCycleRestriction(FieldRestrictionType cycleRestriction) {
        this.cycleRestriction = cycleRestriction;
    }

    public FieldRestrictionType getAcademicPeriodRestriction() {
        return academicPeriodRestriction;
    }

    public void setAcademicPeriodRestriction(FieldRestrictionType academicPeriodRestriction) {
        this.academicPeriodRestriction = academicPeriodRestriction;
    }

    public FieldRestrictionType getPersonRestriction() {
        return personRestriction;
    }

    public void setPersonRestriction(FieldRestrictionType personRestriction) {
        this.personRestriction = personRestriction;
    }

    public Integer getEquivalentPercent() {
        return equivalentPercent;
    }

    public void setEquivalentPercent(Integer equivalentPercent) {
        this.equivalentPercent = equivalentPercent;
    }

    public Cycle getCycle() {
        return cycle;
    }

    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Integer getReview() {
        return review;
    }

    public void setReview(Integer review) {
        this.review = review;
    }

    public Integer getSamplePercent() {
        return samplePercent;
    }

    public void setSamplePercent(Integer samplePercent) {
        this.samplePercent = samplePercent;
    }

    @Override
    public String toString() {
        return "PollForm{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", description='" + description + '\'' +
                ", approvalDate=" + approvalDate +
                ", review=" + review +
                ", pollFormGrouppingType=" + pollFormGrouppingType +
                ", gruppingRestriction=" + gruppingRestriction +
                ", cycleRestriction=" + cycleRestriction +
                ", academicPeriodRestriction=" + academicPeriodRestriction +
                ", personRestriction=" + personRestriction +
                ", equivalentPercent=" + equivalentPercent +
                ", samplePercent=" + samplePercent +
                ", version=" + version +
                ", cycle=" + cycle +
                '}';
    }
}
