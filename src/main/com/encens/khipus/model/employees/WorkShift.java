package com.encens.khipus.model.employees;

import com.encens.khipus.model.BaseModel;
import com.encens.khipus.model.CompanyListener;
import com.encens.khipus.model.UpperCaseStringListener;
import com.encens.khipus.model.admin.Company;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Un turno: la jornada de un dia.
 * <p/>
 * Reemplaza a `HoraryBand`, que guardaba una fila por dia de la semana -4.737 filas para 44
 * horarios reales- y ocho parametros entre `Tolerance` y `Limit`. Aca un turno es uno solo y
 * se usa el dia que haga falta, y quedan dos tolerancias.
 * <p/>
 * Las ocho de antes sobran por dos motivos. `Limit` desaparece porque el motor nuevo no
 * pregunta si una marca "pertenece" a una banda: asocia por solapamiento y no descarta nada.
 * Y de `Tolerance` sobran las de salida tardia, porque quedarse despues de hora no se penaliza:
 * es una salida normal.
 * <p/>
 * Que el turno cruce la medianoche no se configura: se deduce de que la hora de fin sea menor o
 * igual a la de inicio. Un dato que se puede deducir no se guarda, porque guardado puede
 * terminar contradiciendo a las horas.
 *
 * @author
 * @version 6.1.0
 */
@TableGenerator(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA,
        name = "WorkShift.tableGenerator",
        table = com.encens.khipus.util.Constants.SEQUENCE_TABLE_NAME,
        pkColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_PK_COLUMN_NAME,
        valueColumnName = com.encens.khipus.util.Constants.SEQUENCE_TABLE_VALUE_COLUMN_NAME,
        pkColumnValue = "turno",
        allocationSize = com.encens.khipus.util.Constants.SEQUENCE_ALLOCATION_SIZE)

@NamedQueries({
        @NamedQuery(name = "WorkShift.findActive",
                query = "select o from WorkShift o where o.active = :active order by o.name asc"),
        @NamedQuery(name = "WorkShift.countByName",
                query = "select count(o) from WorkShift o where o.name = :name"),
        @NamedQuery(name = "WorkShift.countByNameButThis",
                query = "select count(o) from WorkShift o where o.name = :name and o.id <> :id")
})

@Entity
@Filter(name = com.encens.khipus.util.Constants.COMPANY_FILTER_NAME)
@EntityListeners({CompanyListener.class, UpperCaseStringListener.class})
@Table(schema = com.encens.khipus.util.Constants.KHIPUS_SCHEMA, name = "turno")
public class WorkShift implements BaseModel {

    /* Precarga de la ventana. La ventana mide `duracion + antes + despues`, asi que con 2 y 4
       queda por debajo del doble del turno solo si el turno dura 6 horas o mas. Para los cortos
       -medios turnos de 3, 4 o 5 horas- la precarga es la mitad. Son valores de arranque: cada
       turno queda dueño de los suyos y se editan a mano. */
    public static final int DEFAULT_BEFORE_MINUTES = 120;
    public static final int DEFAULT_AFTER_MINUTES = 240;
    public static final int SHORT_BEFORE_MINUTES = 60;
    public static final int SHORT_AFTER_MINUTES = 120;

    /** Menos de esto es un turno corto y la ventana larga le queda desproporcionada. */
    public static final int SHORT_SHIFT_MINUTES = 6 * 60;

