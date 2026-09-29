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
        /* Se busca por tipo y no todo junto: cuando un prestamo y la pertenencia base cubren
           el mismo dia, gana el prestamo, y eso tiene que ser explicito y no depender del orden
           en que la base devuelva las filas. */
        @NamedQuery(name = "WorkGroupMembership.findByContractAndDate",
                query = "select o from WorkGroupMembership o"
                        + " where o.contract = :contract and o.type = :type"
                        + " and o.startDate <= :date"
                        + " and (o.endDate is null or o.endDate >= :date)"),
        /* Contratos vigentes en una fecha que ese dia no estaban en ningun grupo. Es la lista
           que se ofrece para la asignacion masiva: sumar a alguien que ya esta en otro grupo
           seria un error, y filtrarlo antes evita que el usuario lo descubra de a uno. */
        @NamedQuery(name = "WorkGroupMembership.findContractsWithoutGroup",
                query = "select c from Contract c"
                        + " where c.activeForPayrollGeneration = :active"
                        + " and c.initDate <= :date"
                        + " and (c.endDate is null or c.endDate >= :date)"
                        + " and not exists ("
                        + "   select m from WorkGroupMembership m"
                        + "   where m.contract = c and m.startDate <= :date"
                        + "   and (m.endDate is null or m.endDate >= :date))"
                        + " order by c.employee.lastName asc, c.employee.firstName asc"),
        @NamedQuery(name = "WorkGroupMembership.findContractsForMove",
                query = "select c from Contract c"
                        + " where c.activeForPayrollGeneration = :active"
                        + " and c.initDate <= :date"
                        + " and (c.endDate is null or c.endDate >= :date)"
                        + " order by c.employee.lastName asc, c.employee.firstName asc"),
        /* Todas las pertenencias base vigentes en una fecha. Se pide de una sola vez y se
           cruza en memoria: preguntar el grupo de cada candidato por separado serian
           doscientas consultas para armar una lista. */
        @NamedQuery(name = "WorkGroupMembership.findBaseAtDate",
                query = "select o from WorkGroupMembership o"
                        + " where o.type = :type and o.startDate <= :date"
                        + " and (o.endDate is null or o.endDate >= :date)"),
        @NamedQuery(name = "WorkGroupMembership.findByGroup",
                query = "select o from WorkGroupMembership o where o.workGroup = :workGroup"
                        + " order by o.startDate desc"),
        /* Solo se comparan pertenencias del MISMO tipo: un prestamo tiene que poder pisar a la
           base -es su razon de ser- pero dos prestamos no pueden pisarse entre si. */
        @NamedQuery(name = "WorkGroupMembership.findOverlapping",
                query = "select o from WorkGroupMembership o"
                        + " where o.contract = :contract and o.id <> :id and o.type = :type"
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

    /** Nula mientras la persona siga en el grupo. Obligatoria en un prestamo. */
    @Column(name = "fechafin")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "tipo", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    @NotNull
    private WorkGroupMembershipType type = WorkGroupMembershipType.BASE;

    /** Por que se presto. Vacio en la pertenencia base. */
    @Column(name = "motivo", length = 250)
    @Length(max = 250)
    private String reason;

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

    @Transient
    public boolean isLoan() {
        return WorkGroupMembershipType.LOAN.equals(type);
    }

    public WorkGroupMembershipType getType() {
        return type;
    }

    public void setType(WorkGroupMembershipType type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
