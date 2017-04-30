package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.model.contacts.Person;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : PollCopy, 26-10-2009 08:39:27 PM
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "PollCopy.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "ejemplarencuesta",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "PollCopy.maxRevisionNumber",
                query = "select max(pollCopy.revisionNumber) from PollCopy pollCopy where pollCopy.pollForm =:pollForm"),
        @NamedQuery(name = "PollCopy.countByEvaluatorAndPollForm",
                query = "select count(pollCopy.id) from PollCopy pollCopy where pollCopy.pollForm =:pollForm and pollCopy.evaluator =:evaluator")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "ejemplarencuesta")
public class PollCopy implements BaseModel {
    @Id
    @Column(name = "idejemplarencuesta", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "PollCopy.tableGenerator")
    private Long id;

    @Column(name = "fecharevision", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date revisionDate = new Date();

    @Column(name = "numerorevision", nullable = false)
    private Integer revisionNumber;

    @ManyToOne
    @JoinColumn(name = "idciclo", nullable = true)
    private Cycle cycle;

    @ManyToOne
    @JoinColumn(name = "idperiodocad", nullable = true)
    private AcademicPeriod academicPeriod;

    @ManyToOne
    @JoinColumn(name = "idfacultad", nullable = true)
    private Faculty faculty;

    @ManyToOne
    @JoinColumn(name = "idcarrera", nullable = true)
    private Career career;

    @ManyToOne
    @JoinColumn(name = "idasignatura", nullable = true)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "idevaluador")
    private Person evaluator;

    @ManyToOne
    @JoinColumn(name = "idpersona", nullable = true)
    private Person person;

    @ManyToOne
    @JoinColumn(name = "idformularioencuesta", nullable = false)
    private PollForm pollForm;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idcomentario", nullable = true)
    private Text comment;

    @OneToMany(mappedBy = "pollCopy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    private List<PollPunctuation> pollPunctuationList = new ArrayList<PollPunctuation>(0);

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public PollCopy() {

    }

    public PollCopy(PollCopy pollCopy) {
        setCycle(pollCopy.getCycle());
        setAcademicPeriod(pollCopy.getAcademicPeriod());
        setFaculty(pollCopy.getFaculty());
        setCareer(pollCopy.getCareer());
        setSubject(pollCopy.getSubject());
        setPerson(pollCopy.getPerson());
        setPollForm(pollCopy.getPollForm());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    public Integer getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Integer revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public Cycle getCycle() {
        return cycle;
    }

    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    public AcademicPeriod getAcademicPeriod() {
        return academicPeriod;
    }

    public void setAcademicPeriod(AcademicPeriod academicPeriod) {
        this.academicPeriod = academicPeriod;
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

    public Person getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(Person evaluator) {
        this.evaluator = evaluator;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public PollForm getPollForm() {
        return pollForm;
    }

    public void setPollForm(PollForm pollForm) {
        this.pollForm = pollForm;
    }

    public Text getComment() {
        return comment;
    }

    public void setComment(Text comment) {
        this.comment = comment;
    }

    public List<PollPunctuation> getPollPunctuationList() {
        return pollPunctuationList;
    }

    public void setPollPunctuationList(List<PollPunctuation> pollPunctuationList) {
        this.pollPunctuationList = pollPunctuationList;
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
}
