package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Filter;

import javax.persistence.*;

/**
 * Encens Team
 *
 * @author
 * @version : Question, 21-10-2009 12:07:57 PM
 */
@NamedQueries(
        {
                @NamedQuery(name = "Question.readContent", query = "SELECT question.content FROM Question question WHERE question.id =:questionId"),
                @NamedQuery(name = "Question.findByPollForm", query = "SELECT question FROM Question question " +
                        " left join fetch question.section section" +
                        " left join section.pollForm pollForm" +
                        " where pollForm=:pollForm" +
                        " order by section.sequence,question.sequence")
        })

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Question.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "pregunta",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "pregunta", uniqueConstraints = @UniqueConstraint(columnNames = {"idseccion", "indice"}))
public class Question implements BaseModel {

    @Id
    @Column(name = "idpregunta", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Question.tableGenerator")
    private Long id;

    @Column(name = "indice", nullable = false)
    private Integer sequence;

    @Column(name = "contenido", nullable = false)
    @Lob
    private String content;

    @ManyToOne
    @JoinColumn(name = "idseccion", nullable = false)
    private Section section;

    @ManyToOne
    @JoinColumn(name = "idcriterioevaluacion", nullable = false)
    private EvaluationCriteria evaluationCriteria;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }

    public EvaluationCriteria getEvaluationCriteria() {
        return evaluationCriteria;
    }

    public void setEvaluationCriteria(EvaluationCriteria evaluationCriteria) {
        this.evaluationCriteria = evaluationCriteria;
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

    public String getFullName() {
        return FormatUtils.toTitle(getSequence(), getContent());
    }
}
