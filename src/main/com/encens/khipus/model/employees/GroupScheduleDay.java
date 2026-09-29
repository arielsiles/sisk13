package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Un dia del cronograma de un grupo: que turno le toca, o ninguno.
 * <p/>
 * Una celda de la grilla que hoy se pinta en la planilla. Cuando el turno es nulo, ese dia no
 * hay jornada -descanso, feriado, o el grupo todavia no entraba- y el motor no evalua nada.
 * Confirmado con el usuario: la distincion entre descanso y feriado existe solo en el reporte
 * manual de las 240 horas, no cambia el calculo, y por eso no se modela.
 * <p/>
 * El estado vive en el dia y no en una cabecera de semana porque asi una semana a medio
 * publicar es imposible: cada dia sabe si esta listo. El planificador publica una semana y se
 * marcan sus siete dias.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "GroupScheduleDay.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "cronogramagrupodia",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "GroupScheduleDay.findByGroupAndRange",
                query = "select o from GroupScheduleDay o"
                        + " where o.workGroup = :workGroup"
                        + " and o.day >= :from and o.day <= :to"
                        + " order by o.day asc"),
        @NamedQuery(name = "GroupScheduleDay.findByGroupAndDay",
                query = "select o from GroupScheduleDay o"
                        + " where o.workGroup = :workGroup and o.day = :day"),
        @NamedQuery(name = "GroupScheduleDay.findPublishedByGroupAndRange",
                query = "select o from GroupScheduleDay o"
                        + " where o.workGroup = :workGroup"
                        + " and o.day >= :from and o.day <= :to"
                        + " and o.state = :state and o.workShift is not null"
                        + " order by o.day asc"),
        @NamedQuery(name = "GroupScheduleDay.countByWorkShift",
                query = "select count(o) from GroupScheduleDay o where o.workShift = :workShift")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "cronogramagrupodia",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"idgrupotrabajo", "dia"})})
public class GroupScheduleDay implements BaseModel {

    @Id
    @Column(name = "idcronogramagrupodia", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "GroupScheduleDay.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idgrupotrabajo", nullable = false)
    @NotNull
    private WorkGroup workGroup;

    @Column(name = "dia", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date day;

    /** Nulo: ese dia no hay jornada. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idturno")
    private WorkShift workShift;

    @Column(name = "estado", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    @NotNull
    private ScheduleState state = ScheduleState.DRAFT;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Transient
    public boolean isWorking() {
        return null != workShift;
    }

    @Transient
    public boolean isPublished() {
        return ScheduleState.PUBLISHED.equals(state);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkGroup getWorkGroup() {
        return workGroup;
    }

    public void setWorkGroup(WorkGroup workGroup) {
        this.workGroup = workGroup;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShift workShift) {
        this.workShift = workShift;
    }

    public ScheduleState getState() {
        return state;
    }

    public void setState(ScheduleState state) {
        this.state = state;
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
        return "GroupScheduleDay{" + day + ", turno=" + workShift + ", " + state + "}";
    }
}
