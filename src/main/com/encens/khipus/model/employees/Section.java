package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.FormatUtils;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Encens Team
 *
 * @author
 * @version : Section, 21-10-2009 11:50:17 AM
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "Section.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "seccion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "seccion", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"idformularioencuesta", "indice"}),
        @UniqueConstraint(columnNames = {"idformularioencuesta", "titulo"})
})
public class Section implements BaseModel {

    @Id
    @Column(name = "idseccion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "Section.tableGenerator")
    private Long id;

    @Column(name = "indice", nullable = false)
    private Integer sequence;

    @Column(name = "titulo", length = 250, nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "idformularioencuesta", nullable = false)
    private PollForm pollForm;

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
    @OrderBy("sequence asc")
    private List<Question> questionList = new ArrayList<Question>(0);

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public PollForm getPollForm() {
        return pollForm;
    }

    public void setPollForm(PollForm pollForm) {
        this.pollForm = pollForm;
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

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }

    public Integer getQuestionListSize() {
        return getQuestionList().size();
    }

    public String getFullName() {
        return FormatUtils.toTitle(getSequence(), getTitle());
    }
}
