package com.encens.khipus.action.employees;

import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.HourBankMovement;
import com.encens.khipus.action.employees.dto.HourBankBalance;
import com.encens.khipus.model.employees.HourBankMovementType;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.SpecialDate;
import com.encens.khipus.model.finances.Contract;
import com.encens.khipus.util.employees.attendance.ScheduledJourney;
import com.encens.khipus.service.employees.HourBankService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * El banco de horas de una persona: su saldo y sus movimientos.
 * <p/>
 * El saldo <b>no se guarda</b>: es la suma de los movimientos. Por eso esta pantalla no tiene un
 * campo de saldo editable; para corregir se registra un ajuste, que queda en el libro con motivo
 * y usuario.
 *
 * @author
 * @version 6.1.0
 */
@Name("hourBankAction")
@Scope(ScopeType.CONVERSATION)
public class HourBankAction implements Serializable {

    @Logger
    private Log log;

    @In
    private HourBankService hourBankService;

    @In(create = true)
    private FacesMessages facesMessages;

    private Employee employee;
    private Contract contract;

    /* Cuantas horas vale un dia para esta persona. Se resuelve al elegirla, una vez. */
    private BigDecimal hoursPerDay;
    /* Null: hay que volver a leerla. Se lee al dibujar la pantalla, no al ejecutar la accion. */
    private List<HourBankMovement> movements;

    /* El alta: se comparte entre acumular y tomar permiso porque los campos son casi los mismos
       y tenerlos en dos formularios invitaba a que uno se quedara sin validar. */
    private Date movementDate = new Date();
    private BigDecimal hours;
    private String description;
    private boolean allDayLeave = true;
    /* El tramo, en hora y minutos: es como se carga en pantalla -dos ruedas- y evita el
       calendario, que obligaba a pasar por un dia que despues se descarta. */
    private Long startHour;
    private Long startMinute;
    private Long endHour;
    private Long endMinute;

    /* El movimiento que se esta corrigiendo. Null: se esta registrando uno nuevo. Es el mismo
       formulario, porque corregir un error de tipeo es lo normal y no un caso aparte. */
    private HourBankMovement editing;

    /* El movimiento que el modal esta por borrar. Se elige al tocar el tacho y se confirma
       despues: el borrado se lleva la fecha especial o revierte el pago, asi que se pregunta. */
    private HourBankMovement deleting;

    /* Lo que va a consumir el permiso, calculado antes de guardar. Se muestra para que el error
       se vea en pantalla y no despues, en el saldo. */
    private BigDecimal leaveHours;
    private String leaveLabel;
    private String leaveError;
    /* Con que datos se calculo el resumen. Mientras no cambien, no se vuelve a calcular. */
    private Object[] leaveKey;

    /* El pago: el ciclo al que va y el importe. El importe lo pone RRHH; el recargo legal de la
       hora extra es una regla del pago y no del banco. */
    private PayrollGenerationCycle cycle;
    private BigDecimal amount;

    /* El reporte: los saldos de todos, para el rango elegido. Es lo que se ve mientras no haya
       una persona seleccionada. */
    private Date from;
    private Date to;
    private List<HourBankBalance> balances;

    /* Dos pestanas: los saldos de todos, y los movimientos de una persona. Cada filtro vive con
       lo que filtra, y la pantalla no cambia de forma sola. */
    public static final String TAB_BALANCES = "balancesTab";
    public static final String TAB_MOVEMENTS = "movementsTab";
    private String selectedTab = TAB_BALANCES;

    /* Un solo formulario con un solo boton: se elige que se registra y aparecen los campos que
       corresponden. Tres barras con tres botones confundian. */
    private HourBankMovementType movementType = HourBankMovementType.ACCRUAL;

    @Create
    public void init() {
        /* El reporte arranca en el anio corriente. El banco no tiene periodo, pero mostrar todo
           desde siempre haria una pantalla ilegible el segundo anio. */
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        from = calendar.getTime();
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        to = calendar.getTime();
        loadBalances();
    }

