package com.encens.khipus.service.employees;

import com.encens.khipus.exception.EntryDuplicatedException;
import com.encens.khipus.framework.service.GenericServiceBean;
import com.encens.khipus.model.employees.Employee;
import com.encens.khipus.model.employees.HourBankMovement;
import com.encens.khipus.model.employees.HourBankMovementType;
import com.encens.khipus.model.employees.SpecialDate;
import com.encens.khipus.model.employees.SpecialDateReason;
import com.encens.khipus.model.employees.SpecialDateRol;
import com.encens.khipus.model.employees.SpecialDateTarget;
import com.encens.khipus.model.employees.SpecialDateType;
import com.encens.khipus.action.employees.dto.HourBankBalance;
import com.encens.khipus.model.employees.ExtraHoursWorked;
import com.encens.khipus.model.employees.GeneratedPayrollType;
import com.encens.khipus.model.employees.PayrollGenerationCycle;
import com.encens.khipus.model.employees.WeeklyWorkload;
import com.encens.khipus.model.finances.JobContract;
import com.encens.khipus.util.MessageUtils;
import com.encens.khipus.util.employees.attendance.ScheduledJourney;
import com.encens.khipus.model.finances.Contract;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @version 6.1.0
 */
@Stateless
@Name("hourBankService")
@AutoCreate
public class HourBankServiceBean extends GenericServiceBean implements HourBankService {

    @In
    private ContractConditionService contractConditionService;

    @In
    private JourneyResolverService journeyResolverService;

    @In(required = false)
    private com.encens.khipus.model.admin.User currentUser;

    /** Los minutos de un dia, para el permiso que cruza la medianoche. */
    private static final long MINUTES_OF_DAY = 24 * 60;

    public BigDecimal balanceOf(Contract contract) {
        if (null == contract) {
            return BigDecimal.ZERO;
        }
        Object sum = getEntityManager()
                .createNamedQuery("HourBankMovement.balance")
                .setParameter("contract", contract)
                .getSingleResult();
        return null == sum ? BigDecimal.ZERO : (BigDecimal) sum;
    }

    public BigDecimal balanceOf(Contract contract, Date date) {
        if (null == contract || null == date) {
            return balanceOf(contract);
        }
        Object sum = getEntityManager()
                .createNamedQuery("HourBankMovement.balanceAtDate")
                .setParameter("contract", contract)
                .setParameter("date", date)
                .getSingleResult();
        return null == sum ? BigDecimal.ZERO : (BigDecimal) sum;
    }

    public BigDecimal balanceOf(Employee employee) {
        return balanceOf(bankContractOf(employee));
    }

    public Contract bankContractOf(Employee employee) {
        return null == employee ? null : contractConditionService.findMainContract(employee);
    }

    @SuppressWarnings({"unchecked"})
    public List<HourBankMovement> findMovements(Contract contract) {
        if (null == contract) {
            return new ArrayList<HourBankMovement>();
        }
        return getEntityManager()
                .createNamedQuery("HourBankMovement.findByContract")
                .setParameter("contract", contract)
                .getResultList();
    }

    public HourBankMovement accrue(Contract contract, Date date, BigDecimal hours,
                                   String description) throws EntryDuplicatedException {
        return register(contract, HourBankMovementType.ACCRUAL, date, hours, description);
    }

    public HourBankMovement register(Contract contract, HourBankMovementType type, Date date,
                                     BigDecimal hours, String description)
            throws EntryDuplicatedException {
        HourBankMovement movement = new HourBankMovement();
        movement.setContract(contract);
        movement.setType(type);
        movement.setDate(date);
        movement.setHours(signed(type, hours));
        movement.setDescription(description);
        movement.setUser(currentUser);
        create(movement);
        return movement;
    }

