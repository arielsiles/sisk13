package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.finances.Contract;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Date;

/**
 * El horario fijo de un contrato: que turno le toca cada dia de la semana.
 * <p/>
 * Es la capa 3 de la resolucion, la de administrativos y mantenimiento: gente que no rota y
 * cuya semana es siempre igual. Se guarda por dia de la semana y no por fecha porque no hay
 * nada que planificar: el lunes es el lunes todo el ano.
 * <p/>
 * Lleva vigencia por la misma razon que la pertenencia a un grupo: si alguien cambia de horario
 * en marzo, los dias de febrero se tienen que seguir evaluando contra el horario que tenia
 * entonces. Cambiar el horario cierra el anterior, no lo pisa.
 * <p/>
 * Turno nulo significa que ese dia de la semana no se trabaja: es como se modela el fin de
 * semana de un administrativo.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "ContractWorkShift.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "horariocontrato",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        /* El horario vigente de un contrato en una fecha. La vigencia abierta -fechafin nula-
           se toma como vigente. */
        @NamedQuery(name = "ContractWorkShift.findByContractAndDate",
                query = "select o from ContractWorkShift o"
                        + " where o.contract = :contract"
                        + " and o.dayOfWeek = :dayOfWeek"
                        + " and o.startDate <= :date"
                        + " and (o.endDate is null or o.endDate >= :date)"),
        @NamedQuery(name = "ContractWorkShift.findCurrentByContract",
                query = "select o from ContractWorkShift o"
                        + " where o.contract = :contract and o.endDate is null"
                        + " order by o.dayOfWeek asc"),
        /* Los que ALCANZAN un periodo, no solo los abiertos. Resolver un mes con los abiertos
           hacia desaparecer la jornada de quien despues cambio de horario: al regenerar un mes
           viejo, sus dias quedaban sin jornada y por lo tanto sin falta ni atraso. */
        @NamedQuery(name = "ContractWorkShift.findByContractAndPeriod",
                query = "select o from ContractWorkShift o"
                        + " where o.contract = :contract"
                        + " and o.startDate <= :to"
                        + " and (o.endDate is null or o.endDate >= :from)"
                        + " order by o.dayOfWeek asc, o.startDate asc"),
        @NamedQuery(name = "ContractWorkShift.countByWorkShift",
                query = "select count(o) from ContractWorkShift o where o.workShift = :workShift")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "horariocontrato")
public class ContractWorkShift implements BaseModel {

    @Id
    @Column(name = "idhorariocontrato", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ContractWorkShift.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontrato", nullable = false)
    @NotNull
    private Contract contract;

    /** Constantes de Calendar.DAY_OF_WEEK: 1 domingo ... 7 sabado. */
    @Column(name = "diasemana", nullable = false)
    @NotNull
    private Integer dayOfWeek;

    /** Nulo: ese dia de la semana no se trabaja. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idturno")
    private WorkShift workShift;

    @Column(name = "fechainicio", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date startDate;

    @Column(name = "fechafin")
    @Temporal(TemporalType.DATE)
    private Date endDate;

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

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShift workShift) {
        this.workShift = workShift;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
        return "ContractWorkShift{dia=" + dayOfWeek + ", turno=" + workShift + "}";
    }
}