    public void assignEmployee(Employee selected) {
        this.employee = selected;
        /* Cada persona nueva arranca el formulario limpio y con la fecha de hoy: arrastrar lo que
           quedo cargado para la anterior es la forma segura de registrarle algo a quien no era. */
        clearForm();
        movementDate = new Date();
        movementType = HourBankMovementType.ACCRUAL;
        editing = null;
        load();
    }

    /** Desde el reporte se entra a los movimientos de esa persona. */
    public void select(HourBankBalance balance) {
        assignEmployee(null == balance ? null : balance.getEmployee());
        selectedTab = TAB_MOVEMENTS;
    }

    /** Pide releer los saldos; la consulta ocurre al dibujar, por lo mismo que los movimientos. */
    public void loadBalances() {
        balances = null;
    }

    public void clearEmployee() {
        assignEmployee(null);
    }

    /**
     * Guarda lo que se eligio en el tipo. Es el unico boton del formulario.
     * <p/>
     * Al volver, el reporte de saldos se recalcula: si no, la otra pestana seguiria mostrando el
     * saldo de antes de este movimiento.
     */
    public void save() {
        if (null != editing) {
            modify();
            loadBalances();
            return;
        }
        if (HourBankMovementType.LEAVE.equals(movementType)) {
            takeLeave();
        } else if (HourBankMovementType.PAYMENT.equals(movementType)) {
            payHours();
        } else {
            accrue();
        }
        loadBalances();
    }

    public boolean isLeaveSelected() {
        return HourBankMovementType.LEAVE.equals(movementType);
    }

    public boolean isPaymentSelected() {
        return HourBankMovementType.PAYMENT.equals(movementType);
    }

    /** Las horas se piden salvo en el permiso, donde salen del dia o del tramo. */
    public boolean isHoursRequired() {
        return !isLeaveSelected();
    }

    /** Lo que se puede registrar desde la pantalla. Apertura y ajuste no: los carga el script. */
    public List<HourBankMovementType> getMovementTypes() {
        List<HourBankMovementType> types = new ArrayList<HourBankMovementType>();
        types.add(HourBankMovementType.ACCRUAL);
        types.add(HourBankMovementType.LEAVE);
        types.add(HourBankMovementType.PAYMENT);
        return types;
    }

