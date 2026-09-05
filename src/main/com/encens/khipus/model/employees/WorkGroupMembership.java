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
 * La pertenencia de una persona a un grupo, con vigencia.
 * <p/>
 * Lleva fechas porque la gente se mueve entre grupos segun la planificacion, y porque para
 * evaluar la asistencia de un dia pasado hay que saber en que grupo estaba <b>ese dia</b>, no
 * en cual esta hoy. Sin vigencia, mover a alguien de grupo reescribiria su historia: sus dias
 * anteriores pasarian a evaluarse contra el cronograma del grupo nuevo.
 * <p/>
 * `fechafin` nula significa que sigue en el grupo.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "WorkGroupMembership.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "grupotrabajomiembro",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "WorkGroupMembership.findByContractAndDate",
                query = "select o from WorkGroupMembership o"
                        + " where o.contract = :contract"
                        + " and o.startDate <= :date"
                        + " and (o.endDate is null or o.endDate >= :date)"),
        @NamedQuery(name = "WorkGroupMembership.findByGroup",
                query = "select o from WorkGroupMembership o where o.workGroup = :workGroup"
                        + " order by o.startDate desc"),
        @NamedQuery(name = "WorkGroupMembership.findOverlapping",
                query = "select o from WorkGroupMembership o"
                        + " where o.contract = :contract and o.id <> :id"
                        + " and (o.endDate is null or o.endDate >= :startDate)"
                        + " and (:endDate is null or o.startDate <= :endDate)")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "grupotrabajomiembro")
public class WorkGroupMembership implements BaseModel {

    @Id
    @Column(name = "idgrupotrabajomiembro", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "WorkGroupMembership.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idgrupotrabajo", nullable = false)
    @NotNull
    private WorkGroup workGroup;

    /* Cuelga del contrato y no del empleado: es el contrato el que tiene la relacion laboral,
       y es contra el contrato que se genera la planilla. */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontrato", nullable = false)
    @NotNull
    private Contract contract;

    @Column(name = "fechainicio", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date startDate;

    /** Nula mientras la persona siga en el grupo. */
    @Column(name = "fechafin")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Transient
    public boolean isCurrent() {
        return null == endDate;
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

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
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
        return "WorkGroupMembership{group=" + workGroup + ", desde=" + startDate + "}";
    }
}
