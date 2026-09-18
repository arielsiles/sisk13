package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericService;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.HourBankMovement;
import com.encens.khipus.action.employees.dto.HourBankBalance;
import com.encens.khipus.model.employees.HourBankMovementType;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.util.employees.attendance.ScheduledJourney;

import javax.ejb.Local;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * El banco de horas: lo que se acumula, lo que se usa y cuanto queda.
 * <p/>
 * El saldo <b>siempre</b> se deriva sumando movimientos. Nunca hay un campo con el total: es la
 * unica forma de que corregir un mes viejo no deje el saldo mintiendo.
 *
 * @author
 * @version 6.1.0
 */
@Local
public interface HourBankService extends GenericService {

    /** El saldo en horas del contrato. Puede ser negativo: se tomo mas de lo acumulado. */
    BigDecimal balanceOf(Contract contract);

    /** El saldo a una fecha, para poder explicar el saldo de un mes cerrado. */
    BigDecimal balanceOf(Contract contract, Date date);

    /**
     * El saldo de la persona, resuelto sobre su contrato principal.
     * <p/>
     * Sin principal no hay banco: un contrato secundario es trabajo eventual con fecha de fin, y
     * darle saldo propio abriria la puerta a que la misma hora se acumule dos veces.
     */
    BigDecimal balanceOf(Employee employee);

    /** El contrato al que se le registra: el principal de la persona, o null si no tiene. */
    Contract bankContractOf(Employee employee);

    List<HourBankMovement> findMovements(Contract contract);

    /**
     * Registra horas autorizadas por el jefe de area y validadas por RRHH.
     *
     * @param hours en horas, positivo
     */
    HourBankMovement accrue(Contract contract, Date date, BigDecimal hours, String description)
            throws EntryDuplicatedException;

    /** Un movimiento cualquiera, con el signo ya resuelto por el tipo. */
    HourBankMovement register(Contract contract, HourBankMovementType type, Date date,
                              BigDecimal hours, String description) throws EntryDuplicatedException;

    /**
     * Cuantas horas tiene un dia de esta persona, para convertir a dias.
     * <p/>
     * Sale de `jornadasemanal`, que distingue por genero. Null si no esta configurada.
     */
    BigDecimal hoursPerDayOf(Employee employee);

    /**
     * Toma horas del banco como permiso compensatorio.
     * <p/>
     * Crea la fecha especial <b>y</b> el movimiento, en una sola transaccion. Separados, un fallo
     * dejaria el dia justificado sin descontar del saldo -o el saldo bajando por un permiso que no
     * existe-. Es el mismo motivo por el que una vacacion no se puede cargar como fecha especial
     * a mano.
     *
     * @param startTime hora de inicio; null para un permiso de dia completo
     * @param endTime   hora de fin; null para un permiso de dia completo
     */
    HourBankMovement takeLeave(Contract contract, Date date, Date startTime, Date endTime,
                               String description) throws Exception;

    /** Anula un permiso: borra la fecha especial y el movimiento, juntos. */
    void cancelLeave(HourBankMovement movement) throws Exception;

    /**
     * Corrige un movimiento ya registrado.
     * <p/>
     * Se corrige en lugar de borrar y volver a cargar porque el error de tipeo es lo normal, no la
     * excepcion. Queda el rastro de quien lo modifico y cuando; el saldo no hace falta tocarlo,
     * porque es la suma de los movimientos.
     * <p/>
     * El permiso arrastra su fecha especial: si se corrige el dia o el tramo, la asistencia tiene
     * que decir lo mismo que el banco.
     *
     * @throws IllegalStateException si el movimiento ya no se puede tocar
     */
    HourBankMovement modify(HourBankMovement movement, Date date, BigDecimal hours,
                            Date startTime, Date endTime, PayrollGenerationCycle cycle,
                            BigDecimal amount, String description) throws Exception;

    /** Borra un movimiento: el permiso se lleva su fecha especial, el pago revierte `horasextra`. */
    void remove(HourBankMovement movement) throws Exception;

    /**
     * Si el movimiento todavia se puede corregir o borrar.
     * <p/>
     * Un pago cuya planilla ya se genero, no: esas horas ya se pagaron, y cambiarlas en el banco
     * dejaria el banco diciendo una cosa y la planilla otra. Lo demas siempre se puede corregir.
     */
    boolean isEditable(HourBankMovement movement);

    /**
     * Las horas que consume un permiso.
     * <p/>
     * De dia completo: las horas del <b>turno de ese dia</b> -4, 7, 8 o 12-, no una jornada
     * promedio. Quien no trabaja un turno de 12 h deja de trabajar 12 h, y el banco tiene que
     * descontar lo que realmente dejo de trabajar.
     * <p/>
     * Por horas: el tramo. Si la hora de fin es menor que la de inicio, el tramo cruza la
     * medianoche -23:30 a 03:30 del turno de noche- y se cuenta hasta el dia siguiente.
     * <p/>
     * <b>No falla</b> si el dia no tiene jornada: devuelve null. Un dia sin jornada es un caso
     * normal mientras alguien completa el formulario, y una excepcion desde un EJB termina en la
     * pantalla de error. Quien guarda compara contra {@link #journeyHours} y lo explica.
     *
     * @return las horas, o null si es de dia completo y ese dia no tiene jornada
     */
    BigDecimal leaveHours(Contract contract, Date date, Date startTime, Date endTime);

    /**
     * Las horas que el turno exige ese dia. Null si ese dia no le tocaba trabajar.
     * <p/>
     * Sale del mismo resolutor que usan la asistencia y la planilla: si el banco calculara por su
     * cuenta, un cambio de cronograma dejaria las tres pantallas diciendo cosas distintas.
     */
    BigDecimal journeyHours(Contract contract, Date date);

    /**
     * La jornada de ese dia, con sus horas de inicio y fin. Null si no le tocaba trabajar.
     * <p/>
     * La pantalla la usa para precargar el tramo del permiso con el turno real: el permiso casi
     * siempre es el turno entero o un pedazo grande de el, asi que empezar desde ahi es menos
     * trabajo que buscar el horario en otra pantalla.
     */
    ScheduledJourney journeyOf(Contract contract, Date date);

    /**
     * Paga horas del banco.
     * <p/>
     * Baja el saldo <b>y</b> escribe `horasextra` del ciclo, que es de donde la planilla fiscal
     * viene leyendo desde siempre. Asi la planilla no se entera de nada: lee lo de siempre, del
     * mismo lugar, y lo unico que cambia es quien lo escribe.
     * <p/>
     * El importe lo pone RRHH. El recargo legal de la hora extra -tope de 2 h diarias y 100 %- es
     * una regla del pago y no del banco: inventarlo aca seria calcular plata sin que nadie lo
     * haya pedido.
     */
    HourBankMovement payHours(Contract contract, Date date, BigDecimal hours,
                              PayrollGenerationCycle cycle, BigDecimal amount, String description)
            throws Exception;

    /** Los saldos de todas las personas en un rango. Es el reporte. */
    List<HourBankBalance> balancesBetween(Date from, Date to);
}