    /**
     * El signo lo pone el tipo, no quien llama.
     * <p/>
     * Si lo pusiera quien llama, alcanzaria con un olvido para que un permiso sumara saldo en vez
     * de restarlo. El ajuste es la excepcion: existe justamente para corregir en los dos sentidos,
     * asi que ahi manda el signo del importe.
     */
    private static BigDecimal signed(HourBankMovementType type, BigDecimal hours) {
        BigDecimal amount = null == hours ? BigDecimal.ZERO : hours;
        if (HourBankMovementType.ADJUSTMENT.equals(type)) {
            return amount;
        }
        BigDecimal magnitude = amount.abs();
        return type.isCredit() ? magnitude : magnitude.negate();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Las dos escrituras van en la MISMA transaccion. `create` esta declarado REQUIRES_NEW, pero
     * es un metodo heredado llamado sobre `this`: no pasa por el proxy del contenedor, asi que no
     * abre una transaccion propia y se une a esta. Es como lo resuelve el modulo de vacaciones.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public HourBankMovement takeLeave(Contract contract, Date date, Date startTime, Date endTime,
                                      String description) throws Exception {
        Employee employee = contract.getEmployee();
        boolean allDay = null == startTime || null == endTime;

        SpecialDate specialDate = new SpecialDate();
        specialDate.setTitle(titleOf(description));
        specialDate.setInitPeriod(date);
        specialDate.setEndPeriod(date);
        specialDate.setAllDay(allDay);
        specialDate.setStartTime(allDay ? null : startTime);
        specialDate.setEndTime(allDay ? null : endTime);
        specialDate.setEmployee(employee);
        specialDate.setCredit(SpecialDateType.PAID);
        specialDate.setRolType(SpecialDateRol.FECHA);
        specialDate.setSpecialDateTarget(SpecialDateTarget.EMPLOYEE);
        /* El motivo lo escribe el modulo, no se elige a mano: es lo que permite reportar por causa
           y lo que el motor mira para no cobrar ese tiempo. */
        specialDate.setReason(SpecialDateReason.COMPENSATORY);
        create(specialDate);

        HourBankMovement movement = new HourBankMovement();
        movement.setContract(contract);
        movement.setType(HourBankMovementType.LEAVE);
        movement.setDate(date);
        movement.setHours(checkedLeaveHours(contract, date, startTime, endTime).negate());
        movement.setSpecialDate(specialDate);
        movement.setDescription(description);
        movement.setUser(currentUser);
        create(movement);
        return movement;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cancelLeave(HourBankMovement movement) throws Exception {
        if (null == movement) {
            return;
        }
        SpecialDate specialDate = movement.getSpecialDate();
        delete(movement);
        if (null != specialDate) {
            delete(specialDate);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Cada tipo arrastra lo suyo: el permiso su fecha especial, el pago las horas escritas en
     * `horasextra`. Corregir solo el movimiento dejaria el banco al dia y lo de afuera viejo.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public HourBankMovement modify(HourBankMovement movement, Date date, BigDecimal hours,
                                   Date startTime, Date endTime, PayrollGenerationCycle cycle,
                                   BigDecimal amount, String description) throws Exception {
        if (null == movement) {
            return null;
        }
        if (!isEditable(movement)) {
            throw new IllegalStateException("El movimiento ya no se puede corregir");
        }
        HourBankMovementType type = movement.getType();
        Contract contract = movement.getContract();

        if (HourBankMovementType.LEAVE.equals(type)) {
            BigDecimal leaveHours = checkedLeaveHours(contract, date, startTime, endTime);
            boolean allDay = null == startTime || null == endTime;
            SpecialDate specialDate = movement.getSpecialDate();
            if (null != specialDate) {
                specialDate.setTitle(titleOf(description));
                specialDate.setInitPeriod(date);
                specialDate.setEndPeriod(date);
                specialDate.setAllDay(allDay);
                specialDate.setStartTime(allDay ? null : startTime);
                specialDate.setEndTime(allDay ? null : endTime);
                update(specialDate);
            }
            movement.setHours(leaveHours.negate());
        } else if (HourBankMovementType.PAYMENT.equals(type)) {
            /* El pago viejo se descuenta de su ciclo y el nuevo se suma al suyo: si solo se sumara
               la diferencia, cambiar de ciclo dejaria las horas contadas en los dos. */
            writeExtraHours(contract, movement.getPayrollGenerationCycle(),
                    movement.getHours().abs().negate(), negate(movement.getAmount()));
            writeExtraHours(contract, cycle, hours.abs(), amount);
            movement.setPayrollGenerationCycle(cycle);
            movement.setAmount(amount);
            movement.setHours(hours.abs().negate());
        } else {
            movement.setHours(signed(type, hours));
        }

        movement.setDate(date);
        movement.setDescription(description);
        movement.setModifiedBy(currentUser);
        movement.setModificationDate(new Date());
        update(movement);
        return movement;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void remove(HourBankMovement movement) throws Exception {
        if (null == movement) {
            return;
        }
        if (!isEditable(movement)) {
            throw new IllegalStateException("El movimiento ya no se puede borrar");
        }
        if (HourBankMovementType.LEAVE.equals(movement.getType())) {
            cancelLeave(movement);
            return;
        }
        if (HourBankMovementType.PAYMENT.equals(movement.getType())) {
            writeExtraHours(movement.getContract(), movement.getPayrollGenerationCycle(),
                    movement.getHours().abs().negate(), negate(movement.getAmount()));
        }
        delete(movement);
    }

    @SuppressWarnings({"unchecked"})
    public boolean isEditable(HourBankMovement movement) {
        if (null == movement) {
            return false;
        }
        if (!HourBankMovementType.PAYMENT.equals(movement.getType())
                || null == movement.getPayrollGenerationCycle()) {
            return true;
        }
        /* Lo que cierra el pago es la planilla OFICIAL del ciclo, no cualquier generacion.
           Mirar cualquiera era demasiado duro: una planilla se genera muchas veces como prueba
           -en terdemol, diecinueve veces el mismo mes- y a la primera el pago quedaba trabado,
           sin forma de corregir un error de carga desde la pantalla. Lo que no se toca por atras
           es lo que ya se pago de verdad. */
        List<?> official = getEntityManager()
                .createQuery("select o.id from GeneratedPayroll o"
                        + " where o.payrollGenerationCycle = :cycle"
                        + " and o.generatedPayrollType = :official")
                .setParameter("cycle", movement.getPayrollGenerationCycle())
                .setParameter("official", GeneratedPayrollType.OFFICIAL)
                .setMaxResults(1)
                .getResultList();
        return official.isEmpty();
    }

    /** Nunca por debajo de cero: horas extra negativas no existen. */
    private static BigDecimal floor(BigDecimal value) {
        return value.signum() < 0 ? BigDecimal.ZERO : value;
    }

    private static BigDecimal negate(BigDecimal value) {
        return null == value ? BigDecimal.ZERO : value.negate();
    }

    /** El titulo de la fecha especial: lo que escribio RRHH, o el nombre del permiso. */
    private static String titleOf(String description) {
        return null == description || 0 == description.trim().length()
                ? MessageUtils.getMessage("HourBank.leave.title") : description;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public HourBankMovement payHours(Contract contract, Date date, BigDecimal hours,
                                     PayrollGenerationCycle cycle, BigDecimal amount,
                                     String description) throws Exception {
        HourBankMovement movement = new HourBankMovement();
        movement.setContract(contract);
        movement.setType(HourBankMovementType.PAYMENT);
        movement.setDate(date);
        movement.setHours(hours.abs().negate());
        movement.setPayrollGenerationCycle(cycle);
        movement.setAmount(amount);
        movement.setDescription(description);
        movement.setUser(currentUser);
        create(movement);

        writeExtraHours(contract, cycle, hours.abs(), amount);
        return movement;
    }

    /**
     * Acumula el pago en la fila de `horasextra` del ciclo.
     * <p/>
     * Hay una sola fila por puesto-contrato y ciclo, asi que pagar dos veces en el mismo mes suma
     * sobre la existente. Crear una segunda dejaria a la planilla leyendo cualquiera de las dos.
     */
    @SuppressWarnings({"unchecked"})
    private void writeExtraHours(Contract contract, PayrollGenerationCycle cycle,
                                 BigDecimal hours, BigDecimal amount) throws Exception {
        if (null == cycle || contract.getJobContractList().isEmpty()) {
            return;
        }
        JobContract jobContract = contract.getJobContractList().get(0);
        List<ExtraHoursWorked> existing = getEntityManager()
                .createQuery("select o from ExtraHoursWorked o"
                        + " where o.jobContract = :jobContract"
                        + " and o.payrollGenerationCycle = :cycle")
                .setParameter("jobContract", jobContract)
                .setParameter("cycle", cycle)
                .getResultList();

        BigDecimal paidAmount = null == amount ? BigDecimal.ZERO : amount;
        if (existing.isEmpty()) {
            /* Restar donde no hay fila no crea una fila negativa: el pago que se revierte ya no
               dejo nada escrito, y una fila con horas negativas le llegaria a la planilla. */
            if (hours.signum() <= 0) {
                return;
            }
            ExtraHoursWorked extra = new ExtraHoursWorked();
            extra.setJobContract(jobContract);
            extra.setPayrollGenerationCycle(cycle);
            extra.setExtraHours(hours);
            extra.setTotalPaid(paidAmount);
            create(extra);
        } else {
            ExtraHoursWorked extra = existing.get(0);
            BigDecimal newHours = floor(extra.getExtraHours().add(hours));
            BigDecimal newPaid = floor(extra.getTotalPaid().add(paidAmount));
            if (0 == newHours.signum() && 0 == newPaid.signum()) {
                /* Al revertir el pago la fila queda en cero, y una fila de horas extra con cero
                   horas y cero pesos no dice nada: ensucia el ciclo y hace dudar de si quedo algo
                   pendiente. Se borra, que es lo que el pago hizo al reves cuando se registro. */
                delete(extra);
                return;
            }
            extra.setExtraHours(newHours);
            extra.setTotalPaid(newPaid);
            update(extra);
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<HourBankBalance> balancesBetween(Date from, Date to) {
        List<Object[]> rows = getEntityManager()
                .createNamedQuery("HourBankMovement.balancesInRange")
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        /* Se arma por contrato para que una persona sea una fila, no una por tipo de movimiento.
           El orden se resuelve al final: en la consulta obligaria a agrupar tambien por nombre. */
        Map<Long, HourBankBalance> byContract = new LinkedHashMap<Long, HourBankBalance>();
        for (Object[] row : rows) {
            Contract contract = (Contract) row[0];
            HourBankMovementType type = (HourBankMovementType) row[1];
            BigDecimal hours = (BigDecimal) row[2];

            HourBankBalance balance = byContract.get(contract.getId());
            if (null == balance) {
                balance = new HourBankBalance(contract);
                balance.setHoursPerDay(hoursPerDayOf(contract.getEmployee()));
                byContract.put(contract.getId(), balance);
            }
            if (HourBankMovementType.LEAVE.equals(type)) {
                balance.addUsed(hours);
            } else if (HourBankMovementType.PAYMENT.equals(type)) {
                balance.addPaid(hours);
            } else {
                balance.addAccrued(hours);
            }
        }
        List<HourBankBalance> balances = new ArrayList<HourBankBalance>(byContract.values());
        Collections.sort(balances, new Comparator<HourBankBalance>() {
            public int compare(HourBankBalance a, HourBankBalance b) {
                String left = null == a.getEmployee() ? "" : a.getEmployee().getLastName();
                String right = null == b.getEmployee() ? "" : b.getEmployee().getLastName();
                return null == left ? -1 : left.compareToIgnoreCase(null == right ? "" : right);
            }
        });
        return balances;
    }

    public BigDecimal leaveHours(Contract contract, Date date, Date startTime, Date endTime) {
        if (null == startTime || null == endTime) {
            /* Dia completo: lo que exige el turno de ese dia. Null si ese dia no tiene jornada,
               que NO es un fallo: es lo que la pantalla tiene que poder decir mientras se carga. */
            return journeyHours(contract, date);
        }
        long minutes = (endTime.getTime() - startTime.getTime()) / 60000L;
        if (minutes <= 0) {
            /* El fin antes que el inicio es el turno de noche: 23:30 a 03:30 termina al dia
               siguiente. Es la unica lectura posible de esas dos horas. */
            minutes += MINUTES_OF_DAY;
        }
        return new BigDecimal(minutes).divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Si ese permiso se puede tomar ese dia: hay jornada y el permiso no la excede.
     * <p/>
     * Devuelve un motivo y no un booleano para que la pantalla diga cual de los dos es.
     */
    private String leaveProblem(Contract contract, Date date, BigDecimal hours) {
        BigDecimal journey = journeyHours(contract, date);
        if (null == journey) {
            /* Sin jornada ese dia no hay nada que compensar: un permiso en un dia de descanso
               descontaria horas del banco sin que la persona deje de trabajar. */
            return "El dia no tiene jornada programada";
        }
        if (null == hours || hours.signum() <= 0) {
            return "El permiso no tiene horas";
        }
        if (hours.compareTo(journey) > 0) {
            /* Un permiso no puede cubrir mas que la jornada. Es el tope que convierte un error de
               tipeo -13:30 a 07:30 en un turno de 12 h- en un rechazo y no en 18 h descontadas. */
            return "El permiso excede la jornada del dia";
        }
        return null;
    }

    /**
     * Las horas del permiso, ya validadas, para el momento de guardar.
     * <p/>
     * Aca si corta: guardar un permiso en un dia sin jornada, o mas largo que la jornada, seria
     * descontar horas que nadie dejo de trabajar. La pantalla valida antes y lo explica; esto es
     * la red por si se llega por otro camino.
     */
    private BigDecimal checkedLeaveHours(Contract contract, Date date, Date startTime, Date endTime) {
        BigDecimal hours = leaveHours(contract, date, startTime, endTime);
        String problem = leaveProblem(contract, date, hours);
        if (null != problem) {
            throw new IllegalStateException(problem);
        }
        return hours;
    }

    public ScheduledJourney journeyOf(Contract contract, Date date) {
        if (null == contract || null == date) {
            return null;
        }
        return journeyResolverService.resolve(contract, date);
    }

    public BigDecimal journeyHours(Contract contract, Date date) {
        if (null == contract || null == date) {
            return null;
        }
        List<ScheduledJourney> journeys = journeyResolverService.resolve(contract, date, date);
        long minutes = 0;
        for (ScheduledJourney journey : journeys) {
            if (null != journey) {
                minutes += journey.getMinutes();
            }
        }
        if (minutes <= 0) {
            return null;
        }
        return new BigDecimal(minutes).divide(new BigDecimal(60), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Sale SOLO de `jornadasemanal`. No hay un valor por defecto: con uno escrito en el codigo,
     * una empresa sin la jornada cargada veria dias calculados con un numero que nadie eligio.
     * Sin configuracion devuelve null, y la pantalla lo dice.
     * <p/>
     * No se usa `configuracion.hrsdialaboral`: es otro dato -en terdemol vale 1,00- y dividir por
     * el daria dias de una hora.
     */
    public BigDecimal hoursPerDayOf(Employee employee) {
        if (null == employee || null == employee.getGender()) {
            return null;
        }
        List<?> workloads = getEntityManager()
                .createNamedQuery("WeeklyWorkload.findByGender")
                .setParameter("gender", employee.getGender())
                .setParameter("active", Boolean.TRUE)
                .getResultList();
        if (workloads.isEmpty()) {
            return null;
        }
        BigDecimal daily = ((WeeklyWorkload) workloads.get(0)).getDailyHours();
        return null == daily || daily.signum() <= 0 ? null : daily;
    }
}
