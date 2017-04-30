package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;

import javax.persistence.*;
import java.util.Date;

/**
 * ScheduleEvaluationForm
 *
 * @author
 * @version 2.24
 */

@NamedQueries({
        @NamedQuery(name = "ScheduleEvaluationForm.findPollFormByTypeGestionAndPeriod",
                query = "select sef.pollForm " +
                        " from ScheduleEvaluationForm sef" +
                        " left join sef.scheduleEvaluation scheduleEvaluation" +
                        " left join scheduleEvaluation.cycle cycle" +
                        " left join cycle.gestion gestion" +
                        " left join cycle.cycleType cycleType" +
                        " where sef.type=:type and gestion.year=:year and cycleType.period=:period" +
                        " and scheduleEvaluation.state=:state and sef.startDateTime<=:dateTime and sef.endDateTime>=:dateTime")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "ScheduleEvaluationForm.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "formevaluacionprog",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "formevaluacionprog", schema = Constants.KHIPUS_SCHEMA)
public class ScheduleEvaluationForm implements BaseModel {

    @Id
    @Column(name = "idformevaluacionprog", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ScheduleEvaluationForm.tableGenerator")
    private Long id;

    @Column(name = "fechainicio", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDateTime;

    @Column(name = "fechafin", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDateTime;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idformularioencuesta", nullable = false)
    private PollForm pollForm;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idevaluacionprog", nullable = false)
    private ScheduleEvaluation scheduleEvaluation;

    @Column(name = "muestra", nullable = false)
    private Integer samplePopulation;

    @Column(name = "tipo", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private PollFormType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public ScheduleEvaluationForm() {
    }

    public ScheduleEvaluationForm(Date startDateTime, Date endDateTime, PollFormType type) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public PollForm getPollForm() {
        return pollForm;
    }

    public void setPollForm(PollForm pollForm) {
        this.pollForm = pollForm;
    }

    public ScheduleEvaluation getScheduleEvaluation() {
        return scheduleEvaluation;
    }

    public void setScheduleEvaluation(ScheduleEvaluation scheduleEvaluation) {
        this.scheduleEvaluation = scheduleEvaluation;
    }

    public Integer getSamplePopulation() {
        return samplePopulation;
    }

    public void setSamplePopulation(Integer samplePopulation) {
        this.samplePopulation = samplePopulation;
    }

    public PollFormType getType() {
        return type;
    }

    public void setType(PollFormType type) {
        this.type = type;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "\n\tScheduleEvaluationForm{" +
                "id=" + id +
                ", startDateTime=" + startDateTime +
                ", endDateTime=" + endDateTime +
                ", samplePopulation=" + samplePopulation +
                ", type=" + type +
                ", company=" + company +
                ", version=" + version +
                ", pollForm=" + pollForm +
                '}';
    }
}
