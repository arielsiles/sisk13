package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Movimiento del saldo de vacaciones.
 * <p/>
 * El saldo de un empleado es la suma de sus movimientos hasta una fecha. Antes se guardaban
 * contadores que un recalculo pisaba con el valor "a hoy", asi que el saldo de un mes cerrado
 * era irrecuperable y no habia forma de explicar por que el saldo valia lo que valia. El libro
 * no se pisa: es la misma idea del kardex de almacen.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "VacationMovement.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "movimientovacacion",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "VacationMovement.balanceAtDate",
                query = "select sum(o.days) from VacationMovement o"
                        + " where o.vacationPlanning = :vacationPlanning and o.date <= :date"),
        @NamedQuery(name = "VacationMovement.balance",
                query = "select sum(o.days) from VacationMovement o"
                        + " where o.vacationPlanning = :vacationPlanning"),
        @NamedQuery(name = "VacationMovement.findByVacationPlanning",
                query = "select o from VacationMovement o"
                        + " where o.vacationPlanning = :vacationPlanning"
                        + " order by o.date asc, o.id asc"),
        @NamedQuery(name = "VacationMovement.findByVacation",
                query = "select o from VacationMovement o where o.vacation = :vacation order by o.id asc"),
        @NamedQuery(name = "VacationMovement.findAccrualUpTo",
                query = "select o from VacationMovement o"
                        + " where o.vacationPlanning = :vacationPlanning and o.type = :type"
                        + " and o.date <= :date"),
        @NamedQuery(name = "VacationMovement.sumByType",
                query = "select sum(o.days) from VacationMovement o"
                        + " where o.vacationPlanning = :vacationPlanning and o.type = :type"),
        @NamedQuery(name = "VacationMovement.countByType",
                query = "select count(o) from VacationMovement o"
                        + " where o.vacationPlanning = :vacationPlanning and o.type = :type"),
        @NamedQuery(name = "VacationMovement.countAccrualByGestion",
                query = "select count(o) from VacationMovement o"
                        + " where o.vacationPlanning = :vacationPlanning and o.gestion = :gestion"
                        + " and o.type = :type"),
        @NamedQuery(name = "VacationMovement.sumTakingByVacationPlanning",
                query = "select sum(o.days) from VacationMovement o"
                        + " where o.vacationPlanning = :vacationPlanning and o.type <> :type")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "movimientovacacion")
public class VacationMovement implements BaseModel {

    @Id
    @Column(name = "idmovimientovacacion", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "VacationMovement.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idplanvacacion", nullable = false)
    @NotNull
    private VacationPlanning vacationPlanning;

    @Column(name = "tipo", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    @NotNull
    private VacationMovementType type;

    /* Fecha con la que el movimiento entra al saldo: el aniversario si es un devengo, el
       inicio de la vacacion si es una toma. Es la que permite pedir el saldo a una fecha. */
    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date date;

    /* Con signo: el devengo suma, la toma resta. El saldo es la suma de la columna. */
    @Column(name = "dias", nullable = false, precision = 7, scale = 2)
    @NotNull
    private BigDecimal days;

    /* Gestion de la que provienen los dias devengados. Sirve para saber de que anio viene el
       saldo, no para limitar el consumo: los dias no caducan y se acumulan. */
    @Column(name = "gestion")
    private Integer gestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idvacacion")
    private Vacation vacation;

    @Column(name = "descripcion", length = 250)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", updatable = false)
    private User user;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

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

    public VacationPlanning getVacationPlanning() {
        return vacationPlanning;
    }

    public void setVacationPlanning(VacationPlanning vacationPlanning) {
        this.vacationPlanning = vacationPlanning;
    }

    public VacationMovementType getType() {
        return type;
    }

    public void setType(VacationMovementType type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getDays() {
        return days;
    }

    public void setDays(BigDecimal days) {
        this.days = days;
    }

    public Integer getGestion() {
        return gestion;
    }

    public void setGestion(Integer gestion) {
        this.gestion = gestion;
    }

    public Vacation getVacation() {
        return vacation;
    }

    public void setVacation(Vacation vacation) {
        this.vacation = vacation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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
        return "VacationMovement{id=" + id + ", type=" + type + ", days=" + days + "}";
    }
}