    @Id
    @Column(name = "idturno", nullable = false)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "WorkShift.tableGenerator")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    @NotNull
    @Length(max = 100)
    private String name;

    @Column(name = "horainicio", nullable = false)
    @Temporal(TemporalType.TIME)
    @NotNull
    private Date startHour;

    @Column(name = "horafin", nullable = false)
    @Temporal(TemporalType.TIME)
    @NotNull
    private Date endHour;

    /* Minutos de gracia para entrar. Pasados, hay atraso. */
    @Column(name = "toleranciaentrada", nullable = false)
    @NotNull
    private Integer entryToleranceMinutes = 0;

    /* Minutos de gracia para salir antes. Salir despues no necesita tolerancia. */
    @Column(name = "toleranciasalida", nullable = false)
    @NotNull
    private Integer earlyExitToleranceMinutes = 0;

    /**
     * Cuanto ANTES del inicio puede estar una marca y seguir siendo la entrada de esta jornada.
     * <p/>
     * Junto con {@link #afterMarginMinutes} define la <b>ventana</b> del turno, que es lo que
     * decide <b>de que jornada es una marca</b>. No confundir con las tolerancias, que deciden
     * <b>si esta atrasado</b>: con los 10 minutos de tolerancia, una salida marcada 17:09 para un
     * turno que termina 17:30 no pertenecería a ninguna jornada.
     */
    @Column(name = "margenantes", nullable = false)
    @NotNull
    private Integer beforeMarginMinutes = DEFAULT_BEFORE_MINUTES;

    /**
     * Cuanto DESPUES del fin puede estar una marca y seguir siendo la salida de esta jornada.
     * Es, en la practica, cuantas horas extra se reconocen como parte de la jornada.
     */
    @Column(name = "margendespues", nullable = false)
    @NotNull
    private Integer afterMarginMinutes = DEFAULT_AFTER_MINUTES;

    @Column(name = "activo", nullable = false)
    @Type(type = com.encens.khipus.model.usertype.IntegerBooleanUserType.NAME)
    @NotNull
    private Boolean active = Boolean.TRUE;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "idcompania", nullable = false, updatable = false, insertable = true)
    private Company company;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /**
     * Un turno cruza la medianoche cuando termina a una hora que no es posterior a la de
     * inicio: 19:30 a 07:30. Se deduce, no se configura.
     */
    @Transient
    public boolean isCrossingMidnight() {
        if (null == startHour || null == endHour) {
            return false;
        }
        return minuteOfDay(endHour) <= minuteOfDay(startHour);
    }

    /** Duracion en minutos, contando el cruce de medianoche. */
    @Transient
    public int getDurationMinutes() {
        if (null == startHour || null == endHour) {
            return 0;
        }
        int minutes = minuteOfDay(endHour) - minuteOfDay(startHour);
        return minutes <= 0 ? minutes + 24 * 60 : minutes;
    }

    private static int minuteOfDay(Date hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(hour);
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    /** La ventana del turno, en minutos: lo que dura mas los dos margenes. */
    public int getWindowMinutes() {
        return getDurationMinutes()
                + (null == beforeMarginMinutes ? 0 : beforeMarginMinutes)
                + (null == afterMarginMinutes ? 0 : afterMarginMinutes);
    }

    public Integer getBeforeMarginMinutes() {
        return beforeMarginMinutes;
    }

    public void setBeforeMarginMinutes(Integer beforeMarginMinutes) {
        this.beforeMarginMinutes = beforeMarginMinutes;
    }

    public Integer getAfterMarginMinutes() {
        return afterMarginMinutes;
    }

    public void setAfterMarginMinutes(Integer afterMarginMinutes) {
        this.afterMarginMinutes = afterMarginMinutes;
    }

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

    public Date getStartHour() {
        return startHour;
    }

    public void setStartHour(Date startHour) {
        this.startHour = startHour;
    }

    public Date getEndHour() {
        return endHour;
    }

    public void setEndHour(Date endHour) {
        this.endHour = endHour;
    }

    public Integer getEntryToleranceMinutes() {
        return entryToleranceMinutes;
    }

    public void setEntryToleranceMinutes(Integer entryToleranceMinutes) {
        this.entryToleranceMinutes = entryToleranceMinutes;
    }

    public Integer getEarlyExitToleranceMinutes() {
        return earlyExitToleranceMinutes;
    }

    public void setEarlyExitToleranceMinutes(Integer earlyExitToleranceMinutes) {
        this.earlyExitToleranceMinutes = earlyExitToleranceMinutes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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
        return "WorkShift{id=" + id + ", name='" + name + "'}";
    }
}