    private void load() {
        movements = null;
        hoursPerDay = hourBankService.hoursPerDayOf(employee);
        contract = hourBankService.bankContractOf(employee);
        if (null != employee && null == contract) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.WARN,
                    "HourBank.warn.withoutMainContract", employee.getFullName());
            return;
        }
    }

    /**
     * Los movimientos de la persona, leidos al dibujar.
     * <p/>
     * No se leen dentro de la accion: Seam abre una transaccion para la accion y otra para el
     * dibujado, y la consulta hecha en la primera no ve lo que se acaba de escribir -MySQL le
     * entrega la foto de la base anterior a la escritura-. Leer aca es lo que hace que la lista y
     * el saldo digan lo mismo.
     */
    public List<HourBankMovement> getMovements() {
        if (null == movements) {
            movements = null == contract
                    ? new ArrayList<HourBankMovement>()
                    : hourBankService.findMovements(contract);
        }
        return movements;
    }

    /** Las horas extra autorizadas por el jefe de area, validadas por RRHH. */
    @Restrict("#{s:hasPermission('HOURBANK','CREATE')}")
    public void accrue() {
        if (!ready() || !positiveHours()) {
            return;
        }
        try {
            hourBankService.accrue(contract, movementDate, hours, description);
            clearForm();
            load();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "HourBank.message.saved");
        } catch (Exception e) {
            log.error("No se pudo registrar el movimiento del banco de horas", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.message.failed");
        }
    }

    /**
     * Toma horas del banco como permiso compensatorio.
     * <p/>
     * No se valida que haya saldo: el saldo negativo es un hecho y se muestra. Prohibirlo
     * obligaria a RRHH a inventar un movimiento para cuadrar.
     */
    @Restrict("#{s:hasPermission('HOURBANK','CREATE')}")
    public void takeLeave() {
        if (!ready()) {
            return;
        }
        if (!leaveReady()) {
            return;
        }
        try {
            hourBankService.takeLeave(contract, movementDate,
                    allDayLeave ? null : getStartTime(), allDayLeave ? null : getEndTime(),
                    description);
            clearForm();
            load();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "HourBank.message.saved");
        } catch (Exception e) {
            log.error("No se pudo registrar el permiso compensatorio", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.message.failed");
        }
    }

    /**
     * Carga un movimiento en el formulario para corregirlo.
     * <p/>
     * No hay estados ni aprobaciones: quien registra es RRHH, con la lista del jefe de area en la
     * mano. Lo que hacia falta era poder corregir el error de tipeo sin borrar y volver a cargar,
     * y que quede el rastro de quien lo corrigio.
     */
    @Restrict("#{s:hasPermission('HOURBANK','UPDATE')}")
    public void edit(HourBankMovement movement) {
        if (null == movement) {
            return;
        }
        if (!hourBankService.isEditable(movement)) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.error.notEditable");
            return;
        }
        editing = movement;
        movementType = movement.getType();
        movementDate = movement.getDate();
        description = movement.getDescription();
        cycle = movement.getPayrollGenerationCycle();
        amount = movement.getAmount();
        hours = isLeaveSelected() ? null : movement.getHours().abs();
        SpecialDate specialDate = movement.getSpecialDate();
        allDayLeave = null == specialDate || Boolean.TRUE.equals(specialDate.getAllDay());
        /* Al corregir se cargan las horas que ese permiso tiene, no las del turno: corregir no
           es volver a empezar. Si despues cambian el dia, si vuelven las del turno. */
        setTimes(null == specialDate ? null : specialDate.getStartTime(),
                null == specialDate ? null : specialDate.getEndTime());
        refreshLeave();
    }

    /** Vuelve a registrar un movimiento nuevo, sin tocar el que se estaba corrigiendo. */
    public void cancelEdit() {
        editing = null;
        clearForm();
        movementType = HourBankMovementType.ACCRUAL;
    }

    public boolean isEditing() {
        return null != editing;
    }

    public HourBankMovement getEditing() {
        return editing;
    }

    /** Si ese movimiento todavia se puede corregir o borrar. Lo decide el servicio. */
    public boolean isEditable(HourBankMovement movement) {
        return hourBankService.isEditable(movement);
    }

    /** Guarda la correccion. El saldo no se toca: es la suma de los movimientos. */
    @Restrict("#{s:hasPermission('HOURBANK','UPDATE')}")
    public void modify() {
        if (!ready()) {
            return;
        }
        if (isLeaveSelected() ? !leaveReady() : !positiveHours()) {
            return;
        }
        if (isPaymentSelected() && null == cycle) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.error.cycleRequired");
            return;
        }
        try {
            hourBankService.modify(editing, movementDate, hours,
                    allDayLeave ? null : getStartTime(), allDayLeave ? null : getEndTime(),
                    cycle, amount, description);
            cancelEdit();
            load();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "HourBank.message.updated");
        } catch (Exception e) {
            log.error("No se pudo corregir el movimiento del banco de horas", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.message.failed");
        }
    }

    /** El tacho no borra: elige el movimiento y el modal pregunta. */
    public void prepareDelete(HourBankMovement movement) {
        deleting = movement;
    }

    public HourBankMovement getDeleting() {
        return deleting;
    }

    /** Borra el movimiento elegido: el permiso se lleva su fecha especial, el pago revierte lo escrito. */
    @Restrict("#{s:hasPermission('HOURBANK','DELETE')}")
    public void delete() {
        if (null == deleting) {
            return;
        }
        HourBankMovement movement = deleting;
        deleting = null;
        if (null != editing && editing.equals(movement)) {
            cancelEdit();
        }
        try {
            hourBankService.remove(movement);
            load();
            loadBalances();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "HourBank.message.deleted");
        } catch (Exception e) {
            log.error("No se pudo anular el permiso compensatorio", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.message.deleteFailed");
        }
    }

    /** Paga horas del banco: baja el saldo y escribe el registro que lee la planilla. */
    @Restrict("#{s:hasPermission('HOURBANK','CREATE')}")
    public void payHours() {
        if (!ready() || !positiveHours()) {
            return;
        }
        if (null == cycle) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.error.cycleRequired");
            return;
        }
        try {
            hourBankService.payHours(contract, movementDate, hours, cycle, amount, description);
            clearForm();
            load();
            facesMessages.addFromResourceBundle(StatusMessage.Severity.INFO, "HourBank.message.saved");
        } catch (Exception e) {
            log.error("No se pudo registrar el pago de horas", e);
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.message.failed");
        }
    }

    /**
     * Recalcula lo que va a consumir el permiso, con el dia y las horas que hay cargados.
     * <p/>
     * Se hace antes de guardar, y se muestra: un permiso de 23:30 a 03:30 termina al dia
     * siguiente, y verlo escrito es lo que evita guardar un tramo que no era el que se queria.
     */
    public void refreshLeave() {
        /* Solo invalida: el calculo esta en los getters, asi que ocurre igual aunque nadie toque
           nada. Los eventos de la pantalla siguen llamando aca para no recalcular de mas. */
        leaveKey = null;
    }

    /** Cambio la fecha o el tipo de permiso: el tramo vuelve a seguir al turno de ese dia. */
    public void dateChanged() {
        refreshLeave();
        preloadTimes();
    }

    /** Se ajusto una rueda: solo hay que recalcular lo que consume el permiso. */
    public void timesChanged() {
        refreshLeave();
    }

    /**
     * Carga el tramo con el turno de ese dia.
     * <p/>
     * El permiso casi siempre es el turno entero o un pedazo grande, asi que empezar desde ahi
     * ahorra buscar el horario en otra pantalla.
     * <p/>
     * Ocurre SIEMPRE que cambia el dia o se destilda "dia completo", incluso si habia horas
     * escritas. La regla es una sola y se puede decir en una linea -"las horas son las del turno
     * del dia elegido, ajustalas al final"-; la alternativa, respetar lo escrito, obligaba a
     * adivinar cuando el sistema iba a pisar el dato y cuando no.
     */
    private void preloadTimes() {
        if (allDayLeave) {
            return;
        }
        ScheduledJourney journey = hourBankService.journeyOf(contract, movementDate);
        if (null == journey) {
            setTimes(null, null);
            return;
        }
        setTimes(journey.getStart(), journey.getEnd());
    }

    /**
     * Calcula el resumen del permiso a partir de lo que hay cargado.
     * <p/>
     * Se calcula al dibujar y no al cambiar un campo: cuando dependia de los eventos, elegir
     * "Permiso" con una fecha sin turno no mostraba nada, y el aviso recien aparecia despues de
     * tocar dos veces la casilla. El resultado se guarda hasta que cambia alguno de los datos que
     * lo determinan, porque un getter lo llama la pantalla varias veces por dibujado.
     */
    private void computeLeave() {
        Object[] key = {movementType, contract, movementDate,
                Boolean.valueOf(allDayLeave), startHour, startMinute, endHour, endMinute};
        if (java.util.Arrays.equals(key, leaveKey)) {
            return;
        }
        leaveKey = key;
        leaveHours = null;
        leaveLabel = null;
        leaveError = null;
        if (!isLeaveSelected() || null == contract || null == movementDate) {
            return;
        }
        if (!allDayLeave && (null == getStartTime() || null == getEndTime())) {
            return;
        }
        /* Sin excepciones: un dia sin jornada es un caso normal de carga, no un fallo. Una
           excepcion desde el servicio la envuelve el contenedor EJB y termina en pantalla de
           error, que es exactamente lo que no puede pasar mientras alguien completa un campo. */
        BigDecimal journey = hourBankService.journeyHours(contract, movementDate);
        if (null == journey) {
            leaveError = "HourBank.error.noJourney";
            return;
        }
        BigDecimal hours = hourBankService.leaveHours(contract, movementDate,
                allDayLeave ? null : getStartTime(), allDayLeave ? null : getEndTime());
        if (null == hours || hours.signum() <= 0) {
            leaveError = "HourBank.error.hoursRequired";
            return;
        }
        if (hours.compareTo(journey) > 0) {
            leaveError = "HourBank.error.leaveExceedsJourney";
            leaveLabel = allDayLeave ? null : intervalLabel();
            return;
        }
        leaveHours = hours;
        leaveLabel = allDayLeave ? null : intervalLabel();
    }

    /**
     * "27/07 23:30 - 28/07 07:30": el dia de fin se ve, asi no hay que deducirlo.
     * <p/>
     * Sin el anio: ya esta en el campo de fecha, y la barra tiene que entrar en una linea.
     */
    private String intervalLabel() {
        SimpleDateFormat date = new SimpleDateFormat("dd/MM");
        SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
        Calendar end = Calendar.getInstance();
        end.setTime(movementDate);
        if (!getStartTime().before(getEndTime())) {
            end.add(Calendar.DAY_OF_MONTH, 1);
        }
        return date.format(movementDate) + " " + hour.format(getStartTime())
                + " - " + date.format(end.getTime()) + " " + hour.format(getEndTime());
    }

    /** El permiso esta completo y sin errores. */
    private boolean leaveReady() {
        if (!allDayLeave && (null == getStartTime() || null == getEndTime())) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.error.hoursRequired");
            return false;
        }
        refreshLeave();
        computeLeave();
        if (null != leaveError) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, leaveError);
            return false;
        }
        return true;
    }

    public BigDecimal getLeaveHours() {
        computeLeave();
        return leaveHours;
    }

    public String getLeaveLabel() {
        computeLeave();
        return leaveLabel;
    }

    public String getLeaveError() {
        computeLeave();
        return leaveError;
    }

    /** El permiso, en dias, para leerlo como siempre. */
    public BigDecimal getLeaveHoursInDays() {
        computeLeave();
        return toDays(leaveHours);
    }

    private boolean ready() {
        if (null == contract) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.error.employeeRequired");
            return false;
        }
        if (null == movementDate) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.error.dateRequired");
            return false;
        }
        return true;
    }

    private boolean positiveHours() {
        if (null == hours || hours.signum() <= 0) {
            facesMessages.addFromResourceBundle(StatusMessage.Severity.ERROR, "HourBank.error.hoursRequired");
            return false;
        }
        return true;
    }

    private void clearForm() {
        hours = null;
        leaveHours = null;
        leaveLabel = null;
        leaveError = null;
        leaveKey = null;
        description = null;
        setTimes(null, null);
        amount = null;
    }

    // ------------------------------------------------------------------ saldo

    public BigDecimal getBalance() {
        return hourBankService.balanceOf(contract);
    }

    /** La jornada diaria esta configurada para esta persona. */
    public boolean isDayConfigured() {
        return null != hoursPerDay && hoursPerDay.signum() > 0;
    }

    public BigDecimal getHoursPerDay() {
        return hoursPerDay;
    }

    /**
     * Horas a dias, con la jornada de la persona. Null si no esta configurada: mostrar un cero
     * haria pensar que el saldo en dias es cero.
     */
    public BigDecimal toDays(BigDecimal value) {
        if (null == value || !isDayConfigured()) {
            return null;
        }
        return value.divide(hoursPerDay, 2, BigDecimal.ROUND_HALF_UP);
    }

    /** El saldo en dias, que es como RRHH lo viene mirando. */
    public BigDecimal getBalanceInDays() {
        return toDays(getBalance());
    }

    /** Lo que se esta escribiendo, en dias. Se muestra al lado del campo de horas. */
    public BigDecimal getHoursInDays() {
        return toDays(hours);
    }

    /** Un movimiento, en dias. */
    public BigDecimal daysOf(HourBankMovement movement) {
        return null == movement ? null : toDays(movement.getHours());
    }

    public boolean isNegativeBalance() {
        return getBalance().signum() < 0;
    }

    public BigDecimal sumOf(HourBankMovementType type) {
        BigDecimal total = BigDecimal.ZERO;
        for (HourBankMovement movement : getMovements()) {
            if (type.equals(movement.getType())) {
                total = total.add(movement.getHours());
            }
        }
        return total;
    }

    public BigDecimal getAccrued() {
        return sumOf(HourBankMovementType.ACCRUAL).add(sumOf(HourBankMovementType.OPENING));
    }

    public BigDecimal getUsed() {
        return sumOf(HourBankMovementType.LEAVE).abs();
    }

    public BigDecimal getPaid() {
        return sumOf(HourBankMovementType.PAYMENT).abs();
    }

    public boolean isLeave(HourBankMovement movement) {
        return HourBankMovementType.LEAVE.equals(movement.getType());
    }

    public boolean isLoaded() {
        return null != contract;
    }

    // ---------------------------------------------------------------- getters

    public List<HourBankBalance> getBalances() {
        if (null == balances) {
            balances = hourBankService.balancesBetween(from, to);
        }
        return balances;
    }

    public String getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }

    public HourBankMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(HourBankMovementType movementType) {
        this.movementType = null == movementType ? HourBankMovementType.ACCRUAL : movementType;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public PayrollGenerationCycle getCycle() {
        return cycle;
    }

    public void setCycle(PayrollGenerationCycle cycle) {
        this.cycle = cycle;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Contract getContract() {
        return contract;
    }



    public Date getMovementDate() {
        return movementDate;
    }

    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAllDayLeave() {
        return allDayLeave;
    }

    public void setAllDayLeave(boolean allDayLeave) {
        this.allDayLeave = allDayLeave;
    }

    public Long getStartHour() {
        return startHour;
    }

    public void setStartHour(Long startHour) {
        this.startHour = startHour;
    }

    public Long getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(Long startMinute) {
        this.startMinute = startMinute;
    }

    public Long getEndHour() {
        return endHour;
    }

    public void setEndHour(Long endHour) {
        this.endHour = endHour;
    }

    public Long getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(Long endMinute) {
        this.endMinute = endMinute;
    }

    /** La hora de inicio del tramo, armada con las dos ruedas. Null si falta alguna. */
    private Date getStartTime() {
        return timeOf(startHour, startMinute);
    }

    private Date getEndTime() {
        return timeOf(endHour, endMinute);
    }

    /**
     * Una hora suelta, sin dia.
     * <p/>
     * El dia lo pone la fecha del movimiento: `fechaespecial` guarda el horario aparte del
     * periodo, y el motor lo apoya sobre cada dia que cubre el permiso.
     */
    private static Date timeOf(Long hour, Long minute) {
        if (null == hour || null == minute) {
            return null;
        }
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, hour.intValue());
        time.set(Calendar.MINUTE, minute.intValue());
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        return time.getTime();
    }

    private void setTimes(Date start, Date end) {
        startHour = hourOf(start);
        startMinute = minuteOf(start);
        endHour = hourOf(end);
        endMinute = minuteOf(end);
    }

    private static Long hourOf(Date date) {
        return null == date ? null : Long.valueOf(fieldOf(date, Calendar.HOUR_OF_DAY));
    }

    private static Long minuteOf(Date date) {
        return null == date ? null : Long.valueOf(fieldOf(date, Calendar.MINUTE));
    }

    private static int fieldOf(Date date, int field) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(field);
    }
}
