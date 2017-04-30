package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.common.Text;
import com.encens.khipus.util.Constants;
import com.encens.khipus.util.TextUtil;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * FinalEvaluationForm
 *
 * @author
 * @version 2.7
 */

@NamedQueries({
        @NamedQuery(name = "FinalEvaluationForm.findByCycleType", query = "SELECT finalForm FROM FinalEvaluationForm finalForm" +
                " WHERE finalForm.cycle =:cycle AND finalForm.type =:finalEvaluationFormType"),
        @NamedQuery(name = "FinalEvaluationForm.countByCycleType", query = "SELECT count(finalForm) " +
                " FROM FinalEvaluationForm finalForm" +
                " WHERE finalForm.cycle =:cycle AND finalForm.type =:type"),
        @NamedQuery(name = "FinalEvaluationForm.countByCycleTypeAndFinalForm", query = "SELECT count(finalForm) " +
                " FROM FinalEvaluationForm finalForm" +
                " WHERE finalForm<>:finalForm and " +
                " finalForm.cycle =:cycle AND finalForm.type =:type")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "FinalEvaluationForm.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "formevalfinal",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = Constants.KHIPUS_SCHEMA, name = "formevalfinal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idciclo", "tipo"}))
public class FinalEvaluationForm implements BaseModel {
    @Id
    @Column(name = "idformevalfinal", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "FinalEvaluationForm.tableGenerator")
    private Long id;

    @Column(name = "codigo", length = 50, nullable = false)
    @Length(max = 50)
    private String code;

    @Column(name = "titulo", length = 250, nullable = false)
    @Length(max = 250)
    private String title;

    @Column(name = "subtitulo", length = 250, nullable = false)
    @Length(max = 250)
    private String subtitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idciclo", nullable = false)
    private Cycle cycle;

    @Column(name = "fechaaprob", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date approvalDate;

    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private FinalEvaluationFormType type;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idobjetivo", nullable = false)
    private Text target;

    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "idmetodologia", nullable = true)
    private Text methodology;

    @OneToMany(mappedBy = "finalEvaluationForm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FinalEvaluationPunctuationRange> finalEvaluationPunctuationRangeList = new ArrayList<FinalEvaluationPunctuationRange>(0);

    @Column(name = "revision", nullable = false)
    private Integer revision;

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

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
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

    public FinalEvaluationFormType getType() {
        return type;
    }

    public void setType(FinalEvaluationFormType type) {
        this.type = type;
    }

    public Text getTarget() {
        return target;
    }

    public void setTarget(Text target) {
        this.target = target;
    }

    public Text getMethodology() {
        return methodology;
    }

    public void setMethodology(Text methodology) {
        this.methodology = methodology;
    }

    public String getTargetValue() {
        return TextUtil.getTextValue(getTarget());
    }

    public void setTargetValue(String value) {
        this.target = TextUtil.createOrUpdate(this.target, value);
    }

    public String getMethodologyValue() {
        return TextUtil.getTextValue(getMethodology());
    }

    public void setMethodologyValue(String value) {
        this.methodology = TextUtil.createOrUpdate(this.methodology, value);
    }

    public List<FinalEvaluationPunctuationRange> getFinalEvaluationPunctuationRangeList() {
        return finalEvaluationPunctuationRangeList;
    }

    public void setFinalEvaluationPunctuationRangeList(List<FinalEvaluationPunctuationRange> finalEvaluationPunctuationRangeList) {
        this.finalEvaluationPunctuationRangeList = finalEvaluationPunctuationRangeList;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
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

    @Override
    public String toString() {
        return "FinalEvaluationForm{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", cycle=" + cycle +
                ", approvalDate=" + approvalDate +
                ", type=" + type +
                ", target=" + target +
                ", methodology=" + methodology +
                ", revision=" + revision +
                ", version=" + version +
                ", company=" + company +
                '}';
    }
}
