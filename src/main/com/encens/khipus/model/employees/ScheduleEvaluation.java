package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.util.Constants;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ScheduleEvaluation
 *
 * @author
 * @version 2.24
 */

@NamedQueries({
        @NamedQuery(name = "ScheduleEvaluation.countByName",
                query = "select count(se.id) from ScheduleEvaluation se where upper(se.name)=upper(:name)"),
        @NamedQuery(name = "ScheduleEvaluation.countByNameAndScheduleEvaluation",
                query = "select count(se.id) from ScheduleEvaluation se where upper(se.name)=upper(:name) and se.id<>:scheduleEvaluationId"),
        @NamedQuery(name = "ScheduleEvaluation.countByCycle",
                query = "select count(se.id) from ScheduleEvaluation se where se.cycle.id=:cycleId"),
        @NamedQuery(name = "ScheduleEvaluation.countByCycleAndScheduleEvaluation",
                query = "select count(se.id) from ScheduleEvaluation se where se.cycle.id=:cycleId and se.id<>:scheduleEvaluationId")
})

@TableGenerator(schema = Constants.KHIPUS_SCHEMA,
        name = "ScheduleEvaluation.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "evaluacionprog",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(name = "evaluacionprog", schema = Constants.KHIPUS_SCHEMA)
public class ScheduleEvaluation implements BaseModel {

    @Id
    @Column(name = "idevaluacionprog", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ScheduleEvaluation.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 250)
    @NotNull
    @Length(max = 250)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idciclo", nullable = false)
    private Cycle cycle;

    @Column(name = "estado", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ScheduleEvaluationState state;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @OneToMany(mappedBy = "scheduleEvaluation", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ScheduleEvaluationForm> scheduleEvaluationFormList = new ArrayList<ScheduleEvaluationForm>(0);

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cycle getCycle() {
        return cycle;
    }

    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    public ScheduleEvaluationState getState() {
        return state;
    }

    public void setState(ScheduleEvaluationState state) {
        this.state = state;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<ScheduleEvaluationForm> getScheduleEvaluationFormList() {
        return scheduleEvaluationFormList;
    }

    public void setScheduleEvaluationFormList(List<ScheduleEvaluationForm> scheduleEvaluationFormList) {
        this.scheduleEvaluationFormList = scheduleEvaluationFormList;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ScheduleEvaluation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cycle=" + cycle +
                ", state=" + state +
                ", company=" + company +
                ", scheduleEvaluationFormList=" + scheduleEvaluationFormList +
                ", version=" + version +
                '}';
    }
}
