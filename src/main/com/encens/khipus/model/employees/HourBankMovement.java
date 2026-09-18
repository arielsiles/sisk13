package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.admin.Company;
import com.encens.khipus.model.admin.User;
import com.encens.khipus.model.finances.Contract;
import org.hibernate.annotations.Filter;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Movimiento del banco de horas.
 * <p/>
 * El saldo de una persona es la suma de sus movimientos. No se guarda en ningun campo: con un
 * campo, el dia que alguien corrija un mes viejo el saldo queda mintiendo y nadie se entera. Es
 * la misma idea del libro de vacaciones y del kardex de almacen.
 * <p/>
 * <b>Se guarda en horas</b>, no en dias. Las horas son lo que da el reloj; los dias son una
 * conversion que depende de `jornadasemanal.horasdia` y puede diferir por empresa. Guardar dias
 * obliga a numeros como 0,37, que es lo que aparece en la planilla que RRHH lleva a mano.
 * <p/>
 * Cuelga del <b>contrato</b>, como todo el modulo, y se registra sobre el principal.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "HourBankMovement.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "movimientobancohoras",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "HourBankMovement.balance",
                query = "select sum(o.hours) from HourBankMovement o where o.contract = :contract"),
        @NamedQuery(name = "HourBankMovement.balanceAtDate",
                query = "select sum(o.hours) from HourBankMovement o"
                        + " where o.contract = :contract and o.date <= :date"),
        @NamedQuery(name = "HourBankMovement.findByContract",
                query = "select o from HourBankMovement o where o.contract = :contract"
                        + " order by o.date asc, o.id asc"),
        @NamedQuery(name = "HourBankMovement.findBySpecialDate",
                query = "select o from HourBankMovement o where o.specialDate = :specialDate"
                        + " order by o.id asc"),
        @NamedQuery(name = "HourBankMovement.balancesInRange",
                query = "select o.contract, o.type, sum(o.hours) from HourBankMovement o"
                        + " where o.date >= :from and o.date <= :to"
                        + " group by o.contract, o.type"),
        @NamedQuery(name = "HourBankMovement.sumByTypeInRange",
                query = "select sum(o.hours) from HourBankMovement o"
                        + " where o.contract = :contract and o.type = :type"
                        + " and o.date >= :from and o.date <= :to")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners(CompanyListener.class)
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "movimientobancohoras")
public class HourBankMovement implements BaseModel {

    @Id
    @Column(name = "idmovimientobancohoras", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "HourBankMovement.tableGenerator")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcontrato", nullable = false)
    @NotNull
    private Contract contract;

    @Column(name = "tipo", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    @NotNull
    private HourBankMovementType type;

    /* La fecha con la que el movimiento entra al saldo: el dia del trabajo extra, o el dia en que
       se toma el permiso. Es la que permite pedir el saldo a una fecha y agrupar por mes. */
    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.DATE)
    @NotNull
    private Date date;

    /* Con signo: acumular suma, usar y pagar restan. El saldo es la suma de la columna. */
    @Column(name = "horas", nullable = false, precision = 9, scale = 2)
    @NotNull
    private BigDecimal hours;

    /**
     * El permiso compensatorio que consumio estas horas.
     * <p/>
     * Los dos se crean juntos y se borran juntos. Si quedara la fecha especial sin el movimiento,
     * el dia estaria justificado sin descontar del saldo; al reves, el saldo bajaria por un
     * permiso que no existe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idfechaespecial")
    private SpecialDate specialDate;

    /** El ciclo en el que se pago, cuando el movimiento es un pago. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idciclogeneracionplanilla")
    private PayrollGenerationCycle payrollGenerationCycle;

    /** El importe pagado, cuando el movimiento es un pago. Se guarda para poder revertirlo. */
    @Column(name = "importe", precision = 16, scale = 2)
    private BigDecimal amount;

    @Column(name = "descripcion", length = 250)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuario", updatable = false)
    private User user;

    /* Quien corrigio el movimiento y cuando. Es el rastro que reemplaza al circuito de aprobacion:
       el error de tipeo se corrige, pero la correccion no queda invisible. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idusuariomodificacion")
    private User modifiedBy;

    @Column(name = "fechamodificacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate;

    @Column(name = "fechacreacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public User getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(User modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    /** Se corrigio despues de registrarlo. */
    public boolean isModified() {
        return null != modificationDate;
    }

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

    public HourBankMovementType getType() {
        return type;
    }

    public void setType(HourBankMovementType type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public SpecialDate getSpecialDate() {
        return specialDate;
    }

    public void setSpecialDate(SpecialDate specialDate) {
        this.specialDate = specialDate;
    }

    public PayrollGenerationCycle getPayrollGenerationCycle() {
        return payrollGenerationCycle;
    }

    public void setPayrollGenerationCycle(PayrollGenerationCycle payrollGenerationCycle) {
        this.payrollGenerationCycle = payrollGenerationCycle;
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
}
