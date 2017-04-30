package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;

import javax.persistence.*;
import java.util.Date;

/**
 * Entity for HoraryBand
 *
 * @author
 */

@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "HoraryBand.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "bandahoraria",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries(
        {
                @NamedQuery(name = "HoraryBand.findAll", query = "select o from HoraryBand o ")
        }
)
@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "bandahoraria")
public class HoraryBand implements BaseModel {

    @Id
    @Column(name = "idbandahoraria", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "HoraryBand.tableGenerator")
    private Long id;

    @Temporal(TemporalType.TIME)
    @Column(name = "horainicio", nullable = false)
    private Date initHour;

    @Temporal(TemporalType.TIME)
    @Column(name = "horafin")
    private Date endHour;

    @Column(name = "TIPO", nullable = true)
    private String type;

    @Column(name = "diainicio", nullable = false, length = 200)
    private String initDay;

    @Column(name = "diafin", nullable = false, length = 200)
    @Length(max = 200)
    private String endDay;

    @Column(name = "diapormedio")
    private Integer everyOtherDay = 0;

    @Column(name = "duracion")
    private Integer duration;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "IDTIPOBANDAHORARIA", nullable = true, updatable = false, insertable = true)
    private TypeHoraryBand typeHoraryBand;

    public HoraryBand() {

    }

    public HoraryBand(HoraryBand instance) {
        setInitDay(instance.getInitDay());
        setEndDay(instance.getEndDay());
        setInitHour(instance.getInitHour());
        setEndHour(instance.getEndHour());
        setDuration(instance.getDuration());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getInitHour() {
        return initHour;
    }

    public void setInitHour(Date initHour) {
        this.initHour = initHour;
    }

    public Date getEndHour() {
        return endHour;
    }

    public void setEndHour(Date endHour) {
        this.endHour = endHour;
    }

    public String getInitDay() {
        return initDay;
    }

    public void setInitDay(String initDay) {
        this.initDay = initDay;
    }

    public String getEndDay() {
        return endDay;
    }

    public void setEndDay(String endDay) {
        this.endDay = endDay;
    }

    public Integer getEveryOtherDay() {
        return everyOtherDay;
    }

    public void setEveryOtherDay(Integer everyOtherDay) {
        this.everyOtherDay = everyOtherDay;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TypeHoraryBand getTypeHoraryBand() {
        return typeHoraryBand;
    }

    public void setTypeHoraryBand(TypeHoraryBand typeHoraryBand) {
        this.typeHoraryBand = typeHoraryBand;
    }
}