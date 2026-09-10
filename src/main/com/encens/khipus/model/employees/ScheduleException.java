package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.Contract;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * Una excepcion: a esta persona, este dia, le tocaba otra cosa.
 * <p/>
 * Es la capa 1, la que gana sobre todas las demas. Existe porque en la practica siempre hay un
 * dia suelto -alguien cubre un turno ajeno, entra mas tarde por un permiso, viene un sabado a
 * cerrar un pedido- y sin esto la unica salida seria tocar el cronograma del grupo entero, que
 * afectaria a todos sus companeros.
 * <p/>
 * Turno nulo significa que ese dia no se le evalua asistencia. El motivo es obligatorio: una
 * excepcion sin explicacion es indefendible frente a un reclamo.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "ScheduleException.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "excepcionhorario",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "ScheduleException.findByContractAndDate",
                query = "select o from ScheduleException o"
                        + " where o.contract = :contract and o.date = :date"),
        @NamedQuery(name = "ScheduleException.findByContractAndRange",
                query = "select o from ScheduleException o"
                        + " where o.contract = :contract"
                        + " and o.date >= :from and o.date <= :to"
                        + " order by o.date asc"),
        @NamedQuery(name = "ScheduleException.countByWorkShift",
                query = "select count(o) from ScheduleException o where o.workShift = :workShift")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "excepcionhorario",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"idcontrato", "fecha"})})
public class ScheduleException implements BaseModel {

    @Id
    @Column(name = "idexcepcionhorario", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ScheduleException.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontrato", nullable = false)
    @NotNull
    private Contract contract;

    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date date;

    /** Nulo: ese dia no se le evalua asistencia. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idturno")
    private WorkShift workShift;

    @Column(name = "motivo", nullable = false, length = 250)
    @NotNull
    @Length(max = 250)
    private String reason;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShift workShift) {
        this.workShift = workShift;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
        return "ScheduleException{" + date + ", turno=" + workShift + "}";
    }
}
